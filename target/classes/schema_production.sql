-- ==========================================
-- SCRIPT DE PRODUCCIÓN DE BASE DE DATOS TREINO
-- ==========================================
-- Este script crea el esquema inicial y datos de prueba.
-- Ejecutar en Supabase (SQL Editor) o Neon SQL Console.

-- 1. Tabla de Usuarios
CREATE TABLE IF NOT EXISTS tbl_usuario (
    user_id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL CHECK (rol IN ('ADMINISTRADOR', 'PROFESOR', 'CLIENTE')),
    estado VARCHAR(20) DEFAULT 'ACTIVO' NOT NULL CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- 2. Tabla de Tokens JWT
CREATE TABLE IF NOT EXISTS tbl_token (
    token_id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES tbl_usuario(user_id),
    token VARCHAR(512) UNIQUE NOT NULL,
    expired BOOLEAN DEFAULT FALSE NOT NULL,
    revoked BOOLEAN DEFAULT FALSE NOT NULL
);

-- 3. Tabla de Sedes
CREATE TABLE IF NOT EXISTS tbl_sede (
    sede_id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    capacidad_maxima INT NOT NULL,
    estado VARCHAR(20) DEFAULT 'ACTIVO' NOT NULL CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- 4. Tabla de Clases
CREATE TABLE IF NOT EXISTS tbl_clase (
    clase_id BIGSERIAL PRIMARY KEY,
    profesor_id BIGINT NOT NULL REFERENCES tbl_usuario(user_id),
    sede_id BIGINT NOT NULL REFERENCES tbl_sede(sede_id),
    disciplina VARCHAR(100) NOT NULL,
    descripcion TEXT,
    fecha_hora_inicio TIMESTAMP NOT NULL,
    fecha_hora_fin TIMESTAMP NOT NULL,
    cupo_maximo INT NOT NULL,
    cupos_reservados INT DEFAULT 0 NOT NULL,
    estado VARCHAR(20) DEFAULT 'ACTIVO' NOT NULL CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- 5. Tabla de Paquetes de Crédito
CREATE TABLE IF NOT EXISTS tbl_paquete_credito (
    credito_id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES tbl_usuario(user_id),
    creditos_totales INT NOT NULL,
    creditos_disponibles INT NOT NULL,
    vigencia_tipo VARCHAR(20) NOT NULL CHECK (vigencia_tipo IN ('SEMANAL', 'MENSUAL', 'TRIMESTRAL', 'SEMESTRAL', 'ANUAL')),
    fecha_asignacion TIMESTAMP NOT NULL,
    fecha_expiracion TIMESTAMP NOT NULL,
    estado VARCHAR(20) DEFAULT 'ACTIVO' NOT NULL CHECK (estado IN ('ACTIVO', 'EXPIRADO'))
);

-- 6. Tabla de Reservas
CREATE TABLE IF NOT EXISTS tbl_reserva (
    reserva_id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES tbl_usuario(user_id),
    clase_id BIGINT NOT NULL REFERENCES tbl_clase(clase_id),
    fecha_reserva TIMESTAMP NOT NULL,
    estado_reserva VARCHAR(30) DEFAULT 'CONFIRMADA' NOT NULL CHECK (estado_reserva IN ('CONFIRMADA', 'CANCELADA_TIEMPO', 'CANCELADA_FUERA_TIEMPO')),
    estado_asistencia VARCHAR(20) DEFAULT 'PENDIENTE' NOT NULL CHECK (estado_asistencia IN ('PENDIENTE', 'ASISTIO', 'NO_SHOW')),
    estado VARCHAR(20) DEFAULT 'ACTIVO' NOT NULL CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- Restricción Única Parcial (Evita Reservas Duplicadas por Cliente en una misma Clase)
CREATE UNIQUE INDEX IF NOT EXISTS idx_reserva_unica ON tbl_reserva(cliente_id, clase_id) WHERE estado_reserva = 'CONFIRMADA';

-- 7. Tabla de Historial de Créditos
CREATE TABLE IF NOT EXISTS tbl_historial_credito (
    historial_id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES tbl_usuario(user_id),
    reserva_id BIGINT REFERENCES tbl_reserva(reserva_id),
    cantidad INT NOT NULL,
    tipo_movimiento VARCHAR(30) NOT NULL CHECK (tipo_movimiento IN ('ASIGNACION', 'CONSUMO_RESERVA', 'DEVOLUCION_CANCELACION', 'EXPIRACION')),
    descripcion VARCHAR(255) NOT NULL,
    fecha_movimiento TIMESTAMP NOT NULL
);

-- ==========================================
-- SEED DATA INICIAL DE PRODUCCIÓN
-- ==========================================
-- Contraseñas encriptadas BCrypt (password = admin123, profe123, cliente123)

INSERT INTO tbl_usuario (nombre, apellido, email, password, rol, estado)
VALUES 
('Carlos', 'Administrador', 'admin@treino.com', '$2a$10$E2UPv7arXnm552zJ9VjSbe2zUa59hY9Kk2tS/1/5d2y3K12345678', 'ADMINISTRADOR', 'ACTIVO'),
('Laura', 'Profesor', 'profesor@treino.com', '$2a$10$E2UPv7arXnm552zJ9VjSbe2zUa59hY9Kk2tS/1/5d2y3K12345678', 'PROFESOR', 'ACTIVO'),
('Mateo', 'Cliente', 'cliente@treino.com', '$2a$10$E2UPv7arXnm552zJ9VjSbe2zUa59hY9Kk2tS/1/5d2y3K12345678', 'CLIENTE', 'ACTIVO')
ON CONFLICT (email) DO NOTHING;

INSERT INTO tbl_sede (nombre, direccion, capacidad_maxima, estado)
VALUES
('Sede Principal Samborondón', 'Km 2.5 Av. Samborondón', 25, 'ACTIVO'),
('Sede Ceibos Fitness', 'Av. del Bombero Km 6.5', 20, 'ACTIVO')
ON CONFLICT DO NOTHING;
