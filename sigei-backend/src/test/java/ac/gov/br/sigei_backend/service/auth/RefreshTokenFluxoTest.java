package ac.gov.br.sigei_backend.service.auth;

import ac.gov.br.sigei_backend.config.security.PasswordConfig;
import ac.gov.br.sigei_backend.dto.request.AlterarSenhaRequestDTO;
import ac.gov.br.sigei_backend.dto.request.LoginRequestDTO;
import ac.gov.br.sigei_backend.exceptions.TokenInvalidoException;
import ac.gov.br.sigei_backend.models.RefreshToken;
import ac.gov.br.sigei_backend.models.Usuario;
import ac.gov.br.sigei_backend.models.enums.PerfilUsuario;
import ac.gov.br.sigei_backend.repository.RefreshTokenRepository;
import ac.gov.br.sigei_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Executa o fluxo de sessão com transações reais, para verificar que as
 * revogações ficam gravadas mesmo quando a renovação é rejeitada.
 */
@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import({AuthService.class, JwtService.class, RefreshTokenService.class, PasswordConfig.class})
class RefreshTokenFluxoTest {

    private static final String SENHA = "Senha@123";

    @Autowired AuthService authService;
    @Autowired JwtService jwtService;
    @Autowired UsuarioRepository usuarios;
    @Autowired RefreshTokenRepository refreshTokens;
    @Autowired PasswordEncoder passwordEncoder;

    private Usuario usuario;

    @BeforeEach
    void preparar() {
        usuario = new Usuario();
        usuario.setNome("Gestor");
        usuario.setEmail("gestor@sigei.com");
        usuario.setSenha(passwordEncoder.encode(SENHA));
        usuario.setPerfil(PerfilUsuario.ADMIN);
        usuario.setAtivo(true);
        usuario.setAlterarSenha(false);
        usuario = usuarios.save(usuario);
    }

    @AfterEach
    void limpar() {
        refreshTokens.deleteAll();
        usuarios.deleteAll();
    }

    @Test
    void loginDeveEmitirRefreshTokenGuardandoApenasOHash() {
        SessaoAutenticada sessao = login();

        assertNotNull(sessao.refreshToken());
        assertTrue(jwtService.tokenValido(sessao.tokenAcesso()));
        RefreshToken salvo = refreshTokens.findAll().getFirst();
        assertNotEquals(sessao.refreshToken(), salvo.getTokenHash());
        assertEquals(RefreshTokenService.hash(sessao.refreshToken()), salvo.getTokenHash());
    }

    @Test
    void renovarDeveEmitirNovoParERevogarOTokenUsado() {
        SessaoAutenticada sessao = login();

        SessaoAutenticada renovada = renovar(sessao.refreshToken());

        assertNotEquals(sessao.refreshToken(), renovada.refreshToken());
        assertTrue(jwtService.tokenValido(renovada.tokenAcesso()));
        assertEquals(usuario.getEmail(), jwtService.extrairEmail(renovada.tokenAcesso()));
        assertDoesNotThrow(() -> renovar(renovada.refreshToken()));
    }

    @Test
    void reutilizarTokenLogoAposRotacaoDeveSerRecusadoSemEncerrarAsSessoes() {
        // Duas abas renovando ao mesmo tempo com o mesmo cookie
        SessaoAutenticada sessao = login();
        SessaoAutenticada renovada = renovar(sessao.refreshToken());

        assertThrows(TokenInvalidoException.class, () -> renovar(sessao.refreshToken()));

        assertDoesNotThrow(() -> renovar(renovada.refreshToken()));
    }

    @Test
    void reutilizarTokenAposAToleranciaDeveEncerrarTodasAsSessoes() {
        SessaoAutenticada sessao = login();
        SessaoAutenticada renovada = renovar(sessao.refreshToken());
        RefreshToken usado = refreshTokens.findByTokenHash(RefreshTokenService.hash(sessao.refreshToken())).orElseThrow();
        usado.setRevogadoEm(Instant.now().minus(RefreshTokenService.TOLERANCIA_CONCORRENCIA).minusSeconds(1));
        refreshTokens.save(usado);

        assertThrows(TokenInvalidoException.class, () -> renovar(sessao.refreshToken()));

        // O token legítimo emitido na rotação também foi revogado
        assertThrows(TokenInvalidoException.class, () -> renovar(renovada.refreshToken()));
    }

    @Test
    void deveRejeitarRenovacaoSemToken() {
        assertThrows(TokenInvalidoException.class, () -> renovar(null));
        assertThrows(TokenInvalidoException.class, () -> renovar(" "));
    }

    @Test
    void deveRejeitarTokenExpirado() {
        SessaoAutenticada sessao = login();
        RefreshToken salvo = refreshTokens.findAll().getFirst();
        salvo.setExpiraEm(Instant.now().minusSeconds(1));
        refreshTokens.save(salvo);

        assertThrows(TokenInvalidoException.class, () -> renovar(sessao.refreshToken()));
    }

    @Test
    void deveRejeitarTokenDesconhecido() {
        assertThrows(TokenInvalidoException.class, () -> renovar("token-inexistente"));
    }

    @Test
    void deveRejeitarRenovacaoDeUsuarioDesativado() {
        SessaoAutenticada sessao = login();
        usuario.setAtivo(false);
        usuarios.save(usuario);

        assertThrows(TokenInvalidoException.class, () -> renovar(sessao.refreshToken()));
    }

    @Test
    void logoutDeveImpedirNovaRenovacao() {
        SessaoAutenticada sessao = login();

        authService.logout(sessao.refreshToken());

        assertThrows(TokenInvalidoException.class, () -> renovar(sessao.refreshToken()));
    }

    @Test
    void alterarSenhaDeveRevogarTodasAsSessoes() {
        SessaoAutenticada primeira = login();
        SessaoAutenticada segunda = login();

        authService.alterarSenha(usuario.getId(), new AlterarSenhaRequestDTO(SENHA, "NovaSenha@456", "NovaSenha@456"));

        assertThrows(TokenInvalidoException.class, () -> renovar(primeira.refreshToken()));
        assertThrows(TokenInvalidoException.class, () -> renovar(segunda.refreshToken()));
    }

    @Test
    void excluirUsuarioDeveRemoverSeusTokens() {
        login();

        usuarios.deleteById(usuario.getId());

        assertEquals(0, refreshTokens.count());
    }

    private SessaoAutenticada login() {
        return authService.login(new LoginRequestDTO(usuario.getEmail(), SENHA));
    }

    private SessaoAutenticada renovar(String refreshToken) {
        return authService.renovar(refreshToken);
    }
}
