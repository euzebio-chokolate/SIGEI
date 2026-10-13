package ac.gov.br.sigei_backend.service.importacao;

import ac.gov.br.sigei_backend.dto.response.ImportacaoMatriculasResultadoDTO;
import ac.gov.br.sigei_backend.dto.response.ImportacaoResultadoDTO;
import ac.gov.br.sigei_backend.models.*;
import ac.gov.br.sigei_backend.repository.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
@Import({ImportacaoExcelService.class, ImportacaoMatriculasService.class})
class ImportacaoEscolasMatriculasServiceTest {

    private static final int ANO = 2026;

    @Autowired ImportacaoExcelService importacaoEscolas;
    @Autowired ImportacaoMatriculasService importacaoMatriculas;
    @Autowired TestEntityManager entityManager;
    @Autowired EscolaRepository escolas;
    @Autowired AnoReferenciaRepository anos;
    @Autowired RegistroAnualRepository registros;
    @Autowired InfraestruturaEscolaRepository infraestruturas;
    @Autowired EquipeEscolaRepository equipes;
    @Autowired MatriculaNivelRepository niveis;
    @Autowired MatriculaSerieRepository series;

    @BeforeEach
    void preparar() {
        // A importação de escolas exige os municípios das abreviações da planilha original
        for (String nome : List.of("Feijó", "Marechal Thaumaturgo", "Santa Rosa do Purus")) {
            Municipio municipio = new Municipio();
            municipio.setNome(nome);
            entityManager.persist(municipio);
        }
    }

    @Nested
    class Escolas {

        @Test
        void deveImportarPeloModeloNoAnoInformado() throws IOException {
            ImportacaoResultadoDTO resultado = importarEscolasPeloModelo(ANO);

            assertTrue(resultado.erros().isEmpty(), resultado.erros().toString());
            assertEquals(2, resultado.escolasCriadas());
            assertTrue(anos.findByAno(ANO).isPresent());

            Escola sede = escolas.findAllByCodigo("12000001").stream()
                    .filter(escola -> "SEDE".equals(escola.getTipoUnidade())).findFirst().orElseThrow();
            Escola anexo = escolas.findAllByCodigo("12000001").stream()
                    .filter(escola -> "ANEXO".equals(escola.getTipoUnidade())).findFirst().orElseThrow();
            assertEquals(sede.getId(), anexo.getEscolaSede().getId());

            RegistroAnual registro = registros.findByEscolaAndAnoReferencia_Ano(sede, ANO).orElseThrow();
            assertEquals(30, registro.getTotalAlunos());

            InfraestruturaEscola infraestrutura = infraestruturas.findByRegistroAnual_Id(registro.getId()).orElseThrow();
            assertEquals("FUNCIONANDO", infraestrutura.getSituacao());
            assertEquals("RIO_VERTENTE", infraestrutura.getTipoAgua());
            assertEquals(-8.1585, infraestrutura.getLatitude());
            assertEquals(-70.3521, infraestrutura.getLongitude());
            assertTrue(equipes.findByRegistroAnual_Id(registro.getId()).orElseThrow().isNecessitaTransporte());
        }

        @Test
        void colunaOpcionalAusenteNaoDeveApagarDadosGravados() throws IOException {
            importarEscolasPeloModelo(ANO);

            // Nova planilha sem as colunas SITUAÇÃO e ÁGUA, em outra ordem e outra aba
            ImportacaoResultadoDTO resultado = importarEscolas(planilha("Atualização", 0,
                    new String[]{"ALDEIA", "CÓDIGO", "NOME DA ESCOLA", "MUNICÍPIO", "TERRA INDÍGENA", "POVO", "Nº DE SALAS"},
                    new String[]{"ALTO BONITO", "12000001", "ESC IND ALTO BONITO", "FEIJÓ", "ALTO RIO ENVIRA", "ASHANINKA", "4"}
            ), ANO);

            assertTrue(resultado.erros().isEmpty(), resultado.erros().toString());
            InfraestruturaEscola infraestrutura = infraestruturaDaSede();
            assertEquals(4, infraestrutura.getNumSalas());
            assertEquals("FUNCIONANDO", infraestrutura.getSituacao());
            assertEquals("RIO_VERTENTE", infraestrutura.getTipoAgua());
        }

