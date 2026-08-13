package com.treino.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SedeResponseDTO {
    private Long sedeId;
    private String nombre;
    private String direccion;
    private Integer capacidadMaxima;
}
