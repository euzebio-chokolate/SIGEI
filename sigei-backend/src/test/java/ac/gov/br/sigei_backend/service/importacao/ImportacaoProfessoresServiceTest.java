package ac.gov.br.sigei_backend.service.importacao;

import ac.gov.br.sigei_backend.config.criptografia.CriptografiaDadosSensiveis;
import ac.gov.br.sigei_backend.config.security.AcessoDadosSensiveis;
import ac.gov.br.sigei_backend.dto.response.ImportacaoProfessoresResultadoDTO;
import ac.gov.br.sigei_backend.mappers.ProfessorMapper;
import ac.gov.br.sigei_backend.models.*;
import ac.gov.br.sigei_backend.repository.*;
import ac.gov.br.sigei_backend.service.ProfessorService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@Import({ImportacaoProfessoresService.class, ProfessorService.class, CriptografiaDadosSensiveis.class,
        AcessoDadosSensiveis.class, ImportacaoProfessoresServiceTest.MappersConfig.class})
class ImportacaoProfessoresServiceTest {

    @TestConfiguration
    @ComponentScan(basePackageClasses = ProfessorMapper.class)
    static class MappersConfig {}

    private static final String[] CABECALHO = {
            "ORD", "MUNICIPIO", "LOTACAO", "POVO", "TERRA INDÍGENA ", "ALDEIA", "MATRICULA", "CONT", "NOME",
            "ETNIA DO PROFESSOR", "SEXO", "CARGO_ATUAL", "FORMAÇÃO PROFESSOR", "SEGMENTO OFERTADO", "CPF",
            "INICIO DO CONTRATO", "AMPARO LEGAL", "TIPO DE CONTRATO"
    };

    @Autowired ImportacaoProfessoresService importacao;
    @Autowired TestEntityManager entityManager;
    @Autowired ProfessorRepository professores;
    @Autowired LotacaoProfessorRepository lotacoes;
    @Autowired AnoReferenciaRepository anos;

    private Municipio feijo;
    private Escola sede;
    private Escola anexoI;
    private Escola anexoII;
    private Escola xinaveda;

    private final List<Object[]> linhas = new ArrayList<>();

    @BeforeEach
    void preparar() {
        feijo = municipio("Feijó");
        municipio("Tarauacá");

        TerraIndigena terra = new TerraIndigena();
        terra.setNome("Jaminawa do Alto Rio Envira");
        terra.setMunicipio(feijo);
        entityManager.persist(terra);

        sede = escola("ESC IND ALTO BONITO II ASHANINKA", "SEDE", aldeia("ALTO BONITO", terra));
        anexoI = escola("ESC IND ALTO BONITO II ASHANINKA - ANEXO I", "ANEXO", aldeia("FORMIGUINHA", terra));
        anexoII = escola("ESC IND ALTO BONITO II ASHANINKA - ANEXO II", "ANEXO", aldeia("RURNAI", terra));
        // Grafia diferente da planilha ("XINAWEDA"): identificada pela aldeia
        xinaveda = escola("ESC IND XINAVEDA", "SEDE", aldeia("VIDA NOVA", terra));

        AnoReferencia ano2026 = new AnoReferencia();
        ano2026.setAno(2026);
        entityManager.persist(ano2026);
        entityManager.flush();
    }

