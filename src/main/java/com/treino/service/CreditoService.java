package com.treino.service;

import com.treino.dto.Create.CreditoAsignarDTO;
import com.treino.dto.Create.CreditoQuitarDTO;
import com.treino.dto.Response.CreditoResponseDTO;
import com.treino.entity.HistorialCredito;
import com.treino.entity.PaqueteCredito;
import com.treino.entity.Usuario;
import com.treino.middlewares.BusinessException;
import com.treino.middlewares.ResourceNotFoundException;
import com.treino.repository.HistorialCreditoRepository;
import com.treino.repository.PaqueteCreditoRepository;
import com.treino.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditoService {

    private final PaqueteCreditoRepository paqueteCreditoRepository;
    private final HistorialCreditoRepository historialCreditoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public CreditoResponseDTO asignar(CreditoAsignarDTO dto) {
        Usuario cliente = usuarioRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        LocalDateTime fechaExpiracion = calcularFechaExpiracion(dto.getVigenciaTipo());

        PaqueteCredito paquete = PaqueteCredito.builder()
                .cliente(cliente)
                .creditosTotales(dto.getCantidad())
                .creditosDisponibles(dto.getCantidad())
                .vigenciaTipo(PaqueteCredito.VigenciaTipo.valueOf(dto.getVigenciaTipo()))
                .fechaExpiracion(fechaExpiracion)
                .estado(PaqueteCredito.Estado.ACTIVO)
                .build();
        
        paquete = paqueteCreditoRepository.save(paquete);

        HistorialCredito historial = HistorialCredito.builder()
                .cliente(cliente)
                .cantidad(dto.getCantidad())
                .tipoMovimiento(HistorialCredito.TipoMovimiento.ASIGNACION)
                .descripcion("Asignación de paquete " + dto.getVigenciaTipo())
                .build();
        historialCreditoRepository.save(historial);

        return mapToResponse(paquete);
    }

    private LocalDateTime calcularFechaExpiracion(String vigencia) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = switch (vigencia) {
            case "SEMANAL" -> now.plusWeeks(1);
            case "MENSUAL" -> now.plusMonths(1);
            case "TRIMESTRAL" -> now.plusMonths(3);
            case "SEMESTRAL" -> now.plusMonths(6);
            case "ANUAL" -> now.plusYears(1);
            default -> throw new BusinessException("Vigencia inválida");
        };
        return expiration.withHour(23).withMinute(59).withSecond(59);
    }

    @Transactional
    public void quitar(CreditoQuitarDTO dto) {
        // Implementación simplificada
        Usuario cliente = usuarioRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
                
        List<PaqueteCredito> paquetes = paqueteCreditoRepository.findAvailableCreditsFIFO(cliente.getUserId(), LocalDateTime.now());
        if (paquetes.isEmpty()) {
            throw new BusinessException("El cliente no tiene créditos disponibles para quitar.");
        }
        
        int aQuitar = dto.getCantidad();
        for (PaqueteCredito p : paquetes) {
            if (aQuitar <= 0) break;
            int disp = p.getCreditosDisponibles();
            if (disp >= aQuitar) {
                p.setCreditosDisponibles(disp - aQuitar);
                aQuitar = 0;
            } else {
                p.setCreditosDisponibles(0);
                aQuitar -= disp;
            }
            paqueteCreditoRepository.save(p);
        }
        
        if (aQuitar > 0) {
            throw new BusinessException("El cliente no tiene suficientes créditos para quitar esta cantidad.");
        }

        HistorialCredito historial = HistorialCredito.builder()
                .cliente(cliente)
                .cantidad(-dto.getCantidad())
                .tipoMovimiento(HistorialCredito.TipoMovimiento.EXPIRACION) // O crear tipo personalizado
                .descripcion("Retiro manual de créditos")
                .build();
        historialCreditoRepository.save(historial);
    }

    public int consultarSaldo(Long clienteId) {
        return paqueteCreditoRepository.findAvailableCreditsFIFO(clienteId, LocalDateTime.now())
                .stream().mapToInt(PaqueteCredito::getCreditosDisponibles).sum();
    }

    public List<HistorialCredito> consultarHistorial(Long clienteId) {
        return historialCreditoRepository.findByClienteUserIdOrderByFechaMovimientoDesc(clienteId);
    }

    private CreditoResponseDTO mapToResponse(PaqueteCredito p) {
        return CreditoResponseDTO.builder()
                .creditoId(p.getCreditoId())
                .creditosTotales(p.getCreditosTotales())
                .creditosDisponibles(p.getCreditosDisponibles())
                .vigenciaTipo(p.getVigenciaTipo() != null ? p.getVigenciaTipo().name() : null)
                .fechaAsignacion(p.getFechaAsignacion())
                .fechaExpiracion(p.getFechaExpiracion())
                .estado(p.getEstado() != null ? p.getEstado().name() : "ACTIVO")
                .build();
    }
}
