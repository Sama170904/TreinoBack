package com.treino.controller;

import com.treino.dto.Create.SedeCreateDTO;
import com.treino.dto.Update.SedeUpdateDTO;
import com.treino.dto.Response.SedeResponseDTO;
import com.treino.service.SedeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sedes")
@RequiredArgsConstructor
public class SedeController {

    private final SedeService sedeService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<SedeResponseDTO> listarSedes() {
        return sedeService.listarTodas();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public SedeResponseDTO obtenerSede(@PathVariable Long id) {
        return sedeService.obtenerPorId(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public SedeResponseDTO crearSede(@Valid @RequestBody SedeCreateDTO dto) {
        return sedeService.crear(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public SedeResponseDTO actualizarSede(@PathVariable Long id, @Valid @RequestBody SedeUpdateDTO dto) {
        dto.setId(id);
        return sedeService.actualizar(dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void eliminarSede(@PathVariable Long id) {
        sedeService.eliminar(id);
    }
}
