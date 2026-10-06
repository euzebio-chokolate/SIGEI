package ac.gov.br.sigei_backend.utils;

import ac.gov.br.sigei_backend.models.InfraestruturaEscola;
import ac.gov.br.sigei_backend.models.enums.SituacaoEscola;
import ac.gov.br.sigei_backend.models.enums.TipoAcesso;
import ac.gov.br.sigei_backend.models.enums.TipoAgua;
import ac.gov.br.sigei_backend.models.enums.TipoEnergia;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Situação, tipo de acesso, abastecimento de água e fonte de energia da
 * infraestrutura são String na entidade, mas só aceitam os valores de
 * {@link SituacaoEscola}, {@link TipoAcesso}, {@link TipoAgua} e {@link TipoEnergia},
 * os mesmos oferecidos pelo formulário de escolas e pelo filtro do mapa.
 *
 * <p>Os valores são gravados pelo nome da opção. A comparação ignora acentos,
 * maiúsculas e separadores ("Rio/Vertente" → {@code RIO_VERTENTE}, "Não" →
 * {@code NAO}); vazio vira nulo (não informado).
 */
public final class OpcoesInfraestrutura {

    /** Grafias da planilha de 2025 para a fonte de energia, sem opção de mesmo nome. */
    private static final Map<String, String> SINONIMOS_ENERGIA_IMPORTADA = Map.of(
            "REDE_ELETRICA", TipoEnergia.REDE_PUBLICA.name(),
            // A escola tem energia, mas a planilha não informa a fonte
            "SIM", TipoEnergia.OUTRO.name()
    );

    private OpcoesInfraestrutura() {}

    /** Gravação pela API: valor fora das opções gera IllegalArgumentException (400). */
    public static void validar(InfraestruturaEscola infraestrutura) {
        infraestrutura.setSituacao(converter(infraestrutura.getSituacao(), SituacaoEscola.class, "Situação inválida"));
        infraestrutura.setTipoAcesso(converter(infraestrutura.getTipoAcesso(), TipoAcesso.class, "Tipo de acesso inválido"));
        infraestrutura.setTipoAgua(converter(infraestrutura.getTipoAgua(), TipoAgua.class, "Abastecimento de água inválido"));
        infraestrutura.setTipoEnergia(converter(infraestrutura.getTipoEnergia(), TipoEnergia.class, "Fonte de energia inválida"));
    }

    /*
     * Importação de planilha: valor fora das opções vira nulo. Na planilha de 2025,
     * as escolas paralisadas têm "DESATIVADA" nas colunas de acesso, água e energia,
     * que não é um valor desses campos.
     */

    public static String situacaoImportada(String valor) {
        return converterOuNulo(valor, SituacaoEscola.class, Map.of());
    }

    public static String tipoAcessoImportado(String valor) {
        return converterOuNulo(valor, TipoAcesso.class, Map.of());
    }

    public static String tipoAguaImportado(String valor) {
        return converterOuNulo(valor, TipoAgua.class, Map.of());
    }

    public static String tipoEnergiaImportado(String valor) {
        return converterOuNulo(valor, TipoEnergia.class, SINONIMOS_ENERGIA_IMPORTADA);
    }

    private static <E extends Enum<E>> String converterOuNulo(String valor, Class<E> opcoes,
                                                              Map<String, String> sinonimos) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String sinonimo = sinonimos.get(normalizar(valor));
        if (sinonimo != null) {
            return sinonimo;
        }
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

        String normalizado = normalizar(valor);
        for (E opcao : opcoes.getEnumConstants()) {
            if (opcao.name().equals(normalizado)) {
                return opcao.name();
            }
        }

        String aceitos = Arrays.stream(opcoes.getEnumConstants()).map(Enum::name).collect(Collectors.joining(", "));
        throw new IllegalArgumentException(erro + ": use " + aceitos + ".");
    }

    private static String normalizar(String valor) {
        return Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .replaceAll("[\\s/]+", "_");
    }
}
