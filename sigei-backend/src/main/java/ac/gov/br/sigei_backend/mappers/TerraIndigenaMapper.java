package ac.gov.br.sigei_backend.mappers;

import ac.gov.br.sigei_backend.dto.request.TerraIndigenaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.TerraIndigenaResponseDTO;
import ac.gov.br.sigei_backend.dto.response.resumo.TerraIndigenaResumoDTO;
import ac.gov.br.sigei_backend.models.TerraIndigena;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        uses = {MunicipioMapper.class}
)
public interface TerraIndigenaMapper {
    TerraIndigenaResumoDTO converterResumo(TerraIndigena terraIndigena);
    TerraIndigenaResponseDTO converter(TerraIndigena terraIndigena);

    @Mapping(target = "municipio", ignore = true)
    TerraIndigena converter(TerraIndigenaRequestDTO terraIndigenaRequestDTO);
}
