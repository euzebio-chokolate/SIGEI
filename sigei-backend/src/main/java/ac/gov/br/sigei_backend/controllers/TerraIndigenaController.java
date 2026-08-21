package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.DTOs.request.TerraIndigenaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.TerraIndigenaEstatisticaResponseDTO;
import ac.gov.br.sigei_backend.DTOs.response.TerraIndigenaResponseDTO;
import ac.gov.br.sigei_backend.services.TerraIndigenaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/terra")
@Tag(name = "Terras Indígenas", description = "Endpoints para gerenciamento de terras indígenas")
public class TerraIndigenaController {

    @Autowired
    TerraIndigenaService terraIndigenaService;

    @PostMapping("/salvar")
    @Operation(
            summary = "Salvar terra indígena", description = "Cadastra uma nova terra indígena")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Terra indígena cadastrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ResponseEntity<TerraIndigenaResponseDTO> salvar(@Valid @RequestBody TerraIndigenaRequestDTO terraIndigenaDTO) {
        TerraIndigenaResponseDTO terraIndigenaResponseDTO = terraIndigenaService.salvar(terraIndigenaDTO);
        return new ResponseEntity<>(terraIndigenaResponseDTO, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Buscar terras indígenas", description = "Retorna todas as terras indígenas cadastradas")
    @ApiResponse(
            responseCode = "200",
            description = "Lista de terras indígenas retornada com sucesso")
    public ResponseEntity<List<TerraIndigenaResponseDTO>> buscar() {
        List<TerraIndigenaResponseDTO> terrasIndigenas = terraIndigenaService.buscarTudo();
        return new ResponseEntity<>(terrasIndigenas, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar terra indígena por ID", description = "Retorna os detalhes de uma terra indígena específica através do seu ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Terra indígena encontrada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Terra indígena não encontrada")
    })
    public ResponseEntity<TerraIndigenaResponseDTO> buscarPorId(@Parameter(description = "ID da terra indígena", example = "1") @PathVariable Long id) {
        TerraIndigenaResponseDTO terraIndigenaResponseDTO = terraIndigenaService.buscarPorId(id);
        return new ResponseEntity<>(terraIndigenaResponseDTO, HttpStatus.OK);
    }


    @GetMapping("/estatisticas")
    public ResponseEntity<List<TerraIndigenaEstatisticaResponseDTO>> buscarEstatisticas() {

        List<TerraIndigenaEstatisticaResponseDTO> terras =
                terraIndigenaService.buscarEstatisticas();

        return new ResponseEntity<>(
                terras,
                HttpStatus.OK
        );
    }

    @PutMapping("/editar")
    @Operation(summary = "Editar terra indígena", description = "Atualiza os dados de uma terra indígena")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Terra indígena atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Terra indígena não encontrada")})

    public ResponseEntity<TerraIndigenaResponseDTO> editar(@RequestBody TerraIndigenaRequestDTO terraIndigenaDTO) {
        TerraIndigenaResponseDTO terraIndigenaResponseDTO = terraIndigenaService.editar(terraIndigenaDTO);
        return new ResponseEntity<>(terraIndigenaResponseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/excluir/{id}")
    @Operation(summary = "Excluir terra indígena", description = "Remove uma terra indígena pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Terra indígena excluída com sucesso"),
            @ApiResponse(responseCode = "404", description = "Terra indígena não encontrada")
    })
    public ResponseEntity<Void> excluir(@Parameter(description = "ID da terra indígena", example = "1") @PathVariable Long id) {
        terraIndigenaService.excluir(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
