package ac.gov.br.sigei_backend.controllers.auditoria;

import ac.gov.br.sigei_backend.dto.response.historico.OperacaoHistorico;
import ac.gov.br.sigei_backend.dto.response.historico.PaginaAuditoriaDTO;
import ac.gov.br.sigei_backend.service.auditoria.AuditoriaService;
import ac.gov.br.sigei_backend.service.auditoria.FiltroAuditoria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/auditoria")
@Tag(name = "Auditoria", description = "Trilha de auditoria de todo o sistema")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    @Operation(summary = "Buscar alterações de todo o sistema",
            description = "Retorna, paginadas e da mais recente à mais antiga, as criações, alterações e exclusões de todas as entidades auditadas, "
                    + "com autor (nulo quando feita pelo sistema), data e hora, campos alterados e uma descrição do registro (escola, professor ou nome). "
                    + "CPF, matrícula e número do contrato aparecem apenas como \"(dado protegido)\". Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Filtro ou paginação inválidos", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<PaginaAuditoriaDTO> buscar(
            @Parameter(description = "Tipo de registro (nome da entidade)", example = "Escola") @RequestParam(required = false) String entidade,
            @Parameter(description = "Tipo de operação") @RequestParam(required = false) OperacaoHistorico operacao,
            @Parameter(description = "ID do usuário autor das alterações", example = "1") @RequestParam(required = false) Long usuarioId,
            @Parameter(description = "true para só as alterações feitas pelo sistema (sem autor)") @RequestParam(required = false) Boolean sistema,
            @Parameter(description = "Primeiro dia do período (horário do Acre)", example = "2026-10-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @Parameter(description = "Último dia do período, inclusive (horário do Acre)", example = "2026-10-31") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @Parameter(description = "Página, a partir de zero", example = "0") @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Alterações por página (máximo " + AuditoriaService.TAMANHO_MAXIMO + ")", example = "20") @RequestParam(defaultValue = "20") int tamanho
    ) {
        FiltroAuditoria filtro = new FiltroAuditoria(entidade, operacao, usuarioId, sistema, de, ate);
        return ResponseEntity.ok(auditoriaService.buscar(filtro, pagina, tamanho));
    }
}
