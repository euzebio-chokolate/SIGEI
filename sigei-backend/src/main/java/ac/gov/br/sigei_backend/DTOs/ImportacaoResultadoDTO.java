package ac.gov.br.sigei_backend.DTOs;

import java.util.List;

public record ImportacaoResultadoDTO(
        Integer linhasLidas,
        Integer municipiosCriados,
        Integer terrasIndigenasCriadas,
        Integer povosCriados,
        Integer aldeiasCriadas,
        Integer escolasCriadas,
        Integer registrosAnuaisCriados,
        Integer infraestruturasCriadas,
        Integer equipesCriadas,
        Integer registrosAtualizados,
        Integer linhasIgnoradas,
        List<String> erros
) {
}