package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.dto.request.MatriculaNivelRequestDTO;
import ac.gov.br.sigei_backend.dto.response.MatriculaNivelResponseDTO;
import ac.gov.br.sigei_backend.service.MatriculaNivelService;
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
@RequestMapping("/matriculas-nivel")
@Tag(name = "Matrículas por Nível", description = "Endpoints para gerenciamento de matrículas por nível")
public class MatriculaNivelController {
    @Autowired
    MatriculaNivelService matriculaNivelService;

    @PostMapping
    @Operation(summary = "Salvar matrícula por nível",
            description = "Cadastra um novo registro de matrícula por nível com os dados informados no corpo da requisição. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Operação realizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatriculaNivelResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro relacionado não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade ou registro duplicado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<MatriculaNivelResponseDTO> salvar(@Valid @RequestBody MatriculaNivelRequestDTO matriculaNivelDTO){
        MatriculaNivelResponseDTO matriculaNivelResponseDTO = matriculaNivelService.salvar(matriculaNivelDTO);
        return new ResponseEntity<>(matriculaNivelResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Buscar matrículas por nível",
            description = "Retorna todos os registros de matrículas por nível. Retorna uma lista vazia quando não há cadastros. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<List<MatriculaNivelResponseDTO>> buscarTudo(){
        List<MatriculaNivelResponseDTO> matriculaNivelDTOS = matriculaNivelService.buscarTudo();
        return new ResponseEntity<>(matriculaNivelDTOS, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar matrícula por nível",
            description = "Atualiza os dados de matrícula por nível identificado pelo ID informado no caminho. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MatriculaNivelResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro ou relacionamento informado não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade na atualização", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<MatriculaNivelResponseDTO> editar(@Parameter(description = "ID de matrícula por nível", example = "1") @PathVariable Long id, @Valid @RequestBody MatriculaNivelRequestDTO matriculaNivelDTO){
        MatriculaNivelResponseDTO matriculaNivelResponseDTO = matriculaNivelService.editar(id, matriculaNivelDTO);
        return new ResponseEntity<>(matriculaNivelResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir matrícula por nível",
            description = "Remove o registro de matrícula por nível pelo ID. A resposta de sucesso não possui corpo. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registro excluído", content = @Content),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "O registro possui vínculos que impedem a exclusão", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<Void> excluir(@Parameter(description = "ID do registro de matrícula por nível", example = "1") @PathVariable Long id){
        matriculaNivelService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
