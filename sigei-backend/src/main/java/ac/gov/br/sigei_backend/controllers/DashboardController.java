package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.DTOs.response.dashboard.DashboardResponseDTO;
import ac.gov.br.sigei_backend.services.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardResponseDTO> buscar(
            @RequestParam(required = false) Integer ano
    ) {
        return ResponseEntity.ok(dashboardService.buscar(ano));
    }
}
