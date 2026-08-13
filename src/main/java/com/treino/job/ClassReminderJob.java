package com.treino.job;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClassReminderJob {

    @Scheduled(cron = "0 */30 * * * *")
    public void sendClassReminders() {
        log.info("Buscando clases en las próximas 2 horas para enviar recordatorios...");
        // Implementación de lógica de búsqueda y envío de correos
    }
}
