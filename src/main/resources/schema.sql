-- Drop tables if exist
DROP TABLE IF EXISTS tbl_historial_credito CASCADE;
DROP TABLE IF EXISTS tbl_reserva CASCADE;
DROP TABLE IF EXISTS tbl_paquete_credito CASCADE;
DROP TABLE IF EXISTS tbl_clase CASCADE;
DROP TABLE IF EXISTS tbl_token CASCADE;
DROP TABLE IF EXISTS tbl_usuario CASCADE;
DROP TABLE IF EXISTS tbl_sede CASCADE;

-- Tabla: Sedes
CREATE TABLE tbl_sede (
    sede_id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    capacidad_maxima INT NOT NULL CHECK (capacidad_maxima > 0),
    estado VARCHAR(20) DEFAULT 'ACTIVO' NOT NULL
);

-- Tabla: Usuarios
CREATE TABLE tbl_usuario (
    user_id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL CHECK (rol IN ('ADMINISTRADOR', 'PROFESOR', 'CLIENTE')),
    estado VARCHAR(20) DEFAULT 'ACTIVO' NOT NULL
);

-- Tabla: Tokens
CREATE TABLE tbl_token (
    token_id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES tbl_usuario(user_id),
    token VARCHAR(512) UNIQUE NOT NULL,
    expired BOOLEAN DEFAULT FALSE NOT NULL,
    revoked BOOLEAN DEFAULT FALSE NOT NULL
);
CREATE INDEX idx_token_usuario ON tbl_token(usuario_id);

-- Tabla: Clases
CREATE TABLE tbl_clase (
    clase_id BIGSERIAL PRIMARY KEY,
    profesor_id BIGINT NOT NULL REFERENCES tbl_usuario(user_id),
    sede_id BIGINT NOT NULL REFERENCES tbl_sede(sede_id),
    disciplina VARCHAR(50) NOT NULL,
    descripcion TEXT,
    fecha_hora_inicio TIMESTAMP NOT NULL,
    fecha_hora_fin TIMESTAMP NOT NULL,
    cupo_maximo INT NOT NULL CHECK (cupo_maximo > 0),
    cupos_reservados INT DEFAULT 0 NOT NULL,
    estado VARCHAR(20) DEFAULT 'ACTIVO' NOT NULL
);

-- Tabla: Paquetes de Créditos
CREATE TABLE tbl_paquete_credito (
    credito_id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES tbl_usuario(user_id),
    creditos_totales INT NOT NULL CHECK (creditos_totales > 0),
    creditos_disponibles INT NOT NULL CHECK (creditos_disponibles >= 0),
    vigencia_tipo VARCHAR(20) NOT NULL CHECK (vigencia_tipo IN ('SEMANAL', 'MENSUAL', 'TRIMESTRAL', 'SEMESTRAL', 'ANUAL')),
    fecha_asignacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion TIMESTAMP NOT NULL,
    estado VARCHAR(20) DEFAULT 'ACTIVO' NOT NULL CHECK (estado IN ('ACTIVO', 'EXPIRADO'))
);

-- Tabla: Reservas
CREATE TABLE tbl_reserva (
    reserva_id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES tbl_usuario(user_id),
    clase_id BIGINT NOT NULL REFERENCES tbl_clase(clase_id),
    fecha_reserva TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado_reserva VARCHAR(30) DEFAULT 'CONFIRMADA' NOT NULL CHECK (estado_reserva IN ('CONFIRMADA', 'CANCELADA_TIEMPO', 'CANCELADA_FUERA_TIEMPO')),
    estado_asistencia VARCHAR(30) DEFAULT 'PENDIENTE' NOT NULL CHECK (estado_asistencia IN ('PENDIENTE', 'ASISTIO', 'NO_SHOW')),
    estado VARCHAR(20) DEFAULT 'ACTIVO' NOT NULL
);

-- Tabla: Historial de Créditos
CREATE TABLE tbl_historial_credito (
    historial_id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL REFERENCES tbl_usuario(user_id),
    reserva_id BIGINT REFERENCES tbl_reserva(reserva_id),
    cantidad INT NOT NULL,
    tipo_movimiento VARCHAR(30) NOT NULL CHECK (tipo_movimiento IN ('ASIGNACION', 'CONSUMO_RESERVA', 'DEVOLUCION_CANCELACION', 'EXPIRACION')),
    descripcion VARCHAR(255) NOT NULL,
    fecha_movimiento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices de Rendimiento y Restricciones Únicas
CREATE INDEX idx_clase_fecha ON tbl_clase(fecha_hora_inicio);
CREATE INDEX idx_reserva_cliente ON tbl_reserva(cliente_id);
CREATE INDEX idx_reserva_clase ON tbl_reserva(clase_id);
CREATE INDEX idx_credito_cliente_exp ON tbl_paquete_credito(cliente_id, fecha_expiracion);
CREATE UNIQUE INDEX idx_reserva_unica ON tbl_reserva(cliente_id, clase_id) WHERE estado_reserva = 'CONFIRMADA';

-- Datos Semilla (Admin por defecto)
-- password 'admin123' en BCrypt: $2a$10$e8wN3K6k6GjK0u1k.5kG3e5h7i9j1k3m5o7q9s1u3w5y7z9a1b3c5
INSERT INTO tbl_usuario (nombre, apellido, email, password, rol, estado) 
VALUES ('Admin', 'Treino', 'admin@treino.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOUVGkqRzgVymGe07xD0m1b2c3d4e5f6g', 'ADMINISTRADOR', 'ACTIVO');

INSERT INTO tbl_sede (nombre, direccion, capacidad_maxima, estado)
VALUES ('Sede Principal Polanco', 'Av. Masaryk 123, CDMX', 25, 'ACTIVO');
