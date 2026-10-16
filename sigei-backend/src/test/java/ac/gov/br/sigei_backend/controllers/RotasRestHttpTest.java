package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.models.AnoReferencia;
import ac.gov.br.sigei_backend.models.Povo;
import ac.gov.br.sigei_backend.models.Usuario;
import ac.gov.br.sigei_backend.models.enums.PerfilUsuario;
import ac.gov.br.sigei_backend.repository.AnoReferenciaRepository;
import ac.gov.br.sigei_backend.repository.PovoRepository;
import ac.gov.br.sigei_backend.repository.UsuarioRepository;
import ac.gov.br.sigei_backend.service.auth.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contrato REST dos recursos: o ID da edição vem do caminho e prevalece sobre o do
 * corpo, o cadastro ignora um ID enviado no corpo e a exclusão responde 204.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RotasRestHttpTest {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @Autowired UsuarioRepository usuarios;
    @Autowired PovoRepository povos;
    @Autowired AnoReferenciaRepository anos;

    private String token;
    private Povo povo;

    @BeforeEach
    void preparar() {
        Usuario admin = new Usuario();
        admin.setNome("Admin rotas");
        admin.setEmail("admin.rotas@sigei.com");
        admin.setSenha("hash-irrelevante");
        admin.setPerfil(PerfilUsuario.ADMIN);
        admin.setAtivo(true);
        admin.setAlterarSenha(false);
        token = "Bearer " + jwtService.gerarToken(usuarios.save(admin));

        povo = new Povo();
        povo.setNome("Povo rotas");
        povos.save(povo);
    }

    @Test
    void edicaoUsaOIdDoCaminhoMesmoSemIdNoCorpo() throws Exception {
        long total = povos.count();

        mvc.perform(put("/povos/" + povo.getId())
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Povo renomeado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(povo.getId()))
                .andExpect(jsonPath("$.nome").value("Povo renomeado"));

        assertEquals(total, povos.count());
        assertEquals("Povo renomeado", povos.findById(povo.getId()).orElseThrow().getNome());
    }

    @Test
    void edicaoIgnoraIdDiferenteNoCorpo() throws Exception {
        Povo outro = new Povo();
        outro.setNome("Outro povo rotas");
        povos.save(outro);

        mvc.perform(put("/povos/" + povo.getId())
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":" + outro.getId() + ",\"nome\":\"Povo renomeado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(povo.getId()));

        assertEquals("Outro povo rotas", povos.findById(outro.getId()).orElseThrow().getNome());
    }

    @Test
    void edicaoDeRegistroInexistenteResponde404() throws Exception {
        mvc.perform(put("/povos/999999")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Fantasma\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cadastroIgnoraIdNoCorpoESempreCriaUmNovoRegistro() throws Exception {
        mvc.perform(post("/povos")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":" + povo.getId() + ",\"nome\":\"Povo novo\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Povo novo"));

        assertEquals("Povo rotas", povos.findById(povo.getId()).orElseThrow().getNome());
    }

    @Test
    void exclusaoResponde204() throws Exception {
        mvc.perform(delete("/povos/" + povo.getId()).header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isNoContent());

        assertFalse(povos.existsById(povo.getId()));
    }

    @Test
    void proximoAnoCriaOAnoSeguinteAoMaiorERetornaOAnoCriado() throws Exception {
        AnoReferencia ano = new AnoReferencia();
        ano.setAno(2097);
        anos.save(ano);
        int esperado = anos.findTopByOrderByAnoDesc().orElseThrow().getAno() + 1;

        mvc.perform(post("/anos-referencia/proximo").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.ano").value(esperado));
    }

    @Test
    void infraestruturasDoAnoSaoPublicas() throws Exception {
        mvc.perform(get("/anos/2025/infraestruturas")).andExpect(status().isOk());
        mvc.perform(get("/infraestruturas/mais-recentes")).andExpect(status().isOk());
    }

    @Test
    void demaisListasDoAnoExigemAutenticacao() throws Exception {
        mvc.perform(get("/anos/2025/registros-anuais")).andExpect(status().isUnauthorized());
        mvc.perform(get("/anos/2025/lotacoes")).andExpect(status().isUnauthorized());
        mvc.perform(get("/anos/2025/registros-anuais").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk());
    }

    @Test
    void rotasAntigasComVerboNaoExistemMais() throws Exception {
        mvc.perform(post("/povo/salvar")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Antigo\"}"))
                .andExpect(status().isNotFound());
    }
}
