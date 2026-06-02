package ac.gov.br.sigei_backend.utils;

import ac.gov.br.sigei_backend.DTOs.request.TerraIndigenaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.TerraIndigenaResponseDTO;
import ac.gov.br.sigei_backend.models.TerraIndigena;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        uses = {MunicipioMapper.class}
)
public interface TerraIndigenaMapper {
    TerraIndigenaResponseDTO converter(TerraIndigena terraIndigena);

    @Mapping(target = "municipio", ignore = true)
    TerraIndigena converter(TerraIndigenaRequestDTO terraIndigenaRequestDTO);
}
