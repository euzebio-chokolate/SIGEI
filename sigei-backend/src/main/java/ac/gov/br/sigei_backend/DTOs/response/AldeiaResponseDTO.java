package ac.gov.br.sigei_backend.DTOs.response;

import ac.gov.br.sigei_backend.DTOs.response.resumo.PovoResumoDTO;

import java.util.List;

public record AldeiaResponseDTO(
        Long id,
        String nome,

        TerraIndigenaResponseDTO terraIndigena,
        List<PovoResumoDTO> povos
) {
}
