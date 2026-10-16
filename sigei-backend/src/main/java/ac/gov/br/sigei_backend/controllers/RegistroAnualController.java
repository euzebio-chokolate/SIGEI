package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.dto.request.RegistroAnualRequestDTO;
import ac.gov.br.sigei_backend.dto.response.PaginaDTO;
import ac.gov.br.sigei_backend.dto.response.RegistroAnualResponseDTO;
import ac.gov.br.sigei_backend.service.RegistroAnualService;
import ac.gov.br.sigei_backend.utils.Paginacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/registros-anuais")
@Tag(name = "Registros Anuais", description = "Endpoints para gerenciamento de registros anuais")
public class RegistroAnualController {
    @Autowired
    RegistroAnualService registroAnualService;

    @PostMapping
    @Operation(summary = "Salvar registro anual",
            description = "Cadastra um novo registro de registro anual com os dados informados no corpo da requisição. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Operação realizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegistroAnualResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro relacionado não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade ou registro duplicado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<RegistroAnualResponseDTO> salvar(@Valid @RequestBody RegistroAnualRequestDTO registroDTO){
        RegistroAnualResponseDTO registroAnualResponseDTO = registroAnualService.salvar(registroDTO);
        return new ResponseEntity<>(registroAnualResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Buscar registros anuais",
            description = "Retorna, paginados, os registros anuais de todos os anos, do mais recente ao mais antigo e, no ano, pelo nome da escola. "
                    + "Os filtros são opcionais. Para um ano inteiro sem paginação, use /anos/{ano}/registros-anuais. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Paginação inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<PaginaDTO<RegistroAnualResponseDTO>> buscar(
            @Parameter(description = "Ano civil", example = "2025") @RequestParam(required = false) Integer ano,
            @Parameter(description = "ID da escola", example = "1") @RequestParam(required = false) Long escolaId,
            @Parameter(description = "ID do município da escola", example = "1") @RequestParam(required = false) Long municipioId,
            @Parameter(description = "Página, a partir de zero", example = "0") @RequestParam(defaultValue = "0") int pagina,
            @Parameter(description = "Registros por página (máximo " + Paginacao.TAMANHO_MAXIMO + ")", example = "20") @RequestParam(defaultValue = "" + Paginacao.TAMANHO_PADRAO) int tamanho
    ) {
        return ResponseEntity.ok(registroAnualService.buscar(ano, escolaId, municipioId, pagina, tamanho));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar registro anual",
            description = "Atualiza os dados de registro anual identificado pelo ID informado no caminho. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Operação realizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegistroAnualResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro ou relacionamento informado não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade na atualização", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<RegistroAnualResponseDTO> editar(@Parameter(description = "ID de registro anual", example = "1") @PathVariable Long id, @Valid @RequestBody RegistroAnualRequestDTO registroDTO){
        RegistroAnualResponseDTO registroAnualResponseDTO = registroAnualService.editar(id, registroDTO);
        return new ResponseEntity<>(registroAnualResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir registro anual",
            description = "Remove o registro de registro anual pelo ID. A resposta de sucesso não possui corpo. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registro excluído", content = @Content),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "O registro possui vínculos que impedem a exclusão", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<Void> excluir(@Parameter(description = "ID do registro de registro anual", example = "1") @PathVariable Long id){
        registroAnualService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
