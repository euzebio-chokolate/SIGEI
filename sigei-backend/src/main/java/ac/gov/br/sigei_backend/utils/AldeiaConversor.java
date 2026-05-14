package ac.gov.br.sigei_backend.utils;

import ac.gov.br.sigei_backend.DTOs.response.AldeiaResponseDTO;
import ac.gov.br.sigei_backend.DTOs.response.MunicipioResponseDTO;
import ac.gov.br.sigei_backend.DTOs.response.TerraIndigenaResponseDTO;
import ac.gov.br.sigei_backend.models.Aldeia;
import org.springframework.stereotype.Component;

@Component
public class AldeiaConversor {
    public AldeiaResponseDTO converterAldeia(Aldeia aldeia){

        MunicipioResponseDTO municipioDTO = new MunicipioResponseDTO(
                aldeia.getTerraIndigena().getMunicipio().getId(),
                aldeia.getTerraIndigena().getMunicipio().getNome()
        );

        TerraIndigenaResponseDTO terraIndigenaDTO = new TerraIndigenaResponseDTO(
                aldeia.getTerraIndigena().getId(),
                aldeia.getTerraIndigena().getNome(),
                municipioDTO
        );

        AldeiaResponseDTO aldeiaResponseDTO = new AldeiaResponseDTO(
                aldeia.getId(),
                aldeia.getNome(),
                terraIndigenaDTO
        );
        return aldeiaResponseDTO;
    }
}
