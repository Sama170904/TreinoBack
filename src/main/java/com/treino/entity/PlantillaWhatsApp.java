package com.treino.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_plantilla_whatsapp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlantillaWhatsApp {

    public enum TipoEvento {
        CONFIRMACION_RESERVA,
        RECORDATORIO_CLASE,
        CANCELACION_RESERVA,
        EXPIRACION_CREDITOS
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plantilla_id")
    private Long plantillaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", unique = true, nullable = false, length = 50)
    private TipoEvento tipoEvento;

    @Column(name = "titulo", nullable = false, length = 100)
    private String titulo;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "mensaje_template", nullable = false, columnDefinition = "TEXT")
    private String mensajeTemplate;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "horas_anticipacion")
    @Builder.Default
    private Integer horasAnticipacion = 2;

    @Column(name = "variables_permitidas", length = 255)
    private String variablesPermitidas;
}
