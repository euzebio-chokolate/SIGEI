package ac.gov.br.sigei_backend.mappers;

import ac.gov.br.sigei_backend.DTOs.request.AldeiaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.AldeiaResponseDTO;
import ac.gov.br.sigei_backend.models.Aldeia;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",
        uses = {TerraIndigenaMapper.class}
)
public interface AldeiaMapper {
    public AldeiaResponseDTO converter(Aldeia aldeia);

    @Mapping(target = "terraIndigena", ignore = true)
    public Aldeia converter(AldeiaRequestDTO aldeiaRequestDTO);
}
