package com.treino.service;

import com.treino.dto.Response.PlantillaWhatsAppResponseDTO;
import com.treino.dto.Update.PlantillaWhatsAppUpdateDTO;
import com.treino.entity.PlantillaWhatsApp;
import com.treino.middlewares.ResourceNotFoundException;
import com.treino.repository.PlantillaWhatsAppRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhatsAppConfigServiceTest {

    @Mock
    private PlantillaWhatsAppRepository plantillaRepository;

    @InjectMocks
    private WhatsAppConfigService configService;

    @Test
    @DisplayName("Debe listar todas las plantillas correctamente mapeadas a DTO")
    void testObtenerTodas() {
        PlantillaWhatsApp p = PlantillaWhatsApp.builder()
            .plantillaId(1L)
            .tipoEvento(PlantillaWhatsApp.TipoEvento.CONFIRMACION_RESERVA)
            .titulo("Confirmación")
            .mensajeTemplate("Mensaje {nombre}")
            .activo(true)
            .horasAnticipacion(0)
            .build();

        when(plantillaRepository.findAll()).thenReturn(List.of(p));

        List<PlantillaWhatsAppResponseDTO> dtos = configService.obtenerTodas();

        assertEquals(1, dtos.size());
        assertEquals("CONFIRMACION_RESERVA", dtos.get(0).getTipoEvento());
        assertTrue(dtos.get(0).getActivo());
    }

    @Test
    @DisplayName("Debe actualizar el switch activo y el template de una plantilla existente")
    void testActualizarPlantilla() {
        PlantillaWhatsApp p = PlantillaWhatsApp.builder()
            .plantillaId(1L)
            .tipoEvento(PlantillaWhatsApp.TipoEvento.RECORDATORIO_CLASE)
            .titulo("Recordatorio")
            .mensajeTemplate("Viejo template")
            .activo(true)
            .horasAnticipacion(2)
            .build();

        when(plantillaRepository.findById(1L)).thenReturn(Optional.of(p));
        when(plantillaRepository.save(any(PlantillaWhatsApp.class))).thenAnswer(inv -> inv.getArgument(0));

        PlantillaWhatsAppUpdateDTO updateDTO = PlantillaWhatsAppUpdateDTO.builder()
            .activo(false)
            .mensajeTemplate("Nuevo template personalizado con {hora}")
            .horasAnticipacion(3)
            .build();

        PlantillaWhatsAppResponseDTO resultado = configService.actualizarPlantilla(1L, updateDTO);

        assertFalse(resultado.getActivo());
        assertEquals("Nuevo template personalizado con {hora}", resultado.getMensajeTemplate());
        assertEquals(3, resultado.getHorasAnticipacion());
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si el ID de plantilla no existe")
    void testActualizarPlantilla_NoEncontrada() {
        when(plantillaRepository.findById(999L)).thenReturn(Optional.empty());

        PlantillaWhatsAppUpdateDTO updateDTO = PlantillaWhatsAppUpdateDTO.builder()
            .activo(false)
            .build();

        assertThrows(ResourceNotFoundException.class, () -> configService.actualizarPlantilla(999L, updateDTO));
    }
}
