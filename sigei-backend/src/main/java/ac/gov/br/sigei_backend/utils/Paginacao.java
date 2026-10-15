package ac.gov.br.sigei_backend.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Validação dos parâmetros {@code pagina} (a partir de zero) e {@code tamanho} das
 * rotas paginadas. A ordenação fica na consulta de cada repository.
 */
public final class Paginacao {

    public static final int TAMANHO_PADRAO = 20;
    public static final int TAMANHO_MAXIMO = 100;

    private Paginacao() {
    }

    public static Pageable pagina(int pagina, int tamanho) {
        if (pagina < 0) {
            throw new IllegalArgumentException("A página deve ser zero ou maior.");
        }
        if (tamanho < 1 || tamanho > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("O tamanho da página deve estar entre 1 e " + TAMANHO_MAXIMO + ".");
        }
        return PageRequest.of(pagina, tamanho);
    }
}
