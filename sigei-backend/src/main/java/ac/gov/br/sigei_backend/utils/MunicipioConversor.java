package ac.gov.br.sigei_backend.utils;

import ac.gov.br.sigei_backend.DTOs.request.MunicipioRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.MunicipioResponseDTO;
import ac.gov.br.sigei_backend.models.Municipio;
import org.springframework.stereotype.Component;

@Component
public class MunicipioConversor {
    public MunicipioResponseDTO converterMunicipio(Municipio municipio){
        MunicipioResponseDTO municipioResponseDTO = new MunicipioResponseDTO(
                municipio.getId(),
                municipio.getNome()
        );
        return municipioResponseDTO;
    }

    public Municipio converterDTO(MunicipioRequestDTO municipioRequestDTO){
        Municipio municipio = new Municipio();
        municipio.setId(municipioRequestDTO.id());
        municipio.setNome(municipioRequestDTO.nome());
        return municipio;
    }

}
