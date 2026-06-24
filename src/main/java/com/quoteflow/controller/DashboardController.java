package com.quoteflow.controller;

import com.quoteflow.dto.dashboard.StatsResponse;
import com.quoteflow.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Statistiques du tableau de bord

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Dashboard", description = "Statistiques et indicateurs de performance")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "Statistiques globales")
    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> statsGlobales() {
        return ResponseEntity.ok(dashboardService.statsGlobales());
    }

    @Operation(summary = "Performance d'un employe")
    @GetMapping("/employe/{id}/stats")
    public ResponseEntity<Map<String, Long>> statsEmploye(@PathVariable Long id) {
        return ResponseEntity.ok(dashboardService.statsEmploye(id));
    }
}
