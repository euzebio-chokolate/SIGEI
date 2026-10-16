package ac.gov.br.sigei_backend.config.security;

import ac.gov.br.sigei_backend.models.Usuario;
import ac.gov.br.sigei_backend.models.enums.PerfilUsuario;
import ac.gov.br.sigei_backend.repository.UsuarioRepository;
import ac.gov.br.sigei_backend.service.auth.JwtService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Toda resposta de erro sai como ProblemDetail (application/problem+json, com
 * status, title, detail e instance), venha ela de um service, do Spring MVC, da
 * segurança ou do encaminhamento a /error. Usa um servidor real, porque o MockMvc
 * não faz o encaminhamento a /error.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FormatoErroHttpTest {

    @LocalServerPort int porta;
    @Autowired UsuarioRepository usuarios;
    @Autowired JwtService jwtService;

    private final HttpClient cliente = HttpClient.newHttpClient();
    private Usuario admin;
    private Usuario comum;

    @BeforeEach
    void preparar() {
        admin = usuario("formato.admin@sigei.com", PerfilUsuario.ADMIN);
        comum = usuario("formato.comum@sigei.com", PerfilUsuario.USUARIO);
    }

    @AfterEach
    void limpar() {
        usuarios.deleteById(admin.getId());
        usuarios.deleteById(comum.getId());
    }

    @ParameterizedTest(name = "{0} {1} -> {3}")
    @CsvSource(delimiter = '|', nullValues = "-", value = {
            // método | rota                          | corpo         | status | perfil | detail
            "DELETE   | /povos/999999                 | -             | 404    | ADMIN  | Id de povo inválido ou não encontrado",
            "GET      | /municipios/x/y               | -             | 404    | ADMIN  | O recurso solicitado não foi encontrado.",
            "POST     | /povos                        | {nome         | 400    | ADMIN  | O corpo da requisição é inválido ou está mal formatado.",
            "GET      | /povos/abc                    | -             | 400    | ADMIN  | Valor inválido para id.",
            "GET      | /anos/abc/registros-anuais    | -             | 400    | ADMIN  | Valor inválido para ano.",
            "PATCH    | /povos                        | {}            | 405    | ADMIN  | O método PATCH não é aceito nesta rota.",
            "GET      | /povos                        | -             | 401    | -      | Autenticação necessária. Entre novamente.",
            "POST     | /povos                        | {\"nome\":\"x\"} | 403 | USUARIO | Você não tem permissão para realizar esta ação.",
            "POST     | /auth/login                   | {\"email\":\"ninguem@sigei.com\",\"senha\":\"12345678\"} | 401 | - | E-mail ou senha inválidos."
    })
    void erroSaiComoProblemDetail(String metodo, String rota, String corpo, int status, String perfil, String detalhe)
            throws Exception {
        HttpResponse<String> resposta = enviar(metodo.trim(), rota.trim(), corpo, perfil);

        assertEquals(status, resposta.statusCode());
        assertEquals("application/problem+json", resposta.headers().firstValue("Content-Type").orElse(""));
        String json = resposta.body();
        assertEquals(status, (int) JsonPath.read(json, "$.status"));
        assertNotNull(JsonPath.read(json, "$.title"));
        assertEquals(detalhe.trim(), JsonPath.read(json, "$.detail"));
        assertEquals(rota.trim(), JsonPath.read(json, "$.instance"));
    }

    @Test
    void validacaoTrazAsMensagensPorCampo() throws Exception {
        HttpResponse<String> resposta = enviar("POST", "/povos", "{\"nome\":\"\"}", "ADMIN");

        assertEquals(400, resposta.statusCode());
        assertEquals("application/problem+json", resposta.headers().firstValue("Content-Type").orElse(""));
        Map<String, List<String>> erros = JsonPath.read(resposta.body(), "$.errors");
        assertEquals(Map.of("nome", List.of("Nome não pode ser vazio")), erros);
        // Com um só campo inválido, o detail repete a mensagem dele
        assertEquals("Nome não pode ser vazio", JsonPath.read(resposta.body(), "$.detail"));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            // rota          | corpo                                                                        | campo  | detail
            "/usuarios | {\"nome\":\"X\",\"email\":\"x@sigei.com\",\"senha\":\"12345678\",\"perfil\":\"CHEFE\",\"ativo\":true} | perfil | Valor inválido para o campo perfil. Use um destes: ADMIN, USUARIO.",
            "/anos-referencia    | {\"ano\":\"dois mil\"}                                                       | ano    | Valor inválido para o campo ano."
    })
    void valorDeTipoErradoNoCorpoApontaOCampo(String rota, String corpo, String campo, String detalhe) throws Exception {
        HttpResponse<String> resposta = enviar("POST", rota.trim(), corpo, "ADMIN");

        assertEquals(400, resposta.statusCode());
        assertEquals("application/problem+json", resposta.headers().firstValue("Content-Type").orElse(""));
        assertEquals(detalhe.trim(), JsonPath.read(resposta.body(), "$.detail"));
        assertEquals(List.of(detalhe.trim()), JsonPath.read(resposta.body(), "$.errors." + campo.trim()));
    }

    @Test
    void uploadAcimaDoLimiteResponde413() throws Exception {
        String fronteira = "----sigei-teste";
        byte[] inicio = ("--" + fronteira + "\r\nContent-Disposition: form-data; name=\"arquivo\"; filename=\"grande.xlsx\"\r\n"
                + "Content-Type: application/octet-stream\r\n\r\n").getBytes(StandardCharsets.UTF_8);
        byte[] fim = ("\r\n--" + fronteira + "--\r\n").getBytes(StandardCharsets.UTF_8);
        byte[] corpo = new byte[inicio.length + 21 * 1024 * 1024 + fim.length];
        System.arraycopy(inicio, 0, corpo, 0, inicio.length);
        System.arraycopy(fim, 0, corpo, corpo.length - fim.length, fim.length);

        HttpRequest requisicao = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + "/importacoes/infraestrutura"))
                .POST(HttpRequest.BodyPublishers.ofByteArray(corpo))
                .header("Content-Type", "multipart/form-data; boundary=" + fronteira)
                .header("Authorization", "Bearer " + jwtService.gerarToken(admin))
                .build();
        HttpResponse<String> resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());

        assertEquals(413, resposta.statusCode());
        assertEquals("application/problem+json", resposta.headers().firstValue("Content-Type").orElse(""));
        assertEquals("O arquivo enviado excede o tamanho máximo permitido (20 MB).", JsonPath.read(resposta.body(), "$.detail"));
    }

    private HttpResponse<String> enviar(String metodo, String rota, String corpo, String perfil) throws Exception {
        HttpRequest.Builder requisicao = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + rota))
                .method(metodo, corpo == null
                        ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(corpo.trim()))
                .header("Content-Type", "application/json");
        if (perfil != null) {
            Usuario usuario = "ADMIN".equals(perfil.trim()) ? admin : comum;
            requisicao.header("Authorization", "Bearer " + jwtService.gerarToken(usuario));
        }
        return cliente.send(requisicao.build(), HttpResponse.BodyHandlers.ofString());
    }

    private Usuario usuario(String email, PerfilUsuario perfil) {
        Usuario usuario = new Usuario();
        usuario.setNome("Formato de erro");
        usuario.setEmail(email);
        usuario.setSenha("nao-usada");
        usuario.setPerfil(perfil);
        usuario.setAtivo(true);
        usuario.setAlterarSenha(false);
        return usuarios.save(usuario);
    }
}
