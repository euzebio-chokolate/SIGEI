package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.models.*;
import ac.gov.br.sigei_backend.repository.AnoReferenciaRepository;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
import ac.gov.br.sigei_backend.repository.LotacaoProfessorRepository;
import ac.gov.br.sigei_backend.repository.RegistroAnualRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@Import(AtualizacaoAnualService.class)
class AtualizacaoAnualServiceTest {

    @Autowired AtualizacaoAnualService service;
    @Autowired TestEntityManager entityManager;
    @Autowired EscolaRepository escolas;
    @Autowired AnoReferenciaRepository anos;
    @Autowired RegistroAnualRepository registros;
    @Autowired LotacaoProfessorRepository lotacoes;

    private Escola escola;
    private Professor professor;
    private Municipio municipio;

    @BeforeEach
    void preparar() {
        AnoReferencia ano2025 = new AnoReferencia();
        ano2025.setAno(2025);
        anos.save(ano2025);

        escola = new Escola();
        escola.setCodigo("COD-ANO");
        escola.setNome("Escola virada");
        escola.setTipoUnidade("SEDE");
        escolas.save(escola);

        RegistroAnual registro = new RegistroAnual();
        registro.setEscola(escola);
        registro.setAnoReferencia(ano2025);
        registro.setRede("Estadual");
        registro.setLocalFuncionamento("Prédio próprio");
        registro.setTotalAlunos(42);

        EquipeEscola equipe = new EquipeEscola();
        equipe.setRegistroAnual(registro);
        equipe.setAnoReferencia(ano2025);
        equipe.setTemMerendeira(true);
        registro.setEquipe(equipe);

        InfraestruturaEscola infraestrutura = new InfraestruturaEscola();
        infraestrutura.setRegistroAnual(registro);
        infraestrutura.setNumSalas(3);
        infraestrutura.setFotoUrl("https://exemplo/foto.jpg");
        infraestrutura.setFotoPublicId("sigei/infraestrutura/foto");
        registro.setInfraestrutura(infraestrutura);

        MatriculaNivel nivel = new MatriculaNivel();
        nivel.setRegistro(registro);
        nivel.setNivel("TOTAL_GERAL");
        nivel.setQtdMasculino(20);
        nivel.setQtdFeminino(22);
        nivel.setTotal(42);
        registro.getMatriculasNivel().add(nivel);

        MatriculaSerie serie = new MatriculaSerie();
        serie.setRegistro(registro);
        serie.setDenominacao("1º ano");
        serie.setEtapaEnsino("FUNDAMENTAL");
        serie.setQtdMasculino(4);
        serie.setQtdFeminino(5);
        serie.setTotal(9);
        registro.getMatriculasSerie().add(serie);

        registros.save(registro);

        municipio = new Municipio();
        municipio.setNome("Municipio virada");
        entityManager.persist(municipio);

        professor = new Professor();
        professor.setNome("Professor virada");
        entityManager.persist(professor);

        LotacaoProfessor lotacao = new LotacaoProfessor();
        lotacao.setAnoReferencia(ano2025);
        lotacao.setProfessor(professor);
        lotacao.setEscola(escola);
        lotacao.setMunicipio(municipio);
        lotacao.setCargo("PROFESSOR PS-1 25H");
        lotacao.setSegmento("1º AO 5º ANO");
        lotacao.setNumeroContrato("CT-123");
        lotacao.setInicioContrato(java.time.LocalDate.of(2025, 2, 10));
        lotacao.setAmparoLegal("DOE 13864");
        lotacao.setTipoContrato("PSS");
        lotacoes.save(lotacao);

        // Vaga em aberto no núcleo do município
        LotacaoProfessor vaga = new LotacaoProfessor();
        vaga.setAnoReferencia(ano2025);
        vaga.setMunicipio(municipio);
        vaga.setSegmento("6º AO 9º ANO");
        lotacoes.save(vaga);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void novoAnoDeveCopiarTodosOsCamposDoRegistroMaisRecente() {
        service.iniciarNovoAno();
        entityManager.flush();
        entityManager.clear();

        RegistroAnual novo = registros.findByEscolaAndAnoReferencia_Ano(escola, 2026).orElseThrow();

        assertEquals("Estadual", novo.getRede());
        assertEquals("Prédio próprio", novo.getLocalFuncionamento());
        assertEquals(42, novo.getTotalAlunos());

        assertTrue(novo.getEquipe().isTemMerendeira());
        assertEquals(2026, novo.getEquipe().getAnoReferencia().getAno());

        assertEquals(3, novo.getInfraestrutura().getNumSalas());
        assertEquals("https://exemplo/foto.jpg", novo.getInfraestrutura().getFotoUrl());
        // A imagem é compartilhada; o ID no Cloudinary fica só no registro que fez o upload
        assertNull(novo.getInfraestrutura().getFotoPublicId());

        MatriculaNivel nivel = novo.getMatriculasNivel().getFirst();
        assertEquals(42, nivel.getTotal());

        MatriculaSerie serie = novo.getMatriculasSerie().getFirst();
        assertEquals(4, serie.getQtdMasculino());
        assertEquals(5, serie.getQtdFeminino());
        assertEquals(9, serie.getTotal());

        // O registro de origem não é alterado
        assertEquals(42, registros.findByEscolaAndAnoReferencia_Ano(escola, 2025).orElseThrow().getTotalAlunos());
    }

    @Test
    void novoAnoDeveCopiarAsLotacoesDoUltimoAno() {
        service.iniciarNovoAno();
        entityManager.flush();
        entityManager.clear();

        java.util.List<LotacaoProfessor> copiadas = lotacoes.buscarPorAno(2026);
        assertEquals(2, copiadas.size());

        LotacaoProfessor lotacao = copiadas.stream()
                .filter(item -> item.getProfessor() != null)
                .findFirst()
                .orElseThrow();

        assertEquals(professor.getId(), lotacao.getProfessor().getId());
        assertEquals(escola.getId(), lotacao.getEscola().getId());
        assertEquals(municipio.getId(), lotacao.getMunicipio().getId());
        assertEquals("PROFESSOR PS-1 25H", lotacao.getCargo());
        assertEquals("1º AO 5º ANO", lotacao.getSegmento());
        assertEquals("CT-123", lotacao.getNumeroContrato());
        assertEquals(java.time.LocalDate.of(2025, 2, 10), lotacao.getInicioContrato());
        assertEquals("DOE 13864", lotacao.getAmparoLegal());
        assertEquals("PSS", lotacao.getTipoContrato());

        LotacaoProfessor vaga = copiadas.stream()
                .filter(item -> item.getProfessor() == null)
                .findFirst()
                .orElseThrow();

        assertNull(vaga.getEscola());
        assertEquals("6º AO 9º ANO", vaga.getSegmento());

        // As lotações de origem continuam no ano anterior
        assertEquals(2, lotacoes.buscarPorAno(2025).size());
    }
}
