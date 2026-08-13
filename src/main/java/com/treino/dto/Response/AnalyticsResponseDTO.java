package com.treino.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponseDTO {

    private double ocupacionGlobalPromedio;
    private int totalReservasConfirmadas;
    private String horaMasConcurrida;
    private String horaMenosConcurrida;

    private List<HoraOcupacionDTO> ocupacionPorHorario;
    private List<ProfesorDesempenoDTO> desempenoProfesores;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HoraOcupacionDTO {
        private String horaEtiqueta; // e.g. "15:00 - 16:00"
        private int totalClases;
        private double porcentajeOcupacion;
        private String estadoDemanda; // "ALTA", "MEDIA", "BAJA"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfesorDesempenoDTO {
        private Long profesorId;
        private String nombreProfesor;
        private int clasesDictadas;
        private double porcentajeLlenado;
        private int totalReservas;
        private double porcentajeNoShow;
        private double porcentajeAsistencia;
        private int alumnosUnicosAtendidos;
    }
}
