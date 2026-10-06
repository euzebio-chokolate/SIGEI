package ac.gov.br.sigei_backend.service;

import ac.gov.br.sigei_backend.models.*;
import ac.gov.br.sigei_backend.repository.AnoReferenciaRepository;
import ac.gov.br.sigei_backend.repository.EscolaRepository;
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

    private Escola escola;

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
}
