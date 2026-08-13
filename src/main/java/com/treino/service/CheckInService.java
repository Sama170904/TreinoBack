package com.treino.service;

import com.treino.entity.Clase;
import com.treino.entity.Reserva;
import com.treino.entity.Usuario;
import com.treino.middlewares.BusinessException;
import com.treino.middlewares.ResourceNotFoundException;
import com.treino.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CheckInService {

    private final ReservaRepository reservaRepository;

    @Transactional
    public void marcarAsistencia(Long reservaId, String estado, Usuario usuarioAuth) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada"));

        Clase clase = reserva.getClase();
        LocalDateTime ahora = LocalDateTime.now();

        boolean esAdmin = usuarioAuth != null && (
                usuarioAuth.getRol() == Usuario.Rol.ADMINISTRADOR ||
                usuarioAuth.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ADMINISTRADOR"))
        );

        // 1. La clase debe haber iniciado para poder hacer pase de lista
        if (clase.getFechaHoraInicio() != null && ahora.isBefore(clase.getFechaHoraInicio())) {
            throw new BusinessException("El pase de lista solo está disponible una vez iniciada la clase (Hora de inicio: " 
                + clase.getFechaHoraInicio().toLocalTime() + ").");
        }

        // 2. Si la clase ya finalizó, solo el ADMINISTRADOR puede modificar el pase de lista
        if (clase.getFechaHoraFin() != null && ahora.isAfter(clase.getFechaHoraFin()) && !esAdmin) {
            throw new BusinessException("La clase ya ha finalizado. Solo un Administrador puede modificar la asistencia después de su término.");
        }

        reserva.setEstadoAsistencia(Reserva.EstadoAsistencia.valueOf(estado));
        reservaRepository.save(reserva);
    }
}
