package ac.gov.br.sigei_backend.dto.response;

import java.util.List;

public record ColunaLayoutDTO(
        String titulo,
        Boolean obrigatoria,
        String descricao,
        List<String> sinonimos
) {
}
