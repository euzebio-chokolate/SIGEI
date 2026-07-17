package ac.gov.br.sigei_backend.DTOs.response;

import ac.gov.br.sigei_backend.DTOs.response.resumo.EscolaResumoDTO;

public record AnexoResponseDTO(
        Long id,
        EscolaResumoDTO escola,
        EscolaResumoDTO escolaAnexo
) {
}