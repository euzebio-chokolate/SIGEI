package ac.gov.br.sigei_backend.service.relatorio;

import ac.gov.br.sigei_backend.models.*;
import ac.gov.br.sigei_backend.service.relatorio.Relatorio.Indicador;
import ac.gov.br.sigei_backend.service.relatorio.Relatorio.Secao;
import ac.gov.br.sigei_backend.service.relatorio.RelatorioService.Filtro;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@Import({RelatorioService.class, GeradorPlanilhaRelatorio.class, GeradorPdfRelatorio.class})
class RelatorioServiceTest {

    private static final int ANO = 2026;
    private static final String CPF = "52998224725";
    private static final String MATRICULA = "MAT-998877";

    @Autowired RelatorioService relatorios;
    @Autowired TestEntityManager entityManager;

    private Municipio feijo;
    private Municipio tarauaca;

    @BeforeEach
    void preparar() {
        AnoReferencia ano = new AnoReferencia();
        ano.setAno(ANO);
        entityManager.persist(ano);

        feijo = municipio("Feijó");
        tarauaca = municipio("Tarauacá");

        Escola sede = escola("ESC IND ALTO BONITO", "SEDE", null, aldeia("ALTO BONITO", feijo, "Ashaninka"));
        Escola anexo = escola("ESC IND ALTO BONITO - COQUEIRO", "ANEXO", sede, aldeia("COQUEIRO", feijo, "Ashaninka"));
        Escola mutum = escola("ESC IND MUTUM", "SEDE", null, aldeia("MUTUM", tarauaca, "Yawanawá"));

        RegistroAnual registroSede = registro(sede, ano, 30);
        registro(anexo, ano, 12);
        RegistroAnual registroMutum = registro(mutum, ano, 20);

        infraestrutura(registroSede, "FUNCIONANDO", true, "SOLAR", true);
        infraestrutura(registroMutum, "PARALISADA", false, "NAO", false);

        nivel(registroSede, "TOTAL_GERAL", 30, 16, 14);
        nivel(registroSede, "ENSINO_INFANTIL", 10, null, null);
        nivel(registroSede, "FUNDAMENTAL_ANOS_INICIAIS", 20, null, null);
        nivel(registroMutum, "TOTAL_GERAL", 20, 9, 11);
        nivel(registroMutum, "FUNDAMENTAL_ANOS_INICIAIS", 20, null, null);
        serie(registroSede, "1º ANO", 12);
        serie(registroMutum, "1º ANO", 8);
        serie(registroSede, "2º ANO", 8);

        Professor professor = new Professor();
        professor.setNome("Maria Ashaninka");
        professor.setEtnia("ASHANINKA");
        professor.setSexo("F");
        professor.setCpf(CPF);
        professor.setCpfHash("hash-de-teste");
        professor.setMatricula(MATRICULA);
        entityManager.persist(professor);

        lotacao(ano, professor, sede, feijo, "EFETIVO");
        lotacao(ano, professor, null, feijo, "PSS");
        lotacao(ano, null, mutum, tarauaca, "PSS");

        entityManager.flush();
    }

    @Nested
    class Conteudo {

        @Test
        void painelDeveConsolidarOsIndicadoresDoAno() {
            Relatorio painel = relatorios.montar(TipoRelatorio.PAINEL, new Filtro(ANO, null));

            Secao resumo = secao(painel, "Resumo");
            assertEquals(3L, valor(resumo, "Escolas"));
            assertEquals(1L, valor(resumo, "Anexos"));
            assertEquals(62L, valor(resumo, "Matrículas"));
            assertEquals(2L, valor(resumo, "Municípios"));
            assertEquals(1L, valor(resumo, "Professores lotados"));
            assertEquals(3L, valor(resumo, "Lotações"));
            assertEquals(1L, valor(resumo, "Vagas em aberto"));

            // Infraestrutura: 2 escolas avaliadas, uma com internet, energia e prédio
            List<List<Object>> infra = secao(painel, "Infraestrutura").tabela().linhas();
            assertEquals(List.of("Com internet", 1L, 50.0), infra.get(0));
            assertEquals(List.of("Com energia elétrica", 1L, 50.0), infra.get(1));

            List<Object> totalNiveis = secao(painel, "Matrículas por nível").tabela().total();
            assertEquals(50L, totalNiveis.get(1));
        }

        @Test
        void filtroPorMunicipioDeveRestringirTodasAsSecoes() {
            Relatorio painel = relatorios.montar(TipoRelatorio.PAINEL, new Filtro(ANO, tarauaca));

            Secao resumo = secao(painel, "Resumo");
            assertEquals(1L, valor(resumo, "Escolas"));
            assertEquals(20L, valor(resumo, "Matrículas"));
            assertEquals(1L, valor(resumo, "Vagas em aberto"));
            assertTrue(painel.filtros().contains("Município: Tarauacá"));

            List<List<Object>> evolucao = secao(painel, "Evolução histórica").tabela().linhas();
            assertEquals(List.of("2026", 1L, 20L), evolucao.getFirst());
        }

