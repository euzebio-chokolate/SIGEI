package ac.gov.br.sigei_backend.controllers.importacao;

import ac.gov.br.sigei_backend.dto.response.ImportacaoMatriculasResultadoDTO;
import ac.gov.br.sigei_backend.dto.response.ImportacaoResultadoDTO;
import ac.gov.br.sigei_backend.service.importacao.ImportacaoExcelService;
import ac.gov.br.sigei_backend.service.importacao.ImportacaoMatriculasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/importacoes")
@Tag(name = "Importações", description = "Endpoints para importação de planilhas de infraestrutura e matrículas")
public class ImportacaoExcelController {

    @Autowired
    ImportacaoExcelService importacaoExcelService;

    @Autowired
    ImportacaoMatriculasService importacaoMatriculasService;

    @PostMapping(value = "/infraestrutura", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar infraestrutura escolar",
            description = "Importa a aba Planilha4 de uma planilha Excel no layout esperado pela aplicação, para o ano de referência 2025. Retorna contadores e ocorrências do processamento. Requer perfil ADMIN.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Planilha Excel no layout esperado pela importa??o", required = true,
            content = @Content(mediaType = "multipart/form-data",
                    schema = @Schema(type = "object", requiredProperties = {"arquivo"}),
                    schemaProperties = @SchemaProperty(name = "arquivo",
                            schema = @Schema(type = "string", format = "binary"))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Arquivo vazio, formato inválido, aba Planilha4 ausente ou falha na leitura", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<ImportacaoResultadoDTO> importarInfraestrutura(@Parameter(description = "Planilha Excel no layout esperado pela importação", required = true) @RequestParam("arquivo") MultipartFile arquivo) {
        ImportacaoResultadoDTO resultado = importacaoExcelService.importarInfraestrutura(arquivo);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping(value = "/matriculas", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importar matrículas",
            description = "Importa a aba Censo_2025 de uma planilha Excel no layout esperado pela aplicação, vinculando as matrículas aos registros de 2025. Retorna contadores e ocorrências do processamento. Requer perfil ADMIN.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Planilha Excel no layout esperado pela importa??o", required = true,
            content = @Content(mediaType = "multipart/form-data",
                    schema = @Schema(type = "object", requiredProperties = {"arquivo"}),
                    schemaProperties = @SchemaProperty(name = "arquivo",
                            schema = @Schema(type = "string", format = "binary"))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Arquivo vazio, formato inválido, aba Censo_2025 ausente ou falha na leitura", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<ImportacaoMatriculasResultadoDTO> importarMatriculas(@Parameter(description = "Planilha Excel no layout esperado pela importação", required = true) @RequestParam("arquivo") MultipartFile arquivo) {
        ImportacaoMatriculasResultadoDTO resultado = importacaoMatriculasService.importar(arquivo);
        return ResponseEntity.ok(resultado);
    }
}