    @Test
    void deveImportarProfessoresLotacoesVagasENucleo() throws IOException {
        linha("FEIJO", "ESC. IND. ALTO BONITO II ASHANINKA", "ALTO BONITO", "MARIA DA SILVA ASHANINKA",
                "ASHANINKA", "F", "1º AO 5º ANO", 45566.0, "DOE 13864", "PSS");
        // Mesmo professor em outro segmento: um cadastro, duas lotações
        linha("FEIJO", "ESC. IND. ALTO BONITO II ASHANINKA", "ALTO BONITO", "Maria da Silva Ashaninka",
                "ASHANINKA", "F", "6º AO 9º ANO", 45566.0, "PARECER", "CONTRATO DIRETO");
        // Anexo sem número na planilha: a aldeia decide entre o anexo I e o II
        linha("FEIJO", "ESC. IND. ALTO BONITO II ASHANINKA - ANEXO", "RURNAI", "JOÃO KAXINAWA",
                "HUNI KUI", "M", "1º AO 5º ANO", null, "EFETIVO", "EFETIVO");
        linha("FEIJO", "ESC. IND. XINAWEDA", "VIDA NOVA", "NECESSIDADE", "-", "-", "1º AO 5º ANO",
                null, "-", "NECESSIDADE");
        linha("FEIJO", "NEE SEE", "NEE SEE", "ANA NÃO INDÍGENA", "NÃO INDÍGENA", "F", "NEE SEE",
                46099.0, "PARECER", "CONTRATO DIRETO");
        linha("FEIJO", "ESC. IND. INEXISTENTE", "ALDEIA NENHUMA", "PEDRO", "HUNI KUI", "M", "1º ANO",
                null, null, "PSS");
        linha("CIDADE DESCONHECIDA", "ESC. IND. QUALQUER", null, "PAULO", "HUNI KUI", "M", "1º ANO",
                null, null, "PSS");

        ImportacaoProfessoresResultadoDTO resultado = importar();

        assertEquals(7, resultado.linhasLidas());
        assertEquals(3, resultado.professoresCriados());
        assertEquals(5, resultado.lotacoesCriadas());
        assertEquals(1, resultado.vagasEmAberto());
        assertEquals(1, resultado.lotacoesNoNucleo());
        assertEquals(2, resultado.linhasIgnoradas());
        assertTrue(resultado.erros().get(0).startsWith("Linha 7: Escola não identificada em Feijó"));
        assertEquals("Linha 8: Município não cadastrado: CIDADE DESCONHECIDA", resultado.erros().get(1));

        List<LotacaoProfessor> gravadas = lotacoes.buscarPorAno(2026);

        LotacaoProfessor primeira = lotacaoDe(gravadas, "MARIA DA SILVA ASHANINKA", "1º AO 5º ANO");
        assertEquals(sede.getId(), primeira.getEscola().getId());
        assertEquals(feijo.getId(), primeira.getMunicipio().getId());
        assertEquals("PSS", primeira.getTipoContrato());
        assertEquals(LocalDate.of(2024, 10, 1), primeira.getInicioContrato());
        assertEquals("F", primeira.getProfessor().getSexo());
        assertEquals("ASHANINKA", primeira.getProfessor().getEtnia());
        assertEquals("CONTRATO_DIRETO",
                lotacaoDe(gravadas, "MARIA DA SILVA ASHANINKA", "6º AO 9º ANO").getTipoContrato());

        assertEquals(anexoII.getId(), lotacaoDe(gravadas, "JOÃO KAXINAWA", "1º AO 5º ANO").getEscola().getId());

        LotacaoProfessor vaga = gravadas.stream().filter(l -> l.getProfessor() == null).findFirst().orElseThrow();
        assertEquals(xinaveda.getId(), vaga.getEscola().getId());
        assertNull(vaga.getTipoContrato());

        LotacaoProfessor nucleo = lotacaoDe(gravadas, "ANA NÃO INDÍGENA", "NEE SEE");
        assertNull(nucleo.getEscola());
        assertEquals(feijo.getId(), nucleo.getMunicipio().getId());
    }

    @Test
    void reimportacaoNaoDeveDuplicarNemApagarDadosPreenchidosNoSistema() throws IOException {
        linha("FEIJO", "ESC. IND. ALTO BONITO II ASHANINKA", "ALTO BONITO", "MARIA DA SILVA ASHANINKA",
                "ASHANINKA", "F", "1º AO 5º ANO", 45566.0, "DOE 13864", "PSS");
        importar();

        // O administrador completa os dados sensíveis que vieram em branco
        Professor maria = professores.findAll().getFirst();
        maria.setCpf("52998224725");
        maria.setMatricula("998877");
        LotacaoProfessor lotacao = lotacoes.findAll().getFirst();
        lotacao.setNumeroContrato("CT-2026-001");
        entityManager.flush();

        ImportacaoProfessoresResultadoDTO resultado = importar();

        assertEquals(0, resultado.professoresCriados());
        assertEquals(1, resultado.professoresAtualizados());
        assertEquals(0, resultado.lotacoesCriadas());
        assertEquals(1, resultado.lotacoesAtualizadas());
        assertEquals(1, professores.count());
        assertEquals(1, lotacoes.count());

        entityManager.flush();
        entityManager.clear();

        Professor recarregada = professores.findAll().getFirst();
        assertEquals("52998224725", recarregada.getCpf());
        assertEquals("998877", recarregada.getMatricula());
        assertEquals("CT-2026-001", lotacoes.findAll().getFirst().getNumeroContrato());
    }

