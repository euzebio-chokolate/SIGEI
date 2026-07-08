package ac.gov.br.sigei_backend.DTOs.response;

import ac.gov.br.sigei_backend.DTOs.response.resumo.PovoResumoDTO;

public record AldeiaResponseDTO(

        Long id,

        String nome,

        TerraIndigenaResponseDTO terraIndigena,

        PovoResumoDTO povo

) {
}