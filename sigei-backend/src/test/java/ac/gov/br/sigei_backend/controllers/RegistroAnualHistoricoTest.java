package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.exceptions.handler.ManipuladorGlobalException;
import ac.gov.br.sigei_backend.mappers.RegistroAnualMapper;
import ac.gov.br.sigei_backend.models.*;
import ac.gov.br.sigei_backend.repository.RegistroAnualRepository;
import ac.gov.br.sigei_backend.service.CadastroEscolaService;
import ac.gov.br.sigei_backend.service.EscolaService;
import ac.gov.br.sigei_backend.service.HistoricoEscolaService;
import ac.gov.br.sigei_backend.service.RegistroAnualService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@Import({RegistroAnualService.class, RegistroAnualController.class, EscolaController.class, RegistroAnualHistoricoTest.MappersConfig.class})
class RegistroAnualHistoricoTest {
    @TestConfiguration
    @ComponentScan(basePackageClasses = RegistroAnualMapper.class)
    static class MappersConfig {}

    @Autowired EntityManager entityManager;
    @Autowired RegistroAnualController controller;
    @Autowired EscolaController escolaController;
    // A rota dos registros de uma escola fica em EscolaController; os demais services dele não são usados aqui
    @MockitoBean EscolaService escolaService;
    @MockitoBean CadastroEscolaService cadastroEscolaService;
    @MockitoBean HistoricoEscolaService historicoEscolaService;
    @Autowired RegistroAnualRepository repository;
    private MockMvc mvc;
    private Escola escola;
    private RegistroAnual atual;
    private RegistroAnual antigo;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(controller, escolaController)
                .setControllerAdvice(new ManipuladorGlobalException()).build();
        escola = new Escola();
        escola.setNome("Escola histórica");
        entityManager.persist(escola);
        atual = criarRegistro(escola, 2026, 230);
        antigo = criarRegistro(escola, 1990, 40);
        criarRegistro(escola, 1992, null);

        InfraestruturaEscola infraestrutura = new InfraestruturaEscola();
        infraestrutura.setRegistroAnual(antigo);
        infraestrutura.setNumSalas(2);
        entityManager.persist(infraestrutura);

        MatriculaNivel matricula = new MatriculaNivel();
        matricula.setRegistro(antigo);
        matricula.setNivel("Fundamental");
        matricula.setTotal(40);
        entityManager.persist(matricula);

        EquipeEscola equipe = new EquipeEscola();
        equipe.setRegistroAnual(antigo);
        entityManager.persist(equipe);

