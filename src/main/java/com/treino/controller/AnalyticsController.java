package com.treino.controller;

import com.treino.dto.Response.AnalyticsResponseDTO;
import com.treino.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public AnalyticsResponseDTO getDashboardAnalytics() {
        return analyticsService.getDashboardAnalytics();
    }

    @PostMapping("/churn/archivar/{clienteId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public void archivarAlumnoRiesgo(@PathVariable Long clienteId) {
        analyticsService.archivarAlumnoRiesgo(clienteId);
    }
}
