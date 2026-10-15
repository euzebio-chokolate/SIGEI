package ac.gov.br.sigei_backend.exceptions;

/** E-mail inexistente ou senha incorreta no login; os dois casos têm a mesma resposta. */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("E-mail ou senha inválidos.");
    }
}
