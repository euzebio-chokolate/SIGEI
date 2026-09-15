package ac.gov.br.sigei_backend.controllers.auth;

import ac.gov.br.sigei_backend.dto.request.AlterarSenhaRequestDTO;
import ac.gov.br.sigei_backend.dto.request.LoginRequestDTO;
import ac.gov.br.sigei_backend.dto.response.LoginResponseDTO;
import ac.gov.br.sigei_backend.models.Usuario;
import ac.gov.br.sigei_backend.service.auth.AuthService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints para autenticação e alteração de senha")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário",
            description = "Valida e-mail e senha e retorna os dados do usuário e o token JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de autenticação inválidos", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "E-mail não cadastrado", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Senha incorreta ou usuário desativado, conforme o tratamento atual da aplicação", content = @Content(schema = @Schema()))
    })
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dto) {

        LoginResponseDTO usuario =
                authService.login(dto);

        return new ResponseEntity<>(
                usuario,
                HttpStatus.OK
        );
    }

    @PutMapping("/alterar-senha")
    @Operation(summary = "Alterar senha",
            description = "Altera a senha do usuário autenticado após validar a senha atual e a confirmação da nova senha. Requer autenticação.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Operação realizada com sucesso", content = @Content),
            @ApiResponse(responseCode = "400", description = "Senha atual inválida, confirmação divergente ou nova senha igual à atual", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content)
    })
    public ResponseEntity<Void> alterarSenha(
            @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody AlterarSenhaRequestDTO dto
    ) {
        authService.alterarSenha(usuario.getId(), dto);
        return ResponseEntity.noContent().build();
    }
}
