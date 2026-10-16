package ac.gov.br.sigei_backend.controllers.auth;

import ac.gov.br.sigei_backend.config.security.CookiesSessao;
import ac.gov.br.sigei_backend.dto.request.AlterarSenhaRequestDTO;
import ac.gov.br.sigei_backend.dto.request.LoginRequestDTO;
import ac.gov.br.sigei_backend.dto.response.LoginResponseDTO;
import ac.gov.br.sigei_backend.exceptions.TokenInvalidoException;
import ac.gov.br.sigei_backend.models.Usuario;
import ac.gov.br.sigei_backend.service.auth.AuthService;
import ac.gov.br.sigei_backend.service.auth.SessaoAutenticada;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints para autenticação e alteração de senha")
public class AuthController {

    @Autowired
    AuthService authService;

    @Autowired
    CookiesSessao cookiesSessao;

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário",
            description = "Valida e-mail e senha e retorna os dados do usuário. O token de acesso e o refresh token são gravados em cookies HttpOnly "
                    + "(sigei_acesso e sigei_renovacao), inacessíveis a scripts da página; os tokens não seguem no corpo da resposta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso; cookies de sessão gravados"),
            @ApiResponse(responseCode = "400", description = "Dados de autenticação inválidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos (mesma resposta para e-mail inexistente e senha incorreta)", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Senha correta, mas usuário desativado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dto,
            HttpServletResponse response) {

        SessaoAutenticada sessao = authService.login(dto);
        cookiesSessao.gravar(response, sessao.tokenAcesso(), sessao.refreshToken());

        return ResponseEntity.ok(sessao.usuario());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar sessão",
            description = "Usa o refresh token do cookie sigei_renovacao para gravar novos cookies de sessão e retorna os dados atualizados do usuário. "
                    + "O token apresentado é revogado (rotação); reapresentar um token usado há mais de 30 segundos encerra todas as sessões do usuário. "
                    + "Exige o cabeçalho X-XSRF-TOKEN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operação realizada com sucesso; cookies de sessão renovados"),
            @ApiResponse(responseCode = "401", description = "Cookie ausente, token inválido, expirado, já utilizado ou de usuário desativado; os cookies de sessão são removidos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Token CSRF ausente ou inválido", content = @Content)
    })
    public ResponseEntity<LoginResponseDTO> renovar(
            @Parameter(hidden = true) @CookieValue(name = CookiesSessao.COOKIE_RENOVACAO, required = false) String refreshToken,
            HttpServletResponse response) {
        try {
            SessaoAutenticada sessao = authService.renovar(refreshToken);
            cookiesSessao.gravar(response, sessao.tokenAcesso(), sessao.refreshToken());
            return ResponseEntity.ok(sessao.usuario());
        } catch (TokenInvalidoException erro) {
            // Os cookies removidos aqui seguem na resposta 401 montada pelo ManipuladorGlobalException
            cookiesSessao.limpar(response);
            throw erro;
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Encerrar sessão",
            description = "Revoga o refresh token do cookie sigei_renovacao e remove os cookies de sessão. Responde 204 mesmo sem sessão ativa. "
                    + "Exige o cabeçalho X-XSRF-TOKEN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Operação realizada com sucesso; cookies removidos", content = @Content),
            @ApiResponse(responseCode = "403", description = "Token CSRF ausente ou inválido", content = @Content)
    })
    public ResponseEntity<Void> logout(
            @Parameter(hidden = true) @CookieValue(name = CookiesSessao.COOKIE_RENOVACAO, required = false) String refreshToken,
            HttpServletResponse response) {
        authService.logout(refreshToken);
        cookiesSessao.limpar(response);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/senha")
    @Operation(summary = "Alterar senha",
            description = "Altera a senha do usuário autenticado após validar a senha atual e a confirmação da nova senha. Encerra todas as sessões do usuário e remove os cookies de sessão. Requer autenticação e o cabeçalho X-XSRF-TOKEN.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Operação realizada com sucesso", content = @Content),
            @ApiResponse(responseCode = "400", description = "Senha atual inválida, confirmação divergente ou nova senha igual à atual", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação ausente ou inválida", content = @Content)
    })
    public ResponseEntity<Void> alterarSenha(
            @Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody AlterarSenhaRequestDTO dto,
            HttpServletResponse response
    ) {
        authService.alterarSenha(usuario.getId(), dto);
        cookiesSessao.limpar(response);
        return ResponseEntity.noContent().build();
    }
}
