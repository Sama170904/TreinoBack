package com.treino.service;

import com.treino.dto.Response.WhatsAppStatusDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvolutionApiClient {

    @Value("${evolution.api.url:http://localhost:8084}")
    private String apiUrl;

    @Value("${evolution.api.key:treino_super_secret_api_key_2026}")
    private String apiKey;

    @Value("${evolution.api.instance-name:treino-bot}")
    private String instanceName;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean enviarTexto(String numero, String texto) {
        try {
            String endpoint = apiUrl + "/message/sendText/" + instanceName;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", apiKey);

            Map<String, Object> body = Map.of(
                "number", numero,
                "text", texto,
                "delay", 1000
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("No se pudo enviar WhatsApp a {} (Evolution API inaccesible o no conectado): {}", numero, e.getMessage());
            return false;
        }
    }

    public WhatsAppStatusDTO obtenerEstado() {
        try {
            String endpoint = apiUrl + "/instance/connectionState/" + instanceName;
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", apiKey);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(endpoint, HttpMethod.GET, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map body = response.getBody();
                Map instance = (Map) body.get("instance");
                String state = instance != null ? (String) instance.get("state") : (String) body.get("state");
                if (state == null) state = "close";

                return WhatsAppStatusDTO.builder()
                    .instanceName(instanceName)
                    .state(state)
                    .phone(instance != null ? (String) instance.get("owner") : null)
                    .build();
            }
        } catch (Exception e) {
            log.debug("Evolution API offline o instancia no inicializada: {}", e.getMessage());
        }

        return WhatsAppStatusDTO.builder()
            .instanceName(instanceName)
            .state("DISCONNECTED")
            .error("Servicio Evolution API no disponible o no vinculado")
            .build();
    }

    public WhatsAppStatusDTO conectarInstancia() {
        try {
            // Intentar crear instancia si no existe
            crearInstanciaSiNoExiste();

            String endpoint = apiUrl + "/instance/connect/" + instanceName;
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", apiKey);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(endpoint, HttpMethod.GET, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map body = response.getBody();
                String base64 = (String) body.get("base64");
                String code = (String) body.get("code");
                if (body.get("qrcode") instanceof Map) {
                    Map qrcodeMap = (Map) body.get("qrcode");
                    if (base64 == null) base64 = (String) qrcodeMap.get("base64");
                    if (code == null) code = (String) qrcodeMap.get("code");
                }

                return WhatsAppStatusDTO.builder()
                    .instanceName(instanceName)
                    .state("connecting")
                    .qrcodeBase64(base64 != null ? base64 : code)
                    .build();
            }
        } catch (Exception e) {
            log.error("Error al conectar instancia WhatsApp en Evolution API: {}", e.getMessage());
        }

        return WhatsAppStatusDTO.builder()
            .instanceName(instanceName)
            .state("DISCONNECTED")
            .error("No se pudo generar el código QR de vinculación")
            .build();
    }

    private void crearInstanciaSiNoExiste() {
        try {
            String endpoint = apiUrl + "/instance/create";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", apiKey);

            Map<String, Object> body = Map.of(
                "instanceName", instanceName,
                "qrcode", true,
                "integration", "WHATSAPP-BAILEYS"
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(endpoint, request, Map.class);
        } catch (Exception e) {
            // Si ya existe la instancia simplemente continúa
            log.debug("Instancia {} ya existe o no requirió creación previa: {}", instanceName, e.getMessage());
        }
    }

    public boolean desconectarInstancia() {
        try {
            String endpoint = apiUrl + "/instance/logout/" + instanceName;
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", apiKey);
            HttpEntity<Void> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.DELETE, request, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("Error al desconectar instancia de WhatsApp: {}", e.getMessage());
            return false;
        }
    }
}
