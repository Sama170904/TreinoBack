package com.treino.dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {
    private Long userId;
    private String nombre;
    private String apellido;
    private String email;
    private String rol;
    private String estado;
}
