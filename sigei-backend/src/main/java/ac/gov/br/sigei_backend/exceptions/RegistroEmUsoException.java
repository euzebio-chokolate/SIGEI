package ac.gov.br.sigei_backend.exceptions;

/** Exclusão recusada porque outros registros ainda dependem deste (409). */
public class RegistroEmUsoException extends RegraNegocioException {

    public RegistroEmUsoException(String mensagem) {
        super(mensagem);
    }
}
