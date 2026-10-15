package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.dto.request.EquipeEscolaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.EquipeEscolaResponseDTO;
import ac.gov.br.sigei_backend.service.EquipeEscolaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/equipe")
@Tag(name = "Equipes Escolares", description = "Endpoints para gerenciamento de equipes escolares")
public class EquipeEscolaController {

    @Autowired
    EquipeEscolaService equipeEscolaService;

    @PostMapping("/salvar")
    @Operation(summary = "Salvar equipe escolar",
            description = "Cadastra um novo registro de equipe escolar com os dados informados no corpo da requisição. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Operação realizada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = EquipeEscolaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro relacionado não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade ou registro duplicado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity salvar(@Valid @RequestBody EquipeEscolaRequestDTO equipeDTO){
        EquipeEscolaResponseDTO equipeEscolaResponseDTO = equipeEscolaService.salvar(equipeDTO);
        return new ResponseEntity<EquipeEscolaResponseDTO>(equipeEscolaResponseDTO, HttpStatus.CREATED);
    }

    @PutMapping("/editar")
    @Operation(summary = "Editar equipe escolar",
            description = "Atualiza os dados de equipe escolar identificado pelo campo id do corpo da requisição. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Registro ou relacionamento informado não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade na atualização", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<EquipeEscolaResponseDTO> editar(@Valid @RequestBody EquipeEscolaRequestDTO equipeDTO){
        EquipeEscolaResponseDTO equipeEscolaResponseDTO = equipeEscolaService.editar(equipeDTO);
        return new ResponseEntity<EquipeEscolaResponseDTO>(equipeEscolaResponseDTO, HttpStatus.OK);
    }

}
