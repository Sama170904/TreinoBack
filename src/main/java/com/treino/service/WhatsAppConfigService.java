package com.treino.service;

import com.treino.dto.Response.PlantillaWhatsAppResponseDTO;
import com.treino.dto.Update.PlantillaWhatsAppUpdateDTO;
import com.treino.entity.PlantillaWhatsApp;
import com.treino.middlewares.ResourceNotFoundException;
import com.treino.repository.PlantillaWhatsAppRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppConfigService {

    private final PlantillaWhatsAppRepository plantillaRepository;

    @PostConstruct
    @Transactional
    public void seedDefaultPlantillas() {
        if (plantillaRepository.count() == 0) {
            log.info("Inicializando las 4 plantillas predeterminadas de WhatsApp en Treino...");

            PlantillaWhatsApp p1 = PlantillaWhatsApp.builder()
                .tipoEvento(PlantillaWhatsApp.TipoEvento.CONFIRMACION_RESERVA)
                .titulo("Confirmación Inmediata de Reserva")
                .descripcion("Se envía al instante en que el alumno reserva un cupo en una clase.")
                .mensajeTemplate("¡Hola {nombre}! 🎉 Tu reserva para la clase de *{disciplina}* el día *{fecha}* a las *{hora}* en *{sede}* ha sido confirmada con éxito. ¡Te esperamos para darlo todo! 💪")
                .activo(true)
                .horasAnticipacion(0)
                .variablesPermitidas("{nombre}, {disciplina}, {fecha}, {hora}, {sede}")
                .build();

            PlantillaWhatsApp p2 = PlantillaWhatsApp.builder()
                .tipoEvento(PlantillaWhatsApp.TipoEvento.RECORDATORIO_CLASE)
                .titulo("Recordatorio Previo a la Clase")
                .descripcion("Se envía automáticamente unas horas antes del inicio de la clase reservada.")
                .mensajeTemplate("Hola {nombre} 👋 Te recordamos que tu clase de *{disciplina}* comienza en {horas_anticipacion} horas (a las *{hora}* en *{sede}*). ¡Llega 5 minutos antes para el calentamiento!")
                .activo(true)
                .horasAnticipacion(2)
                .variablesPermitidas("{nombre}, {disciplina}, {fecha}, {hora}, {sede}, {horas_anticipacion}")
                .build();

            PlantillaWhatsApp p3 = PlantillaWhatsApp.builder()
                .tipoEvento(PlantillaWhatsApp.TipoEvento.CANCELACION_RESERVA)
                .titulo("Cancelación de Reserva & Devolución")
                .descripcion("Se envía cuando el alumno cancela su reserva a tiempo y se le reintegra el crédito.")
                .mensajeTemplate("Hola {nombre}, confirmamos que tu reserva para la clase de *{disciplina}* a las *{hora}* ha sido cancelada. Tu crédito ha sido devuelto a tu cuenta exitosamente. 🔄")
                .activo(true)
                .horasAnticipacion(0)
                .variablesPermitidas("{nombre}, {disciplina}, {fecha}, {hora}, {sede}")
                .build();

            PlantillaWhatsApp p4 = PlantillaWhatsApp.builder()
                .tipoEvento(PlantillaWhatsApp.TipoEvento.EXPIRACION_CREDITOS)
                .titulo("Alerta de Créditos por Vencer")
                .descripcion("Notifica al alumno cuando sus créditos están próximos a caducar.")
                .mensajeTemplate("¡Hola {nombre}! ⚠️ Tienes *{creditos} créditos* que vencen en {horas_anticipacion} horas. ¡Ingresa a Treino y agenda tus clases para no perderlos!")
                .activo(true)
                .horasAnticipacion(48)
                .variablesPermitidas("{nombre}, {creditos}, {horas_anticipacion}")
                .build();

            plantillaRepository.saveAll(List.of(p1, p2, p3, p4));
            log.info("Plantillas de WhatsApp inicializadas exitosamente.");
        }
    }

    public List<PlantillaWhatsAppResponseDTO> obtenerTodas() {
        return plantillaRepository.findAll().stream()
            .map(this::mapToDTO)
            .toList();
    }

    @Transactional
    public PlantillaWhatsAppResponseDTO actualizarPlantilla(Long id, PlantillaWhatsAppUpdateDTO dto) {
        PlantillaWhatsApp plantilla = plantillaRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Plantilla no encontrada con ID: " + id));

        if (dto.getActivo() != null) {
            plantilla.setActivo(dto.getActivo());
        }
        if (dto.getMensajeTemplate() != null && !dto.getMensajeTemplate().isBlank()) {
            plantilla.setMensajeTemplate(dto.getMensajeTemplate());
        }
        if (dto.getHorasAnticipacion() != null && dto.getHorasAnticipacion() >= 0) {
            plantilla.setHorasAnticipacion(dto.getHorasAnticipacion());
        }

        PlantillaWhatsApp guardada = plantillaRepository.save(plantilla);
        return mapToDTO(guardada);
    }

    private PlantillaWhatsAppResponseDTO mapToDTO(PlantillaWhatsApp p) {
        return PlantillaWhatsAppResponseDTO.builder()
            .plantillaId(p.getPlantillaId())
            .tipoEvento(p.getTipoEvento().name())
            .titulo(p.getTitulo())
            .descripcion(p.getDescripcion())
            .mensajeTemplate(p.getMensajeTemplate())
            .activo(p.getActivo())
            .horasAnticipacion(p.getHorasAnticipacion())
            .variablesPermitidas(p.getVariablesPermitidas())
            .build();
    }
}
