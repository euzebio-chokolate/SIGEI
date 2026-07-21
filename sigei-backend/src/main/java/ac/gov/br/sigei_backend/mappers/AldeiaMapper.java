package ac.gov.br.sigei_backend.mappers;

import ac.gov.br.sigei_backend.DTOs.request.AldeiaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.AldeiaResponseDTO;
import ac.gov.br.sigei_backend.DTOs.response.resumo.AldeiaResumoDTO;
import ac.gov.br.sigei_backend.models.Aldeia;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = {
                TerraIndigenaMapper.class
        }
)
public interface AldeiaMapper {
    AldeiaResponseDTO converter(Aldeia aldeia);
    AldeiaResumoDTO converterResumo(Aldeia aldeia);

    @Mapping(target = "terraIndigena", ignore = true)
    @Mapping(target = "povo", ignore = true)
    Aldeia converter(AldeiaRequestDTO dto);
}