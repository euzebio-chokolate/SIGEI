package ac.gov.br.sigei_backend.mappers;

import ac.gov.br.sigei_backend.DTOs.request.AnexoRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.AnexoResponseDTO;
import ac.gov.br.sigei_backend.models.Anexo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
        componentModel = "spring",
        uses = {EscolaMapper.class}
)
public interface AnexoMapper {

    AnexoResponseDTO converter(Anexo anexo);

    @Mapping(target = "escola", ignore = true)
    @Mapping(target = "escolaAnexo", ignore = true)
    Anexo converter(AnexoRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "escola", ignore = true)
    @Mapping(target = "escolaAnexo", ignore = true)
    void atualizar(AnexoRequestDTO dto, @MappingTarget Anexo anexo);
}