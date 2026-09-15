package ac.gov.br.sigei_backend.mappers;

import ac.gov.br.sigei_backend.dto.request.MatriculaSerieRequestDTO;
import ac.gov.br.sigei_backend.dto.response.MatriculaSerieResponseDTO;
import ac.gov.br.sigei_backend.models.MatriculaSerie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MatriculaSerieMapper {
    MatriculaSerieResponseDTO converter(MatriculaSerie matriculaSerie);

    @Mapping(target = "registro", ignore = true)
    MatriculaSerie converter(MatriculaSerieRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registro", ignore = true)
    void atualizar(MatriculaSerieRequestDTO dto, @MappingTarget MatriculaSerie matriculaSerie);
}
