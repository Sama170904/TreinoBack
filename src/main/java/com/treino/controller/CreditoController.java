package com.treino.controller;

import com.treino.dto.Create.CreditoAsignarDTO;
import com.treino.dto.Create.CreditoQuitarDTO;
import com.treino.dto.Response.CreditoResponseDTO;
import com.treino.entity.HistorialCredito;
import com.treino.entity.Usuario;
import com.treino.service.CreditoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/creditos")
@RequiredArgsConstructor
public class CreditoController {

    private final CreditoService creditoService;

    @PostMapping("/asignar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public CreditoResponseDTO asignarCreditos(@Valid @RequestBody CreditoAsignarDTO dto) {
        return creditoService.asignar(dto);
    }

    @PostMapping("/quitar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void quitarCreditos(@Valid @RequestBody CreditoQuitarDTO dto) {
        creditoService.quitar(dto);
    }

    @GetMapping("/mi-saldo")
    @PreAuthorize("hasRole('CLIENTE')")
    public int miSaldo(@AuthenticationPrincipal Usuario usuario) {
        return creditoService.consultarSaldo(usuario.getUserId());
    }

    @GetMapping("/historial")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ADMINISTRADOR')")
    public List<HistorialCredito> historial(
            @RequestParam(required = false) Long clienteId,
            @AuthenticationPrincipal Usuario usuario) {
        Long targetId = clienteId != null ? clienteId : usuario.getUserId();
        return creditoService.consultarHistorial(targetId);
    }
}
