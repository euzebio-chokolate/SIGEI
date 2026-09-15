package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.dto.request.PovoRequestDTO;
import ac.gov.br.sigei_backend.dto.response.PovoResponseDTO;
import ac.gov.br.sigei_backend.service.PovoService;
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
@RequestMapping("/povo")
@Tag(name = "Povos", description = "Endpoints para gerenciamento de povos")
public class PovoController {
    @Autowired
    PovoService povoService;

    @PostMapping("/salvar")
    @Operation(summary = "Salvar povo",
            description = "Cadastra um novo registro de povo com os dados informados no corpo da requisição. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Operação realizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PovoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade ou registro duplicado", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity salvar(@Valid @RequestBody PovoRequestDTO povoDTO){
        PovoResponseDTO povoResponseDTO = povoService.salvar(povoDTO);
        return new ResponseEntity<PovoResponseDTO>(povoResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping("")
    @Operation(summary = "Buscar povos",
            description = "Retorna todos os registros de povos. Retorna uma lista vazia quando não há cadastros. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PovoResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity buscarTudo(){
        List<PovoResponseDTO> povos = povoService.buscarTudo();
        return new ResponseEntity<List<PovoResponseDTO>>(povos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar povo por ID",
            description = "Retorna os detalhes de povo correspondente ao ID informado. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<PovoResponseDTO> buscarPorId(@Parameter(description = "ID de povo", example = "1") @PathVariable Long id){
        PovoResponseDTO povoResponseDTO = povoService.buscarPorId(id);
        return new ResponseEntity<>(povoResponseDTO, HttpStatus.OK);
    }

    @PutMapping("/editar")
    @Operation(summary = "Editar povo",
            description = "Atualiza os dados de povo identificado pelo campo id do corpo da requisição. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Registro ou relacionamento informado não encontrado", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade na atualização", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<PovoResponseDTO> editar(@RequestBody PovoRequestDTO povoDTO){
        PovoResponseDTO povoResponseDTO = povoService.editar(povoDTO);
        return new ResponseEntity<PovoResponseDTO>(povoResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/excluir/{id}")
    @Operation(summary = "Excluir povo",
            description = "Remove o registro de povo pelo ID. A resposta de sucesso não possui corpo. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso", content = @Content),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "O registro possui vínculos que impedem a exclusão", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<?> excluir(@Parameter(description = "ID do registro de povo", example = "1") @PathVariable Long id){
        povoService.excluir(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
