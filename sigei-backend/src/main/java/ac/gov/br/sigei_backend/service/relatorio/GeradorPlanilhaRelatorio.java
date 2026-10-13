package ac.gov.br.sigei_backend.service.relatorio;

import ac.gov.br.sigei_backend.service.relatorio.Relatorio.Coluna;
import ac.gov.br.sigei_backend.service.relatorio.Relatorio.Indicador;
import ac.gov.br.sigei_backend.service.relatorio.Relatorio.Secao;
import ac.gov.br.sigei_backend.service.relatorio.Relatorio.Tabela;
import ac.gov.br.sigei_backend.service.relatorio.Relatorio.TipoColuna;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gera o relatório em .xlsx: a aba "Resumo" com título, filtros e indicadores, e
 * uma aba por tabela, com cabeçalho fixo, filtro automático e números como
 * números, para que a planilha possa ser reprocessada.
 */
@Component
public class GeradorPlanilhaRelatorio {

    private static final int LARGURA_MAXIMA = 60;

    public byte[] gerar(Relatorio relatorio, String emitidoEm) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            Estilos estilos = new Estilos(workbook);
            Set<String> nomesUsados = new HashSet<>();

            gerarResumo(workbook, estilos, relatorio, emitidoEm, nomesUsados);

            for (Secao secao : relatorio.secoes()) {
                if (secao.tabela() != null) {
                    gerarTabela(workbook, estilos, secao, nomeAba(secao.titulo(), nomesUsados));
                }
            }

