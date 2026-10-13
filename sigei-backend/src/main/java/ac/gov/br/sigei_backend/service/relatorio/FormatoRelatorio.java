package ac.gov.br.sigei_backend.service.relatorio;

import java.util.Arrays;

public enum FormatoRelatorio {

    XLSX("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    PDF("pdf", "application/pdf");

    private final String extensao;
    private final String tipoMidia;

    FormatoRelatorio(String extensao, String tipoMidia) {
        this.extensao = extensao;
        this.tipoMidia = tipoMidia;
    }

    public String extensao() {
        return extensao;
    }

    public String tipoMidia() {
        return tipoMidia;
    }

    public static FormatoRelatorio daExtensao(String extensao) {
        return Arrays.stream(values())
                .filter(formato -> formato.extensao.equalsIgnoreCase(extensao))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Formato de relatório inválido: " + extensao + ". Use xlsx ou pdf."));
    }
}
