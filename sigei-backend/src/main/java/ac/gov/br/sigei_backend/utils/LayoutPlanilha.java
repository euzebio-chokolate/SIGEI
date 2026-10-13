package ac.gov.br.sigei_backend.utils;

import ac.gov.br.sigei_backend.dto.response.ColunaLayoutDTO;
import ac.gov.br.sigei_backend.dto.response.LayoutImportacaoDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.stream.Stream;

/**
 * Layout de uma importação de Excel, descrito pelas colunas esperadas.
 *
 * <p>As colunas são localizadas pelo título no cabeçalho, e não pela posição: a
 * planilha pode ter colunas em outra ordem, colunas extras e qualquer nome de aba.
 * O cabeçalho é procurado nas primeiras linhas de cada aba, e a primeira linha que
 * contém todas as colunas obrigatórias é usada. A comparação ignora acentos,
 * maiúsculas e pontuação ({@code Nº DE SALAS} = {@code N DE SALAS}) e aceita
 * títulos que começam por um nome conhecido seguido de outras palavras
 * ({@code Nº DE ALUNOS 2025}), depois de tentar os títulos exatos.
 *
 * <p>Cabeçalhos em duas linhas também são aceitos: onde a primeira linha está vazia
 * ou traz um título desconhecido (um grupo mesclado, como {@code Matrículas}), vale
 * o título da linha seguinte, e os dados começam depois dela.
 */
public final class LayoutPlanilha<C extends Enum<C> & ColunaPlanilha> {

    /** Linhas examinadas no início de cada aba à procura do cabeçalho. */
    private static final int LINHAS_PROCURADAS = 10;

    private final Class<C> tipo;
    private final String titulo;
    private final String descricao;
    private final List<String> instrucoes;

    /**
     * @param titulo    nome exibido na tela ("Quadro de professores")
     * @param descricao complemento das mensagens de erro ("do quadro de professores")
     */
    public LayoutPlanilha(Class<C> tipo, String titulo, String descricao, List<String> instrucoes) {
        this.tipo = tipo;
        this.titulo = titulo;
        this.descricao = descricao;
        this.instrucoes = List.copyOf(instrucoes);
    }

    public List<C> colunas() {
        return List.of(tipo.getEnumConstants());
    }

    /**
     * Procura o cabeçalho nas abas da planilha.
     *
     * @throws IllegalArgumentException se nenhuma aba tiver todas as colunas
     *                                  obrigatórias; a mensagem lista as que faltam
     *                                  na aba mais próxima do layout
     */
    public PlanilhaLocalizada<C> localizar(Workbook workbook) {
        Set<C> menorFalta = null;

        for (Sheet aba : workbook) {
            int ultimaLinha = Math.min(aba.getLastRowNum(), LINHAS_PROCURADAS - 1);

            for (int indice = 0; indice <= ultimaLinha; indice++) {
                PlanilhaLocalizada<C> candidata = resolver(aba, indice);
                Set<C> faltantes = faltantes(candidata);

                if (faltantes.isEmpty()) {
                    return candidata;
                }

                if (menorFalta == null || faltantes.size() < menorFalta.size()) {
                    menorFalta = faltantes;
                }
            }
        }

        Collection<C> faltantes = menorFalta == null ? obrigatorias() : menorFalta;

        throw new IllegalArgumentException(
                "A planilha não está no layout " + descricao + ": colunas obrigatórias não encontradas: " +
                        String.join(", ", faltantes.stream().map(ColunaPlanilha::titulo).toList()) +
                        ". Confira o cabeçalho ou use o modelo de importação."
        );
    }

