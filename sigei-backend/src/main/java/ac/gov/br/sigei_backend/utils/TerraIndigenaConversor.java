package ac.gov.br.sigei_backend.utils;

import ac.gov.br.sigei_backend.DTOs.response.MunicipioResponseDTO;
import ac.gov.br.sigei_backend.DTOs.response.TerraIndigenaResponseDTO;
import ac.gov.br.sigei_backend.models.TerraIndigena;
import org.springframework.stereotype.Component;

@Component
public class TerraIndigenaConversor {
    public TerraIndigenaResponseDTO converterTerraIndigena(TerraIndigena terraIndigena){
         MunicipioResponseDTO municipioDTO = new MunicipioResponseDTO(
                 terraIndigena.getMunicipio().getId(),
                 terraIndigena.getMunicipio().getNome()
         );

         TerraIndigenaResponseDTO terraIndigenaResponseDTO = new TerraIndigenaResponseDTO(
                 terraIndigena.getId(),
                 terraIndigena.getNome(),
                 municipioDTO
         );
         return terraIndigenaResponseDTO;
    }
}
