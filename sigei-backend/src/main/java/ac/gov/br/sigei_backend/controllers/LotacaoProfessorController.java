package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.dto.request.LotacaoProfessorRequestDTO;
import ac.gov.br.sigei_backend.dto.response.LotacaoProfessorResponseDTO;
import ac.gov.br.sigei_backend.service.LotacaoProfessorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lotacao")
@Tag(name = "Lotações de professores", description = "Lotação anual dos professores em escolas ou no núcleo do município. Sem professor, a lotação é uma vaga em aberto. O número do contrato só é devolvido ao perfil ADMIN.")
public class LotacaoProfessorController {

    @Autowired
    LotacaoProfessorService lotacaoService;

    @PostMapping("/salvar")
    @Operation(summary = "Salvar lotação",
            description = "Cadastra uma lotação. Sem professorId, é uma vaga em aberto; sem escolaId, é uma lotação no núcleo e exige municipioId. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Operação realizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LotacaoProfessorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou tipo de contrato fora das opções", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Professor, ano, escola ou município não encontrado", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<LotacaoProfessorResponseDTO> salvar(@Valid @RequestBody LotacaoProfessorRequestDTO lotacaoDTO) {
        return new ResponseEntity<>(lotacaoService.salvar(lotacaoDTO), HttpStatus.CREATED);
    }

    @GetMapping("")
    @Operation(summary = "Buscar lotações",
            description = "Retorna as lotações de todos os anos. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LotacaoProfessorResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<List<LotacaoProfessorResponseDTO>> buscarTudo() {
        return ResponseEntity.ok(lotacaoService.buscarTudo());
    }

    @GetMapping("/buscarPorAno")
    @Operation(summary = "Buscar lotações por ano",
            description = "Retorna as lotações do ano civil informado (por exemplo, 2026), por município e escola. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LotacaoProfessorResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<List<LotacaoProfessorResponseDTO>> buscarPorAno(@Parameter(description = "Ano civil", example = "2026") @RequestParam Integer ano) {
        return ResponseEntity.ok(lotacaoService.buscarPorAno(ano));
    }

    @GetMapping("/buscarPorProfessor")
    @Operation(summary = "Buscar lotações de um professor",
            description = "Retorna as lotações do professor em todos os anos, da mais recente à mais antiga. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LotacaoProfessorResponseDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Professor não encontrado", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<List<LotacaoProfessorResponseDTO>> buscarPorProfessor(@Parameter(description = "ID do professor", example = "1") @RequestParam Long professorId) {
        return ResponseEntity.ok(lotacaoService.buscarPorProfessor(professorId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar lotação por ID", description = "Retorna a lotação. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<LotacaoProfessorResponseDTO> buscarPorId(@Parameter(description = "ID da lotação", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(lotacaoService.buscarPorId(id));
    }

    @PutMapping("/editar")
    @Operation(summary = "Editar lotação",
            description = "Atualiza a lotação identificada pelo campo id do corpo. O número do contrato informado substitui o gravado; vazio limpa o campo. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou tipo de contrato fora das opções", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Registro ou relacionamento não encontrado", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<LotacaoProfessorResponseDTO> editar(@Valid @RequestBody LotacaoProfessorRequestDTO lotacaoDTO) {
        return ResponseEntity.ok(lotacaoService.editar(lotacaoDTO));
    }

    @DeleteMapping("/excluir/{id}")
    @Operation(summary = "Excluir lotação", description = "Remove a lotação. A resposta de sucesso não possui corpo. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Operação realizada com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<Void> excluir(@Parameter(description = "ID da lotação", example = "1") @PathVariable Long id) {
        lotacaoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
