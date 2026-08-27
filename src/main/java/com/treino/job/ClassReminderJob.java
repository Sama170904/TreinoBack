package com.treino.job;

import com.treino.entity.PlantillaWhatsApp;
import com.treino.entity.Reserva;
import com.treino.repository.PlantillaWhatsAppRepository;
import com.treino.repository.ReservaRepository;
import com.treino.service.NotificacionWhatsAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClassReminderJob {

    private final ReservaRepository reservaRepository;
    private final PlantillaWhatsAppRepository plantillaRepository;
    private final NotificacionWhatsAppService notificacionService;

    @Scheduled(cron = "0 */30 * * * *")
    @Transactional
    public void sendClassReminders() {
        try {
            Optional<PlantillaWhatsApp> opt = plantillaRepository.findByTipoEvento(PlantillaWhatsApp.TipoEvento.RECORDATORIO_CLASE);
            if (opt.isEmpty() || !opt.get().getActivo()) {
                return;
            }

            int horas = opt.get().getHorasAnticipacion() != null ? opt.get().getHorasAnticipacion() : 2;
            LocalDateTime ahora = LocalDateTime.now();
            LocalDateTime limite = ahora.plusHours(horas);

            List<Reserva> reservasProximas = reservaRepository
                .findByEstadoReservaAndClaseFechaHoraInicioBetween(Reserva.EstadoReserva.CONFIRMADA, ahora, limite);

            for (Reserva r : reservasProximas) {
                notificacionService.notificarRecordatorioClase(r);
            }
        } catch (Exception e) {
            log.error("Error en ClassReminderJob: {}", e.getMessage());
        }
    }
}
