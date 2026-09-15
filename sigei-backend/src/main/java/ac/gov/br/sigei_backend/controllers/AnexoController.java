package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.dto.request.AnexoRequestDTO;
import ac.gov.br.sigei_backend.dto.response.AnexoResponseDTO;
import ac.gov.br.sigei_backend.service.AnexoService;
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
@RequestMapping("/anexo")
@Tag(name = "Anexos", description = "Endpoints para gerenciamento de vínculos de escolas anexo")
public class AnexoController {

    @Autowired
    AnexoService anexoService;

    @PostMapping("/salvar")
    @Operation(summary = "Salvar vínculo de escola anexo",
            description = "Cadastra um novo registro de vínculo de escola anexo com os dados informados no corpo da requisição. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Operação realizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AnexoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Registro relacionado não encontrado", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade ou registro duplicado", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity salvar(@Valid @RequestBody AnexoRequestDTO anexoDTO){
        AnexoResponseDTO anexoResponseDTO = anexoService.salvar(anexoDTO);
        return new ResponseEntity<AnexoResponseDTO>(anexoResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping("")
    @Operation(summary = "Buscar vínculos de escolas anexo",
            description = "Retorna todos os registros de vínculos de escolas anexo. Retorna uma lista vazia quando não há cadastros. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AnexoResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity buscarTudo(){
        List<AnexoResponseDTO> anexos = anexoService.buscarTudo();
        return new ResponseEntity<List<AnexoResponseDTO>>(anexos, HttpStatus.OK);
    }

    @PutMapping("/editar")
    @Operation(summary = "Editar vínculo de escola anexo",
            description = "Atualiza os dados de vínculo de escola anexo identificado pelo campo id do corpo da requisição. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Registro ou relacionamento informado não encontrado", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade na atualização", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<AnexoResponseDTO> editar(@RequestBody AnexoRequestDTO anexoDTO){
        AnexoResponseDTO anexoResponseDTO = anexoService.editar(anexoDTO);
        return new ResponseEntity<AnexoResponseDTO>(anexoResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/excluir/{id}")
    @Operation(summary = "Excluir vínculo de escola anexo",
            description = "Remove o registro de vínculo de escola anexo pelo ID. A resposta de sucesso não possui corpo. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso", content = @Content),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "O registro possui vínculos que impedem a exclusão", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<AnexoResponseDTO> excluir(@Parameter(description = "ID do registro de vínculo de escola anexo", example = "1") @PathVariable Long id){
        anexoService.excluir(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
