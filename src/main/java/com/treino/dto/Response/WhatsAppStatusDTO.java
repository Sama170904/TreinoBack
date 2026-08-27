package com.treino.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppStatusDTO {
    private String state; // "open" (conectado), "connecting", "close", "DISCONNECTED"
    private String instanceName;
    private String phone;
    private String qrcodeBase64;
    private String pairingCode;
    private String error;
}
