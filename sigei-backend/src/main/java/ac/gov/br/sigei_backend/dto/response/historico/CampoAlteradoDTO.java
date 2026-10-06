package ac.gov.br.sigei_backend.dto.response.historico;

/**
 * Valor de um campo antes e depois de uma alteração. Na criação, o valor anterior
 * é nulo; na exclusão, o novo. Relacionamentos aparecem pelo ID, com sufixo {@code Id}.
 */
public record CampoAlteradoDTO(
        String campo,
        Object valorAnterior,
        Object valorNovo
) {
}
