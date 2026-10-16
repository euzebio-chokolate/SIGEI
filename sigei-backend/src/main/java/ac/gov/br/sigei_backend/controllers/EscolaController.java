package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.dto.request.CadastroEscolaRequestDTO;
import ac.gov.br.sigei_backend.dto.request.EscolaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.CadastroEscolaResponseDTO;
import ac.gov.br.sigei_backend.dto.response.EscolaResponseDTO;
import ac.gov.br.sigei_backend.dto.response.RegistroAnualResponseDTO;
import ac.gov.br.sigei_backend.dto.response.detalhe.EscolaDetalheResponseDTO;
import ac.gov.br.sigei_backend.dto.response.historico.AlteracaoHistoricoDTO;
import ac.gov.br.sigei_backend.service.CadastroEscolaService;
import ac.gov.br.sigei_backend.service.EscolaService;
import ac.gov.br.sigei_backend.service.HistoricoEscolaService;
import ac.gov.br.sigei_backend.service.RegistroAnualService;
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
@RequestMapping("/escolas")
@Tag(name = "Escolas", description = "Endpoints para gerenciamento de escolas")
public class EscolaController {
    @Autowired
    EscolaService escolaService;

    @Autowired
    CadastroEscolaService cadastroEscolaService;

    @Autowired
    HistoricoEscolaService historicoEscolaService;

    @Autowired
    RegistroAnualService registroAnualService;

    @PostMapping
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
    public ResponseEntity<EscolaResponseDTO> salvar(@Valid @RequestBody EscolaRequestDTO escolaDTO){
        EscolaResponseDTO escolaResponseDTO = escolaService.salvar(escolaDTO);
        return new ResponseEntity<>(escolaResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Buscar escolas",
            description = "Retorna todos os registros de escolas. Retorna uma lista vazia quando não há cadastros. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<List<EscolaResponseDTO>> buscarTudo(){
        List<EscolaResponseDTO> escolas = escolaService.buscarTudo();
        return new ResponseEntity<>(escolas, HttpStatus.OK);
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

    @GetMapping("/{id}/registros-anuais")
    @Operation(summary = "Buscar registros anuais da escola",
            description = "Retorna os registros da escola em ordem crescente de ano, incluindo total de alunos, "
                    + "matrículas, equipe e infraestrutura. Os limites do período são opcionais e inclusivos. "
                    + "Anos sem registro não são incluídos. Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registros encontrados; lista vazia quando não há registros no período"),
            @ApiResponse(responseCode = "400", description = "ID ou período inválido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Escola não encontrada", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<List<RegistroAnualResponseDTO>> buscarRegistrosAnuais(
            @Parameter(description = "ID da escola", example = "1") @PathVariable Long id,
            @Parameter(description = "Primeiro ano do período", example = "1990") @RequestParam(required = false) Integer anoInicial,
            @Parameter(description = "Último ano do período", example = "2026") @RequestParam(required = false) Integer anoFinal) {
        return ResponseEntity.ok(registroAnualService.buscarHistorico(id, anoInicial, anoFinal));
    }

    @GetMapping("/{id}/registros-anuais/{ano}")
    @Operation(summary = "Buscar registro anual da escola em um ano",
            description = "Retorna o registro anual da escola no ano informado (valor do ano, como 2025, e não o ID do ano). Requer perfil ADMIN ou USUARIO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consulta realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID da escola ou ano em formato inválido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Escola ou registro anual não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<RegistroAnualResponseDTO> buscarRegistroAnual(
            @Parameter(description = "ID da escola", example = "1") @PathVariable Long id,
            @Parameter(description = "Ano de referência (valor do ano, não o ID)", example = "2025") @PathVariable Integer ano) {
        return ResponseEntity.ok(registroAnualService.buscarPorEscolaEAno(id, ano));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar escola",
            description = "Atualiza os dados de escola identificado pelo ID informado no caminho. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro ou relacionamento informado não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade na atualização", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<EscolaResponseDTO> editar(@Parameter(description = "ID de escola", example = "1") @PathVariable Long id, @Valid @RequestBody EscolaRequestDTO escolaDTO){
        EscolaResponseDTO escolaResponseDTO = escolaService.editar(id, escolaDTO);
        return new ResponseEntity<>(escolaResponseDTO, HttpStatus.OK);
    }

    @PostMapping(value = "/cadastro-completo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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

    @PutMapping(value = "/{id}/cadastro-completo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Editar cadastro completo de escola",
            description = "Atualiza, em uma única transação, a escola identificada pelo ID do caminho e cria ou atualiza o registro do ano informado com equipe, infraestrutura, matrículas e foto opcional. "
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
            @Parameter(description = "ID da escola", example = "1") @PathVariable Long id,
            @Valid @RequestPart("dados") CadastroEscolaRequestDTO dados,
            @Parameter(description = "Foto da infraestrutura (opcional)") @RequestPart(value = "foto", required = false) MultipartFile foto
    ) throws IOException {
        CadastroEscolaResponseDTO cadastro = cadastroEscolaService.editar(id, dados, foto);
        return ResponseEntity.ok(cadastro);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir escola",
            description = "Remove o registro de escola pelo ID. A resposta de sucesso não possui corpo. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registro excluído", content = @Content),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "O registro possui vínculos que impedem a exclusão", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<Void> excluir(@Parameter(description = "ID do registro de escola", example = "1") @PathVariable Long id){
        escolaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

}
