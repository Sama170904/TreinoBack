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
public class ReservaResponseDTO {
    private Long reservaId;
    private Long clienteId;
    private String clienteNombre;
    private Long claseId;
    private String claseDisciplina;
    private String sedeNombre;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private LocalDateTime fechaReserva;
    private String estadoReserva;
    private String estadoAsistencia;
    private ClaseDetalleDTO clase;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClaseDetalleDTO {
        private Long claseId;
        private String disciplina;
        private String descripcion;
        private LocalDateTime fechaHoraInicio;
        private LocalDateTime fechaHoraFin;
        private SedeDetalleDTO sede;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SedeDetalleDTO {
        private Long sedeId;
        private String nombre;
        private String direccion;
    }
}
