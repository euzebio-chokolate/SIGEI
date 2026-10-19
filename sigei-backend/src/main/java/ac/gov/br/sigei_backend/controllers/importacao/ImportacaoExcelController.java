package ac.gov.br.sigei_backend.controllers.importacao;

import ac.gov.br.sigei_backend.dto.response.ImportacaoMatriculasResultadoDTO;
import ac.gov.br.sigei_backend.dto.response.ImportacaoProfessoresResultadoDTO;
import ac.gov.br.sigei_backend.dto.response.ImportacaoResultadoDTO;
import ac.gov.br.sigei_backend.dto.response.LayoutImportacaoDTO;
import ac.gov.br.sigei_backend.service.importacao.ImportacaoExcelService;
import ac.gov.br.sigei_backend.service.importacao.ImportacaoMatriculasService;
import ac.gov.br.sigei_backend.service.importacao.ImportacaoProfessoresService;
import ac.gov.br.sigei_backend.service.importacao.SimulacaoImportacao;
import ac.gov.br.sigei_backend.utils.LayoutPlanilha;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

@RestController
@RequestMapping("/importacoes")
@Tag(name = "Importações", description = "Endpoints para importação de planilhas de infraestrutura, matrículas e professores")
public class ImportacaoExcelController {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private static final String SIMULACAO =
            "true para pré-visualizar: processa a planilha e devolve o resultado, sem gravar nada";

    /** Layouts por tipo, na ordem exibida na tela de importação. */
    private static final Map<String, LayoutPlanilha<?>> LAYOUTS = new LinkedHashMap<>();

    static {
        LAYOUTS.put("infraestrutura", ImportacaoExcelService.LAYOUT);
        LAYOUTS.put("matriculas", ImportacaoMatriculasService.LAYOUT);
        LAYOUTS.put("professores", ImportacaoProfessoresService.LAYOUT);
    }

    @Autowired
    ImportacaoExcelService importacaoExcelService;

    @Autowired
    ImportacaoMatriculasService importacaoMatriculasService;

    @Autowired
    ImportacaoProfessoresService importacaoProfessoresService;

    @Autowired
    SimulacaoImportacao simulacaoImportacao;

