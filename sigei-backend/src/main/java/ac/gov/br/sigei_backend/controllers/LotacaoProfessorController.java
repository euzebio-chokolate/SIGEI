package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.dto.request.LotacaoProfessorRequestDTO;
import ac.gov.br.sigei_backend.dto.response.LotacaoProfessorResponseDTO;
import ac.gov.br.sigei_backend.dto.response.PaginaDTO;
import ac.gov.br.sigei_backend.service.LotacaoProfessorService;
import ac.gov.br.sigei_backend.utils.Paginacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lotacoes")
@Tag(name = "Lotações de professores", description = "Lotação anual dos professores em escolas ou no núcleo do município. Sem professor, a lotação é uma vaga em aberto. O número do contrato só é devolvido ao perfil ADMIN.")
public class LotacaoProfessorController {

    @Autowired
    LotacaoProfessorService lotacaoService;

    @PostMapping
    @Operation(summary = "Salvar lotação",
            description = "Cadastra uma lotação. Sem professorId, é uma vaga em aberto; sem escolaId, é uma lotação no núcleo e exige municipioId. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Operação realizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LotacaoProfessorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou tipo de contrato fora das opções", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Professor, ano, escola ou município não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<LotacaoProfessorResponseDTO> salvar(@Valid @RequestBody LotacaoProfessorRequestDTO lotacaoDTO) {
        return new ResponseEntity<>(lotacaoService.salvar(lotacaoDTO), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Buscar lotações",
            description = "Retorna, paginadas, as lotações de todos os anos, do mais recente ao mais antigo e, no ano, por município, escola (núcleo primeiro) e professor (vagas por último). "
                    + "Os filtros são opcionais. Para um ano inteiro sem paginação, use /anos/{ano}/lotacoes. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Paginação inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<PaginaDTO<LotacaoProfessorResponseDTO>> buscar(
            @Parameter(description = "Ano civil", example = "2026") @RequestParam(required = false) Integer ano,
            @Parameter(description = "ID do professor", example = "1") @RequestParam(required = false) Long professorId,
            @Parameter(description = "ID da escola", example = "1") @RequestParam(required = false) Long escolaId,
            @Parameter(description = "ID do município", example = "1") @RequestParam(required = false) Long municipioId,
            @Parameter(description = "Página, a partir de zero", example = "0") @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Lotações por página (máximo " + Paginacao.TAMANHO_MAXIMO + ")", example = "20") @RequestParam(defaultValue = "" + Paginacao.TAMANHO_PADRAO) int tamanho
    ) {
        return ResponseEntity.ok(lotacaoService.buscar(ano, professorId, escolaId, municipioId, pagina, tamanho));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar lotação por ID", description = "Retorna a lotação. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<LotacaoProfessorResponseDTO> buscarPorId(@Parameter(description = "ID da lotação", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(lotacaoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar lotação",
            description = "Atualiza a lotação identificada pelo ID informado no caminho. O número do contrato informado substitui o gravado; vazio limpa o campo. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou tipo de contrato fora das opções", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro ou relacionamento não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<LotacaoProfessorResponseDTO> editar(@Parameter(description = "ID de lotação", example = "1") @PathVariable Long id, @Valid @RequestBody LotacaoProfessorRequestDTO lotacaoDTO) {
        return ResponseEntity.ok(lotacaoService.editar(id, lotacaoDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir lotação", description = "Remove a lotação. A resposta de sucesso não possui corpo. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Operação realizada com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<Void> excluir(@Parameter(description = "ID da lotação", example = "1") @PathVariable Long id) {
        lotacaoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
