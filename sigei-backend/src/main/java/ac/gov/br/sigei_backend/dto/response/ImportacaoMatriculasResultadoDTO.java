package ac.gov.br.sigei_backend.dto.response;

import java.util.List;

public record ImportacaoMatriculasResultadoDTO(
        Integer linhasLidas,
        Integer escolasEncontradas,
        Integer registrosAnuaisEncontrados,
        Integer niveisInseridos,
        Integer seriesInseridas,
        Integer registrosAnuaisAtualizados,
        Integer linhasIgnoradas,
        List<String> erros
) {
}