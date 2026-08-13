package com.treino.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tbl_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(unique = true, nullable = false, length = 512)
    private String token;

    @Column(nullable = false)
    @Builder.Default
    private boolean expired = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;
}
