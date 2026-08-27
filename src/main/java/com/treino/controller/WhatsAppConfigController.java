package com.treino.controller;

import com.treino.dto.Response.PlantillaWhatsAppResponseDTO;
import com.treino.dto.Response.WhatsAppStatusDTO;
import com.treino.dto.Update.PlantillaWhatsAppUpdateDTO;
import com.treino.service.EvolutionApiClient;
import com.treino.service.NotificacionWhatsAppService;
import com.treino.service.WhatsAppConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/whatsapp")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class WhatsAppConfigController {

    private final WhatsAppConfigService configService;
    private final EvolutionApiClient apiClient;
    private final NotificacionWhatsAppService notificacionService;

    @GetMapping("/plantillas")
    public List<PlantillaWhatsAppResponseDTO> obtenerPlantillas() {
        return configService.obtenerTodas();
    }

    @PutMapping("/plantillas/{plantillaId}")
    public PlantillaWhatsAppResponseDTO actualizarPlantilla(
        @PathVariable Long plantillaId,
        @RequestBody PlantillaWhatsAppUpdateDTO dto
    ) {
        return configService.actualizarPlantilla(plantillaId, dto);
    }

    @GetMapping("/estado")
    public WhatsAppStatusDTO obtenerEstadoBot() {
        return apiClient.obtenerEstado();
    }

    @PostMapping("/conectar")
    public WhatsAppStatusDTO conectarBot() {
        return apiClient.conectarInstancia();
    }

    @PostMapping("/desconectar")
    public Map<String, Object> desconectarBot() {
        boolean desconectado = apiClient.desconectarInstancia();
        return Map.of("desconectado", desconectado);
    }

    @PostMapping("/test-send")
    public Map<String, Object> enviarPrueba(@RequestBody Map<String, String> body) {
        String telefono = body.get("telefono");
        String mensaje = body.get("mensaje");
        if (telefono == null || telefono.isBlank()) {
            return Map.of("enviado", false, "error", "El número de teléfono es obligatorio");
        }
        String formattedPhone = notificacionService.normalizarTelefono(telefono);
        boolean enviado = apiClient.enviarTexto(formattedPhone, mensaje != null ? mensaje : "¡Mensaje de prueba de Treino WhatsApp Bot! 🚀");
        return Map.of("enviado", enviado, "telefono", formattedPhone);
    }
}
