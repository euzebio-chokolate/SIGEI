package ac.gov.br.sigei_backend.utils;

import ac.gov.br.sigei_backend.models.enums.LocalFuncionamento;
import ac.gov.br.sigei_backend.models.enums.Rede;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Rede e local de funcionamento do registro anual são String na entidade, mas só
 * aceitam os valores de {@link Rede} e {@link LocalFuncionamento}, os mesmos do
 * formulário de escolas. A comparação ignora acentos e maiúsculas
 * ("Estadual" → {@code ESTADUAL}).
 */
public final class OpcoesRegistroAnual {

    private OpcoesRegistroAnual() {
    }

    /** Gravação pela API: valor fora das opções gera IllegalArgumentException (400). */
    public static String rede(String valor) {
        return converter(valor, Rede.class, "Rede inválida");
    }

    /** Gravação pela API: valor fora das opções gera IllegalArgumentException (400). */
    public static String localFuncionamento(String valor) {
        return converter(valor, LocalFuncionamento.class, "Local de funcionamento inválido");
    }

    private static <E extends Enum<E>> String converter(String valor, Class<E> opcoes, String erro) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        String normalizado = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT);
        for (E opcao : opcoes.getEnumConstants()) {
            if (opcao.name().equals(normalizado)) {
                return opcao.name();
            }
        }

        String aceitos = Arrays.stream(opcoes.getEnumConstants()).map(Enum::name).collect(Collectors.joining(", "));
        throw new IllegalArgumentException(erro + ": use " + aceitos + ".");
    }
}
