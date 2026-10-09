package ac.gov.br.sigei_backend.utils;

/**
 * Normaliza e valida CPF. O valor é gravado só com os 11 dígitos; a pontuação é
 * opcional na entrada.
 */
public final class CpfUtils {

    private CpfUtils() {
    }

    /**
     * Devolve os 11 dígitos do CPF, ou nulo quando vazio. CPF com tamanho ou dígitos
     * verificadores inválidos gera IllegalArgumentException (400). A mensagem não
     * repete o valor informado, para que ele não chegue aos logs.
     */
    public static String normalizar(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            return null;
        }

        String digitos = cpf.replaceAll("[.\\-\\s]", "");

        if (!digitos.matches("\\d{11}") || !digitosVerificadoresValidos(digitos)) {
            throw new IllegalArgumentException("CPF inválido.");
        }

        return digitos;
    }

    private static boolean digitosVerificadoresValidos(String cpf) {
        // Sequências repetidas (000.000.000-00, 111...) passam no cálculo, mas não existem
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        return calcularDigito(cpf, 9) == cpf.charAt(9) - '0'
                && calcularDigito(cpf, 10) == cpf.charAt(10) - '0';
    }

    private static int calcularDigito(String cpf, int quantidade) {
        int soma = 0;

        for (int i = 0; i < quantidade; i++) {
            soma += (cpf.charAt(i) - '0') * (quantidade + 1 - i);
        }

        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
