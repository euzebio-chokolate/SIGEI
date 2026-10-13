package ac.gov.br.sigei_backend.service.relatorio;

import ac.gov.br.sigei_backend.service.relatorio.Relatorio.Coluna;
import ac.gov.br.sigei_backend.service.relatorio.Relatorio.Indicador;
import ac.gov.br.sigei_backend.service.relatorio.Relatorio.Secao;
import ac.gov.br.sigei_backend.service.relatorio.Relatorio.Tabela;
import ac.gov.br.sigei_backend.service.relatorio.Relatorio.TipoColuna;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Gera o relatório em PDF (A4), com cabeçalho de tabela repetido a cada página e
 * rodapé com a data de emissão e a numeração das páginas.
 */
@Component
public class GeradorPdfRelatorio {

    private static final Locale PT_BR = Locale.of("pt", "BR");

    private static final Color VERDE = new Color(6, 78, 59);
    private static final Color VERDE_CLARO = new Color(209, 250, 229);
    private static final Color CINZA_CLARO = new Color(248, 250, 252);
    private static final Color CINZA = new Color(100, 116, 139);
    private static final Color BORDA = new Color(226, 232, 240);

    private static final Font TITULO = new Font(Font.HELVETICA, 16, Font.BOLD, VERDE);
    private static final Font FILTROS = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
    private static final Font SECAO = new Font(Font.HELVETICA, 12, Font.BOLD, VERDE);
    private static final Font CABECALHO = new Font(Font.HELVETICA, 8, Font.BOLD, Color.WHITE);
    private static final Font CELULA = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.BLACK);
    private static final Font TOTAL = new Font(Font.HELVETICA, 8, Font.BOLD, Color.BLACK);
    private static final Font CABECALHO_COMPACTO = new Font(Font.HELVETICA, 7, Font.BOLD, Color.WHITE);
    private static final Font CELULA_COMPACTA = new Font(Font.HELVETICA, 7, Font.NORMAL, Color.BLACK);
    private static final Font TOTAL_COMPACTO = new Font(Font.HELVETICA, 7, Font.BOLD, Color.BLACK);

    /** A partir deste número de colunas, a tabela usa fonte menor. */
    private static final int COLUNAS_TABELA_COMPACTA = 10;
    private static final Font ROTULO_INDICADOR = new Font(Font.HELVETICA, 8, Font.NORMAL, CINZA);
    private static final Font VALOR_INDICADOR = new Font(Font.HELVETICA, 13, Font.BOLD, VERDE);
    private static final Font RODAPE = new Font(Font.HELVETICA, 7, Font.NORMAL, CINZA);
    private static final Font VAZIO = new Font(Font.HELVETICA, 9, Font.ITALIC, CINZA);

    private static final int INDICADORES_POR_LINHA = 4;

    public byte[] gerar(Relatorio relatorio, String emitidoEm) {
        Rectangle pagina = relatorio.paisagem() ? PageSize.A4.rotate() : PageSize.A4;
        Document documento = new Document(pagina, 30, 30, 30, 40);
        ByteArrayOutputStream saida = new ByteArrayOutputStream();

        try {
            PdfWriter escritor = PdfWriter.getInstance(documento, saida);
            escritor.setPageEvent(new Rodape("SIGEI · " + relatorio.titulo() + " · Emitido em " + emitidoEm));
            documento.addTitle(relatorio.titulo());
            documento.addCreator("SIGEI");
            documento.open();

            Paragraph titulo = new Paragraph(relatorio.titulo(), TITULO);
            documento.add(titulo);

            Paragraph filtros = new Paragraph(relatorio.filtros(), FILTROS);
            filtros.setSpacingAfter(8);
            documento.add(filtros);

            for (Secao secao : relatorio.secoes()) {
                adicionarSecao(documento, secao);
            }

            documento.close();
            return saida.toByteArray();

        } catch (DocumentException e) {
            throw new IllegalStateException("Não foi possível gerar o PDF do relatório.", e);
        }
    }

    private void adicionarSecao(Document documento, Secao secao) throws DocumentException {
        Paragraph titulo = new Paragraph(secao.titulo(), SECAO);
        titulo.setSpacingBefore(10);
        titulo.setSpacingAfter(6);
        documento.add(titulo);

        if (!secao.indicadores().isEmpty()) {
            documento.add(indicadores(secao.indicadores()));
        }

        if (secao.tabela() != null) {
            if (secao.tabela().linhas().isEmpty()) {
                documento.add(new Paragraph("Nenhum registro para os filtros informados.", VAZIO));
            } else {
                documento.add(tabela(secao.tabela()));
            }
        }
    }

    private PdfPTable indicadores(List<Indicador> indicadores) {
        PdfPTable grade = new PdfPTable(INDICADORES_POR_LINHA);
        grade.setWidthPercentage(100);
        grade.setSpacingAfter(6);

        for (Indicador indicador : indicadores) {
            PdfPCell celula = new PdfPCell();
            celula.setBorderColor(BORDA);
            celula.setBackgroundColor(CINZA_CLARO);
            celula.setPadding(6);
            celula.addElement(new Paragraph(formatar(indicador.valor(), TipoColuna.NUMERO), VALOR_INDICADOR));
            celula.addElement(new Paragraph(indicador.rotulo(), ROTULO_INDICADOR));
            grade.addCell(celula);
        }

        // Completa a última linha da grade
        int restantes = (INDICADORES_POR_LINHA - indicadores.size() % INDICADORES_POR_LINHA) % INDICADORES_POR_LINHA;
        for (int i = 0; i < restantes; i++) {
            PdfPCell vazia = new PdfPCell();
            vazia.setBorder(Rectangle.NO_BORDER);
            grade.addCell(vazia);
        }

        return grade;
    }

    private PdfPTable tabela(Tabela tabela) throws DocumentException {
        List<Coluna> colunas = tabela.colunas();
        PdfPTable pdf = new PdfPTable(colunas.size());
        pdf.setWidthPercentage(100);
        pdf.setHeaderRows(1);

        float[] larguras = new float[colunas.size()];
        for (int i = 0; i < colunas.size(); i++) {
            larguras[i] = colunas.get(i).largura();
        }
        pdf.setWidths(larguras);

        boolean compacta = colunas.size() >= COLUNAS_TABELA_COMPACTA;
        Font cabecalho = compacta ? CABECALHO_COMPACTO : CABECALHO;
        Font fonteCelula = compacta ? CELULA_COMPACTA : CELULA;
        Font fonteTotal = compacta ? TOTAL_COMPACTO : TOTAL;

        for (Coluna coluna : colunas) {
            PdfPCell celula = celula(coluna.titulo(), cabecalho, coluna.tipo(), VERDE);
            pdf.addCell(celula);
        }

        int indice = 0;
        for (List<Object> linha : tabela.linhas()) {
            Color fundo = indice++ % 2 == 1 ? CINZA_CLARO : Color.WHITE;
            adicionarLinha(pdf, colunas, linha, fonteCelula, fundo, "—");
        }

        if (tabela.total() != null) {
            adicionarLinha(pdf, colunas, tabela.total(), fonteTotal, VERDE_CLARO, "");
        }

        return pdf;
    }

    /** @param vazio texto das células sem valor */
    private void adicionarLinha(PdfPTable pdf, List<Coluna> colunas, List<Object> valores, Font fonte, Color fundo,
                                String vazio) {
        for (int i = 0; i < colunas.size(); i++) {
            Object valor = i < valores.size() ? valores.get(i) : null;
            String texto = valor == null ? vazio : formatar(valor, colunas.get(i).tipo());
            pdf.addCell(celula(texto, fonte, colunas.get(i).tipo(), fundo));
        }
    }

    private static PdfPCell celula(String texto, Font fonte, TipoColuna tipo, Color fundo) {
        PdfPCell celula = new PdfPCell(new Phrase(texto, fonte));
        celula.setBackgroundColor(fundo);
        celula.setBorderColor(BORDA);
        celula.setPadding(4);
        celula.setHorizontalAlignment(tipo == TipoColuna.TEXTO ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
        celula.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return celula;
    }

    static String formatar(Object valor, TipoColuna tipo) {
        if (valor == null) {
            return "—";
        }

        if (valor instanceof Number numero) {
            if (tipo == TipoColuna.PERCENTUAL) {
                NumberFormat formato = NumberFormat.getNumberInstance(PT_BR);
                formato.setMinimumFractionDigits(1);
                formato.setMaximumFractionDigits(1);
                return formato.format(numero.doubleValue()) + "%";
            }

            return NumberFormat.getIntegerInstance(PT_BR).format(numero.longValue());
        }

        return valor.toString();
    }

    /** Rodapé de cada página: descrição à esquerda e número da página à direita. */
    private static final class Rodape extends PdfPageEventHelper {

        private final String texto;

        Rodape(String texto) {
            this.texto = texto;
        }

        @Override
        public void onEndPage(PdfWriter escritor, Document documento) {
            PdfContentByte conteudo = escritor.getDirectContent();
            float y = documento.bottom() - 20;

            ColumnText.showTextAligned(conteudo, Element.ALIGN_LEFT,
                    new Phrase(texto, RODAPE), documento.left(), y, 0);
            ColumnText.showTextAligned(conteudo, Element.ALIGN_RIGHT,
                    new Phrase("Página " + escritor.getPageNumber(), RODAPE), documento.right(), y, 0);
        }
    }
}
