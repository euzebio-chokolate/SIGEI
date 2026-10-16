package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.dto.request.InfraestruturaEscolaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.InfraestruturaEscolaResponseDTO;
import ac.gov.br.sigei_backend.service.InfraestruturaEscolaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/infraestruturas")
@Tag(name = "Infraestruturas Escolares", description = "Endpoints para gerenciamento de infraestruturas escolares")
public class InfraestruturaEscolaController {
    @Autowired
    InfraestruturaEscolaService infraestruturaService;

    @PostMapping
    @Operation(summary = "Salvar infraestrutura escolar",
            description = "Cadastra um novo registro de infraestrutura escolar com os dados informados no corpo da requisição. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Operação realizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = InfraestruturaEscolaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro relacionado não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade ou registro duplicado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<InfraestruturaEscolaResponseDTO> salvar(@Valid @RequestBody InfraestruturaEscolaRequestDTO infraDTO){
        InfraestruturaEscolaResponseDTO infraestruturaEscolaResponseDTO = infraestruturaService.salvar(infraDTO);
        return new ResponseEntity<>(infraestruturaEscolaResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Buscar infraestruturas escolares",
            description = "Retorna todos os registros de infraestruturas escolares. Retorna uma lista vazia quando não há cadastros.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso")
    })
    public ResponseEntity<List<InfraestruturaEscolaResponseDTO>> buscarTudo(){
        List<InfraestruturaEscolaResponseDTO> infras = infraestruturaService.buscarTudo();
        return new ResponseEntity<>(infras, HttpStatus.OK);
    }

    @GetMapping("/mais-recentes")
    @Operation(summary = "Buscar a infraestrutura mais recente de cada escola",
            description = "Retorna uma infraestrutura por escola, a do registro anual mais recente que a possua. Usado pelo mapa público, "
                    + "que não deve repetir marcadores de escolas com mais de um ano registrado. Retorna uma lista vazia quando não há cadastros.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso")
    })
    public ResponseEntity<List<InfraestruturaEscolaResponseDTO>> buscarMaisRecentes() {
        return ResponseEntity.ok(infraestruturaService.buscarMaisRecentes());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar infraestrutura escolar",
            description = "Atualiza os dados de infraestrutura escolar identificado pelo ID informado no caminho. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro ou relacionamento informado não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade na atualização", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<InfraestruturaEscolaResponseDTO> editar(@Parameter(description = "ID de infraestrutura escolar", example = "1") @PathVariable Long id, @Valid @RequestBody InfraestruturaEscolaRequestDTO infraDTO){
        InfraestruturaEscolaResponseDTO infraestruturaEscolaResponseDTO = infraestruturaService.editar(id, infraDTO);
        return new ResponseEntity<>(infraestruturaEscolaResponseDTO, HttpStatus.OK);
    }

    @PostMapping("/{id}/foto")
    @Operation(summary = "Adicionar foto da infraestrutura",
            description = "Recebe a imagem no campo foto de uma requisição multipart/form-data, envia ao Cloudinary e atualiza a referência da foto na infraestrutura. Requer perfil ADMIN.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Imagem da infraestrutura escolar", required = true,
            content = @Content(mediaType = "multipart/form-data",
                    schema = @Schema(type = "object", requiredProperties = {"foto"}),
                    schemaProperties = @SchemaProperty(name = "foto",
                            schema = @Schema(type = "string", format = "binary"))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Campo foto ausente ou requisição multipart inválida", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Infraestrutura não encontrada", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "503", description = "Armazenamento de imagens não configurado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Falha no envio da imagem ao armazenamento", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<InfraestruturaEscolaResponseDTO> adicionarFoto(
            @Parameter(description = "ID do registro de infraestrutura escolar", example = "1") @PathVariable Long id,
            @Parameter(description = "Arquivo de imagem enviado como multipart/form-data", required = true) @RequestParam("foto") MultipartFile foto
    ) throws IOException {

        InfraestruturaEscolaResponseDTO infraestrutura =
                infraestruturaService.adicionarFoto(id, foto);

        return new ResponseEntity<>(
                infraestrutura,
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir infraestrutura escolar",
            description = "Remove o registro de infraestrutura escolar pelo ID. A resposta de sucesso não possui corpo. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registro excluído", content = @Content),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "O registro possui vínculos que impedem a exclusão", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<Void> excluir(@Parameter(description = "ID do registro de infraestrutura escolar", example = "1") @PathVariable Long id){
        infraestruturaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

}
