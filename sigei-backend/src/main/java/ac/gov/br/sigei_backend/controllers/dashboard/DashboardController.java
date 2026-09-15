package ac.gov.br.sigei_backend.controllers.dashboard;

import ac.gov.br.sigei_backend.dto.response.dashboard.DashboardResponseDTO;
import ac.gov.br.sigei_backend.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard", description = "Endpoints para consulta dos indicadores da educação escolar indígena")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    @Operation(summary = "Buscar indicadores do dashboard",
            description = "Retorna os indicadores do ano selecionado e as séries históricas de escolas e matrículas. Sem o parâmetro ano, utiliza o maior ano de referência cadastrado. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Ano informado em formato inválido", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Ano de referência não encontrado ou nenhum ano cadastrado", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<DashboardResponseDTO> buscar(
            @Parameter(description = "Ano de referência; se omitido, utiliza o maior ano cadastrado", example = "2025") @RequestParam(required = false) Integer ano
    ) {
        return ResponseEntity.ok(dashboardService.buscar(ano));
    }
}
