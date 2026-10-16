package ac.gov.br.sigei_backend.controllers;

import ac.gov.br.sigei_backend.models.*;
import ac.gov.br.sigei_backend.models.enums.PerfilUsuario;
import ac.gov.br.sigei_backend.repository.*;
import ac.gov.br.sigei_backend.service.auth.JwtService;
import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Listagens: as rotas sem ano ({@code GET /registroAnual} e {@code GET /lotacao}),
 * que crescem a cada ano, são paginadas e filtradas no servidor; as listagens
 * por ano carregam os relacionamentos com um número fixo de consultas, e não
 * com uma por registro (N+1).
 */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ListagensHttpTest {

    // Anos fora do intervalo usado pelos outros testes, que compartilham o banco H2
    private static final int ANO = 1961;
    private static final int OUTRO_ANO = 1962;

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @Autowired EntityManager entityManager;
    @Autowired UsuarioRepository usuarios;
    @Autowired MunicipioRepository municipios;
    @Autowired TerraIndigenaRepository terras;
    @Autowired PovoRepository povos;
    @Autowired AldeiaRepository aldeias;
    @Autowired EscolaRepository escolas;
    @Autowired AnoReferenciaRepository anos;
    @Autowired RegistroAnualRepository registros;
    @Autowired ProfessorRepository professores;
    @Autowired LotacaoProfessorRepository lotacoes;

    private String token;
    private Municipio municipio;
    private Municipio outroMunicipio;
    private Povo povo;
    private AnoReferencia ano;
    private AnoReferencia outroAno;

    @BeforeEach
    void preparar() {
        Usuario admin = new Usuario();
        admin.setNome("Admin listagens");
        admin.setEmail("admin.listagens@sigei.com");
        admin.setSenha("hash-irrelevante");
        admin.setPerfil(PerfilUsuario.ADMIN);
        admin.setAtivo(true);
        admin.setAlterarSenha(false);
        token = "Bearer " + jwtService.gerarToken(usuarios.save(admin));

        municipio = municipio("Município listagens");
        outroMunicipio = municipio("Outro município listagens");
        povo = new Povo();
        povo.setNome("Povo listagens");
        povos.save(povo);
        ano = ano(ANO);
        outroAno = ano(OUTRO_ANO);
    }

    @Test
    void registrosSemAnoSaoPaginadosEFiltrados() throws Exception {
        RegistroAnual b = registro(escola("Escola B", municipio), ano);
        RegistroAnual a = registro(escola("Escola A", municipio), ano);
        RegistroAnual c = registro(escola("Escola C", municipio), ano);
        registro(escola("Escola D", outroMunicipio), ano);
        registro(escolas.findById(a.getEscola().getId()).orElseThrow(), outroAno);

        // Pelo nome da escola, dentro do ano
        listar("/registros-anuais?ano=" + ANO + "&municipioId=" + municipio.getId() + "&tamanho=2")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[*].id").value(contains(a.getId().intValue(), b.getId().intValue())))
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamanho").value(2))
                .andExpect(jsonPath("$.totalItens").value(3))
                .andExpect(jsonPath("$.totalPaginas").value(2));
        listar("/registros-anuais?ano=" + ANO + "&municipioId=" + municipio.getId() + "&tamanho=2&pagina=1")
                .andExpect(jsonPath("$.itens[*].id").value(contains(c.getId().intValue())));

        // Uma escola em todos os anos: do mais recente ao mais antigo
        listar("/registros-anuais?escolaId=" + a.getEscola().getId())
                .andExpect(jsonPath("$.itens[*].anoReferencia.ano").value(contains(OUTRO_ANO, ANO)));
    }

    @Test
    void lotacoesSemAnoSaoPaginadasNaOrdemDaTela() throws Exception {
        Escola escola = escola("Escola lotação", municipio);
        Professor professor = professor("Professor listagens");
        LotacaoProfessor vaga = lotacao(null, escola, ano);
        LotacaoProfessor naEscola = lotacao(professor, escola, ano);
        LotacaoProfessor noNucleo = lotacao(professor, null, ano);
        LotacaoProfessor anoSeguinte = lotacao(professor, escola, outroAno);

        // Núcleo antes das escolas; vaga sem professor no fim
        listar("/lotacoes?ano=" + ANO + "&municipioId=" + municipio.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itens[*].id").value(contains(
                        noNucleo.getId().intValue(), naEscola.getId().intValue(), vaga.getId().intValue())))
                .andExpect(jsonPath("$.totalItens").value(3));

        listar("/lotacoes?professorId=" + professor.getId() + "&tamanho=1")
                .andExpect(jsonPath("$.itens[*].id").value(contains(anoSeguinte.getId().intValue())))
                .andExpect(jsonPath("$.totalItens").value(3))
                .andExpect(jsonPath("$.totalPaginas").value(3));
    }

    @Test
    void paginacaoInvalidaResponde400() throws Exception {
        listar("/registros-anuais?tamanho=101")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("O tamanho da página deve estar entre 1 e 100."));
        listar("/lotacoes?pagina=-1")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("A página deve ser zero ou maior."));
    }

    @Test
    void listagensPorAnoNaoFazemUmaConsultaPorRegistro() throws Exception {
        Professor professor = professor("Professor N+1");
        for (int i = 0; i < 12; i++) {
            Escola escola = escola("Escola N+1 " + i, i % 2 == 0 ? municipio : outroMunicipio);
            RegistroAnual registro = registro(escola, ano);
            completar(registro);
            lotacao(i % 3 == 0 ? null : professor, escola, ano);
        }

        assertConsultasLimitadas("/anos/" + ANO + "/registros-anuais");
        assertConsultasLimitadas("/registros-anuais?ano=" + ANO);
        assertConsultasLimitadas("/anos/" + ANO + "/lotacoes");
        assertConsultasLimitadas("/lotacoes?ano=" + ANO);
        assertConsultasLimitadas("/escolas");
    }

    /** Com 12 registros, uma consulta por registro passaria de 12; o lote mantém poucas. */
    private void assertConsultasLimitadas(String rota) throws Exception {
        entityManager.flush();
        entityManager.clear();
        Statistics estatisticas = entityManager.getEntityManagerFactory().unwrap(SessionFactory.class).getStatistics();
        estatisticas.clear();

        listar(rota).andExpect(status().isOk());

        long consultas = estatisticas.getPrepareStatementCount();
        assertTrue(consultas <= 10, rota + " fez " + consultas + " consultas");
    }

    private ResultActions listar(String rota) throws Exception {
        return mvc.perform(get(rota).header(HttpHeaders.AUTHORIZATION, token));
    }

    private Municipio municipio(String nome) {
        Municipio novo = new Municipio();
        novo.setNome(nome);
        return municipios.save(novo);
    }

    private AnoReferencia ano(int valor) {
        AnoReferencia novo = new AnoReferencia();
        novo.setAno(valor);
        return anos.save(novo);
    }

    private Escola escola(String nome, Municipio municipioDaEscola) {
        TerraIndigena terra = new TerraIndigena();
        terra.setNome("Terra " + nome);
        terra.setMunicipio(municipioDaEscola);
        terras.save(terra);
        Aldeia aldeia = new Aldeia();
        aldeia.setNome("Aldeia " + nome);
        aldeia.setTerraIndigena(terra);
        aldeia.setPovo(povo);
        aldeias.save(aldeia);

        Escola escola = new Escola();
        escola.setCodigo("COD-" + nome);
        escola.setNome(nome);
        escola.setTipoUnidade("SEDE");
        escola.setAldeia(aldeia);
        return escolas.save(escola);
    }

    private RegistroAnual registro(Escola escola, AnoReferencia anoDoRegistro) {
        RegistroAnual registro = new RegistroAnual();
        registro.setEscola(escola);
        registro.setAnoReferencia(anoDoRegistro);
        return registros.save(registro);
    }

    /** Equipe, infraestrutura e matrículas, que a listagem também devolve. */
    private void completar(RegistroAnual registro) {
        EquipeEscola equipe = new EquipeEscola();
        equipe.setRegistroAnual(registro);
        equipe.setAnoReferencia(registro.getAnoReferencia());
        registro.setEquipe(equipe);

        InfraestruturaEscola infraestrutura = new InfraestruturaEscola();
        infraestrutura.setRegistroAnual(registro);
        infraestrutura.setNumSalas(2);
        registro.setInfraestrutura(infraestrutura);

        List<MatriculaNivel> niveis = new ArrayList<>();
        for (String nivel : List.of("INFANTIL", "TOTAL_GERAL")) {
            MatriculaNivel matricula = new MatriculaNivel();
            matricula.setRegistro(registro);
            matricula.setNivel(nivel);
            matricula.setTotal(10);
            niveis.add(matricula);
        }
        registro.getMatriculasNivel().addAll(niveis);
        registros.save(registro);
    }

    private Professor professor(String nome) {
        Professor professor = new Professor();
        professor.setNome(nome);
        return professores.save(professor);
    }

    private LotacaoProfessor lotacao(Professor professor, Escola escola, AnoReferencia anoDaLotacao) {
        LotacaoProfessor lotacao = new LotacaoProfessor();
        lotacao.setProfessor(professor);
        lotacao.setEscola(escola);
        lotacao.setMunicipio(municipio);
        lotacao.setAnoReferencia(anoDaLotacao);
        lotacao.setCargo("Professor");
        return lotacoes.save(lotacao);
    }
}
