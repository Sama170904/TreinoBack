package com.treino.service;

import com.treino.entity.*;
import com.treino.repository.LogNotificacionWhatsAppRepository;
import com.treino.repository.PlantillaWhatsAppRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionWhatsAppServiceTest {

    @Mock
    private PlantillaWhatsAppRepository plantillaRepository;

    @Mock
    private LogNotificacionWhatsAppRepository logRepository;

    @Mock
    private EvolutionApiClient apiClient;

    @InjectMocks
    private NotificacionWhatsAppService notificacionService;

    private Usuario usuarioPrueba;
    private Sede sedePrueba;
    private Clase clasePrueba;
    private Reserva reservaPrueba;
    private PlantillaWhatsApp plantillaConfirmacion;

    @BeforeEach
    void setUp() {
        usuarioPrueba = Usuario.builder()
            .userId(1L)
            .nombre("Mateo")
            .apellido("Benítez")
            .email("mateo@treino.com")
            .telefono("0987689886")
            .rol(Usuario.Rol.CLIENTE)
            .build();

        sedePrueba = Sede.builder()
            .sedeId(1L)
            .nombre("Sede Samborondón")
            .direccion("Km 2.5 Vía Samborondón")
            .build();

        clasePrueba = Clase.builder()
            .claseId(10L)
            .disciplina("Pilates Reformer")
            .fechaHoraInicio(LocalDateTime.of(2026, 8, 25, 18, 0))
            .fechaHoraFin(LocalDateTime.of(2026, 8, 25, 19, 0))
            .sede(sedePrueba)
            .build();

        reservaPrueba = Reserva.builder()
            .reservaId(100L)
            .cliente(usuarioPrueba)
            .clase(clasePrueba)
            .estadoReserva(Reserva.EstadoReserva.CONFIRMADA)
            .build();

        plantillaConfirmacion = PlantillaWhatsApp.builder()
            .plantillaId(1L)
            .tipoEvento(PlantillaWhatsApp.TipoEvento.CONFIRMACION_RESERVA)
            .titulo("Confirmación Inmediata de Reserva")
            .mensajeTemplate("¡Hola {nombre}! Tu reserva para {disciplina} el {fecha} a las {hora} en {sede} fue confirmada.")
            .activo(true)
            .horasAnticipacion(0)
            .build();
    }

    @Test
    @DisplayName("Debe normalizar correctamente números de teléfono de Ecuador")
    void testNormalizarTelefono() {
        assertEquals("593987689886", notificacionService.normalizarTelefono("0987689886"));
        assertEquals("593987689886", notificacionService.normalizarTelefono("+593 98 768 9886"));
        assertEquals("593987689886", notificacionService.normalizarTelefono("593987689886"));
        assertEquals("593987689886", notificacionService.normalizarTelefono("098-768-9886"));
    }

    @Test
    @DisplayName("Debe enviar confirmación de reserva y reemplazar variables dinámicas")
    void testNotificarConfirmacionReserva_Exitoso() {
        when(plantillaRepository.findByTipoEvento(PlantillaWhatsApp.TipoEvento.CONFIRMACION_RESERVA))
            .thenReturn(Optional.of(plantillaConfirmacion));
        when(apiClient.enviarTexto(anyString(), anyString())).thenReturn(true);

        notificacionService.notificarConfirmacionReserva(reservaPrueba);

        ArgumentCaptor<String> phoneCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> msgCaptor = ArgumentCaptor.forClass(String.class);

        verify(apiClient, times(1)).enviarTexto(phoneCaptor.capture(), msgCaptor.capture());

        assertEquals("593987689886", phoneCaptor.getValue());
        String msg = msgCaptor.getValue();
        assertTrue(msg.contains("Mateo"));
        assertTrue(msg.contains("Pilates Reformer"));
        assertTrue(msg.contains("18:00"));
        assertTrue(msg.contains("Sede Samborondón"));

        verify(logRepository, times(1)).save(any(LogNotificacionWhatsApp.class));
    }

    @Test
    @DisplayName("No debe enviar mensaje si el switch de la plantilla está desactivado (activo = false)")
    void testNotificarConfirmacionReserva_Desactivado() {
        plantillaConfirmacion.setActivo(false);
        when(plantillaRepository.findByTipoEvento(PlantillaWhatsApp.TipoEvento.CONFIRMACION_RESERVA))
            .thenReturn(Optional.of(plantillaConfirmacion));

        notificacionService.notificarConfirmacionReserva(reservaPrueba);

        verify(apiClient, never()).enviarTexto(anyString(), anyString());
        verify(logRepository, never()).save(any(LogNotificacionWhatsApp.class));
    }

    @Test
    @DisplayName("Debe evitar enviar recordatorios duplicados para la misma reserva")
    void testNotificarRecordatorioClase_Deduplicacion() {
        PlantillaWhatsApp plantillaRecordatorio = PlantillaWhatsApp.builder()
            .plantillaId(2L)
            .tipoEvento(PlantillaWhatsApp.TipoEvento.RECORDATORIO_CLASE)
            .mensajeTemplate("Recordatorio para {nombre}")
            .activo(true)
            .horasAnticipacion(2)
            .build();

        when(plantillaRepository.findByTipoEvento(PlantillaWhatsApp.TipoEvento.RECORDATORIO_CLASE))
            .thenReturn(Optional.of(plantillaRecordatorio));
        // Simular que ya existe un log previo con ENVIADO
        when(logRepository.existsByTipoEventoAndReferenciaIdAndEstadoEnvio(
            PlantillaWhatsApp.TipoEvento.RECORDATORIO_CLASE, 100L, LogNotificacionWhatsApp.EstadoEnvio.ENVIADO
        )).thenReturn(true);

        notificacionService.notificarRecordatorioClase(reservaPrueba);

        verify(apiClient, never()).enviarTexto(anyString(), anyString());
    }
}
