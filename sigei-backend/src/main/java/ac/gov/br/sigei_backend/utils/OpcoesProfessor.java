package ac.gov.br.sigei_backend.utils;

import ac.gov.br.sigei_backend.models.enums.TipoContrato;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * O tipo de contrato da lotação é String na entidade, mas só aceita os valores de
 * {@link TipoContrato}. A comparação ignora acentos, maiúsculas e separadores
 * ("Contrato direto" → {@code CONTRATO_DIRETO}); vazio vira nulo.
 */
public final class OpcoesProfessor {

    private OpcoesProfessor() {
    }

    /** Gravação pela API: valor fora das opções gera IllegalArgumentException (400). */
    public static String tipoContrato(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        String normalizado = normalizar(valor);
        for (TipoContrato opcao : TipoContrato.values()) {
            if (opcao.name().equals(normalizado)) {
                return opcao.name();
            }
        }

        String aceitos = Arrays.stream(TipoContrato.values()).map(Enum::name).collect(Collectors.joining(", "));
        throw new IllegalArgumentException("Tipo de contrato inválido: use " + aceitos + ".");
    }

    /** Importação de planilha: valor fora das opções vira nulo. */
    public static String tipoContratoImportado(String valor) {
        try {
            return tipoContrato(valor);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Sexo: aceita M/F ou Masculino/Feminino; outros valores viram nulo. */
    public static String sexoImportado(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return switch (normalizar(valor)) {
            case "M", "MASCULINO" -> "M";
            case "F", "FEMININO" -> "F";
            default -> null;
        };
    }

    private static String normalizar(String valor) {
        return Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[\\s/-]+", "_");
    }
}
