package com.treino.controller;

import com.treino.dto.Create.UsuarioCreateDTO;
import com.treino.dto.Update.UsuarioUpdateDTO;
import com.treino.dto.Response.UsuarioResponseDTO;
import com.treino.entity.Usuario;
import com.treino.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UsuarioResponseDTO miPerfil(@AuthenticationPrincipal Usuario usuario) {
        return usuarioService.obtenerPerfil(usuario.getUserId());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ROLE_ADMINISTRADOR')")
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioService.listarTodos();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ROLE_ADMINISTRADOR')")
    public UsuarioResponseDTO crearUsuario(@Valid @RequestBody UsuarioCreateDTO dto) {
        return usuarioService.crear(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public UsuarioResponseDTO actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateDTO dto,
            @AuthenticationPrincipal Usuario usuarioAuth) {
        // Un usuario común solo puede actualizar su propio perfil, Admin puede actualizar a cualquiera
        boolean esAdmin = usuarioAuth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
        if (!esAdmin && !usuarioAuth.getUserId().equals(id)) {
            throw new RuntimeException("No tienes permisos para modificar este perfil");
        }
        dto.setId(id);
        return usuarioService.actualizar(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void eliminarUsuario(@PathVariable Long id) {
        usuarioService.eliminar(id);
    }
}
