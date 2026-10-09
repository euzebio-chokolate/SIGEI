package ac.gov.br.sigei_backend.dto.response;

import java.util.List;

public record ImportacaoProfessoresResultadoDTO(
        Integer linhasLidas,
        Integer professoresCriados,
        Integer professoresAtualizados,
        Integer lotacoesCriadas,
        Integer lotacoesAtualizadas,
        Integer vagasEmAberto,
        Integer lotacoesNoNucleo,
        Integer linhasIgnoradas,
        List<String> erros
) {
}
