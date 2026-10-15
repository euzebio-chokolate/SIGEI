package ac.gov.br.sigei_backend.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Uma página de uma listagem. {@code pagina} começa em zero, como os parâmetros
 * {@code pagina} e {@code tamanho} das rotas paginadas (ver {@code utils/Paginacao}).
 */
public record PaginaDTO<T>(
        List<T> itens,
        int pagina,
        int tamanho,
        long totalItens,
        int totalPaginas
) {

    public static <E, T> PaginaDTO<T> de(Page<E> pagina, Function<E, T> conversor) {
        return new PaginaDTO<>(
                pagina.getContent().stream().map(conversor).toList(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages()
        );
    }
}
