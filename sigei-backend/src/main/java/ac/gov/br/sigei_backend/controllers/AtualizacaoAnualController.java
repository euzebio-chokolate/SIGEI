package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.service.AtualizacaoAnualService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ProblemDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/atualizacao-anual")
@Tag(name = "Atualização Anual", description = "Endpoints para abertura de um novo ano de referência")
public class AtualizacaoAnualController {

    @Autowired
    AtualizacaoAnualService atualizacaoAnualService;

    @PostMapping("/iniciar")
    @Operation(summary = "Iniciar novo ano de referência",
            description = "Cria o ano seguinte ao maior ano cadastrado e copia os últimos registros das escolas, incluindo equipe, infraestrutura e matrículas. Escolas sem registro anterior são ignoradas. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "O novo ano já foi iniciado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Nenhum ano de referência cadastrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de integridade ao criar o novo ano", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuário sem permissão para esta operação", content = @Content)
    })
    public ResponseEntity<String> iniciarNovoAno() {

        atualizacaoAnualService.iniciarNovoAno();

        return new ResponseEntity<>(
                "Novo ano iniciado com sucesso",
                HttpStatus.CREATED
        );
    }
}