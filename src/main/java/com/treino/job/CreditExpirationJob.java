package com.treino.job;

import com.treino.entity.PaqueteCredito;
import com.treino.entity.PlantillaWhatsApp;
import com.treino.repository.PaqueteCreditoRepository;
import com.treino.repository.PlantillaWhatsAppRepository;
import com.treino.service.NotificacionWhatsAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreditExpirationJob {

    private final PaqueteCreditoRepository paqueteCreditoRepository;
    private final PlantillaWhatsAppRepository plantillaRepository;
    private final NotificacionWhatsAppService notificacionService;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void markExpiredCredits() {
        List<PaqueteCredito> paquetes = paqueteCreditoRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        // 1. Marcar paquetes vencidos
        for (PaqueteCredito p : paquetes) {
            if (p.getEstado() == PaqueteCredito.Estado.ACTIVO && p.getFechaExpiracion().isBefore(now)) {
                p.setEstado(PaqueteCredito.Estado.EXPIRADO);
                paqueteCreditoRepository.save(p);
            }
        }

        // 2. Alerta de créditos por vencer
        try {
            Optional<PlantillaWhatsApp> opt = plantillaRepository.findByTipoEvento(PlantillaWhatsApp.TipoEvento.EXPIRACION_CREDITOS);
            if (opt.isPresent() && opt.get().getActivo()) {
                int horas = opt.get().getHorasAnticipacion() != null ? opt.get().getHorasAnticipacion() : 48;
                LocalDateTime limiteAlerta = now.plusHours(horas);

                for (PaqueteCredito p : paquetes) {
                    if (p.getEstado() == PaqueteCredito.Estado.ACTIVO 
                        && p.getCreditosDisponibles() > 0
                        && p.getFechaExpiracion().isAfter(now)
                        && p.getFechaExpiracion().isBefore(limiteAlerta)) {
                        
                        notificacionService.notificarExpiracionCreditos(
                            p.getCliente(), 
                            p.getCreditosDisponibles(), 
                            p.getCreditoId(), 
                            horas
                        );
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error al procesar alertas de expiración de créditos: {}", e.getMessage());
        }
    }
}
