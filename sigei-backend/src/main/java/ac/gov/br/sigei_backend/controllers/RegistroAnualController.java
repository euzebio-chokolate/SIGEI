package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.dto.request.RegistroAnualRequestDTO;
import ac.gov.br.sigei_backend.dto.response.RegistroAnualResponseDTO;
import ac.gov.br.sigei_backend.service.RegistroAnualService;
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
@RequestMapping("/registroAnual")
@Tag(name = "Registros Anuais", description = "Endpoints para gerenciamento de registros anuais")
public class RegistroAnualController {
    @Autowired
    RegistroAnualService registroAnualService;

    @PostMapping("/salvar")
    @Operation(summary = "Salvar registro anual",
            description = "Cadastra um novo registro de registro anual com os dados informados no corpo da requisição. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Operação realizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegistroAnualResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Registro relacionado não encontrado", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade ou registro duplicado", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity salvar(@Valid @RequestBody RegistroAnualRequestDTO registroDTO){
        RegistroAnualResponseDTO registroAnualResponseDTO = registroAnualService.salvar(registroDTO);
        return new ResponseEntity<RegistroAnualResponseDTO>(registroAnualResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping("")
    @Operation(summary = "Buscar registros anuais",
            description = "Retorna todos os registros de registros anuais. Retorna uma lista vazia quando não há cadastros. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<List<RegistroAnualResponseDTO>> buscarTudo(){
        List<RegistroAnualResponseDTO> registroDTOS = registroAnualService.buscarTudo();
        return new ResponseEntity<List<RegistroAnualResponseDTO>>(registroDTOS, HttpStatus.OK);
    }

    @GetMapping("/buscarPorEscolaEAno")
    @Operation(summary = "Buscar registro anual por escola e ano",
            description = "Retorna o registro anual correspondente ao ID da escola e ao ano informado. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID da escola ou ano ausente ou em formato inválido", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Escola ou registro anual não encontrado", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<RegistroAnualResponseDTO> buscarPorEscolaEAno(@Parameter(description = "ID da escola", example = "1") @RequestParam Long escolaId, @Parameter(description = "Ano de referência (valor do ano, não o ID)", example = "2025") @RequestParam Integer ano) {
        RegistroAnualResponseDTO registro = registroAnualService.buscarPorEscolaEAno(escolaId, ano);
        return new ResponseEntity<>(registro, HttpStatus.OK);
    }

    @GetMapping("/historico")
    @Operation(summary = "Consultar histórico da escola",
            description = "Retorna os registros da escola em ordem crescente de ano, incluindo total de alunos, "
                    + "matrículas, equipe e infraestrutura. Os limites do período são opcionais e inclusivos. "
                    + "Anos sem registro não são incluídos. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico encontrado; lista vazia quando não há registros no período"),
            @ApiResponse(responseCode = "400", description = "ID ou período inválido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Escola não encontrada", content = @Content),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão", content = @Content)
    })
    public ResponseEntity<List<RegistroAnualResponseDTO>> buscarHistorico(
            @Parameter(description = "ID da escola", example = "1") @RequestParam Long escolaId,
            @Parameter(description = "Primeiro ano do período", example = "1990") @RequestParam(required = false) Integer anoInicial,
            @Parameter(description = "Último ano do período", example = "2026") @RequestParam(required = false) Integer anoFinal) {
        return ResponseEntity.ok(registroAnualService.buscarHistorico(escolaId, anoInicial, anoFinal));
    }

    @GetMapping("/buscarPorAno")
    @Operation(summary = "Buscar registros anuais por ano",
            description = "Retorna os registros de registros anuais do ano informado. Retorna uma lista vazia quando não há resultados. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Ano ausente ou em formato inválido", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<List<RegistroAnualResponseDTO>> buscarPorAno(@Parameter(description = "Ano de referência (valor do ano, não o ID)", example = "2025") @RequestParam Integer ano) {
        List<RegistroAnualResponseDTO> registros = registroAnualService.buscarPorAno(ano);
        return new ResponseEntity<>(registros, HttpStatus.OK
        );
    }

    @PutMapping("/editar")
    @Operation(summary = "Editar registro anual",
            description = "Atualiza os dados de registro anual identificado pelo campo id do corpo da requisição. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Operação realizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegistroAnualResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Registro ou relacionamento informado não encontrado", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade na atualização", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity editar(@Valid @RequestBody RegistroAnualRequestDTO registroDTO){
        RegistroAnualResponseDTO registroAnualResponseDTO = registroAnualService.editar(registroDTO);
        return new ResponseEntity<RegistroAnualResponseDTO>(registroAnualResponseDTO, HttpStatus.CREATED);
    }

    @DeleteMapping("/excluir/{id}")
    @Operation(summary = "Excluir registro anual",
            description = "Remove o registro de registro anual pelo ID. A resposta de sucesso não possui corpo. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso", content = @Content),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "409", description = "O registro possui vínculos que impedem a exclusão", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<?> excluir(@Parameter(description = "ID do registro de registro anual", example = "1") @PathVariable Long id){
        registroAnualService.excluir(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
