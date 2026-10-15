package ac.gov.br.sigei_backend.exceptions;

/**
 * Operação recusada por conflitar com dados já gravados (registro duplicado,
 * vínculo existente). Responde 409 com a mensagem, que deve servir para a tela.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