        @Test
        void matriculasDevemSomarPorEscolaMunicipioESerie() {
            Relatorio matriculas = relatorios.montar(TipoRelatorio.MATRICULAS, new Filtro(ANO, null));

            List<Object> total = secao(matriculas, "Por escola").tabela().total();
            // Infantil, anos iniciais, ..., total, masculino, feminino. O anexo sem
            // níveis entra pelo total de alunos do registro.
            assertEquals(Arrays.asList("Total", null, null, 10L, 40L, 0L, 0L, 0L, 0L, 62L, 25L, 25L), total);

            List<List<Object>> series = secao(matriculas, "Por série (sem EJA e AEE)").tabela().linhas();
            assertEquals("1º ANO", series.get(0).get(1));
            assertEquals(20L, series.get(0).get(2));
        }

        @Test
        void professoresDevemTrazerVagasENucleo() {
            Relatorio professores = relatorios.montar(TipoRelatorio.PROFESSORES, new Filtro(ANO, null));

            List<List<Object>> lotacoes = secao(professores, "Lotações").tabela().linhas();
            assertEquals(3, lotacoes.size());
            assertTrue(lotacoes.stream().anyMatch(l -> "Núcleo de educação".equals(l.get(1))));
            assertTrue(lotacoes.stream().anyMatch(l -> "Vaga em aberto".equals(l.get(2))));
            assertEquals(1L, valor(secao(professores, "Resumo"), "Lotações no núcleo"));
        }
    }

    @Nested
    class Arquivos {

        @Test
        void planilhaDeveTerResumoUmaAbaPorTabelaESemDadosSensiveis() throws IOException {
            ArquivoRelatorio arquivo = relatorios.gerar(TipoRelatorio.PROFESSORES, FormatoRelatorio.XLSX, ANO, null);

            assertEquals("sigei-professores-2026.xlsx", arquivo.nome());

            try (Workbook planilha = WorkbookFactory.create(new ByteArrayInputStream(arquivo.conteudo()))) {
                assertEquals("Resumo", planilha.getSheetName(0));
                assertNotNull(planilha.getSheet("Lotações"));
                assertNotNull(planilha.getSheet("Por município"));

                String conteudo = textoDa(planilha);
                assertTrue(conteudo.contains("Maria Ashaninka"));
                assertFalse(conteudo.contains(CPF));
                assertFalse(conteudo.contains(MATRICULA));
            }
        }

        @Test
        void pdfDeveSerGeradoComTituloEFiltroESemDadosSensiveis() throws IOException {
            ArquivoRelatorio arquivo = relatorios.gerar(TipoRelatorio.PROFESSORES, FormatoRelatorio.PDF, ANO, feijo.getId());

            assertEquals("sigei-professores-2026-feijo.pdf", arquivo.nome());
            assertEquals("application/pdf", arquivo.tipoMidia());

            PdfReader leitor = new PdfReader(arquivo.conteudo());
            PdfTextExtractor extrator = new PdfTextExtractor(leitor);
            StringBuilder texto = new StringBuilder();
            for (int pagina = 1; pagina <= leitor.getNumberOfPages(); pagina++) {
                texto.append(extrator.getTextFromPage(pagina));
            }
            leitor.close();

            assertTrue(texto.toString().contains("Professores e lotações"));
            assertTrue(texto.toString().contains("Município: Feijó"));
            assertFalse(texto.toString().contains(CPF));
            assertFalse(texto.toString().contains(MATRICULA));
        }

        @Test
        void todosOsRelatoriosDevemSerGeradosNosDoisFormatos() {
            for (TipoRelatorio tipo : TipoRelatorio.values()) {
                for (FormatoRelatorio formato : FormatoRelatorio.values()) {
                    ArquivoRelatorio arquivo = relatorios.gerar(tipo, formato, null, null);
                    assertTrue(arquivo.conteudo().length > 0, tipo + " " + formato);
                    assertTrue(arquivo.nome().endsWith("-2026." + formato.extensao()));
                }
            }
        }

        @Test
        void anoOuMunicipioInexistenteDevemResultarEmNaoEncontrado() {
            assertThrows(NoSuchElementException.class,
                    () -> relatorios.gerar(TipoRelatorio.PAINEL, FormatoRelatorio.PDF, 1999, null));
            assertThrows(NoSuchElementException.class,
                    () -> relatorios.gerar(TipoRelatorio.PAINEL, FormatoRelatorio.PDF, ANO, 999_999L));
        }
    }

