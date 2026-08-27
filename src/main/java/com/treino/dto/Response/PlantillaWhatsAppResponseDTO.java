package com.treino.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantillaWhatsAppResponseDTO {
    private Long plantillaId;
    private String tipoEvento;
    private String titulo;
    private String descripcion;
    private String mensajeTemplate;
    private Boolean activo;
    private Integer horasAnticipacion;
    private String variablesPermitidas;
}