    @Test
    void cpfDaPlanilhaDeveSerValidadoEIndexado() throws IOException {
        linha("FEIJO", "ESC. IND. ALTO BONITO II ASHANINKA", "ALTO BONITO", "MARIA", "ASHANINKA", "F",
                "1º AO 5º ANO", null, null, "PSS");
        linhas.getLast()[14] = 52998224725.0; // CPF gravado como número
        linha("FEIJO", "ESC. IND. ALTO BONITO II ASHANINKA", "ALTO BONITO", "JOSÉ", "ASHANINKA", "M",
                "6º AO 9º ANO", null, null, "PSS");
        linhas.getLast()[14] = "123.456.789-00";

        ImportacaoProfessoresResultadoDTO resultado = importar();

        assertEquals(1, resultado.professoresCriados());
        assertEquals(List.of("Linha 3: CPF inválido."), resultado.erros());

        Professor maria = professores.findAll().getFirst();
        assertEquals("52998224725", maria.getCpf());
        assertEquals(64, maria.getCpfHash().length());
    }

    @Nested
    class Validacoes {

        @Test
        void anoNaoIniciadoDeveImpedirAImportacao() {
            anos.findByAno(2026).ifPresent(ano -> {
                entityManager.remove(ano);
                entityManager.flush();
            });

            IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> importar());
            assertTrue(erro.getMessage().startsWith("O ano 2026 ainda não foi iniciado"));
        }

        @Test
        void planilhaForaDoLayoutDeveSerRecusada() throws IOException {
            cabecalhoAlterado = true;

            IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> importar());
            assertTrue(erro.getMessage().startsWith("A planilha não está no layout do quadro de professores"));
        }

        @Test
        void arquivoQueNaoEhXlsxDeveSerRecusado() {
            MockMultipartFile arquivo = new MockMultipartFile("arquivo", "quadro.csv", null, new byte[]{1});

            IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                    () -> importacao.importar(arquivo, 2026));
            assertEquals("O arquivo deve estar no formato .xlsx.", erro.getMessage());
        }
    }

    private boolean cabecalhoAlterado = false;

    private ImportacaoProfessoresResultadoDTO importar() throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            Sheet planilha = workbook.createSheet("Planilha1");

            Row cabecalho = planilha.createRow(0);
            for (int coluna = 0; coluna < CABECALHO.length; coluna++) {
                cabecalho.createCell(coluna).setCellValue(cabecalhoAlterado && coluna == 8 ? "PROFESSOR" : CABECALHO[coluna]);
            }

            for (int indice = 0; indice < linhas.size(); indice++) {
                Row linha = planilha.createRow(indice + 1);
                Object[] valores = linhas.get(indice);

                for (int coluna = 0; coluna < valores.length; coluna++) {
                    if (valores[coluna] instanceof Double numero) {
                        linha.createCell(coluna).setCellValue(numero);
                    } else if (valores[coluna] != null) {
                        linha.createCell(coluna).setCellValue(valores[coluna].toString());
                    }
                }
            }

            workbook.write(saida);
            return importacao.importar(new MockMultipartFile("arquivo", "quadro.xlsx", null, saida.toByteArray()), 2026);
        }
    }

    private void linha(String municipio, String lotacao, String aldeia, String nome, String etnia, String sexo,
                       String segmento, Double inicioContrato, String amparo, String tipoContrato) {
        Object[] valores = new Object[18];
        valores[0] = (double) (linhas.size() + 1);
        valores[1] = municipio;
        valores[2] = lotacao;
        valores[5] = aldeia;
        valores[8] = nome;
        valores[9] = etnia;
        valores[10] = sexo;
        valores[11] = "PROFESSOR PS-1 25H";
        valores[12] = "ENSINO MÉDIO";
        valores[13] = segmento;
        valores[15] = inicioContrato;
        valores[16] = amparo;
        valores[17] = tipoContrato;
        linhas.add(valores);
    }

    private static LotacaoProfessor lotacaoDe(List<LotacaoProfessor> gravadas, String professor, String segmento) {
        return gravadas.stream()
                .filter(l -> l.getProfessor() != null && l.getProfessor().getNome().equalsIgnoreCase(professor))
                .filter(l -> segmento.equals(l.getSegmento()))
                .findFirst()
                .orElseThrow();
    }

    private Municipio municipio(String nome) {
        Municipio municipio = new Municipio();
        municipio.setNome(nome);
        return entityManager.persist(municipio);
    }

    private Aldeia aldeia(String nome, TerraIndigena terra) {
        Povo povo = new Povo();
        povo.setNome("Povo " + nome);
        entityManager.persist(povo);

        Aldeia aldeia = new Aldeia();
        aldeia.setNome(nome);
        aldeia.setTerraIndigena(terra);
        aldeia.setPovo(povo);
        return entityManager.persist(aldeia);
    }

    private Escola escola(String nome, String tipo, Aldeia aldeia) {
        Escola escola = new Escola();
        escola.setCodigo("COD-" + nome.hashCode());
        escola.setNome(nome);
        escola.setTipoUnidade(tipo);
        escola.setAldeia(aldeia);
        return entityManager.persist(escola);
    }
}