        @Test
        void deveAceitarOFormatoDaPlanilhaOriginal() throws IOException {
            // Coordenadas em graus/minutos/segundos sob COORDENADAS (longitude sem título)
            // e o anexo indicado por ANEXO na coluna de ordem
            ImportacaoResultadoDTO resultado = importarEscolas(planilha("Planilha4", 0,
                    new String[]{"ORD.", "MUNICÍPIO", "CÓDIGO", "NOME DA ESCOLA", "TERRA INDÍGENA", "ALDEIA", "POVO",
                            "SITUAÇÃO", "Nº DE ALUNOS 2025", "COORDENADAS", ""},
                    new String[]{"1", "FEIJÓ", "12000001", "ESC IND ALTO BONITO", "ALTO RIO ENVIRA", "ALTO BONITO",
                            "ASHANINKA", "FUNCIONANDO", "30", "8°09'30.60\"S", "70°21'07.56\"O"},
                    new String[]{"ANEXO", "FEIJÓ", "12000001", "ESC IND ALTO BONITO - COQUEIRO", "ALTO RIO ENVIRA",
                            "COQUEIRO", "ASHANINKA", "FUNCIONANDO", "12", "-", "-"}
            ), ANO);

            assertTrue(resultado.erros().isEmpty(), resultado.erros().toString());
            assertEquals(2, resultado.escolasCriadas());

            InfraestruturaEscola infraestrutura = infraestruturaDaSede();
            assertEquals(-8.1585, infraestrutura.getLatitude());
            assertEquals(-70.3521, infraestrutura.getLongitude());
        }

        @Test
        void colunaObrigatoriaAusenteDeveImpedirAImportacao() {
            IllegalArgumentException erro = assertThrows(IllegalArgumentException.class, () -> importarEscolas(
                    planilha("Dados", 0, new String[]{"MUNICÍPIO", "NOME DA ESCOLA", "ALDEIA", "POVO", "TERRA INDÍGENA"}),
                    ANO));

            assertTrue(erro.getMessage().contains("colunas obrigatórias não encontradas: CÓDIGO"), erro.getMessage());
        }

        @Test
        void anoForaDoIntervaloDeveSerRecusado() {
            IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                    () -> importarEscolasPeloModelo(1990));

