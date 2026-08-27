package com.treino.service;

import com.treino.entity.Usuario;
import com.treino.middlewares.BusinessException;
import com.treino.middlewares.ResourceNotFoundException;
import com.treino.repository.ClaseRepository;
import com.treino.repository.HistorialCreditoRepository;
import com.treino.repository.PaqueteCreditoRepository;
import com.treino.repository.ReservaRepository;
import com.treino.entity.Clase;
import com.treino.entity.PaqueteCredito;
import com.treino.entity.Reserva;
import com.treino.entity.HistorialCredito;
import com.treino.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.treino.dto.Response.ReservaResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ClaseRepository claseRepository;
    private final PaqueteCreditoRepository creditoRepository;
    private final ReservaRepository reservaRepository;
    private final HistorialCreditoRepository historialRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionWhatsAppService notificacionWhatsAppService;

    @Transactional
    public ReservaResponseDTO crearReserva(Long clienteId, Long claseId) {
        Usuario cliente = usuarioRepository.findById(clienteId)
            .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        // 1. Bloqueo Pesimista en BD
        Clase clase = claseRepository.findByIdForUpdate(claseId)
            .orElseThrow(() -> new ResourceNotFoundException("La clase no existe"));

        // 2. Verificación Estricta de Cupos
        if (clase.getCuposReservados() >= clase.getCupoMaximo()) {
            throw new BusinessException("El último cupo acaba de ser tomado");
        }

        // 2.5. Verificación de Reserva Duplicada
        if (reservaRepository.findByClienteUserIdAndClaseClaseIdAndEstadoReserva(clienteId, claseId, Reserva.EstadoReserva.CONFIRMADA).isPresent()) {
            throw new BusinessException("Ya tienes una reserva activa para esta clase");
        }

        // 3. Verificación de Créditos Activos (FIFO)
        List<PaqueteCredito> creditosValidos = creditoRepository
            .findAvailableCreditsFIFO(clienteId, LocalDateTime.now());

        if (creditosValidos.isEmpty()) {
            throw new BusinessException("No tienes créditos disponibles o vigentes para reservar");
        }

        PaqueteCredito paquete = creditosValidos.get(0);

        // 4. Consumo Atómico de Crédito (previene doble gasto)
        int rowsAffected = creditoRepository.consumirCreditoAtomico(paquete.getCreditoId());
        if (rowsAffected == 0) {
            throw new BusinessException("No tienes créditos disponibles o vigentes para reservar");
        }

        // 5. Incrementar Cupo Reservado
        clase.setCuposReservados(clase.getCuposReservados() + 1);
        claseRepository.save(clase);

        // 6. Crear Registro de Reserva
        Reserva reserva = Reserva.builder()
            .cliente(cliente)
            .clase(clase)
            .fechaReserva(LocalDateTime.now())
            .estadoReserva(Reserva.EstadoReserva.CONFIRMADA)
            .estadoAsistencia(Reserva.EstadoAsistencia.PENDIENTE)
            .build();
        reserva = reservaRepository.save(reserva);

        // 7. Registrar Historial
        HistorialCredito historial = HistorialCredito.builder()
            .cliente(cliente)
            .reserva(reserva)
            .cantidad(-1)
            .tipoMovimiento(HistorialCredito.TipoMovimiento.CONSUMO_RESERVA)
            .descripcion("Reserva para clase de " + clase.getDisciplina())
            .fechaMovimiento(LocalDateTime.now())
            .build();
        historialRepository.save(historial);

        // 8. Notificación Automática WhatsApp
        notificacionWhatsAppService.notificarConfirmacionReserva(reserva);

        return mapToResponse(reserva);
    }

    @Transactional
    public ReservaResponseDTO cancelarReserva(Long reservaId, Long clienteId) {
        Reserva reserva = reservaRepository.findById(reservaId)
            .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));
            
        if (!reserva.getCliente().getUserId().equals(clienteId)) {
            throw new BusinessException("No puedes cancelar una reserva que no te pertenece");
        }

        if (reserva.getEstadoReserva() != Reserva.EstadoReserva.CONFIRMADA) {
            throw new BusinessException("La reserva ya está cancelada");
        }

        Clase clase = claseRepository.findByIdForUpdate(reserva.getClase().getClaseId())
            .orElseThrow(() -> new ResourceNotFoundException("La clase no existe"));

        LocalDateTime limiteCancelacion = clase.getFechaHoraInicio().minusMinutes(15);
        
        if (LocalDateTime.now().isBefore(limiteCancelacion)) {
            reserva.setEstadoReserva(Reserva.EstadoReserva.CANCELADA_TIEMPO);
            // Devolver crédito
            List<PaqueteCredito> creditos = creditoRepository.findAvailableCreditsFIFO(clienteId, LocalDateTime.now());
            PaqueteCredito paquete = creditos.isEmpty() ? null : creditos.get(0);
            if (paquete != null) {
                paquete.setCreditosDisponibles(paquete.getCreditosDisponibles() + 1);
                creditoRepository.save(paquete);
                
                HistorialCredito historial = HistorialCredito.builder()
                    .cliente(new Usuario(clienteId))
                    .reserva(reserva)
                    .cantidad(1)
                    .tipoMovimiento(HistorialCredito.TipoMovimiento.DEVOLUCION_CANCELACION)
                    .descripcion("Devolución por cancelación a tiempo")
                    .fechaMovimiento(LocalDateTime.now())
                    .build();
                historialRepository.save(historial);
            }
        } else {
            reserva.setEstadoReserva(Reserva.EstadoReserva.CANCELADA_FUERA_TIEMPO);
        }

        // Liberar cupo
        clase.setCuposReservados(clase.getCuposReservados() - 1);
        claseRepository.save(clase);
        reservaRepository.save(reserva);

        // Notificación Automática WhatsApp
        notificacionWhatsAppService.notificarCancelacionReserva(reserva);

        return mapToResponse(reserva);
    }

    public List<ReservaResponseDTO> obtenerMisReservas(Long clienteId) {
        List<Reserva> reservas = reservaRepository.findByClienteUserIdOrderByFechaReservaDesc(clienteId);
        return reservas.stream().map(this::mapToResponse).toList();
    }

    public List<ReservaResponseDTO> obtenerReservasPorClase(Long claseId) {
        List<Reserva> reservas = reservaRepository.findByClaseClaseId(claseId);
        return reservas.stream().map(this::mapToResponse).toList();
    }

    private ReservaResponseDTO mapToResponse(Reserva r) {
        ReservaResponseDTO.ClaseDetalleDTO claseDTO = null;
        if (r.getClase() != null) {
            Clase c = r.getClase();
            ReservaResponseDTO.SedeDetalleDTO sedeDTO = null;
            if (c.getSede() != null) {
                sedeDTO = ReservaResponseDTO.SedeDetalleDTO.builder()
                    .sedeId(c.getSede().getSedeId())
                    .nombre(c.getSede().getNombre())
                    .direccion(c.getSede().getDireccion())
                    .build();
            }
            claseDTO = ReservaResponseDTO.ClaseDetalleDTO.builder()
                .claseId(c.getClaseId())
                .disciplina(c.getDisciplina())
                .descripcion(c.getDescripcion())
                .fechaHoraInicio(c.getFechaHoraInicio())
                .fechaHoraFin(c.getFechaHoraFin())
                .sede(sedeDTO)
                .build();
        }

        String clienteNombre = null;
        if (r.getCliente() != null && r.getCliente().getNombre() != null) {
            clienteNombre = r.getCliente().getNombre() + " " + r.getCliente().getApellido();
        }

        return ReservaResponseDTO.builder()
                .reservaId(r.getReservaId())
                .clienteId(r.getCliente() != null ? r.getCliente().getUserId() : null)
                .clienteNombre(clienteNombre)
                .claseId(r.getClase() != null ? r.getClase().getClaseId() : null)
                .claseDisciplina(r.getClase() != null ? r.getClase().getDisciplina() : null)
                .sedeNombre(r.getClase() != null && r.getClase().getSede() != null ? r.getClase().getSede().getNombre() : null)
                .fechaHoraInicio(r.getClase() != null ? r.getClase().getFechaHoraInicio() : null)
                .fechaHoraFin(r.getClase() != null ? r.getClase().getFechaHoraFin() : null)
                .fechaReserva(r.getFechaReserva())
                .estadoReserva(r.getEstadoReserva() != null ? r.getEstadoReserva().name() : null)
                .estadoAsistencia(r.getEstadoAsistencia() != null ? r.getEstadoAsistencia().name() : null)
                .clase(claseDTO)
                .build();
    }
}
