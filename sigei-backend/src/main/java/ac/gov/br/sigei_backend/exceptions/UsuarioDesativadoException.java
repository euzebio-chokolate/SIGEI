package ac.gov.br.sigei_backend.exceptions;

/** Login com a senha correta de um usuário desativado. */
public class UsuarioDesativadoException extends RuntimeException {

    public UsuarioDesativadoException() {
        super("Usuário desativado. Procure o administrador do sistema.");
    }
}
