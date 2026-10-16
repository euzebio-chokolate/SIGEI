package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.dto.response.InfraestruturaEscolaResponseDTO;
import ac.gov.br.sigei_backend.dto.response.LotacaoProfessorResponseDTO;
import ac.gov.br.sigei_backend.dto.response.RegistroAnualResponseDTO;
import ac.gov.br.sigei_backend.service.InfraestruturaEscolaService;
import ac.gov.br.sigei_backend.service.LotacaoProfessorService;
import ac.gov.br.sigei_backend.service.RegistroAnualService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Listas completas de um ano, sem paginação, usadas pelas telas. O {ano} do caminho é o
 * valor do ano civil (2025), e não o ID de {@code AnoReferencia}, que fica em /anos-referencia.
 */
@RestController
@RequestMapping("/anos/{ano}")
@Tag(name = "Consultas por ano", description = "Listas completas de um ano civil, sem paginação")
public class AnoController {

    @Autowired
    RegistroAnualService registroAnualService;

    @Autowired
    InfraestruturaEscolaService infraestruturaService;

    @Autowired
    LotacaoProfessorService lotacaoService;

    @GetMapping("/registros-anuais")
    @Operation(summary = "Buscar registros anuais do ano",
            description = "Retorna todos os registros anuais do ano informado. Retorna uma lista vazia quando não há resultados. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = RegistroAnualResponseDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Ano em formato inválido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<List<RegistroAnualResponseDTO>> buscarRegistrosAnuais(
            @Parameter(description = "Ano civil (valor do ano, não o ID)", example = "2025") @PathVariable Integer ano) {
        return ResponseEntity.ok(registroAnualService.buscarPorAno(ano));
    }

    @GetMapping("/infraestruturas")
    @Operation(summary = "Buscar infraestruturas escolares do ano",
            description = "Retorna as infraestruturas escolares do ano informado. Retorna uma lista vazia quando não há resultados. Público, como as demais leituras de infraestrutura.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = InfraestruturaEscolaResponseDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Ano em formato inválido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<List<InfraestruturaEscolaResponseDTO>> buscarInfraestruturas(
            @Parameter(description = "Ano civil (valor do ano, não o ID)", example = "2025") @PathVariable Integer ano) {
        return ResponseEntity.ok(infraestruturaService.buscarPorAno(ano));
    }

    @GetMapping("/lotacoes")
    @Operation(summary = "Buscar lotações do ano",
            description = "Retorna as lotações do ano civil informado, por município e escola. O número do contrato só é devolvido ao perfil ADMIN. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LotacaoProfessorResponseDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Ano em formato inválido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<List<LotacaoProfessorResponseDTO>> buscarLotacoes(
            @Parameter(description = "Ano civil (valor do ano, não o ID)", example = "2026") @PathVariable Integer ano) {
        return ResponseEntity.ok(lotacaoService.buscarPorAno(ano));
    }
}
