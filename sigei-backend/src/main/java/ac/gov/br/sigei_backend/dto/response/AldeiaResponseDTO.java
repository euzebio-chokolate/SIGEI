package ac.gov.br.sigei_backend.dto.response;

import ac.gov.br.sigei_backend.dto.response.resumo.PovoResumoDTO;

public record AldeiaResponseDTO(

        Long id,

        String nome,

        TerraIndigenaResponseDTO terraIndigena,

        PovoResumoDTO povo

) {
}