package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.models.*;
import ac.gov.br.sigei_backend.models.enums.PerfilUsuario;
import ac.gov.br.sigei_backend.repository.*;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Trilha de auditoria pela cadeia HTTP real: autor vindo do JWT e acesso restrito a ADMIN. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HistoricoEscolaHttpTest {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @Autowired UsuarioRepository usuarios;
    @Autowired EscolaRepository escolas;
    @Autowired AldeiaRepository aldeias;
    @Autowired TerraIndigenaRepository terras;
    @Autowired MunicipioRepository municipios;
    @Autowired PovoRepository povos;

    private Usuario admin;
    private Usuario leitor;
    private Aldeia aldeia;

    @BeforeEach
    void preparar() {
        admin = usuario("admin.historico@sigei.com", PerfilUsuario.ADMIN);
        leitor = usuario("leitor.historico@sigei.com", PerfilUsuario.USUARIO);

        Municipio municipio = new Municipio();
        municipio.setNome("Municipio HTTP");
        municipios.save(municipio);
        TerraIndigena terra = new TerraIndigena();
        terra.setNome("Terra HTTP");
        terra.setMunicipio(municipio);
        terras.save(terra);
        Povo povo = new Povo();
        povo.setNome("Povo HTTP");
        povos.save(povo);
        aldeia = new Aldeia();
        aldeia.setNome("Aldeia HTTP");
        aldeia.setTerraIndigena(terra);
        aldeia.setPovo(povo);
        aldeias.save(aldeia);
    }

    @AfterEach
    void limpar() {
        escolas.deleteAll();
        aldeias.deleteAll();
        povos.deleteAll();
        terras.deleteAll();
        municipios.deleteAll();
        usuarios.deleteById(admin.getId());
        usuarios.deleteById(leitor.getId());
    }

    @Test
    void alteracaoPelaApiDeveRegistrarOUsuarioAutenticado() throws Exception {
        String corpo = mvc.perform(post("/escola/salvar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"COD-HTTP","nome":"Escola HTTP","tipoUnidade":"SEDE","aldeiaId":%d}
                                """.formatted(aldeia.getId())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long escolaId = Long.parseLong(corpo.replaceAll("^\\{\"id\":(\\d+).*$", "$1"));

        mvc.perform(get("/escola/" + escolaId + "/historico").header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].entidade").value("Escola"))
                .andExpect(jsonPath("$[0].operacao").value("CRIACAO"))
                .andExpect(jsonPath("$[0].usuarioId").value(admin.getId()))
                .andExpect(jsonPath("$[0].usuarioEmail").value(admin.getEmail()))
                .andExpect(jsonPath("$[0].dataHora").isNotEmpty());
    }

    @Test
    void historicoDeveSerRestritoAoAdministrador() throws Exception {
        mvc.perform(get("/escola/1/historico").header(HttpHeaders.AUTHORIZATION, bearer(leitor)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/escola/1/historico"))
                .andExpect(status().isUnauthorized());
        // O leitor continua consultando as demais rotas de escola
        mvc.perform(get("/escola").header(HttpHeaders.AUTHORIZATION, bearer(leitor)))
                .andExpect(status().isOk());
    }

    private String bearer(Usuario usuario) {
        return "Bearer " + jwtService.gerarToken(usuario);
    }

    private Usuario usuario(String email, PerfilUsuario perfil) {
        Usuario usuario = new Usuario();
        usuario.setNome("Historico " + perfil);
        usuario.setEmail(email);
        usuario.setSenha("hash-irrelevante");
        usuario.setPerfil(perfil);
        usuario.setAtivo(true);
        usuario.setAlterarSenha(false);
        return usuarios.save(usuario);
    }
}
