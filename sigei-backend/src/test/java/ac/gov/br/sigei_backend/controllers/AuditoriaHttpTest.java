package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.models.Povo;
import ac.gov.br.sigei_backend.models.Usuario;
import ac.gov.br.sigei_backend.models.enums.PerfilUsuario;
import ac.gov.br.sigei_backend.repository.PovoRepository;
import ac.gov.br.sigei_backend.repository.UsuarioRepository;
import ac.gov.br.sigei_backend.service.auth.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Auditoria geral pela cadeia HTTP real: autor vindo do JWT, filtros e acesso restrito a ADMIN. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditoriaHttpTest {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @Autowired UsuarioRepository usuarios;
    @Autowired PovoRepository povos;

    private Usuario admin;
    private Usuario leitor;

    @BeforeEach
    void preparar() {
        admin = usuario("admin.auditoria@sigei.com", PerfilUsuario.ADMIN);
        leitor = usuario("leitor.auditoria@sigei.com", PerfilUsuario.USUARIO);
    }

    @AfterEach
    void limpar() {
        povos.deleteAll();
        usuarios.deleteById(admin.getId());
        usuarios.deleteById(leitor.getId());
    }

    @Test
    void deveListarAsAlteracoesFeitasPeloAdministrador() throws Exception {
        mvc.perform(post("/povos")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Povo auditado via HTTP\"}"))
                .andExpect(status().is2xxSuccessful());

        mvc.perform(get("/auditoria")
                        .param("usuarioId", admin.getId().toString())
                        .param("entidade", "Povo")
                        .param("tamanho", "10")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alteracoes[0].entidade").value("Povo"))
                .andExpect(jsonPath("$.alteracoes[0].operacao").value("CRIACAO"))
                .andExpect(jsonPath("$.alteracoes[0].descricao").value("Povo auditado via HTTP"))
                .andExpect(jsonPath("$.alteracoes[0].usuarioEmail").value(admin.getEmail()))
                .andExpect(jsonPath("$.totalAlteracoes").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.tamanho").value(10));
    }

    @Test
    void auditoriaDeveSerRestritaAoAdministrador() throws Exception {
        mvc.perform(get("/auditoria").header(HttpHeaders.AUTHORIZATION, bearer(leitor)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/auditoria"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void filtroInvalidoDeveResultarEmRequisicaoInvalida() throws Exception {
        mvc.perform(get("/auditoria").param("entidade", "Inexistente").header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/auditoria").param("tamanho", "500").header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isBadRequest());
    }

    private String bearer(Usuario usuario) {
        return "Bearer " + jwtService.gerarToken(usuario);
    }

    private Usuario usuario(String email, PerfilUsuario perfil) {
        Usuario usuario = new Usuario();
        usuario.setNome("Auditoria " + perfil);
        usuario.setEmail(email);
        usuario.setSenha("hash-irrelevante");
        usuario.setPerfil(perfil);
        usuario.setAtivo(true);
        usuario.setAlterarSenha(false);
        return usuarios.save(usuario);
    }
}
