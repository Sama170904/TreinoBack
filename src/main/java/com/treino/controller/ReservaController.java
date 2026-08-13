package com.treino.controller;

import com.treino.dto.Create.ReservaCreateDTO;
import com.treino.dto.Response.ReservaResponseDTO;
import com.treino.entity.Usuario;
import com.treino.service.CheckInService;
import com.treino.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;
    private final CheckInService checkInService;

    @GetMapping("/mis-reservas")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMINISTRADOR')")
    public List<ReservaResponseDTO> misReservas(@AuthenticationPrincipal Usuario usuario) {
        return reservaService.obtenerMisReservas(usuario.getUserId());
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ReservaResponseDTO crearReserva(
            @Valid @RequestBody ReservaCreateDTO dto,
            @AuthenticationPrincipal Usuario usuario) {
        return reservaService.crearReserva(usuario.getUserId(), dto.getClaseId());
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('CLIENTE')")
    public ReservaResponseDTO cancelarReserva(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        return reservaService.cancelarReserva(id, usuario.getUserId());
    }

    @PutMapping("/{id}/check-in")
    @PreAuthorize("hasAnyRole('PROFESOR', 'ADMINISTRADOR')")
    public void marcarCheckIn(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, String> body,
            @RequestParam(required = false) String estado,
            @AuthenticationPrincipal Usuario usuario) {
        String estadoFinal = (body != null && body.containsKey("estado")) ? body.get("estado") : estado;
        if (estadoFinal == null || estadoFinal.isBlank()) {
            throw new IllegalArgumentException("El estado de asistencia es obligatorio");
        }
        checkInService.marcarAsistencia(id, estadoFinal, usuario);
    }
}
