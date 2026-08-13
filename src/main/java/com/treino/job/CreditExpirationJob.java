package com.treino.job;

import com.treino.entity.PaqueteCredito;
import com.treino.repository.PaqueteCreditoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CreditExpirationJob {

    private final PaqueteCreditoRepository paqueteCreditoRepository;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void markExpiredCredits() {
        // En un proyecto real se recomienda una consulta UPDATE nativa, pero para mantener la compatibilidad con el modelo de datos
        List<PaqueteCredito> paquetes = paqueteCreditoRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        for (PaqueteCredito p : paquetes) {
            if (p.getEstado() == PaqueteCredito.Estado.ACTIVO && p.getFechaExpiracion().isBefore(now)) {
                p.setEstado(PaqueteCredito.Estado.EXPIRADO);
                paqueteCreditoRepository.save(p);
            }
        }
    }
}
