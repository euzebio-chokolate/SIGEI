package ac.gov.br.sigei_backend.mappers;

import ac.gov.br.sigei_backend.DTOs.request.InfraestruturaEscolaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.InfraestruturaEscolaResponseDTO;
import ac.gov.br.sigei_backend.models.InfraestruturaEscola;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
        componentModel = "spring",
        uses = {EscolaMapper.class}
)
public interface InfraestruturaEscolaMapper {

    InfraestruturaEscolaResponseDTO converter(InfraestruturaEscola infraestrutura);

    @Mapping(target = "escola", ignore = true)
    InfraestruturaEscola converter(InfraestruturaEscolaRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "escola", ignore = true)
    void atualizar(InfraestruturaEscolaRequestDTO dto,
                   @MappingTarget InfraestruturaEscola infraestrutura);
}