package ac.gov.br.sigei_backend.controllers.relatorio;

import ac.gov.br.sigei_backend.service.relatorio.ArquivoRelatorio;
import ac.gov.br.sigei_backend.service.relatorio.FormatoRelatorio;
import ac.gov.br.sigei_backend.service.relatorio.RelatorioService;
import ac.gov.br.sigei_backend.service.relatorio.TipoRelatorio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/relatorios")
@Tag(name = "Relatórios", description = "Relatórios exportáveis em planilha (.xlsx) e PDF")
public class RelatorioController {

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @GetMapping("/{tipo}")
    @Operation(summary = "Gerar relatório",
            description = "Gera o relatório painel (indicadores consolidados), escolas, matriculas ou professores do ano informado, opcionalmente de um município, em xlsx ou pdf. CPF, matrícula e número do contrato nunca são incluídos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo do relatório", content = {
                    @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                    @Content(mediaType = "application/pdf")}),
            @ApiResponse(responseCode = "400", description = "Formato inválido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "404", description = "Relatório, ano ou município inexistente", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<byte[]> gerar(
            @Parameter(description = "painel, escolas, matriculas ou professores", required = true) @PathVariable String tipo,
            @Parameter(description = "xlsx ou pdf") @RequestParam(defaultValue = "xlsx") String formato,
            @Parameter(description = "Ano de referência; sem ele, o mais recente", example = "2025") @RequestParam(required = false) Integer ano,
            @Parameter(description = "ID do município; sem ele, todos") @RequestParam(required = false) Long municipioId) {

        ArquivoRelatorio arquivo = relatorioService.gerar(
                TipoRelatorio.daChave(tipo), FormatoRelatorio.daExtensao(formato), ano, municipioId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(arquivo.tipoMidia()))
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(arquivo.nome()).build().toString())
                .body(arquivo.conteudo());
    }
}
