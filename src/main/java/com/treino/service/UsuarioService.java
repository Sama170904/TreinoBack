package com.treino.service;

import com.treino.dto.Create.UsuarioCreateDTO;
import com.treino.dto.Update.UsuarioUpdateDTO;
import com.treino.dto.Response.UsuarioResponseDTO;
import com.treino.entity.Usuario;
import com.treino.middlewares.ResourceNotFoundException;
import com.treino.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponseDTO crear(UsuarioCreateDTO dto) {
        Usuario usuario = Usuario.builder()
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .rol(Usuario.Rol.valueOf(dto.getRol()))
                .estado(Usuario.Estado.ACTIVO)
                .build();
        usuario = usuarioRepository.save(usuario);
        return mapToResponse(usuario);
    }

    public UsuarioResponseDTO obtenerPerfil(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return mapToResponse(usuario);
    }

    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UsuarioResponseDTO actualizar(UsuarioUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        usuario.setNombre(dto.getNombre());
        usuario.setApellido(dto.getApellido());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        usuario.setRol(Usuario.Rol.valueOf(dto.getRol()));
        usuario = usuarioRepository.save(usuario);
        return mapToResponse(usuario);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        usuarioRepository.deleteById(id);
    }

    private UsuarioResponseDTO mapToResponse(Usuario u) {
        return UsuarioResponseDTO.builder()
                .userId(u.getUserId())
                .nombre(u.getNombre())
                .apellido(u.getApellido())
                .email(u.getEmail())
                .rol(u.getRol().name())
                .estado(u.getEstado() != null ? u.getEstado().name() : "ACTIVO")
                .build();
    }
}
