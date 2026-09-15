package ac.gov.br.sigei_backend.dto.response;

import ac.gov.br.sigei_backend.dto.response.resumo.EscolaResumoDTO;

public record AnexoResponseDTO(
        Long id,
        EscolaResumoDTO escola,
        EscolaResumoDTO escolaAnexo
) {
}