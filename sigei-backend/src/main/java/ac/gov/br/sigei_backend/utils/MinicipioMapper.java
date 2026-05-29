package ac.gov.br.sigei_backend.utils;

import ac.gov.br.sigei_backend.DTOs.request.MunicipioRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.MunicipioResponseDTO;
import ac.gov.br.sigei_backend.models.Municipio;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring"
)
public interface MinicipioMapper {
    public MunicipioResponseDTO converterMunicipio(Municipio municipio);
    public  Municipio converterDTO(MunicipioRequestDTO municipioRequestDTO);
}
