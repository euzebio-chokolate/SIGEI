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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Dados sensíveis dos professores pela cadeia HTTP real: cifrados no banco e na
 * auditoria, devolvidos só ao ADMIN e gravados só por ele.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfessorHttpTest {

    private static final String CPF = "52998224725";

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @Autowired JdbcTemplate jdbc;
    @Autowired UsuarioRepository usuarios;
    @Autowired ProfessorRepository professores;
    @Autowired LotacaoProfessorRepository lotacoes;
    @Autowired AnoReferenciaRepository anos;
    @Autowired MunicipioRepository municipios;

    private Usuario admin;
    private Usuario leitor;
    private AnoReferencia ano;
    private Municipio municipio;

    @BeforeEach
    void preparar() {
        admin = usuario("admin.professor@sigei.com", PerfilUsuario.ADMIN);
        leitor = usuario("leitor.professor@sigei.com", PerfilUsuario.USUARIO);

        ano = new AnoReferencia();
        ano.setAno(2026);
        anos.save(ano);

        municipio = new Municipio();
        municipio.setNome("Municipio professores");
        municipios.save(municipio);
    }

    @AfterEach
    void limpar() {
        lotacoes.deleteAll();
        professores.deleteAll();
        municipios.delete(municipio);
        anos.delete(ano);
        usuarios.deleteById(admin.getId());
        usuarios.deleteById(leitor.getId());
    }

    @Test
    void dadosSensiveisDevemSerGravadosCifradosInclusiveNaAuditoria() throws Exception {
        long id = criarProfessor("529.982.247-25", "MAT-778899");

        String cpf = jdbc.queryForObject("select cpf from professor where id = ?", String.class, id);
        String matricula = jdbc.queryForObject("select matricula from professor where id = ?", String.class, id);
        String cpfHash = jdbc.queryForObject("select cpf_hash from professor where id = ?", String.class, id);
        List<String> cpfAuditado = jdbc.queryForList("select cpf from professor_aud where id = ?", String.class, id);

        assertTrue(cpf.startsWith("v1:"));
        assertFalse(cpf.contains(CPF));
        assertTrue(matricula.startsWith("v1:"));
        assertFalse(matricula.contains("778899"));
        assertEquals(64, cpfHash.length());
        assertFalse(cpfAuditado.isEmpty());
        cpfAuditado.forEach(valor -> assertTrue(valor.startsWith("v1:")));
    }

    @Test
    void somenteOAdministradorDeveReceberOsDadosSensiveis() throws Exception {
        long id = criarProfessor(CPF, "MAT-1");
        long lotacaoId = criarLotacao(id, "CT-2026-15");

        mvc.perform(get("/professor/" + id).header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpf").value(CPF))
                .andExpect(jsonPath("$.matricula").value("MAT-1"));

        mvc.perform(get("/professor/" + id).header(HttpHeaders.AUTHORIZATION, bearer(leitor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Professora"))
                .andExpect(jsonPath("$.cpf").doesNotExist())
                .andExpect(jsonPath("$.matricula").doesNotExist());

        mvc.perform(get("/professor").header(HttpHeaders.AUTHORIZATION, bearer(leitor)))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(CPF))));

        mvc.perform(get("/lotacao/" + lotacaoId).header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(jsonPath("$.numeroContrato").value("CT-2026-15"));

        mvc.perform(get("/lotacao/buscarPorAno").param("ano", "2026").header(HttpHeaders.AUTHORIZATION, bearer(leitor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].professor.nome").value("Maria Professora"))
                .andExpect(jsonPath("$[0].numeroContrato").doesNotExist());
    }

    @Test
    void gravacaoDeveSerRestritaAoAdministrador() throws Exception {
        mvc.perform(post("/professor/salvar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(leitor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Tentativa\"}"))
                .andExpect(status().isForbidden());

        mvc.perform(get("/professor"))
                .andExpect(status().isUnauthorized());

        mvc.perform(post("/importacoes/professores").header(HttpHeaders.AUTHORIZATION, bearer(leitor)))
                .andExpect(status().isForbidden());
    }

    @Test
    void cpfInvalidoOuRepetidoDeveSerRecusadoSemRepetirOValor() throws Exception {
        criarProfessor(CPF, null);

        mvc.perform(post("/professor/salvar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Outra\",\"cpf\":\"529.982.247-25\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("CPF já cadastrado para outro professor."));

        mvc.perform(post("/professor/salvar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Outra\",\"cpf\":\"123.456.789-00\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("CPF inválido."));
    }

    @Test
    void lotacaoSemEscolaExigeMunicipioEVagaDispensaProfessor() throws Exception {
        mvc.perform(post("/lotacao/salvar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"anoReferenciaId\":%d,\"segmento\":\"1º AO 5º ANO\"}".formatted(ano.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Informe a escola ou, para lotação no núcleo, o município"));

        mvc.perform(post("/lotacao/salvar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"anoReferenciaId\":%d,\"municipioId\":%d,\"tipoContrato\":\"temporario\"}"
                                .formatted(ano.getId(), municipio.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Tipo de contrato inválido")));

        mvc.perform(post("/lotacao/salvar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"anoReferenciaId\":%d,\"municipioId\":%d,\"tipoContrato\":\"contrato direto\"}"
                                .formatted(ano.getId(), municipio.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.professor").doesNotExist())
                .andExpect(jsonPath("$.escola").doesNotExist())
                .andExpect(jsonPath("$.municipio.id").value(municipio.getId()))
                .andExpect(jsonPath("$.tipoContrato").value("CONTRATO_DIRETO"));
    }

    @Test
    void excluirProfessorDeveExcluirSuasLotacoes() throws Exception {
        long id = criarProfessor(null, null);
        criarLotacao(id, null);

        mvc.perform(delete("/professor/excluir/" + id).header(HttpHeaders.AUTHORIZATION, bearer(admin)))
                .andExpect(status().isNoContent());

        assertEquals(0, lotacoes.count());
    }

    private long criarProfessor(String cpf, String matricula) throws Exception {
        String corpo = mvc.perform(post("/professor/salvar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Maria Professora","cpf":%s,"matricula":%s,"etnia":"HUNI KUI","sexo":"F"}
                                """.formatted(json(cpf), json(matricula))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return Long.parseLong(corpo.replaceAll("^\\{\"id\":(\\d+).*$", "$1"));
    }

    private long criarLotacao(long professorId, String numeroContrato) throws Exception {
        String corpo = mvc.perform(post("/lotacao/salvar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"professorId":%d,"anoReferenciaId":%d,"municipioId":%d,"numeroContrato":%s,"tipoContrato":"PSS"}
                                """.formatted(professorId, ano.getId(), municipio.getId(), json(numeroContrato))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return Long.parseLong(corpo.replaceAll("^\\{\"id\":(\\d+).*$", "$1"));
    }

    private static String json(String valor) {
        return valor == null ? "null" : "\"" + valor + "\"";
    }

    private String bearer(Usuario usuario) {
        return "Bearer " + jwtService.gerarToken(usuario);
    }

    private Usuario usuario(String email, PerfilUsuario perfil) {
        Usuario usuario = new Usuario();
        usuario.setNome("Professores " + perfil);
        usuario.setEmail(email);
        usuario.setSenha("hash-irrelevante");
        usuario.setPerfil(perfil);
        usuario.setAtivo(true);
        usuario.setAlterarSenha(false);
        return usuarios.save(usuario);
    }
}
