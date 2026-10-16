package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.dto.request.ProfessorRequestDTO;
import ac.gov.br.sigei_backend.dto.response.ProfessorResponseDTO;
import ac.gov.br.sigei_backend.dto.response.LotacaoProfessorResponseDTO;
import ac.gov.br.sigei_backend.service.LotacaoProfessorService;
import ac.gov.br.sigei_backend.service.ProfessorService;
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
@RequestMapping("/professores")
@Tag(name = "Professores", description = "Dados pessoais dos professores. CPF e matrícula são sensíveis: ficam cifrados no banco e só são devolvidos ao perfil ADMIN.")
public class ProfessorController {

    @Autowired
    ProfessorService professorService;

    @Autowired
    LotacaoProfessorService lotacaoService;

    @PostMapping
    @Operation(summary = "Salvar professor",
            description = "Cadastra um professor. O CPF é opcional, aceita pontuação e precisa ser válido e único. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Operação realizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProfessorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, CPF inválido ou já cadastrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<ProfessorResponseDTO> salvar(@Valid @RequestBody ProfessorRequestDTO professorDTO) {
        return new ResponseEntity<>(professorService.salvar(professorDTO), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Buscar professores",
            description = "Retorna os professores em ordem alfabética. CPF e matrícula vêm nulos para quem não é ADMIN. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProfessorResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<List<ProfessorResponseDTO>> buscarTudo() {
        return ResponseEntity.ok(professorService.buscarTudo());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar professor por ID",
            description = "Retorna o professor. CPF e matrícula vêm nulos para quem não é ADMIN. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<ProfessorResponseDTO> buscarPorId(@Parameter(description = "ID do professor", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(professorService.buscarPorId(id));
    }

    @GetMapping("/{id}/lotacoes")
    @Operation(summary = "Buscar lotações do professor",
            description = "Retorna as lotações do professor em todos os anos, da mais recente à mais antiga. O número do contrato só é devolvido ao perfil ADMIN. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LotacaoProfessorResponseDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Professor não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<List<LotacaoProfessorResponseDTO>> buscarLotacoes(@Parameter(description = "ID do professor", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(lotacaoService.buscarPorProfessor(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar professor",
            description = "Atualiza o professor identificado pelo ID informado no caminho. CPF e matrícula informados substituem os gravados; vazios limpam o campo. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, CPF inválido ou já cadastrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<ProfessorResponseDTO> editar(@Parameter(description = "ID de professor", example = "1") @PathVariable Long id, @Valid @RequestBody ProfessorRequestDTO professorDTO) {
        return ResponseEntity.ok(professorService.editar(id, professorDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir professor",
            description = "Remove o professor e as suas lotações. A resposta de sucesso não possui corpo. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Operação realizada com sucesso", content = @Content),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<Void> excluir(@Parameter(description = "ID do professor", example = "1") @PathVariable Long id) {
        professorService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
