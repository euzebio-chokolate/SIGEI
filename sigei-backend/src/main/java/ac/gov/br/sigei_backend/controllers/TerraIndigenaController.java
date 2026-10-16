package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.dto.request.TerraIndigenaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.TerraIndigenaEstatisticaResponseDTO;
import ac.gov.br.sigei_backend.dto.response.TerraIndigenaResponseDTO;
import ac.gov.br.sigei_backend.service.TerraIndigenaService;
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
@RequestMapping("/terras-indigenas")
@Tag(name = "Terras Indígenas", description = "Endpoints para gerenciamento de terras indígenas")
public class TerraIndigenaController {

    @Autowired
    TerraIndigenaService terraIndigenaService;

    @PostMapping
    @Operation(summary = "Salvar terra indígena",
            description = "Cadastra um novo registro de terra indígena com os dados informados no corpo da requisição. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro relacionado não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade ou registro duplicado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<TerraIndigenaResponseDTO> salvar(@Valid @RequestBody TerraIndigenaRequestDTO terraIndigenaDTO) {
        TerraIndigenaResponseDTO terraIndigenaResponseDTO = terraIndigenaService.salvar(terraIndigenaDTO);
        return new ResponseEntity<>(terraIndigenaResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Buscar terras indígenas",
            description = "Retorna todos os registros de terras indígenas. Retorna uma lista vazia quando não há cadastros. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<List<TerraIndigenaResponseDTO>> buscar() {
        List<TerraIndigenaResponseDTO> terrasIndigenas = terraIndigenaService.buscarTudo();
        return new ResponseEntity<>(terrasIndigenas, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar terra indígena por ID",
            description = "Retorna os detalhes de terra indígena correspondente ao ID informado. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<TerraIndigenaResponseDTO> buscarPorId(@Parameter(description = "ID da terra indígena", example = "1") @PathVariable Long id) {
        TerraIndigenaResponseDTO terraIndigenaResponseDTO = terraIndigenaService.buscarPorId(id);
        return new ResponseEntity<>(terraIndigenaResponseDTO, HttpStatus.OK);
    }

    @GetMapping("/estatisticas")
    @Operation(summary = "Buscar estatísticas das terras indígenas",
            description = "Retorna o município e a quantidade de aldeias e escolas por terra indígena. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<List<TerraIndigenaEstatisticaResponseDTO>> buscarEstatisticas() {

        List<TerraIndigenaEstatisticaResponseDTO> terras =
                terraIndigenaService.buscarEstatisticas();

        return new ResponseEntity<>(
                terras,
                HttpStatus.OK
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar terra indígena",
            description = "Atualiza os dados de terra indígena identificado pelo ID informado no caminho. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro ou relacionamento informado não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade na atualização", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })

    public ResponseEntity<TerraIndigenaResponseDTO> editar(@Parameter(description = "ID de terra indígena", example = "1") @PathVariable Long id, @Valid @RequestBody TerraIndigenaRequestDTO terraIndigenaDTO) {
        TerraIndigenaResponseDTO terraIndigenaResponseDTO = terraIndigenaService.editar(id, terraIndigenaDTO);
        return new ResponseEntity<>(terraIndigenaResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir terra indígena",
            description = "Remove o registro de terra indígena pelo ID. A resposta de sucesso não possui corpo. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registro excluído", content = @Content),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "O registro possui vínculos que impedem a exclusão", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<Void> excluir(@Parameter(description = "ID da terra indígena", example = "1") @PathVariable Long id) {
        terraIndigenaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
