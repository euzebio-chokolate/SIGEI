package ac.gov.br.sigei_backend.mappers;

import ac.gov.br.sigei_backend.dto.request.MunicipioRequestDTO;
import ac.gov.br.sigei_backend.dto.response.MunicipioResponseDTO;
import ac.gov.br.sigei_backend.dto.response.resumo.MunicipioResumoDTO;
import ac.gov.br.sigei_backend.models.Municipio;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MunicipioMapper {
    MunicipioResumoDTO converterResumo(Municipio municipio);
    MunicipioResponseDTO converter(Municipio municipio);
    Municipio converter(MunicipioRequestDTO municipioRequestDTO);
}
