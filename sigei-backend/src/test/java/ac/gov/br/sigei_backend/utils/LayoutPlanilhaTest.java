package ac.gov.br.sigei_backend.utils;

import ac.gov.br.sigei_backend.utils.LayoutPlanilha.PlanilhaLocalizada;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LayoutPlanilhaTest {

    enum Coluna implements ColunaPlanilha {
        CODIGO("CÓDIGO", true, "INEP"),
        ESCOLA("NOME DA ESCOLA", true, "ESCOLA"),
        ALUNOS("Nº DE ALUNOS", false),
        INFANTIL("ENSINO INFANTIL", false),
        PRIMEIRO_ANO("1º ANO", false),
        OBSERVACAO("OBSERVAÇÃO", false);

        private final String titulo;
        private final boolean obrigatoria;
        private final List<String> sinonimos;

        Coluna(String titulo, boolean obrigatoria, String... sinonimos) {
            this.titulo = titulo;
            this.obrigatoria = obrigatoria;
            this.sinonimos = List.of(sinonimos);
        }

        @Override public String titulo() { return titulo; }
        @Override public boolean obrigatoria() { return obrigatoria; }
        @Override public String descricao() { return "Descrição de " + titulo; }
        @Override public List<String> sinonimos() { return sinonimos; }
    }

    private final LayoutPlanilha<Coluna> layout =
            new LayoutPlanilha<>(Coluna.class, "Teste", "de teste", List.of("Instrução de teste."));

    @Test
    void deveLocalizarColunasPeloTituloEmQualquerOrdemEAba() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            linhas(workbook.createSheet("Resumo"), new String[]{"TOTAL GERAL", "10"});
            linhas(workbook.createSheet("Qualquer nome"),
                    new String[]{"Título da planilha"},
                    new String[]{"escola", "Observação", "Código"},
                    new String[]{"ESC IND A", "", "123"});

            PlanilhaLocalizada<Coluna> planilha = layout.localizar(workbook);

            assertEquals("Qualquer nome", planilha.aba().getSheetName());
            assertEquals(2, planilha.coluna(Coluna.CODIGO));
            assertEquals(0, planilha.coluna(Coluna.ESCOLA));
            assertEquals(1, planilha.coluna(Coluna.OBSERVACAO));
            assertEquals(-1, planilha.coluna(Coluna.ALUNOS));
            assertFalse(planilha.possui(Coluna.ALUNOS));
            assertEquals(2, planilha.primeiraLinhaDados());
            assertEquals(1, planilha.linhas().size());
        }
    }

    @Test
    void deveAceitarTituloComPalavrasAMaisDepoisDeTentarOsExatos() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            linhas(workbook.createSheet("Dados"),
                    new String[]{"CODIGO INEP DA UNIDADE", "NOME DA ESCOLA", "Nº DE ALUNOS 2025", "INEP"});

            PlanilhaLocalizada<Coluna> planilha = layout.localizar(workbook);

            // "INEP" exato tem prioridade sobre "CODIGO INEP DA UNIDADE", que só começa por "CODIGO"
            assertEquals(3, planilha.coluna(Coluna.CODIGO));
            assertEquals(2, planilha.coluna(Coluna.ALUNOS));
        }
    }

    @Test
    void deveLerCabecalhoEmDuasLinhasComGrupoMesclado() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            linhas(workbook.createSheet("Censo"),
                    new String[]{"Código", "Nome da Escola", "Matrículas", ""},
                    new String[]{"", "", "Ensino Infantil", "1º Ano"},
                    new String[]{"123", "ESC IND A", "4", "5"});

            PlanilhaLocalizada<Coluna> planilha = layout.localizar(workbook);

            assertEquals(2, planilha.coluna(Coluna.INFANTIL));
            assertEquals(3, planilha.coluna(Coluna.PRIMEIRO_ANO));
            assertEquals(2, planilha.primeiraLinhaDados());
        }
    }

    @Test
    void cabecalhoDeUmaLinhaNaoDeveTratarAPrimeiraLinhaDeDadosComoSubcabecalho() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            linhas(workbook.createSheet("Dados"),
                    new String[]{"CÓDIGO", "NOME DA ESCOLA", "OBSERVAÇÃO"},
                    new String[]{"123", "ESC IND A", "ENSINO INFANTIL"});

            PlanilhaLocalizada<Coluna> planilha = layout.localizar(workbook);

            assertFalse(planilha.possui(Coluna.INFANTIL));
            assertEquals(1, planilha.primeiraLinhaDados());
        }
    }

    @Test
    void colunasObrigatoriasAusentesDevemSerListadas() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            linhas(workbook.createSheet("Dados"), new String[]{"CÓDIGO", "ALDEIA"});

            IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                    () -> layout.localizar(workbook));

            assertEquals("A planilha não está no layout de teste: colunas obrigatórias não encontradas: " +
                    "NOME DA ESCOLA. Confira o cabeçalho ou use o modelo de importação.", erro.getMessage());
        }
    }

    @Test
    void modeloGeradoDeveSerAceitoPeloProprioLayout() throws IOException {
        try (Workbook modelo = WorkbookFactory.create(new ByteArrayInputStream(layout.gerarModelo()))) {
            PlanilhaLocalizada<Coluna> planilha = layout.localizar(modelo);

            assertEquals("Dados", planilha.aba().getSheetName());
            assertEquals(Coluna.values().length, planilha.colunas().size());
            assertNotNull(modelo.getSheet("Instruções"));
            assertEquals("Instrução de teste.", modelo.getSheet("Instruções").getRow(1).getCell(0).getStringCellValue());
        }
    }

    @Test
    void layoutDeveSerDescritoParaATela() {
        var dto = layout.paraDTO("teste");

        assertEquals("teste", dto.tipo());
        assertEquals("Teste", dto.titulo());
        assertEquals(Coluna.values().length, dto.colunas().size());
        assertEquals(List.of("INEP"), dto.colunas().getFirst().sinonimos());
        assertTrue(dto.colunas().getFirst().obrigatoria());
    }

    private static void linhas(Sheet aba, String[]... valores) {
        for (int indice = 0; indice < valores.length; indice++) {
            Row linha = aba.createRow(indice);

            for (int coluna = 0; coluna < valores[indice].length; coluna++) {
                if (!valores[indice][coluna].isEmpty()) {
                    linha.createCell(coluna).setCellValue(valores[indice][coluna]);
                }
            }
        }
    }
}
