package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.models.Usuario;
import ac.gov.br.sigei_backend.models.enums.PerfilUsuario;
import ac.gov.br.sigei_backend.repository.UsuarioRepository;
import ac.gov.br.sigei_backend.service.auth.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A consulta de usuários (nomes, e-mails e perfis) é só do ADMIN, embora a regra
 * geral libere as leituras ao USUARIO. Esconder a tela no frontend não basta.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UsuarioAcessoHttpTest {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @Autowired UsuarioRepository usuarios;

    private Usuario admin;
    private Usuario comum;

    @BeforeEach
    void preparar() {
        admin = usuario("acesso.admin@sigei.com", PerfilUsuario.ADMIN);
        comum = usuario("acesso.comum@sigei.com", PerfilUsuario.USUARIO);
    }

    @Test
    void usuarioComumNaoListaNemConsultaUsuarios() throws Exception {
        mvc.perform(get("/usuarios").header(HttpHeaders.AUTHORIZATION, token(comum))).andExpect(status().isForbidden());
        mvc.perform(get("/usuarios/" + admin.getId()).header(HttpHeaders.AUTHORIZATION, token(comum)))
                .andExpect(status().isForbidden());
        // Nem os próprios dados: o login já devolve o que a tela precisa
        mvc.perform(get("/usuarios/" + comum.getId()).header(HttpHeaders.AUTHORIZATION, token(comum)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminListaEConsultaUsuarios() throws Exception {
        mvc.perform(get("/usuarios").header(HttpHeaders.AUTHORIZATION, token(admin))).andExpect(status().isOk());
        mvc.perform(get("/usuarios/" + comum.getId()).header(HttpHeaders.AUTHORIZATION, token(admin)))
                .andExpect(status().isOk());
    }

    @Test
    void usuarioComumContinuaLendoOsDemaisCadastros() throws Exception {
        mvc.perform(get("/povos").header(HttpHeaders.AUTHORIZATION, token(comum))).andExpect(status().isOk());
    }

    private String token(Usuario usuario) {
        return "Bearer " + jwtService.gerarToken(usuario);
    }

    private Usuario usuario(String email, PerfilUsuario perfil) {
        Usuario usuario = new Usuario();
        usuario.setNome("Acesso " + perfil);
        usuario.setEmail(email);
        usuario.setSenha("hash-irrelevante");
        usuario.setPerfil(perfil);
        usuario.setAtivo(true);
        usuario.setAlterarSenha(false);
        return usuarios.save(usuario);
    }
}