    private PlanilhaLocalizada<C> resolver(Sheet aba, int indiceCabecalho) {
        Row cabecalho = aba.getRow(indiceCabecalho);
        Row subcabecalho = aba.getRow(indiceCabecalho + 1);

        int totalColunas = Math.max(totalColunas(cabecalho), totalColunas(subcabecalho));
        String[] principal = new String[totalColunas];
        String[] secundario = new String[totalColunas];
        boolean[] aceitaSubcabecalho = new boolean[totalColunas];

        for (int coluna = 0; coluna < totalColunas; coluna++) {
            principal[coluna] = normalizarCabecalho(ExcelUtils.lerTexto(cabecalho, coluna));
            secundario[coluna] = normalizarCabecalho(ExcelUtils.lerTexto(subcabecalho, coluna));
            aceitaSubcabecalho[coluna] = principal[coluna].isEmpty() || !reconhecido(principal[coluna]);
        }

        Map<C, Integer> encontradas = new EnumMap<>(tipo);
        Set<Integer> usadas = new HashSet<>();
        boolean usouSubcabecalho = false;

        // Títulos exatos têm prioridade sobre os aproximados, e a linha principal
        // sobre o subcabeçalho
        for (boolean exato : new boolean[]{true, false}) {
            for (boolean sub : new boolean[]{false, true}) {
                for (C coluna : tipo.getEnumConstants()) {
                    if (encontradas.containsKey(coluna)) {
                        continue;
                    }

                    for (String nome : nomesNormalizados(coluna)) {
                        int posicao = procurar(sub ? secundario : principal, nome, exato, usadas,
                                sub ? aceitaSubcabecalho : null);

                        if (posicao >= 0) {
                            encontradas.put(coluna, posicao);
                            usadas.add(posicao);
                            usouSubcabecalho |= sub;
                            break;
                        }
                    }
                }
            }
        }

        return new PlanilhaLocalizada<>(aba, indiceCabecalho + (usouSubcabecalho ? 2 : 1), encontradas);
    }

    private static int procurar(String[] titulos, String nome, boolean exato, Set<Integer> usadas,
                                boolean[] permitidas) {
        for (int coluna = 0; coluna < titulos.length; coluna++) {
            if (usadas.contains(coluna) || (permitidas != null && !permitidas[coluna])) {
                continue;
            }

            if (exato ? titulos[coluna].equals(nome) : titulos[coluna].startsWith(nome + " ")) {
                return coluna;
            }
        }

        return -1;
    }

    /** Se o título corresponde a alguma coluna do layout, exata ou aproximadamente. */
    private boolean reconhecido(String titulo) {
        return Stream.of(tipo.getEnumConstants())
                .flatMap(coluna -> nomesNormalizados(coluna).stream())
                .anyMatch(nome -> titulo.equals(nome) || titulo.startsWith(nome + " "));
    }

    private static List<String> nomesNormalizados(ColunaPlanilha coluna) {
        List<String> nomes = new ArrayList<>();
        nomes.add(normalizarCabecalho(coluna.titulo()));
        coluna.sinonimos().forEach(sinonimo -> nomes.add(normalizarCabecalho(sinonimo)));
        return nomes;
    }

    private Set<C> faltantes(PlanilhaLocalizada<C> planilha) {
        Set<C> faltantes = EnumSet.noneOf(tipo);

        for (C coluna : obrigatorias()) {
            if (!planilha.possui(coluna)) {
                faltantes.add(coluna);
            }
        }

        return faltantes;
    }

    private List<C> obrigatorias() {
        return colunas().stream().filter(ColunaPlanilha::obrigatoria).toList();
    }

    private static int totalColunas(Row linha) {
        return linha == null ? 0 : Math.max(linha.getLastCellNum(), 0);
    }

