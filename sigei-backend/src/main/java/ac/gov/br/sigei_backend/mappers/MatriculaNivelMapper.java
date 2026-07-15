package ac.gov.br.sigei_backend.mappers;

import ac.gov.br.sigei_backend.DTOs.request.MatriculaNivelRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.MatriculaNivelResponseDTO;
import ac.gov.br.sigei_backend.models.MatriculaNivel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
        componentModel = "spring",
        uses = {RegistroAnualMapper.class}
)
public interface MatriculaNivelMapper {

    MatriculaNivelResponseDTO converter(MatriculaNivel matricula);

    @Mapping(target = "registro", ignore = true)
    MatriculaNivel converter(MatriculaNivelRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registro", ignore = true)
    void atualizar(MatriculaNivelRequestDTO dto, @MappingTarget MatriculaNivel matricula);
}