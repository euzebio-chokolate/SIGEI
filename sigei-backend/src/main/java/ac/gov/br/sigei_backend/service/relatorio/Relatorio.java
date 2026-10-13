package ac.gov.br.sigei_backend.service.relatorio;

import java.util.List;

/**
 * Conteúdo de um relatório, independente do formato: seções com indicadores e
 * tabelas. {@link GeradorPlanilhaRelatorio} e {@link GeradorPdfRelatorio} o
 * convertem em planilha e PDF.
 *
 * @param paisagem PDF em orientação paisagem, para tabelas largas
 */
public record Relatorio(String titulo, String filtros, boolean paisagem, List<Secao> secoes) {

    /** Seção com indicadores em destaque e uma tabela, ambos opcionais. */
    public record Secao(String titulo, List<Indicador> indicadores, Tabela tabela) {

        public static Secao deIndicadores(String titulo, List<Indicador> indicadores) {
            return new Secao(titulo, indicadores, null);
        }

        public static Secao deTabela(String titulo, Tabela tabela) {
            return new Secao(titulo, List.of(), tabela);
        }
    }

    /** Valor numérico (Number) ou texto. */
    public record Indicador(String rotulo, Object valor) {
    }

    /**
     * Linhas com um valor por coluna: Number nas colunas numéricas e percentuais,
     * String nas de texto; nulo é célula vazia. {@code total} é a linha de totais,
     * opcional.
     */
    public record Tabela(List<Coluna> colunas, List<List<Object>> linhas, List<Object> total) {

        public Tabela(List<Coluna> colunas, List<List<Object>> linhas) {
            this(colunas, linhas, null);
        }
    }

    /** @param largura peso relativo da coluna no PDF */
    public record Coluna(String titulo, TipoColuna tipo, float largura) {

        public static Coluna texto(String titulo, float largura) {
            return new Coluna(titulo, TipoColuna.TEXTO, largura);
        }

        public static Coluna numero(String titulo) {
            return new Coluna(titulo, TipoColuna.NUMERO, 1);
        }

        public static Coluna numero(String titulo, float largura) {
            return new Coluna(titulo, TipoColuna.NUMERO, largura);
        }

        public static Coluna percentual(String titulo) {
            return new Coluna(titulo, TipoColuna.PERCENTUAL, 1);
        }
    }

    /** Percentuais são gravados de 0 a 100. */
    public enum TipoColuna {
        TEXTO, NUMERO, PERCENTUAL
    }
}
