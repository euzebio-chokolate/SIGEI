package ac.gov.br.sigei_backend.utils;

import ac.gov.br.sigei_backend.exceptions.RegistroEmUsoException;

import java.util.ArrayList;
import java.util.List;

/**
 * Monta a recusa de uma exclusão com os registros que ainda dependem do excluído,
 * por exemplo "Não é possível excluir o povo: ele tem 2 aldeias vinculadas."
 * Os services conferem os vínculos antes do delete, para que a resposta explique
 * o motivo em vez de repassar a violação de chave estrangeira do banco.
 */
public final class Vinculos {

    private final String inicio;
    private final List<String> partes = new ArrayList<>();

    private Vinculos(String inicio) {
        this.inicio = inicio;
    }

    /** @param inicio início da mensagem até os dois-pontos, por exemplo "Não é possível excluir o povo: ele tem" */
    public static Vinculos para(String inicio) {
        return new Vinculos(inicio);
    }

    /** Acrescenta a quantidade, se houver, no singular ou no plural ("1 aldeia vinculada", "2 aldeias vinculadas"). */
    public Vinculos com(long quantidade, String singular, String plural) {
        if (quantidade > 0) {
            partes.add(quantidade + " " + (quantidade == 1 ? singular : plural));
        }
        return this;
    }

    /** Lança {@link RegistroEmUsoException} se algum vínculo foi encontrado. */
    public void verificar() {
        if (partes.isEmpty()) {
            return;
        }
        String lista = partes.size() == 1
                ? partes.getFirst()
                : String.join(", ", partes.subList(0, partes.size() - 1)) + " e " + partes.getLast();
        throw new RegistroEmUsoException(inicio + " " + lista + ".");
    }
}
