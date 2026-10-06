package ac.gov.br.sigei_backend.utils;

import ac.gov.br.sigei_backend.models.InfraestruturaEscola;
import ac.gov.br.sigei_backend.models.enums.SituacaoEscola;
import ac.gov.br.sigei_backend.models.enums.TipoAcesso;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Situação e tipo de acesso da infraestrutura são String na entidade, mas só
 * aceitam os valores de {@link SituacaoEscola} e {@link TipoAcesso}, os mesmos
 * oferecidos pelo formulário de escolas e pelo filtro do mapa. Os valores são
 * gravados em maiúsculas e sem acentos; vazio vira nulo (não informado).
 */
public final class OpcoesInfraestrutura {

    private OpcoesInfraestrutura() {}

    /** Gravação pela API: valor fora das opções gera IllegalArgumentException (400). */
    public static void validar(InfraestruturaEscola infraestrutura) {
        infraestrutura.setSituacao(converter(infraestrutura.getSituacao(), SituacaoEscola.class, "Situação inválida"));
        infraestrutura.setTipoAcesso(converter(infraestrutura.getTipoAcesso(), TipoAcesso.class, "Tipo de acesso inválido"));
    }

    /**
     * Importação de planilha: valor fora das opções vira nulo. Na planilha de 2025,
     * as escolas paralisadas têm "DESATIVADA" na coluna de acesso, que não é um
     * tipo de acesso.
     */
    public static String situacaoImportada(String valor) {
        return converterOuNulo(valor, SituacaoEscola.class);
    }

    public static String tipoAcessoImportado(String valor) {
        return converterOuNulo(valor, TipoAcesso.class);
    }

    private static <E extends Enum<E>> String converterOuNulo(String valor, Class<E> opcoes) {
        try {
            return converter(valor, opcoes, "");
        } catch (IllegalArgumentException e) {
            return null;
        }
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
