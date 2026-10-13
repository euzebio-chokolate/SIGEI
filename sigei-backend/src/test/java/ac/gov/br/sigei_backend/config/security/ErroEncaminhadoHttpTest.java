package ac.gov.br.sigei_backend.config.security;

import ac.gov.br.sigei_backend.models.Usuario;
import ac.gov.br.sigei_backend.models.enums.PerfilUsuario;
import ac.gov.br.sigei_backend.repository.UsuarioRepository;
import ac.gov.br.sigei_backend.service.auth.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Erros encaminhados para /error (rota inexistente, exceção sem tratamento) devem
 * chegar ao cliente com o status real. O filtro JWT não roda no encaminhamento, e
 * sem a liberação do DispatcherType.ERROR o usuário autenticado recebia 401, que o
 * frontend trata como sessão expirada e encerra a sessão.
 *
 * <p>Usa um servidor real: o MockMvc não faz o encaminhamento para /error.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ErroEncaminhadoHttpTest {

    @LocalServerPort int porta;
    @Autowired UsuarioRepository usuarios;
    @Autowired JwtService jwtService;

    private final HttpClient cliente = HttpClient.newHttpClient();
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        usuario = new Usuario();
        usuario.setNome("Consulta erro");
        usuario.setEmail("erro-encaminhado@sigei.com");
        usuario.setSenha("nao-usada");
        usuario.setPerfil(PerfilUsuario.USUARIO);
        usuario.setAtivo(true);
        usuario.setAlterarSenha(false);
        usuario = usuarios.save(usuario);
    }

    @AfterEach
    void limpar() {
        usuarios.deleteById(usuario.getId());
    }

    @Test
    void rotaInexistenteDeveResponder404AoUsuarioAutenticado() throws Exception {
        assertEquals(404, get("/municipio/buscarTudo", jwtService.gerarToken(usuario)));
    }

    @Test
    void rotaInexistenteProtegidaContinuaExigindoAutenticacao() throws Exception {
        assertEquals(401, get("/municipio/buscarTudo", null));
    }

    private int get(String caminho, String token) throws Exception {
        HttpRequest.Builder requisicao = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho)).GET();

        if (token != null) {
            requisicao.header("Authorization", "Bearer " + token);
        }

        return cliente.send(requisicao.build(), HttpResponse.BodyHandlers.discarding()).statusCode();
    }
}