            workbook.write(saida);
            return saida.toByteArray();

        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível gerar a planilha do relatório.", e);
        }
    }

    private void gerarResumo(Workbook workbook, Estilos estilos, Relatorio relatorio, String emitidoEm,
                             Set<String> nomesUsados) {
        Sheet aba = workbook.createSheet(nomeAba("Resumo", nomesUsados));
        int linha = 0;

        celula(aba.createRow(linha++), 0, relatorio.titulo(), estilos.titulo);
        celula(aba.createRow(linha++), 0, relatorio.filtros(), null);
        celula(aba.createRow(linha++), 0, "Emitido em " + emitidoEm + " pelo SIGEI", estilos.discreto);

        for (Secao secao : relatorio.secoes()) {
            if (secao.indicadores().isEmpty()) {
                continue;
            }

            linha++;
            celula(aba.createRow(linha++), 0, secao.titulo(), estilos.subtitulo);

            for (Indicador indicador : secao.indicadores()) {
                Row row = aba.createRow(linha++);
                celula(row, 0, indicador.rotulo(), null);
                valor(row, 1, indicador.valor(), TipoColuna.NUMERO, estilos, false);
            }
        }

        aba.setColumnWidth(0, 45 * 256);
        aba.setColumnWidth(1, 18 * 256);
    }

    private void gerarTabela(Workbook workbook, Estilos estilos, Secao secao, String nome) {
        Sheet aba = workbook.createSheet(nome);
        Tabela tabela = secao.tabela();
        List<Coluna> colunas = tabela.colunas();
        int[] larguras = new int[colunas.size()];

        Row cabecalho = aba.createRow(0);
        for (int coluna = 0; coluna < colunas.size(); coluna++) {
            celula(cabecalho, coluna, colunas.get(coluna).titulo(), estilos.cabecalho);
            larguras[coluna] = colunas.get(coluna).titulo().length();
        }

        int indice = 1;
        for (List<Object> valores : tabela.linhas()) {
            escreverLinha(aba.createRow(indice++), colunas, valores, estilos, false, larguras);
        }

        if (!tabela.linhas().isEmpty()) {
            aba.setAutoFilter(new CellRangeAddress(0, indice - 1, 0, colunas.size() - 1));
        }

        if (tabela.total() != null) {
            escreverLinha(aba.createRow(indice), colunas, tabela.total(), estilos, true, larguras);
        }

        aba.createFreezePane(0, 1);

        for (int coluna = 0; coluna < colunas.size(); coluna++) {
            aba.setColumnWidth(coluna, Math.min(LARGURA_MAXIMA, larguras[coluna] + 3) * 256);
        }
    }

    private void escreverLinha(Row row, List<Coluna> colunas, List<Object> valores, Estilos estilos,
                               boolean total, int[] larguras) {
        for (int coluna = 0; coluna < colunas.size(); coluna++) {
            Object valor = coluna < valores.size() ? valores.get(coluna) : null;
            valor(row, coluna, valor, colunas.get(coluna).tipo(), estilos, total);

            if (valor != null) {
                larguras[coluna] = Math.max(larguras[coluna], valor.toString().length());
            }
        }
    }

    private static void valor(Row row, int coluna, Object valor, TipoColuna tipo, Estilos estilos, boolean total) {
        Cell celula = row.createCell(coluna);

        if (valor instanceof Number numero) {
            celula.setCellValue(numero.doubleValue());
            celula.setCellStyle(tipo == TipoColuna.PERCENTUAL
                    ? (total ? estilos.percentualTotal : estilos.percentual)
                    : (total ? estilos.numeroTotal : estilos.numero));
        } else if (valor != null) {
            celula.setCellValue(valor.toString());
            celula.setCellStyle(total ? estilos.textoTotal : null);
        } else if (total) {
            celula.setCellStyle(estilos.textoTotal);
        }
    }

    private static void celula(Row row, int coluna, String texto, CellStyle estilo) {
        Cell celula = row.createCell(coluna);
        celula.setCellValue(texto);

        if (estilo != null) {
            celula.setCellStyle(estilo);
        }
    }

    /** Nome de aba válido (até 31 caracteres, sem caracteres proibidos) e único. */
    private static String nomeAba(String titulo, Set<String> usados) {
        String base = WorkbookUtil.createSafeSheetName(titulo);
        String nome = base;

        for (int sufixo = 2; !usados.add(nome.toLowerCase()); sufixo++) {
            String complemento = " (" + sufixo + ")";
            nome = base.substring(0, Math.min(base.length(), 31 - complemento.length())) + complemento;
        }

        return nome;
    }

    private static final class Estilos {

        final CellStyle titulo;
        final CellStyle subtitulo;
        final CellStyle discreto;
        final CellStyle cabecalho;
        final CellStyle numero;
        final CellStyle percentual;
        final CellStyle textoTotal;
        final CellStyle numeroTotal;
        final CellStyle percentualTotal;

        Estilos(Workbook workbook) {
            DataFormat formatos = workbook.createDataFormat();

            titulo = estilo(workbook, fonte(workbook, true, 14, IndexedColors.DARK_GREEN));
            subtitulo = estilo(workbook, fonte(workbook, true, 11, IndexedColors.DARK_GREEN));
            discreto = estilo(workbook, fonte(workbook, false, 9, IndexedColors.GREY_50_PERCENT));

            cabecalho = estilo(workbook, fonte(workbook, true, 10, IndexedColors.WHITE));
            cabecalho.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
            cabecalho.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cabecalho.setWrapText(true);

            numero = workbook.createCellStyle();
            numero.setDataFormat(formatos.getFormat("#,##0"));
            percentual = workbook.createCellStyle();
            percentual.setDataFormat(formatos.getFormat("0.0\"%\""));

            Font negrito = fonte(workbook, true, 10, IndexedColors.BLACK);
            textoTotal = total(workbook, negrito);
            numeroTotal = total(workbook, negrito);
            numeroTotal.setDataFormat(formatos.getFormat("#,##0"));
            percentualTotal = total(workbook, negrito);
            percentualTotal.setDataFormat(formatos.getFormat("0.0\"%\""));
        }

        private static CellStyle total(Workbook workbook, Font fonte) {
            CellStyle estilo = estilo(workbook, fonte);
            estilo.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            estilo.setBorderTop(BorderStyle.THIN);
            return estilo;
        }

        private static CellStyle estilo(Workbook workbook, Font fonte) {
            CellStyle estilo = workbook.createCellStyle();
            estilo.setFont(fonte);
            return estilo;
        }

        private static Font fonte(Workbook workbook, boolean negrito, int tamanho, IndexedColors cor) {
            Font fonte = workbook.createFont();
            fonte.setBold(negrito);
            fonte.setFontHeightInPoints((short) tamanho);
            fonte.setColor(cor.getIndex());
            return fonte;
        }
    }
}
