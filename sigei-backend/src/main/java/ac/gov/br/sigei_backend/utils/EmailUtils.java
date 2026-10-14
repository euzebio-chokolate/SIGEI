package ac.gov.br.sigei_backend.utils;

import java.util.Locale;

/**
 * Normaliza e-mails de usuário. O e-mail é gravado e buscado sem espaços nas pontas
 * e em minúsculas, para que {@code Joao@x.com} e {@code joao@x.com} sejam o mesmo
 * usuário, no cadastro e no login.
 */
public final class EmailUtils {

    private EmailUtils() {
    }

    public static String normalizar(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
