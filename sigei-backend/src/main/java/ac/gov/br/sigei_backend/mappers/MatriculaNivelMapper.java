package ac.gov.br.sigei_backend.mappers;

import ac.gov.br.sigei_backend.dto.request.MatriculaNivelRequestDTO;
import ac.gov.br.sigei_backend.dto.response.MatriculaNivelResponseDTO;
import ac.gov.br.sigei_backend.models.MatriculaNivel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MatriculaNivelMapper {

    MatriculaNivelResponseDTO converter(MatriculaNivel matriculaNivel);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registro", ignore = true)
    MatriculaNivel converter(MatriculaNivelRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registro", ignore = true)
    void atualizar(MatriculaNivelRequestDTO dto, @MappingTarget MatriculaNivel matriculaNivel);
}