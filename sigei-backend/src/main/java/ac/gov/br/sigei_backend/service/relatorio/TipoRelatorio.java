package ac.gov.br.sigei_backend.service.relatorio;

import java.util.Arrays;
import java.util.NoSuchElementException;

/** Relatórios disponíveis em GET /relatorios/{tipo}. */
public enum TipoRelatorio {

    PAINEL("painel", "Indicadores consolidados"),
    ESCOLAS("escolas", "Escolas e infraestrutura"),
    MATRICULAS("matriculas", "Matrículas por etapa"),
    PROFESSORES("professores", "Professores e lotações");

    private final String chave;
    private final String titulo;

    TipoRelatorio(String chave, String titulo) {
        this.chave = chave;
        this.titulo = titulo;
    }

    public String chave() {
        return chave;
    }

    public String titulo() {
        return titulo;
    }

    public static TipoRelatorio daChave(String chave) {
        return Arrays.stream(values())
                .filter(tipo -> tipo.chave.equalsIgnoreCase(chave))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Relatório inexistente: " + chave));
    }
}
