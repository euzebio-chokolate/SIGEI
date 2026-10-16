package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.dto.request.LoginRequestDTO;
import ac.gov.br.sigei_backend.dto.request.UsuarioRequestDTO;
import ac.gov.br.sigei_backend.dto.response.UsuarioResponseDTO;
import ac.gov.br.sigei_backend.models.Usuario;
import ac.gov.br.sigei_backend.models.enums.PerfilUsuario;
import ac.gov.br.sigei_backend.repository.RefreshTokenRepository;
import ac.gov.br.sigei_backend.repository.UsuarioRepository;
import ac.gov.br.sigei_backend.service.auth.AuthService;
import ac.gov.br.sigei_backend.service.auth.RefreshTokenService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ac.gov.br.sigei_backend.exceptions.RegraNegocioException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cadastro, login e exclusão de usuários com transações reais (H2). Cada teste usa
 * e-mails próprios, porque os dados permanecem no banco entre os testes.
 */
@SpringBootTest
@ActiveProfiles("test")
class UsuarioServiceTest {

    @Autowired UsuarioService usuarioService;
    @Autowired AuthService authService;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired RefreshTokenRepository refreshTokenRepository;
    @Autowired RefreshTokenService refreshTokenService;

    private UsuarioRequestDTO requisicao(Long id, String email) {
        return new UsuarioRequestDTO(id, "Usuário de teste", email, "senha-forte-123", PerfilUsuario.USUARIO, true);
    }

    @Nested
    class Email {

        @Test
        void deveGravarEmailSemEspacosEEmMinusculas() {
            UsuarioResponseDTO salvo = usuarioService.salvar(requisicao(null, "  Maria.Silva@SIGEI.ac.gov.br "));

            assertEquals("maria.silva@sigei.ac.gov.br", salvo.email());
        }

        @Test
        void naoDeveCadastrarEmailQueSoDifereNaCaixa() {
            usuarioService.salvar(requisicao(null, "joao@sigei.ac.gov.br"));

            assertThrows(RegraNegocioException.class,
                    () -> usuarioService.salvar(requisicao(null, "Joao@SIGEI.ac.gov.br")));
        }

        @Test
        void naoDeveEditarParaEmailDeOutroUsuarioComOutraCaixa() {
            usuarioService.salvar(requisicao(null, "ana@sigei.ac.gov.br"));
            UsuarioResponseDTO outro = usuarioService.salvar(requisicao(null, "pedro@sigei.ac.gov.br"));

            assertThrows(RegraNegocioException.class,
                    () -> usuarioService.editar(outro.id(), requisicao(outro.id(), "ANA@sigei.ac.gov.br")));
        }

        @Test
        void deveEditarMantendoOProprioEmailComOutraCaixa() {
            UsuarioResponseDTO salvo = usuarioService.salvar(requisicao(null, "lucia@sigei.ac.gov.br"));

            UsuarioResponseDTO editado = usuarioService.editar(salvo.id(), requisicao(salvo.id(), "Lucia@SIGEI.ac.gov.br"));

            assertEquals("lucia@sigei.ac.gov.br", editado.email());
        }

        @Test
        void deveEntrarComEmailDigitadoEmOutraCaixa() {
            usuarioService.salvar(requisicao(null, "Carlos@sigei.ac.gov.br"));

            var sessao = authService.login(new LoginRequestDTO("CARLOS@Sigei.AC.gov.br", "senha-forte-123"));

            assertEquals("carlos@sigei.ac.gov.br", sessao.usuario().email());
        }
    }

    @Test
    void deveExcluirUsuarioQueJaFezLogin() {
        Usuario usuario = new Usuario();
        usuario.setNome("Usuário com sessão");
        usuario.setEmail("exclusao-com-sessao@sigei.ac.gov.br");
        usuario.setSenha("x");
        usuario.setPerfil(PerfilUsuario.USUARIO);
        usuario.setAtivo(true);
        usuario.setAlterarSenha(false);
        usuario = usuarioRepository.save(usuario);
        long tokensAntes = refreshTokenRepository.count();
        refreshTokenService.emitir(usuario);
        assertEquals(tokensAntes + 1, refreshTokenRepository.count());

        usuarioService.excluir(usuario.getId());

        assertFalse(usuarioRepository.existsById(usuario.getId()));
        assertEquals(tokensAntes, refreshTokenRepository.count());
    }
}
