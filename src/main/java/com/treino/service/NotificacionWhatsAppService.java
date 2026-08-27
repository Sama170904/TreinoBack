package com.treino.service;

import com.treino.entity.*;
import com.treino.repository.LogNotificacionWhatsAppRepository;
import com.treino.repository.PlantillaWhatsAppRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionWhatsAppService {

    private final PlantillaWhatsAppRepository plantillaRepository;
    private final LogNotificacionWhatsAppRepository logRepository;
    private final EvolutionApiClient apiClient;

    @Transactional
    public void notificarConfirmacionReserva(Reserva reserva) {
        try {
            Optional<PlantillaWhatsApp> opt = plantillaRepository.findByTipoEvento(PlantillaWhatsApp.TipoEvento.CONFIRMACION_RESERVA);
            if (opt.isEmpty() || !opt.get().getActivo()) return;

            Usuario cliente = reserva.getCliente();
            if (cliente == null || cliente.getTelefono() == null || cliente.getTelefono().isBlank()) return;

            String telefono = normalizarTelefono(cliente.getTelefono());
            Clase clase = reserva.getClase();

            DateTimeFormatter fechaFmt = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", new Locale("es", "EC"));
            DateTimeFormatter horaFmt = DateTimeFormatter.ofPattern("HH:mm");

            Map<String, String> variables = Map.of(
                "{nombre}", cliente.getNombre() != null ? cliente.getNombre() : "Cliente",
                "{disciplina}", clase.getDisciplina(),
                "{fecha}", clase.getFechaHoraInicio().format(fechaFmt),
                "{hora}", clase.getFechaHoraInicio().format(horaFmt),
                "{sede}", clase.getSede() != null ? clase.getSede().getNombre() : "Sede Principal"
            );

            String mensaje = procesarTemplate(opt.get().getMensajeTemplate(), variables);
            boolean enviado = apiClient.enviarTexto(telefono, mensaje);

            guardarLog(PlantillaWhatsApp.TipoEvento.CONFIRMACION_RESERVA, cliente, telefono, mensaje, reserva.getReservaId(), enviado);
        } catch (Exception e) {
            log.error("Excepción controlada al procesar notificación de confirmación de reserva: {}", e.getMessage());
        }
    }

    @Transactional
    public void notificarRecordatorioClase(Reserva reserva) {
        try {
            Optional<PlantillaWhatsApp> opt = plantillaRepository.findByTipoEvento(PlantillaWhatsApp.TipoEvento.RECORDATORIO_CLASE);
            if (opt.isEmpty() || !opt.get().getActivo()) return;

            // Deduplicación: no enviar si ya fue enviado exitosamente para esta reserva
            if (logRepository.existsByTipoEventoAndReferenciaIdAndEstadoEnvio(
                PlantillaWhatsApp.TipoEvento.RECORDATORIO_CLASE, 
                reserva.getReservaId(), 
                LogNotificacionWhatsApp.EstadoEnvio.ENVIADO)) {
                return;
            }

            Usuario cliente = reserva.getCliente();
            if (cliente == null || cliente.getTelefono() == null || cliente.getTelefono().isBlank()) return;

            String telefono = normalizarTelefono(cliente.getTelefono());
            Clase clase = reserva.getClase();

            DateTimeFormatter fechaFmt = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", new Locale("es", "EC"));
            DateTimeFormatter horaFmt = DateTimeFormatter.ofPattern("HH:mm");

            Map<String, String> variables = Map.of(
                "{nombre}", cliente.getNombre() != null ? cliente.getNombre() : "Cliente",
                "{disciplina}", clase.getDisciplina(),
                "{fecha}", clase.getFechaHoraInicio().format(fechaFmt),
                "{hora}", clase.getFechaHoraInicio().format(horaFmt),
                "{sede}", clase.getSede() != null ? clase.getSede().getNombre() : "Sede Principal",
                "{horas_anticipacion}", String.valueOf(opt.get().getHorasAnticipacion())
            );

            String mensaje = procesarTemplate(opt.get().getMensajeTemplate(), variables);
            boolean enviado = apiClient.enviarTexto(telefono, mensaje);

            guardarLog(PlantillaWhatsApp.TipoEvento.RECORDATORIO_CLASE, cliente, telefono, mensaje, reserva.getReservaId(), enviado);
        } catch (Exception e) {
            log.error("Excepción controlada al procesar recordatorio de clase: {}", e.getMessage());
        }
    }

    @Transactional
    public void notificarCancelacionReserva(Reserva reserva) {
        try {
            Optional<PlantillaWhatsApp> opt = plantillaRepository.findByTipoEvento(PlantillaWhatsApp.TipoEvento.CANCELACION_RESERVA);
            if (opt.isEmpty() || !opt.get().getActivo()) return;

            Usuario cliente = reserva.getCliente();
            if (cliente == null || cliente.getTelefono() == null || cliente.getTelefono().isBlank()) return;

            String telefono = normalizarTelefono(cliente.getTelefono());
            Clase clase = reserva.getClase();

            DateTimeFormatter horaFmt = DateTimeFormatter.ofPattern("HH:mm");

            Map<String, String> variables = Map.of(
                "{nombre}", cliente.getNombre() != null ? cliente.getNombre() : "Cliente",
                "{disciplina}", clase != null ? clase.getDisciplina() : "Clase",
                "{hora}", clase != null ? clase.getFechaHoraInicio().format(horaFmt) : "",
                "{sede}", clase != null && clase.getSede() != null ? clase.getSede().getNombre() : "Sede Principal"
            );

            String mensaje = procesarTemplate(opt.get().getMensajeTemplate(), variables);
            boolean enviado = apiClient.enviarTexto(telefono, mensaje);

            guardarLog(PlantillaWhatsApp.TipoEvento.CANCELACION_RESERVA, cliente, telefono, mensaje, reserva.getReservaId(), enviado);
        } catch (Exception e) {
            log.error("Excepción controlada al procesar notificación de cancelación de reserva: {}", e.getMessage());
        }
    }

    @Transactional
    public void notificarExpiracionCreditos(Usuario cliente, int creditosDisponibles, Long paqueteId, int horasAnticipacion) {
        try {
            Optional<PlantillaWhatsApp> opt = plantillaRepository.findByTipoEvento(PlantillaWhatsApp.TipoEvento.EXPIRACION_CREDITOS);
            if (opt.isEmpty() || !opt.get().getActivo()) return;

            if (cliente == null || cliente.getTelefono() == null || cliente.getTelefono().isBlank()) return;

            // Deduplicación para este paquete
            if (logRepository.existsByTipoEventoAndReferenciaIdAndEstadoEnvio(
                PlantillaWhatsApp.TipoEvento.EXPIRACION_CREDITOS, 
                paqueteId, 
                LogNotificacionWhatsApp.EstadoEnvio.ENVIADO)) {
                return;
            }

            String telefono = normalizarTelefono(cliente.getTelefono());

            Map<String, String> variables = Map.of(
                "{nombre}", cliente.getNombre() != null ? cliente.getNombre() : "Cliente",
                "{creditos}", String.valueOf(creditosDisponibles),
                "{horas_anticipacion}", String.valueOf(horasAnticipacion)
            );

            String mensaje = procesarTemplate(opt.get().getMensajeTemplate(), variables);
            boolean enviado = apiClient.enviarTexto(telefono, mensaje);

            guardarLog(PlantillaWhatsApp.TipoEvento.EXPIRACION_CREDITOS, cliente, telefono, mensaje, paqueteId, enviado);
        } catch (Exception e) {
            log.error("Excepción controlada al notificar expiración de créditos: {}", e.getMessage());
        }
    }

    public String normalizarTelefono(String rawPhone) {
        if (rawPhone == null) return "";
        String digits = rawPhone.replaceAll("[^0-9]", "");
        if (digits.startsWith("0")) {
            return "593" + digits.substring(1);
        }
        if (!digits.startsWith("593") && digits.length() == 9) {
            return "593" + digits;
        }
        return digits;
    }

    private String procesarTemplate(String template, Map<String, String> variables) {
        String resultado = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            resultado = resultado.replace(entry.getKey(), entry.getValue() != null ? entry.getValue() : "");
        }
        return resultado;
    }

    private void guardarLog(PlantillaWhatsApp.TipoEvento tipo, Usuario cliente, String telefono, String mensaje, Long referenciaId, boolean enviado) {
        try {
            LogNotificacionWhatsApp logEntry = LogNotificacionWhatsApp.builder()
                .tipoEvento(tipo)
                .destinatario(cliente)
                .telefono(telefono)
                .mensajeEnviado(mensaje)
                .referenciaId(referenciaId)
                .estadoEnvio(enviado ? LogNotificacionWhatsApp.EstadoEnvio.ENVIADO : LogNotificacionWhatsApp.EstadoEnvio.FALLIDO)
                .detalleError(enviado ? null : "Evolution API no respondió con éxito")
                .fechaEnvio(LocalDateTime.now())
                .build();
            logRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Error al guardar log de notificación de WhatsApp: {}", e.getMessage());
        }
    }
}
