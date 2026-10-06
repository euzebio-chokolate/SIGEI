package ac.gov.br.sigei_backend.controllers.auth;

import ac.gov.br.sigei_backend.config.security.CookiesSessao;
import ac.gov.br.sigei_backend.models.Usuario;
import ac.gov.br.sigei_backend.models.enums.PerfilUsuario;
import ac.gov.br.sigei_backend.repository.PovoRepository;
import ac.gov.br.sigei_backend.repository.RefreshTokenRepository;
import ac.gov.br.sigei_backend.repository.UsuarioRepository;
import ac.gov.br.sigei_backend.service.auth.JwtService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Verifica sessão por cookies HttpOnly e proteção CSRF com a cadeia de segurança real. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSessaoHttpTest {

    private static final String EMAIL = "sessao@sigei.com";
    private static final String SENHA = "Senha@123";
    private static final String CSRF = "csrf-de-teste";

    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired RefreshTokenRepository refreshTokens;
    @Autowired PovoRepository povos;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;

    private Usuario usuario;

    @BeforeEach
    void preparar() {
        usuario = new Usuario();
        usuario.setNome("Gestor sessão");
        usuario.setEmail(EMAIL);
        usuario.setSenha(passwordEncoder.encode(SENHA));
        usuario.setPerfil(PerfilUsuario.ADMIN);
        usuario.setAtivo(true);
        usuario.setAlterarSenha(false);
        usuario = usuarios.save(usuario);
    }

    @AfterEach
    void limpar() {
        povos.deleteAll();
        refreshTokens.deleteAll();
        usuarios.deleteById(usuario.getId());
    }

    @Test
    void loginDeveGravarTokensEmCookiesHttpOnlySemExporNoCorpo() throws Exception {
        MockHttpServletResponse resposta = login();

        assertFalse(resposta.getContentAsString().contains("token"));
        assertTrue(resposta.getContentAsString().contains(EMAIL));

        for (String nome : List.of(CookiesSessao.COOKIE_ACESSO, CookiesSessao.COOKIE_RENOVACAO)) {
            String setCookie = setCookie(resposta, nome);
            assertTrue(setCookie.contains("HttpOnly"), setCookie);
            assertTrue(setCookie.contains("SameSite=Strict"), setCookie);
            assertTrue(setCookie.contains("Path=/"), setCookie);
        }

        // Cookie CSRF legível pelo frontend (sem HttpOnly)
        String xsrf = setCookie(resposta, "XSRF-TOKEN");
        assertFalse(xsrf.contains("HttpOnly"), xsrf);
    }

    @Test
    void cookieDeAcessoDeveAutenticarConsultas() throws Exception {
        Cookie acesso = cookie(login(), CookiesSessao.COOKIE_ACESSO);

        mvc.perform(get("/usuario").cookie(acesso)).andExpect(status().isOk());
        mvc.perform(get("/usuario")).andExpect(status().isUnauthorized());
    }

    @Test
    void alteracaoComCookieDeveExigirTokenCsrf() throws Exception {
        Cookie acesso = cookie(login(), CookiesSessao.COOKIE_ACESSO);

        mvc.perform(post("/povo/salvar").cookie(acesso)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Povo CSRF\"}"))
                .andExpect(status().isForbidden());

        mvc.perform(post("/povo/salvar").cookie(acesso, new Cookie("XSRF-TOKEN", CSRF))
                        .header("X-XSRF-TOKEN", "outro-valor")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Povo CSRF\"}"))
                .andExpect(status().isForbidden());

        mvc.perform(post("/povo/salvar").cookie(acesso, new Cookie("XSRF-TOKEN", CSRF))
                        .header("X-XSRF-TOKEN", CSRF)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Povo CSRF\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void alteracaoComBearerDeveDispensarCsrf() throws Exception {
        mvc.perform(post("/povo/salvar")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.gerarToken(usuario))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Povo Bearer\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void refreshDeveRenovarCookiesERejeitarOTokenUsado() throws Exception {
        Cookie renovacao = cookie(login(), CookiesSessao.COOKIE_RENOVACAO);

        MockHttpServletResponse renovada = mvc.perform(comCsrf(post("/auth/refresh")).cookie(renovacao))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(EMAIL)))
                .andReturn().getResponse();

        Cookie nova = cookie(renovada, CookiesSessao.COOKIE_RENOVACAO);
        assertNotEquals(renovacao.getValue(), nova.getValue());
        mvc.perform(get("/usuario").cookie(cookie(renovada, CookiesSessao.COOKIE_ACESSO)))
                .andExpect(status().isOk());

        // Token antigo: recusado, e a resposta remove os cookies de sessão
        MockHttpServletResponse recusada = mvc.perform(comCsrf(post("/auth/refresh")).cookie(renovacao))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse();
        assertTrue(setCookie(recusada, CookiesSessao.COOKIE_ACESSO).contains("Max-Age=0"));
    }

    @Test
    void refreshDeveExigirCsrfECookie() throws Exception {
        Cookie renovacao = cookie(login(), CookiesSessao.COOKIE_RENOVACAO);

        mvc.perform(post("/auth/refresh").cookie(renovacao)).andExpect(status().isForbidden());
        mvc.perform(comCsrf(post("/auth/refresh")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Sessão não encontrada")));
    }

    @Test
    void logoutDeveRevogarSessaoERemoverCookies() throws Exception {
        Cookie renovacao = cookie(login(), CookiesSessao.COOKIE_RENOVACAO);

        MockHttpServletResponse saida = mvc.perform(comCsrf(post("/auth/logout")).cookie(renovacao))
                .andExpect(status().isNoContent())
                .andReturn().getResponse();

        assertTrue(setCookie(saida, CookiesSessao.COOKIE_ACESSO).contains("Max-Age=0"));
        assertTrue(setCookie(saida, CookiesSessao.COOKIE_RENOVACAO).contains("Max-Age=0"));
        mvc.perform(comCsrf(post("/auth/refresh")).cookie(renovacao)).andExpect(status().isUnauthorized());
    }

    @Test
    void emailInexistenteESenhaIncorretaDevemTerAMesmaResposta401() throws Exception {
        MockHttpServletResponse senhaIncorreta = tentarLogin(EMAIL, "senha-errada")
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("E-mail ou senha")))
                .andReturn().getResponse();
        MockHttpServletResponse emailInexistente = tentarLogin("naoexiste@sigei.com", SENHA)
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse();

        assertEquals(senhaIncorreta.getContentAsString(), emailInexistente.getContentAsString());
        assertTrue(senhaIncorreta.getHeaders(HttpHeaders.SET_COOKIE).stream()
                .noneMatch(valor -> valor.startsWith(CookiesSessao.COOKIE_ACESSO + "=")));
    }

    @Test
    void usuarioDesativadoSoDeveSerInformadoAQuemAcertaASenha() throws Exception {
        usuario.setAtivo(false);
        usuarios.save(usuario);

        tentarLogin(EMAIL, SENHA)
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("desativado")));
        tentarLogin(EMAIL, "senha-errada")
                .andExpect(status().isUnauthorized());
    }

    private org.springframework.test.web.servlet.ResultActions tentarLogin(String email, String senha) throws Exception {
        return mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"));
    }

    private MockHttpServletResponse login() throws Exception {
        return mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + EMAIL + "\",\"senha\":\"" + SENHA + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse();
    }

    private static MockHttpServletRequestBuilder comCsrf(
            MockHttpServletRequestBuilder requisicao) {
        return requisicao.cookie(new Cookie("XSRF-TOKEN", CSRF)).header("X-XSRF-TOKEN", CSRF);
    }

    private static String setCookie(MockHttpServletResponse resposta, String nome) {
        return resposta.getHeaders(HttpHeaders.SET_COOKIE).stream()
                .filter(valor -> valor.startsWith(nome + "="))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Set-Cookie ausente: " + nome));
    }

    private static Cookie cookie(MockHttpServletResponse resposta, String nome) {
        String valor = setCookie(resposta, nome);
        return new Cookie(nome, valor.substring(nome.length() + 1, valor.indexOf(';')));
    }
}