    @GetMapping("/layouts")
    @Operation(summary = "Listar layouts de importação",
            description = "Retorna, para cada tipo de importação, as instruções e as colunas aceitas no cabeçalho, com os títulos alternativos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content)
    })
    public ResponseEntity<List<LayoutImportacaoDTO>> listarLayouts() {
        return ResponseEntity.ok(LAYOUTS.entrySet().stream()
                .map(layout -> layout.getValue().paraDTO(layout.getKey()))
                .toList());
    }

    @GetMapping("/modelos/{tipo}")
    @Operation(summary = "Baixar modelo de importação",
            description = "Gera uma planilha .xlsx com o cabeçalho do tipo informado (infraestrutura, matriculas ou professores) e uma aba de instruções.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Planilha modelo", content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "404", description = "Tipo de importação inexistente", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<byte[]> baixarModelo(@Parameter(description = "infraestrutura, matriculas ou professores", required = true) @PathVariable String tipo) {
        LayoutPlanilha<?> layout = LAYOUTS.get(tipo);

        if (layout == null) {
            throw new NoSuchElementException("Tipo de importação inexistente: " + tipo);
        }

        return ResponseEntity.ok()
                .contentType(XLSX)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("modelo-importacao-" + tipo + ".xlsx")
                        .build()
                        .toString())
                .body(layout.gerarModelo());
    }

    @PostMapping(value = "/professores", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar quadro de professores",
            description = "Importa o quadro de escolas e professores indígenas para o ano informado, que precisa estar iniciado (sem ano, o mais recente). As colunas são localizadas pelo cabeçalho, em qualquer aba (GET /importacoes/layouts). As escolas são identificadas pelo município, nome e aldeia. Pode ser repetida: células vazias não apagam dados já gravados, como CPF, matrícula e número do contrato. Retorna contadores e ocorrências do processamento. Com simulacao=true, só pré-visualiza: devolve o resultado que a gravação teria, sem gravar nada. Requer perfil ADMIN.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Planilha Excel do quadro de professores", required = true,
            content = @Content(mediaType = "multipart/form-data",
                    schema = @Schema(type = "object", requiredProperties = {"arquivo"}),
                    schemaProperties = @SchemaProperty(name = "arquivo",
                            schema = @Schema(type = "string", format = "binary"))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Arquivo vazio, formato inválido, colunas obrigatórias ausentes ou ano não iniciado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<ImportacaoProfessoresResultadoDTO> importarProfessores(
            @Parameter(description = "Planilha Excel do quadro de professores", required = true) @RequestParam("arquivo") MultipartFile arquivo,
            @Parameter(description = "Ano das lotações, por exemplo 2026; sem ele, o mais recente") @RequestParam(required = false) Integer ano,
            @Parameter(description = SIMULACAO) @RequestParam(defaultValue = "false") boolean simulacao) {
        return ResponseEntity.ok(executar(simulacao, () -> importacaoProfessoresService.importar(arquivo, ano)));
    }

    @PostMapping(value = "/infraestrutura", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar escolas e infraestrutura",
            description = "Importa escolas, infraestrutura e equipe para o ano informado, criado se ainda não existir (sem ano, o mais recente). As colunas são localizadas pelo cabeçalho, em qualquer aba (GET /importacoes/layouts). Retorna contadores e ocorrências do processamento. Com simulacao=true, só pré-visualiza: devolve o resultado que a gravação teria, sem gravar nada. Requer perfil ADMIN.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Planilha Excel de escolas e infraestrutura", required = true,
            content = @Content(mediaType = "multipart/form-data",
                    schema = @Schema(type = "object", requiredProperties = {"arquivo"}),
                    schemaProperties = @SchemaProperty(name = "arquivo",
                            schema = @Schema(type = "string", format = "binary"))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Arquivo vazio, formato inválido, colunas obrigatórias ausentes, ano inválido ou falha na leitura", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<ImportacaoResultadoDTO> importarInfraestrutura(
            @Parameter(description = "Planilha Excel de escolas e infraestrutura", required = true) @RequestParam("arquivo") MultipartFile arquivo,
            @Parameter(description = "Ano de referência dos dados, por exemplo 2025; sem ele, o mais recente") @RequestParam(required = false) Integer ano,
            @Parameter(description = SIMULACAO) @RequestParam(defaultValue = "false") boolean simulacao) {
        ImportacaoResultadoDTO resultado =
                executar(simulacao, () -> importacaoExcelService.importarInfraestrutura(arquivo, ano));
        return ResponseEntity.ok(resultado);
    }

    @PostMapping(value = "/matriculas", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar matrículas",
            description = "Importa as matrículas por etapa para os registros anuais do ano informado, que precisa estar cadastrado (sem ano, o mais recente). As colunas são localizadas pelo cabeçalho, de uma ou duas linhas, em qualquer aba (GET /importacoes/layouts). Retorna contadores e ocorrências do processamento. Com simulacao=true, só pré-visualiza: devolve o resultado que a gravação teria, sem gravar nada. Requer perfil ADMIN.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Planilha Excel de matrículas", required = true,
            content = @Content(mediaType = "multipart/form-data",
                    schema = @Schema(type = "object", requiredProperties = {"arquivo"}),
                    schemaProperties = @SchemaProperty(name = "arquivo",
                            schema = @Schema(type = "string", format = "binary"))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Arquivo vazio, formato inválido, colunas obrigatórias ausentes, ano não cadastrado ou falha na leitura", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<ImportacaoMatriculasResultadoDTO> importarMatriculas(
            @Parameter(description = "Planilha Excel de matrículas", required = true) @RequestParam("arquivo") MultipartFile arquivo,
            @Parameter(description = "Ano dos registros anuais, por exemplo 2025; sem ele, o mais recente") @RequestParam(required = false) Integer ano,
            @Parameter(description = SIMULACAO) @RequestParam(defaultValue = "false") boolean simulacao) {
        ImportacaoMatriculasResultadoDTO resultado =
                executar(simulacao, () -> importacaoMatriculasService.importar(arquivo, ano));
        return ResponseEntity.ok(resultado);
    }

    /** Com simulação, a importação roda numa transação desfeita ao final (pré-visualização). */
    private <T> T executar(boolean simulacao, Supplier<T> importacao) {
        return simulacao ? simulacaoImportacao.simular(importacao) : importacao.get();
    }
}
