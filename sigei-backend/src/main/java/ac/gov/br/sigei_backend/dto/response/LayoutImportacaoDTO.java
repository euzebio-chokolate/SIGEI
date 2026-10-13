package ac.gov.br.sigei_backend.dto.response;

import java.util.List;

public record LayoutImportacaoDTO(
        String tipo,
        String titulo,
        List<String> instrucoes,
        List<ColunaLayoutDTO> colunas
) {
}
