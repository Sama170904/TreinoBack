package com.treino.dto.Update;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlantillaWhatsAppUpdateDTO {
    private String mensajeTemplate;
    private Boolean activo;
    private Integer horasAnticipacion;
}
