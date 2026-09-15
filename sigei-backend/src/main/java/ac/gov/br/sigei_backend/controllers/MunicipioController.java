package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.dto.request.MunicipioRequestDTO;
import ac.gov.br.sigei_backend.dto.response.MunicipioEstatisticaResponseDTO;
import ac.gov.br.sigei_backend.dto.response.MunicipioResponseDTO;
import ac.gov.br.sigei_backend.service.MunicipioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/municipio")
@Tag(name = "Municípios", description = "Endpoints para gerenciamento de municípios")
public class MunicipioController {

    @Autowired
    MunicipioService municipioService;

    @PostMapping("/salvar")
    @Operation(summary = "Salvar município",
            description = "Cadastra um novo registro de município com os dados informados no corpo da requisição. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Operação realizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MunicipioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade ou registro duplicado", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity salvar(@Valid @RequestBody MunicipioRequestDTO municipioDTO){
        MunicipioResponseDTO municipioResponseDTO = municipioService.salvar(municipioDTO);
        return new ResponseEntity<MunicipioResponseDTO>(municipioResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping("")
    @Operation(summary = "Buscar municípios",
            description = "Retorna todos os registros de municípios. Retorna uma lista vazia quando não há cadastros. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<List<MunicipioResponseDTO>> buscar(){
        List<MunicipioResponseDTO> municipios = municipioService.buscarTodos();
        return new ResponseEntity<List<MunicipioResponseDTO>>(municipios, HttpStatus.OK);
    }

    @GetMapping("/estatisticas")
    @Operation(summary = "Buscar estatísticas dos municípios",
            description = "Retorna a quantidade de terras indígenas, aldeias e escolas por município. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<List<MunicipioEstatisticaResponseDTO>> buscarEstatisticas() {

        List<MunicipioEstatisticaResponseDTO> municipios =
                municipioService.buscarEstatisticas();

        return new ResponseEntity<>(
                municipios,
                HttpStatus.OK
        );
    }

    @PutMapping("/editar")
    @Operation(summary = "Editar município",
            description = "Atualiza os dados de município identificado pelo campo id do corpo da requisição. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Registro ou relacionamento informado não encontrado", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade na atualização", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<MunicipioResponseDTO> editar(@RequestBody MunicipioRequestDTO municipioDTO){
        MunicipioResponseDTO municipioResponseDTO = municipioService.editar(municipioDTO);
        return new ResponseEntity<MunicipioResponseDTO>(municipioResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/excluir/{id}")
    @Operation(summary = "Excluir município",
            description = "Remove o registro de município pelo ID. A resposta de sucesso não possui corpo. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso", content = @Content),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "O registro possui vínculos que impedem a exclusão", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<?> excluir(@Parameter(description = "ID do registro de município", example = "1") @PathVariable Long id){
        municipioService.excluir(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
