package ac.gov.br.sigei_backend.DTOs.response;

import ac.gov.br.sigei_backend.DTOs.response.resumo.EscolaResumoDTO;

public record EscolaResponseDTO(
        Long id,
        String codigo,
        String nome,
        String tipoUnidade,
        EscolaResumoDTO escolaSede,
        AldeiaResponseDTO aldeia
) {
}