    /** Sem acentos, em maiúsculas, só letras, números e espaços simples. */
    public static String normalizarCabecalho(String texto) {
        String normalizado = ExcelUtils.normalizar(texto);

        if (normalizado == null) {
            return "";
        }

        return normalizado
                .replaceAll("[^A-Z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public LayoutImportacaoDTO paraDTO(String chave) {
        return new LayoutImportacaoDTO(
                chave,
                titulo,
                instrucoes,
                colunas().stream()
                        .map(coluna -> new ColunaLayoutDTO(
                                coluna.titulo(), coluna.obrigatoria(), coluna.descricao(), coluna.sinonimos()))
                        .toList()
        );
    }

    /**
     * Gera o modelo da importação: a aba {@code Dados}, só com o cabeçalho (colunas
     * obrigatórias destacadas), e a aba {@code Instruções}, com as regras e o
     * conteúdo esperado em cada coluna.
     */
    public byte[] gerarModelo() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            CellStyle obrigatoria = estiloCabecalho(workbook, IndexedColors.LIGHT_GREEN);
            CellStyle opcional = estiloCabecalho(workbook, IndexedColors.GREY_25_PERCENT);
            CellStyle negrito = estiloCabecalho(workbook, null);

            Sheet dados = workbook.createSheet("Dados");
            Row cabecalho = dados.createRow(0);
            List<C> colunas = colunas();

            for (int indice = 0; indice < colunas.size(); indice++) {
                C coluna = colunas.get(indice);
                Cell celula = cabecalho.createCell(indice);
                celula.setCellValue(coluna.titulo());
                celula.setCellStyle(coluna.obrigatoria() ? obrigatoria : opcional);
                dados.setColumnWidth(indice, Math.max(14, coluna.titulo().length() + 4) * 256);
            }

            dados.createFreezePane(0, 1);

            Sheet ajuda = workbook.createSheet("Instruções");
            int linha = 0;

            Cell tituloAjuda = ajuda.createRow(linha++).createCell(0);
            tituloAjuda.setCellValue("Modelo de importação: " + titulo);
            tituloAjuda.setCellStyle(negrito);

            for (String instrucao : instrucoes) {
                ajuda.createRow(linha++).createCell(0).setCellValue(instrucao);
            }

            linha++;
            Row titulosAjuda = ajuda.createRow(linha++);
            String[] titulos = {"Coluna", "Obrigatória", "Conteúdo", "Também aceito como"};

            for (int indice = 0; indice < titulos.length; indice++) {
                Cell celula = titulosAjuda.createCell(indice);
                celula.setCellValue(titulos[indice]);
                celula.setCellStyle(negrito);
            }

            for (C coluna : colunas) {
                Row descricaoColuna = ajuda.createRow(linha++);
                descricaoColuna.createCell(0).setCellValue(coluna.titulo());
                descricaoColuna.createCell(1).setCellValue(coluna.obrigatoria() ? "Sim" : "Não");
                descricaoColuna.createCell(2).setCellValue(coluna.descricao());
                descricaoColuna.createCell(3).setCellValue(String.join("; ", coluna.sinonimos()));
            }

            ajuda.setColumnWidth(0, 40 * 256);
            ajuda.setColumnWidth(1, 12 * 256);
            ajuda.setColumnWidth(2, 90 * 256);
            ajuda.setColumnWidth(3, 50 * 256);

            workbook.write(saida);
            return saida.toByteArray();

        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível gerar o modelo de importação.", e);
        }
    }

    private static CellStyle estiloCabecalho(Workbook workbook, IndexedColors cor) {
        Font fonte = workbook.createFont();
        fonte.setBold(true);

        CellStyle estilo = workbook.createCellStyle();
        estilo.setFont(fonte);

        if (cor != null) {
            estilo.setFillForegroundColor(cor.getIndex());
            estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        return estilo;
    }

    /**
     * Aba e posições encontradas. {@link #coluna} devolve -1 para uma coluna
     * opcional ausente; as leituras de {@link ExcelUtils} tratam -1 como célula vazia.
     */
    public record PlanilhaLocalizada<C extends Enum<C>>(Sheet aba, int primeiraLinhaDados, Map<C, Integer> colunas) {

        public int coluna(C coluna) {
            return colunas.getOrDefault(coluna, -1);
        }

        public boolean possui(C coluna) {
            return colunas.containsKey(coluna);
        }

        /** Cópia com a coluna na posição informada, para deduções feitas pelo serviço. */
        public PlanilhaLocalizada<C> comColuna(C coluna, int posicao) {
            Map<C, Integer> copia = new HashMap<>(colunas);
            copia.put(coluna, posicao);
            return new PlanilhaLocalizada<>(aba, primeiraLinhaDados, copia);
        }

        /** Linhas de dados não vazias, na ordem da planilha. */
        public List<Row> linhas() {
            List<Row> linhas = new ArrayList<>();

            for (int indice = primeiraLinhaDados; indice <= aba.getLastRowNum(); indice++) {
                Row linha = aba.getRow(indice);

                if (!ExcelUtils.linhaVazia(linha)) {
                    linhas.add(linha);
                }
            }

            return linhas;
        }
    }
}
