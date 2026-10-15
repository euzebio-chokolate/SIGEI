package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.dto.request.CadastroEscolaRequestDTO;
import ac.gov.br.sigei_backend.dto.request.EscolaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.CadastroEscolaResponseDTO;
import ac.gov.br.sigei_backend.dto.response.EscolaResponseDTO;
import ac.gov.br.sigei_backend.dto.response.detalhe.EscolaDetalheResponseDTO;
import ac.gov.br.sigei_backend.dto.response.historico.AlteracaoHistoricoDTO;
import ac.gov.br.sigei_backend.service.CadastroEscolaService;
import ac.gov.br.sigei_backend.service.EscolaService;
import ac.gov.br.sigei_backend.service.HistoricoEscolaService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/escola")
@Tag(name = "Escolas", description = "Endpoints para gerenciamento de escolas")
public class EscolaController {
    @Autowired
    EscolaService escolaService;

    @Autowired
    CadastroEscolaService cadastroEscolaService;

    @Autowired
    HistoricoEscolaService historicoEscolaService;

    @PostMapping("/salvar")
    @Operation(summary = "Salvar escola",
            description = "Cadastra um novo registro de escola com os dados informados no corpo da requisição. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Operação realizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EscolaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro relacionado não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade ou registro duplicado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity salvar(@Valid @RequestBody EscolaRequestDTO escolaDTO){
        EscolaResponseDTO escolaResponseDTO = escolaService.salvar(escolaDTO);
        return new ResponseEntity<EscolaResponseDTO>(escolaResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping("")
    @Operation(summary = "Buscar escolas",
            description = "Retorna todos os registros de escolas. Retorna uma lista vazia quando não há cadastros. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<List<EscolaResponseDTO>> buscarTudo(){
        List<EscolaResponseDTO> escolas = escolaService.buscarTudo();
        return new ResponseEntity<List<EscolaResponseDTO>>(escolas, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar escola por ID",
            description = "Retorna os detalhes de escola correspondente ao ID informado. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<EscolaDetalheResponseDTO> buscarPorId(@Parameter(description = "ID do registro de escola", example = "1") @PathVariable Long id) {
        EscolaDetalheResponseDTO escola = escolaService.buscarPorId(id);
        return ResponseEntity.ok(escola);
    }

    @GetMapping("/{id}/historico")
    @Operation(summary = "Buscar trilha de auditoria da escola",
            description = "Retorna as alterações da escola, de seus registros anuais e, em cada registro, de equipe, infraestrutura e matrículas, "
                    + "da mais recente à mais antiga, com data e hora, autor (nulo quando a alteração foi feita pelo sistema), operação e campos alterados. "
                    + "Escolas excluídas mantêm o histórico. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AlteracaoHistoricoDTO.class)))),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Escola sem histórico de auditoria", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<List<AlteracaoHistoricoDTO>> buscarHistorico(@Parameter(description = "ID da escola", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(historicoEscolaService.buscarPorEscola(id));
    }

    @PutMapping("/editar")
    @Operation(summary = "Editar escola",
            description = "Atualiza os dados de escola identificado pelo campo id do corpo da requisição. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro ou relacionamento informado não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade na atualização", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<EscolaResponseDTO> editar(@Valid @RequestBody EscolaRequestDTO escolaDTO){
        EscolaResponseDTO escolaResponseDTO = escolaService.editar(escolaDTO);
        return new ResponseEntity<EscolaResponseDTO>(escolaResponseDTO, HttpStatus.OK);
    }

    @PostMapping(value = "/salvarCompleto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Salvar cadastro completo de escola",
            description = "Cadastra, em uma única transação, a escola e o registro do ano informado com equipe, infraestrutura, matrículas e foto opcional. "
                    + "Envie multipart/form-data com a parte dados (JSON) e, opcionalmente, a parte foto. Se qualquer etapa falhar, nada é gravado. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Operação realizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CadastroEscolaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro relacionado não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade ou registro duplicado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "503", description = "Foto enviada sem armazenamento de imagens configurado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<CadastroEscolaResponseDTO> salvarCompleto(
            @Valid @RequestPart("dados") CadastroEscolaRequestDTO dados,
            @Parameter(description = "Foto da infraestrutura (opcional)") @RequestPart(value = "foto", required = false) MultipartFile foto
    ) throws IOException {
        CadastroEscolaResponseDTO cadastro = cadastroEscolaService.salvar(dados, foto);
        return new ResponseEntity<>(cadastro, HttpStatus.CREATED);
    }

    @PutMapping(value = "/editarCompleto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Editar cadastro completo de escola",
            description = "Atualiza, em uma única transação, a escola identificada por dados.escola.id e cria ou atualiza o registro do ano informado com equipe, infraestrutura, matrículas e foto opcional. "
                    + "Registros de outros anos não são alterados. Se qualquer etapa falhar, nada é gravado. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CadastroEscolaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Escola ou relacionamento informado não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade na atualização", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "503", description = "Foto enviada sem armazenamento de imagens configurado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<CadastroEscolaResponseDTO> editarCompleto(
            @Valid @RequestPart("dados") CadastroEscolaRequestDTO dados,
            @Parameter(description = "Foto da infraestrutura (opcional)") @RequestPart(value = "foto", required = false) MultipartFile foto
    ) throws IOException {
        CadastroEscolaResponseDTO cadastro = cadastroEscolaService.editar(dados, foto);
        return ResponseEntity.ok(cadastro);
    }

    @DeleteMapping("/excluir/{id}")
    @Operation(summary = "Excluir escola",
            description = "Remove o registro de escola pelo ID. A resposta de sucesso não possui corpo. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso", content = @Content),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "O registro possui vínculos que impedem a exclusão", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<?> excluir(@Parameter(description = "ID do registro de escola", example = "1") @PathVariable Long id){
        escolaService.excluir(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
