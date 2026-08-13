package com.treino.controller;

import com.treino.dto.Create.ClaseCreateDTO;
import com.treino.dto.Update.ClaseUpdateDTO;
import com.treino.dto.Response.ClaseResponseDTO;
import com.treino.dto.Response.ReservaResponseDTO;
import com.treino.entity.Usuario;
import com.treino.service.ClaseService;
import com.treino.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/clases")
@RequiredArgsConstructor
public class ClaseController {

    private final ClaseService claseService;
    private final ReservaService reservaService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<ClaseResponseDTO> listarClases(
            @RequestParam(required = false) Long sedeId,
            @RequestParam(required = false) String disciplina,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return claseService.listar(sedeId, disciplina, fecha);
    }

    @GetMapping("/{id}/reservas")
    @PreAuthorize("hasAnyRole('PROFESOR', 'ADMINISTRADOR')")
    public List<ReservaResponseDTO> listarReservasPorClase(@PathVariable Long id) {
        return reservaService.obtenerReservasPorClase(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('PROFESOR', 'ADMINISTRADOR')")
    public ClaseResponseDTO crearClase(
            @Valid @RequestBody ClaseCreateDTO dto,
            @AuthenticationPrincipal Usuario usuario) {
        return claseService.crear(dto, usuario.getUserId());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROFESOR', 'ADMINISTRADOR')")
    public ClaseResponseDTO actualizarClase(
            @PathVariable Long id,
            @Valid @RequestBody ClaseUpdateDTO dto,
            @AuthenticationPrincipal Usuario usuario) {
        dto.setId(id);
        String role = usuario.getAuthorities().iterator().next().getAuthority();
        return claseService.actualizar(dto, usuario.getUserId(), role);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROFESOR', 'ADMINISTRADOR')")
    public void eliminarClase(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        String role = usuario.getAuthorities().iterator().next().getAuthority();
        claseService.eliminar(id, usuario.getUserId(), role);
    }
}
