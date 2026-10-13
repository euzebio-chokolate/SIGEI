package ac.gov.br.sigei_backend.utils;

import java.util.List;

/**
 * Coluna de um layout de importação. Implementada pelos enums de colunas de cada
 * importação e lida por {@link LayoutPlanilha}.
 */
public interface ColunaPlanilha {

    /** Título usado no modelo; também é aceito no cabeçalho da planilha. */
    String titulo();

    boolean obrigatoria();

    /** Conteúdo esperado, exibido na tela e na aba de instruções do modelo. */
    String descricao();

    /** Outros títulos aceitos no cabeçalho, além do {@link #titulo()}. */
    List<String> sinonimos();
}
