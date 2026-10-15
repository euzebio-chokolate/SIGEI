package ac.gov.br.sigei_backend.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Leitura de células. Coluna negativa (coluna opcional ausente no layout) é lida
 * como célula vazia.
 */
public final class ExcelUtils {

    private static final Pattern PRIMEIRO_ANO = Pattern.compile("(?<!\\d)\\d{4}(?!\\d)");

    private ExcelUtils() {
    }

    public static String lerTexto(
            Row linha,
            int numeroColuna
    ) {
        if (linha == null || numeroColuna < 0) {
            return null;
        }

        Cell celula = linha.getCell(
                numeroColuna,
                Row.MissingCellPolicy.RETURN_BLANK_AS_NULL
        );

        if (celula == null) {
            return null;
        }

        DataFormatter formatador = new DataFormatter();

        String valor = formatador
                .formatCellValue(celula)
                .trim();

        if (valor.isBlank() || valor.equals("-")) {
            return null;
        }

        return valor.replaceAll("\\s+", " ");
    }

    public static String lerCodigo(
            Row linha,
            int numeroColuna
    ) {
        if (linha == null || numeroColuna < 0) {
            return null;
        }

        Cell celula = linha.getCell(
                numeroColuna,
                Row.MissingCellPolicy.RETURN_BLANK_AS_NULL
        );

        if (celula == null) {
            return null;
        }

        if (celula.getCellType() == CellType.NUMERIC) {
            return BigDecimal
                    .valueOf(celula.getNumericCellValue())
                    .stripTrailingZeros()
                    .toPlainString();
        }

        String codigo = lerTexto(linha, numeroColuna);

        if (codigo == null) {
            return null;
        }

        return codigo.replaceAll("\\.0$", "");
    }

    public static Integer lerInteiro(
            Row linha,
            int numeroColuna
    ) {
        String valor = lerTexto(linha, numeroColuna);

        if (valor == null) {
            return null;
        }

        try {
            return new BigDecimal(
                    valor.replace(",", ".")
            ).intValueExact();

        } catch (ArithmeticException |
                 NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Valor inteiro inválido na coluna " +
                            (numeroColuna + 1) +
                            ": " +
                            valor
            );
        }
    }

    /**
     * Ano de quatro dígitos. Com mais de um ano na célula, como "2014 e 2025"
     * (construção e reforma), vale o primeiro.
     */
    public static Integer lerAno(
            Row linha,
            int numeroColuna
    ) {
        String valor = lerTexto(linha, numeroColuna);

        if (valor == null) {
            return null;
        }

        try {
            return lerInteiro(linha, numeroColuna);

        } catch (IllegalArgumentException e) {
            Matcher ano = PRIMEIRO_ANO.matcher(valor);

            if (ano.find()) {
                return Integer.valueOf(ano.group());
            }

            throw e;
        }
    }

    public static Boolean lerBooleano(
            Row linha,
            int numeroColuna
    ) {
        String valor = lerTexto(linha, numeroColuna);

        if (valor == null) {
            return null;
        }

        String normalizado = normalizar(valor);

        return switch (normalizado) {
            case "SIM" -> true;

            case "NAO",
                 "DESATIVADA",
                 "DESATIVADO",
                 "PARALISADA",
                 "PARALISADO",
                 "COM O PONTO, FALTA A LIBERACAO DO SINAL",
                 "ATUALIZAR FOTO",
                 "FALTA FOTO DA ESCOLA" -> false;

            default -> throw new IllegalArgumentException(
                    "Valor booleano inválido na coluna " +
                            (numeroColuna + 1) +
                            ": " +
                            valor
            );
        };
    }

    public static String normalizar(String texto) {
        if (texto == null) {
            return null;
        }

        String semAcentos = Normalizer.normalize(
                texto,
                Normalizer.Form.NFD
        ).replaceAll("\\p{M}", "");

        return semAcentos
                .trim()
                .replaceAll("\\s+", " ")
                .toUpperCase(Locale.ROOT);
    }

    /**
     * Lê uma data gravada como data do Excel (número de série) ou como texto
     * dd/MM/aaaa. Vazio ou "-" resultam em nulo.
     */
    public static LocalDate lerData(
            Row linha,
            int numeroColuna
    ) {
        if (linha == null || numeroColuna < 0) {
            return null;
        }

        Cell celula = linha.getCell(
                numeroColuna,
                Row.MissingCellPolicy.RETURN_BLANK_AS_NULL
        );

        if (celula == null) {
            return null;
        }

        if (celula.getCellType() == CellType.NUMERIC) {
            double valor = celula.getNumericCellValue();

            if (!DateUtil.isValidExcelDate(valor)) {
                throw new IllegalArgumentException(
                        "Data inválida na coluna " + (numeroColuna + 1)
                );
            }

            return DateUtil.getLocalDateTime(valor).toLocalDate();
        }

        String texto = lerTexto(linha, numeroColuna);

        if (texto == null) {
            return null;
        }

        try {
            return LocalDate.parse(
                    texto,
                    DateTimeFormatter.ofPattern("dd/MM/uuuu")
            );

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Data inválida na coluna " +
                            (numeroColuna + 1) +
                            ": " +
                            texto
            );
        }
    }

    public static boolean linhaVazia(Row linha) {
        if (linha == null) {
            return true;
        }

        DataFormatter formatador = new DataFormatter();

        for (Cell celula : linha) {
            String valor = formatador
                    .formatCellValue(celula)
                    .trim();

            if (!valor.isBlank()) {
                return false;
            }
        }

        return true;
    }
}