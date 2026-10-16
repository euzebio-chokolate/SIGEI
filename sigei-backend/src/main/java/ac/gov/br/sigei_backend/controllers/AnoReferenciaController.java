package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.dto.request.AnoReferenciaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.AnoReferenciaResponseDTO;
import ac.gov.br.sigei_backend.service.AnoReferenciaService;
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
@RequestMapping("/anos-referencia")
@Tag(name = "Anos de Referência", description = "Endpoints para gerenciamento de anos de referência")
public class AnoReferenciaController {

    @Autowired
    AnoReferenciaService anoReferenciaService;

    @PostMapping
    @Operation(summary = "Salvar ano de referência",
            description = "Cadastra um novo registro de ano de referência com os dados informados no corpo da requisição. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade ou registro duplicado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<AnoReferenciaResponseDTO> salvar(@Valid @RequestBody AnoReferenciaRequestDTO dto) {
        return new ResponseEntity<>(anoReferenciaService.salvar(dto), HttpStatus.CREATED);
    }

    @PostMapping("/proximo")
    @Operation(summary = "Iniciar o próximo ano de referência",
            description = "Cria o ano seguinte ao maior ano cadastrado e copia os últimos registros das escolas, incluindo equipe, infraestrutura e matrículas, "
                    + "além das lotações de professores do último ano. Escolas sem registro anterior são ignoradas. Retorna o ano criado. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ano criado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AnoReferenciaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "O novo ano já foi iniciado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Nenhum ano de referência cadastrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade ao criar o novo ano", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<AnoReferenciaResponseDTO> iniciarProximo() {
        return new ResponseEntity<>(anoReferenciaService.iniciarProximo(), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Buscar anos de referência",
            description = "Retorna todos os registros de anos de referência. Retorna uma lista vazia quando não há cadastros. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<List<AnoReferenciaResponseDTO>> buscarTudo() {
        return ResponseEntity.ok(anoReferenciaService.buscarTudo());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar ano de referência",
            description = "Atualiza os dados de ano de referência identificado pelo ID informado no caminho. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro ou relacionamento informado não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade na atualização", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<AnoReferenciaResponseDTO> editar(@Parameter(description = "ID de ano de referência", example = "1") @PathVariable Long id, @Valid @RequestBody AnoReferenciaRequestDTO dto) {
        return ResponseEntity.ok(anoReferenciaService.editar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir ano de referência",
            description = "Remove o registro de ano de referência pelo ID. A resposta de sucesso não possui corpo. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Operação realizada com sucesso", content = @Content),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "O registro possui vínculos que impedem a exclusão", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<Void> excluir(@Parameter(description = "ID do registro de ano de referência", example = "1") @PathVariable Long id) {
        anoReferenciaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}