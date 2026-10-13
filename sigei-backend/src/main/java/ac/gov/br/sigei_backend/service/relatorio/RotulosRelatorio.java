package ac.gov.br.sigei_backend.service.relatorio;

import java.util.Map;

/**
 * Rótulos de exibição dos valores gravados, os mesmos do frontend
 * ({@code infraestrutura-opcoes.ts} e modelos de professores). Valores
 * desconhecidos aparecem como estão.
 */
final class RotulosRelatorio {

    static final Map<String, String> NIVEIS = Map.of(
            "ENSINO_INFANTIL", "Ensino Infantil",
            "FUNDAMENTAL_ANOS_INICIAIS", "Fundamental - Anos Iniciais",
            "FUNDAMENTAL_ANOS_FINAIS", "Fundamental - Anos Finais",
            "ENSINO_MEDIO", "Ensino Médio",
            "EJA", "EJA",
            "AEE", "AEE"
    );

    private static final Map<String, String> SITUACOES = Map.of(
            "FUNCIONANDO", "Funcionando",
            "PARALISADA", "Paralisada",
            "DESATIVADA", "Desativada"
    );

    private static final Map<String, String> ACESSOS = Map.of(
            "TERRESTRE", "Terrestre",
            "FLUVIAL", "Fluvial",
            "MISTO", "Misto"
    );

    private static final Map<String, String> AGUA = Map.of(
            "REDE_PUBLICA", "Rede pública",
            "POCO", "Poço",
            "CACIMBA", "Cacimba",
            "RIO", "Rio",
            "IGARAPE", "Igarapé",
            "VERTENTE", "Vertente",
            "RIO_VERTENTE", "Rio / Vertente",
            "OUTRO", "Outro",
            "NAO", "Não possui"
    );

    private static final Map<String, String> ENERGIA = Map.of(
            "REDE_PUBLICA", "Rede pública",
            "GERADOR", "Gerador",
            "SOLAR", "Solar",
            "OUTRO", "Outro",
            "NAO", "Não possui"
    );

    private static final Map<String, String> CONTRATOS = Map.of(
            "EFETIVO", "Efetivo",
            "PSS", "PSS",
            "CONTRATO_DIRETO", "Contrato direto",
            "PERMUTA", "Permuta"
    );

    private static final Map<String, String> SEXOS = Map.of("M", "Masculino", "F", "Feminino");

    private RotulosRelatorio() {
    }

    static String nivel(String valor) {
        return rotulo(NIVEIS, valor);
    }

    static String situacao(String valor) {
        return rotulo(SITUACOES, valor);
    }

    static String acesso(String valor) {
        return rotulo(ACESSOS, valor);
    }

    static String agua(String valor) {
        return rotulo(AGUA, valor);
    }

    static String energia(String valor) {
        return rotulo(ENERGIA, valor);
    }

    static String contrato(String valor) {
        return rotulo(CONTRATOS, valor);
    }

    static String sexo(String valor) {
        return rotulo(SEXOS, valor);
    }

    static String tipoUnidade(String valor) {
        return valor == null ? null : "ANEXO".equalsIgnoreCase(valor) ? "Anexo" : "Sede";
    }

    static String simNao(Boolean valor) {
        return valor == null ? null : valor ? "Sim" : "Não";
    }

    private static String rotulo(Map<String, String> rotulos, String valor) {
        return valor == null || valor.isBlank() ? null : rotulos.getOrDefault(valor, valor);
    }
}
