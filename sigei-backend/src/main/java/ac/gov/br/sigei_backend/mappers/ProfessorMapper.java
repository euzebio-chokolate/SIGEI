package ac.gov.br.sigei_backend.mappers;

import ac.gov.br.sigei_backend.dto.request.ProfessorRequestDTO;
import ac.gov.br.sigei_backend.dto.response.ProfessorResponseDTO;
import ac.gov.br.sigei_backend.dto.response.resumo.ProfessorResumoDTO;
import ac.gov.br.sigei_backend.models.Professor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProfessorMapper {

    ProfessorResponseDTO converter(Professor professor);

    ProfessorResumoDTO converterResumo(Professor professor);

    // O CPF é normalizado e indexado pelo service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cpf", ignore = true)
    @Mapping(target = "cpfHash", ignore = true)
    @Mapping(target = "lotacoes", ignore = true)
    void atualizar(ProfessorRequestDTO dto, @MappingTarget Professor professor);
}