    @Test
    void tipoEFormatoDesconhecidosDevemSerRecusados() {
        assertThrows(NoSuchElementException.class, () -> TipoRelatorio.daChave("financeiro"));
        assertThrows(IllegalArgumentException.class, () -> FormatoRelatorio.daExtensao("docx"));
        assertEquals(TipoRelatorio.MATRICULAS, TipoRelatorio.daChave("Matriculas"));
    }

    private static Secao secao(Relatorio relatorio, String titulo) {
        return relatorio.secoes().stream().filter(s -> s.titulo().equals(titulo)).findFirst().orElseThrow();
    }

    private static long valor(Secao secao, String rotulo) {
        Object valor = secao.indicadores().stream()
                .filter(i -> i.rotulo().equals(rotulo))
                .map(Indicador::valor)
                .findFirst()
                .orElseThrow();
        return ((Number) valor).longValue();
    }

    private static String textoDa(Workbook planilha) {
        DataFormatter formatador = new DataFormatter();
        StringBuilder texto = new StringBuilder();

        for (Sheet aba : planilha) {
            for (Row linha : aba) {
                for (Cell celula : linha) {
                    texto.append(formatador.formatCellValue(celula)).append('|');
                }
            }
        }

        return texto.toString();
    }

    private Municipio municipio(String nome) {
        Municipio municipio = new Municipio();
        municipio.setNome(nome);
        return entityManager.persist(municipio);
    }

    private Aldeia aldeia(String nome, Municipio municipio, String nomePovo) {
        TerraIndigena terra = new TerraIndigena();
        terra.setNome("TI " + nome);
        terra.setMunicipio(municipio);
        entityManager.persist(terra);

        Povo povo = new Povo();
        povo.setNome(nomePovo + " " + nome);
        entityManager.persist(povo);

        Aldeia aldeia = new Aldeia();
        aldeia.setNome(nome);
        aldeia.setTerraIndigena(terra);
        aldeia.setPovo(povo);
        return entityManager.persist(aldeia);
    }

    private Escola escola(String nome, String tipo, Escola sede, Aldeia aldeia) {
        Escola escola = new Escola();
        escola.setCodigo("120" + Math.abs(nome.hashCode() % 100000));
        escola.setNome(nome);
        escola.setTipoUnidade(tipo);
        escola.setEscolaSede(sede);
        escola.setAldeia(aldeia);
        return entityManager.persist(escola);
    }

    private RegistroAnual registro(Escola escola, AnoReferencia ano, int alunos) {
        RegistroAnual registro = new RegistroAnual();
        registro.setEscola(escola);
        registro.setAnoReferencia(ano);
        registro.setTotalAlunos(alunos);
        return entityManager.persist(registro);
    }

    private void infraestrutura(RegistroAnual registro, String situacao, boolean internet, String energia, boolean predio) {
        InfraestruturaEscola infraestrutura = new InfraestruturaEscola();
        infraestrutura.setRegistroAnual(registro);
        infraestrutura.setSituacao(situacao);
        infraestrutura.setTemInternet(internet);
        infraestrutura.setTipoEnergia(energia);
        infraestrutura.setTemPredio(predio);
        entityManager.persist(infraestrutura);
    }

    private void nivel(RegistroAnual registro, String nivel, int total, Integer masculino, Integer feminino) {
        MatriculaNivel matricula = new MatriculaNivel();
        matricula.setRegistro(registro);
        matricula.setNivel(nivel);
        matricula.setTotal(total);
        matricula.setQtdMasculino(masculino);
        matricula.setQtdFeminino(feminino);
        entityManager.persist(matricula);
    }

    private void serie(RegistroAnual registro, String denominacao, int total) {
        MatriculaSerie serie = new MatriculaSerie();
        serie.setRegistro(registro);
        serie.setDenominacao(denominacao);
        serie.setEtapaEnsino("FUNDAMENTAL_ANOS_INICIAIS");
        serie.setTotal(total);
        entityManager.persist(serie);
    }

    private void lotacao(AnoReferencia ano, Professor professor, Escola escola, Municipio municipio, String tipoContrato) {
        LotacaoProfessor lotacao = new LotacaoProfessor();
        lotacao.setAnoReferencia(ano);
        lotacao.setProfessor(professor);
        lotacao.setEscola(escola);
        lotacao.setMunicipio(municipio);
        lotacao.setCargo("PROFESSOR PS-1");
        lotacao.setSegmento("1º AO 5º ANO");
        lotacao.setTipoContrato(tipoContrato);
        lotacao.setInicioContrato(LocalDate.of(2026, 2, 1));
        entityManager.persist(lotacao);
    }
}
