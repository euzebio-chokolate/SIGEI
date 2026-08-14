package ac.gov.br.sigei_backend.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CoordenadaUtils {

    private static final Pattern PADRAO_DMS = Pattern.compile(
            "^\\s*(\\d{1,3})\\s*[°º]\\s*" +
                    "(\\d{1,2})\\s*['’′]\\s*" +
                    "(\\d{1,2}(?:[.,]\\d+)?)\\s*" +
                    "[\"”″]?\\s*([NSEWOL])\\s*$",
            Pattern.CASE_INSENSITIVE
    );

    private CoordenadaUtils() {
        // Impede a criação de objetos dessa classe.
    }

    public static Double converterLatitude(String coordenada) {
        return converter(
                coordenada,
                TipoCoordenada.LATITUDE
        );
    }

    public static Double converterLongitude(String coordenada) {
        return converter(
                coordenada,
                TipoCoordenada.LONGITUDE
        );
    }

    private static Double converter(
            String coordenada,
            TipoCoordenada tipo
    ) {
        if (coordenada == null) {
            return null;
        }

        String valor = coordenada.trim();

        if (valor.isBlank() || valor.equals("-")) {
            return null;
        }

        valor = valor
                .toUpperCase(Locale.ROOT)
                .replace("OESTE", "O")
                .replace("LESTE", "L")
                .replace("NORTE", "N")
                .replace("SUL", "S");

        Matcher matcher = PADRAO_DMS.matcher(valor);

        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Formato de coordenada inválido: " +
                            coordenada
            );
        }

        int graus = Integer.parseInt(matcher.group(1));
        int minutos = Integer.parseInt(matcher.group(2));

        double segundos = Double.parseDouble(
                matcher.group(3).replace(",", ".")
        );

        char direcao = Character.toUpperCase(
                matcher.group(4).charAt(0)
        );

        validar(
                graus,
                minutos,
                segundos,
                direcao,
                tipo
        );

        double decimal =
                graus
                        + minutos / 60.0
                        + segundos / 3600.0;

        /*
         * Sul e Oeste precisam ficar negativos.
         * A planilha utiliza a letra O para Oeste.
         */
        if (direcao == 'S' ||
                direcao == 'O' ||
                direcao == 'W') {

            decimal = -decimal;
        }

        return BigDecimal.valueOf(decimal)
                .setScale(7, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static void validar(
            int graus,
            int minutos,
            double segundos,
            char direcao,
            TipoCoordenada tipo
    ) {
        if (minutos < 0 || minutos >= 60) {
            throw new IllegalArgumentException(
                    "Os minutos da coordenada devem estar " +
                            "entre 0 e 59."
            );
        }

        if (segundos < 0 || segundos >= 60) {
            throw new IllegalArgumentException(
                    "Os segundos da coordenada devem estar " +
                            "entre 0 e 59.999."
            );
        }

        if (tipo == TipoCoordenada.LATITUDE) {
            validarLatitude(graus, direcao);
        } else {
            validarLongitude(graus, direcao);
        }
    }

    private static void validarLatitude(
            int graus,
            char direcao
    ) {
        if (graus > 90) {
            throw new IllegalArgumentException(
                    "A latitude não pode ultrapassar 90 graus."
            );
        }

        if (direcao != 'N' && direcao != 'S') {
            throw new IllegalArgumentException(
                    "A latitude precisa terminar em N ou S."
            );
        }
    }

    private static void validarLongitude(
            int graus,
            char direcao
    ) {
        if (graus > 180) {
            throw new IllegalArgumentException(
                    "A longitude não pode ultrapassar 180 graus."
            );
        }

        if (direcao != 'L' &&
                direcao != 'O' &&
                direcao != 'E' &&
                direcao != 'W') {

            throw new IllegalArgumentException(
                    "A longitude precisa terminar em " +
                            "L, O, E ou W."
            );
        }
    }

    private enum TipoCoordenada {
        LATITUDE,
        LONGITUDE
    }
}