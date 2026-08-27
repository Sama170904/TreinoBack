package com.treino.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_log_notificacion_whatsapp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogNotificacionWhatsApp {

    public enum EstadoEnvio {
        ENVIADO,
        FALLIDO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 50)
    private PlantillaWhatsApp.TipoEvento tipoEvento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;

    @Column(name = "telefono", nullable = false, length = 30)
    private String telefono;

    @Column(name = "mensaje_enviado", nullable = false, columnDefinition = "TEXT")
    private String mensajeEnviado;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_envio", nullable = false, length = 20)
    private EstadoEnvio estadoEnvio;

    @Column(name = "detalle_error", columnDefinition = "TEXT")
    private String detalleError;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;
}