        Escola outra = new Escola();
        outra.setNome("Outra escola");
        entityManager.persist(outra);
        RegistroAnual outroRegistro = new RegistroAnual();
        outroRegistro.setEscola(outra);
        outroRegistro.setAnoReferencia(antigo.getAnoReferencia());
        outroRegistro.setTotalAlunos(999);
        entityManager.persist(outroRegistro);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void retornaHistoricoCronologicoSomenteDaEscolaComDadosDeCadaAno() throws Exception {
        mvc.perform(get("/escolas/" + escola.getId() + "/registros-anuais"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].anoReferencia.ano", contains(1990, 1992, 2026)))
                .andExpect(jsonPath("$[0].totalAlunos").value(40))
                .andExpect(jsonPath("$[0].infraestrutura.numSalas").value(2))
                .andExpect(jsonPath("$[0].matriculasNivel[0].total").value(40))
                .andExpect(jsonPath("$[0].equipe.id").isNumber())
                .andExpect(jsonPath("$[1].totalAlunos").value(nullValue()))
                .andExpect(jsonPath("$[1].infraestrutura").value(nullValue()))
                .andExpect(jsonPath("$[2].totalAlunos").value(230));
        assertEquals(atual.getId(), repository.findTopByEscolaOrderByAnoReferencia_AnoDesc(escola).orElseThrow().getId());
    }

    @ParameterizedTest
    @CsvSource({"1990,1992,2", "1990,1990,1", "1991,2025,1", "1991,,2", ",1992,2", "2000,2005,0"})
    void filtraPeriodoComLimitesInclusivosEOpcionais(Integer inicio, Integer fim, int quantidade) throws Exception {
        var request = get("/escolas/" + escola.getId() + "/registros-anuais");
        if (inicio != null) request.param("anoInicial", inicio.toString());
        if (fim != null) request.param("anoFinal", fim.toString());
        mvc.perform(request).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(quantidade)));
    }

    @ParameterizedTest
    @CsvSource({"2026,1990", "0,1990", "1990,-1"})
    void rejeitaPeriodoInvalido(String inicio, String fim) throws Exception {
        mvc.perform(get("/escolas/" + escola.getId() + "/registros-anuais")
                        .param("anoInicial", inicio).param("anoFinal", fim))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validaEscolaEFormatoDosParametros() throws Exception {
        mvc.perform(get("/escolas/0/registros-anuais")).andExpect(status().isBadRequest());
        mvc.perform(get("/escolas/999999/registros-anuais"))
                .andExpect(status().isNotFound());
        mvc.perform(get("/escolas/" + escola.getId() + "/registros-anuais")
                .param("anoInicial", "abc")).andExpect(status().isBadRequest());
    }

    @Test
    void escolaSemRegistrosRetornaListaVazia() throws Exception {
        Escola vazia = new Escola();
        entityManager.persist(vazia);
        mvc.perform(get("/escolas/" + vazia.getId() + "/registros-anuais"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void cadastraAnoAntigoComTotalSemAlterarAnoAtualEImpedeDuplicata() throws Exception {
        AnoReferencia ano = new AnoReferencia();
        ano.setAno(1991);
        entityManager.persist(ano);
        String body = """
                {"escolaId": %d, "anoReferenciaId": %d, "rede": "Estadual",
                 "localFuncionamento": "Rural", "totalAlunos": 55}
                """.formatted(escola.getId(), ano.getId());
        mvc.perform(post("/registros-anuais").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.totalAlunos").value(55))
                .andExpect(jsonPath("$.anoReferencia.ano").value(1991));
        entityManager.flush();
        entityManager.clear();
        assertEquals(55, repository.findByEscolaAndAnoReferencia_Ano(escola, 1991).orElseThrow().getTotalAlunos());
        assertEquals(230, repository.findById(atual.getId()).orElseThrow().getTotalAlunos());
        mvc.perform(post("/registros-anuais").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void totalOpcionalPreservaCompatibilidadeERejeitaNegativo() throws Exception {
        AnoReferencia ano = new AnoReferencia();
        ano.setAno(1989);
        entityManager.persist(ano);
        String body = """
                {"escolaId": %d, "anoReferenciaId": %d, "rede": "Estadual",
                 "localFuncionamento": "Rural"%s}
                """;
        mvc.perform(post("/registros-anuais").contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted(escola.getId(), ano.getId(), ", \"totalAlunos\": -1")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/registros-anuais").contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted(escola.getId(), ano.getId(), "")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.totalAlunos").value(nullValue()));
    }

    @Test
    void edicaoSemTotalPreservaValorExistenteEZeroPodeSerInformado() throws Exception {
        String body = """
                {"id": %d, "escolaId": %d, "anoReferenciaId": %d, "rede": "Estadual",
                 "localFuncionamento": "Rural"%s}
                """;
        mvc.perform(put("/registros-anuais/" + antigo.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted(antigo.getId(), escola.getId(), antigo.getAnoReferencia().getId(), "")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalAlunos").value(40));
        mvc.perform(put("/registros-anuais/" + antigo.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content(body.formatted(antigo.getId(), escola.getId(), antigo.getAnoReferencia().getId(), ", \"totalAlunos\": 0")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalAlunos").value(0));
        entityManager.flush();
        entityManager.clear();
        assertEquals(0, repository.findById(antigo.getId()).orElseThrow().getTotalAlunos());
        assertEquals(230, repository.findById(atual.getId()).orElseThrow().getTotalAlunos());
    }

    private RegistroAnual criarRegistro(Escola escola, int ano, Integer total) {
        AnoReferencia referencia = new AnoReferencia();
        referencia.setAno(ano);
        entityManager.persist(referencia);
        RegistroAnual registro = new RegistroAnual();
        registro.setEscola(escola);
        registro.setAnoReferencia(referencia);
        registro.setRede("Estadual");
        registro.setLocalFuncionamento("Rural");
        registro.setTotalAlunos(total);
        entityManager.persist(registro);
        return registro;
    }
}