            assertTrue(erro.getMessage().startsWith("Ano de referência inválido: 1990"));
            assertTrue(anos.findByAno(1990).isEmpty());
        }
    }

    @Nested
    class Matriculas {

        @Test
        void deveImportarCabecalhoEmDuasLinhasNoAnoInformado() throws IOException {
            importarEscolasPeloModelo(ANO);

            String[] grupo = new String[25];
            String[] etapas = new String[25];
            String[] valores = new String[25];
            String[][] colunas = {
                    {"Código", "", "12000001"}, {"Nome da Escola", "", "ESC IND ALTO BONITO"},
                    {"Aldeia", "", "ALTO BONITO"}, {"M", "", "16"}, {"F", "", "14"},
                    {"Matrículas", "Ensino Infantil", "3"}, {"", "1º Ano", "2"}, {"", "2º Ano", "2"},
                    {"", "3º Ano", "2"}, {"", "4º Ano", "2"}, {"", "5º Ano", "2"}, {"", "Subtotal", "10"},
                    {"", "6º Ano", "3"}, {"", "7º Ano", "3"}, {"", "8º Ano", "3"}, {"", "9º Ano", "3"},
                    {"", "1ª Série", "1"}, {"", "2ª Série", "1"}, {"", "3ª Série", "1"}, {"", "EJA", "2"},
                    {"", "AEE", "0"}
            };

            for (int coluna = 0; coluna < colunas.length; coluna++) {
                grupo[coluna] = colunas[coluna][0];
                etapas[coluna] = colunas[coluna][1];
                valores[coluna] = colunas[coluna][2];
            }

            ImportacaoMatriculasResultadoDTO resultado = importarMatriculas(
                    planilha("Censo_" + ANO, 0, grupo, etapas, valores), ANO);

            assertTrue(resultado.erros().isEmpty(), resultado.erros().toString());
            assertEquals(1, resultado.registrosAnuaisAtualizados());

            RegistroAnual registro = registroDaSede();
            assertEquals(30, registro.getTotalAlunos());

            Map<String, Integer> totais = niveis.findByRegistro(registro).stream()
                    .collect(Collectors.toMap(MatriculaNivel::getNivel, MatriculaNivel::getTotal));
            assertEquals(10, totais.get("FUNDAMENTAL_ANOS_INICIAIS"));
            assertEquals(12, totais.get("FUNDAMENTAL_ANOS_FINAIS"));
            assertEquals(3, totais.get("ENSINO_MEDIO"));
            assertEquals(2, totais.get("EJA"));

            Map<String, MatriculaSerie> porSerie = series.findByRegistro(registro).stream()
                    .collect(Collectors.toMap(MatriculaSerie::getDenominacao, Function.identity()));
            assertEquals(3, porSerie.get("9º ANO").getTotal());
        }

        @Test
        void anoNaoCadastradoDeveImpedirAImportacao() throws IOException {
            byte[] modelo = ImportacaoMatriculasService.LAYOUT.gerarModelo();

            IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                    () -> importacaoMatriculas.importar(arquivo(modelo), 2020));

            assertTrue(erro.getMessage().startsWith("O ano 2020 não está cadastrado"));
        }
    }

    private InfraestruturaEscola infraestruturaDaSede() {
        return infraestruturas.findByRegistroAnual_Id(registroDaSede().getId()).orElseThrow();
    }

    private RegistroAnual registroDaSede() {
        Escola sede = escolas.findFirstByCodigoAndTipoUnidadeIgnoreCase("12000001", "SEDE").orElseThrow();
        return registros.findByEscolaAndAnoReferencia_Ano(sede, ANO).orElseThrow();
    }

    /** Preenche o modelo gerado pelo próprio layout com uma sede e um anexo. */
    private ImportacaoResultadoDTO importarEscolasPeloModelo(int ano) throws IOException {
        try (Workbook modelo = WorkbookFactory.create(new ByteArrayInputStream(ImportacaoExcelService.LAYOUT.gerarModelo()))) {
            Sheet dados = modelo.getSheet("Dados");
            Row cabecalho = dados.getRow(0);

            Map<String, String> sede = Map.ofEntries(
                    Map.entry("TIPO DE UNIDADE", "SEDE"), Map.entry("MUNICÍPIO", "FEIJÓ"),
                    Map.entry("CÓDIGO", "12000001"), Map.entry("NOME DA ESCOLA", "ESC IND ALTO BONITO"),
                    Map.entry("TERRA INDÍGENA", "ALTO RIO ENVIRA"), Map.entry("ALDEIA", "ALTO BONITO"),
                    Map.entry("POVO", "ASHANINKA"), Map.entry("SITUAÇÃO", "Funcionando"),
                    Map.entry("Nº DE ALUNOS", "30"), Map.entry("LATITUDE", "-8.1585"),
                    Map.entry("LONGITUDE", "-70,3521"), Map.entry("ÁGUA", "Rio/Vertente"),
                    Map.entry("TEM NECESSIDADE DE TRANSPORTE ESCOLAR", "SIM")
            );
            Map<String, String> anexo = Map.of(
                    "TIPO DE UNIDADE", "ANEXO", "MUNICÍPIO", "FEIJÓ", "CÓDIGO", "12000001",
                    "NOME DA ESCOLA", "ESC IND ALTO BONITO - COQUEIRO", "TERRA INDÍGENA", "ALTO RIO ENVIRA",
                    "ALDEIA", "COQUEIRO", "POVO", "ASHANINKA"
            );

            // O anexo vem antes da sede: a importação processa as sedes primeiro
            List<Map<String, String>> linhas = List.of(anexo, sede);

            for (int indice = 0; indice < linhas.size(); indice++) {
                Row linha = dados.createRow(indice + 1);

                for (int coluna = 0; coluna < cabecalho.getLastCellNum(); coluna++) {
                    String valor = linhas.get(indice).get(cabecalho.getCell(coluna).getStringCellValue());

                    if (valor != null) {
                        linha.createCell(coluna).setCellValue(valor);
                    }
                }
            }

            return importarEscolas(bytes(modelo), ano);
        }
    }

    private ImportacaoResultadoDTO importarEscolas(byte[] planilha, int ano) {
        return importacaoEscolas.importarInfraestrutura(arquivo(planilha), ano);
    }

    private ImportacaoMatriculasResultadoDTO importarMatriculas(byte[] planilha, int ano) {
        return importacaoMatriculas.importar(arquivo(planilha), ano);
    }

    private static MockMultipartFile arquivo(byte[] conteudo) {
        return new MockMultipartFile("arquivo", "planilha.xlsx", null, conteudo);
    }

    private static byte[] planilha(String aba, int primeiraLinha, String[]... linhas) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet planilha = workbook.createSheet(aba);

            for (int indice = 0; indice < linhas.length; indice++) {
                Row linha = planilha.createRow(primeiraLinha + indice);

                for (int coluna = 0; coluna < linhas[indice].length; coluna++) {
                    String valor = linhas[indice][coluna];

                    if (valor != null && !valor.isEmpty()) {
                        linha.createCell(coluna).setCellValue(valor);
                    }
                }
            }

            return bytes(workbook);
        }
    }

    private static byte[] bytes(Workbook workbook) throws IOException {
        try (ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            workbook.write(saida);
            return saida.toByteArray();
        }
    }
}
