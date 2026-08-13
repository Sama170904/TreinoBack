package com.treino.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditoResponseDTO {
    private Long creditoId;
    private Integer creditosTotales;
    private Integer creditosDisponibles;
    private String vigenciaTipo;
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaExpiracion;
    private String estado;
}
