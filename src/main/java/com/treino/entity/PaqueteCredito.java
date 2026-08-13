package com.treino.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "tbl_paquete_credito")
@SQLDelete(sql = "UPDATE tbl_paquete_credito SET estado = 'EXPIRADO' WHERE credito_id = ?")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PaqueteCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long creditoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @Column(nullable = false)
    private int creditosTotales;

    @Column(nullable = false)
    private int creditosDisponibles;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VigenciaTipo vigenciaTipo;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fechaAsignacion = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Estado estado = Estado.ACTIVO;

    public enum Estado { ACTIVO, EXPIRADO }
    public enum VigenciaTipo { SEMANAL, MENSUAL, TRIMESTRAL, SEMESTRAL, ANUAL }
}
