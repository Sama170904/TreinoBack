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
public class ClaseResponseDTO {
    private Long claseId;
    private Long profesorId;
    private String profesorNombre;
    private Long sedeId;
    private String sedeNombre;
    private String disciplina;
    private String descripcion;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private Integer cupoMaximo;
    private Integer cuposReservados;
    private String estado;
}
