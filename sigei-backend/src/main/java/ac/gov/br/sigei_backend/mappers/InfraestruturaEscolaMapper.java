package ac.gov.br.sigei_backend.mappers;

import ac.gov.br.sigei_backend.dto.request.InfraestruturaEscolaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.InfraestruturaEscolaResponseDTO;
import ac.gov.br.sigei_backend.models.InfraestruturaEscola;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
        componentModel = "spring",
        uses = {
                EscolaMapper.class,
                AnoReferenciaMapper.class
        }
)
public interface InfraestruturaEscolaMapper {
    @Mapping(
            target = "escola",
            source = "registroAnual.escola"
    )
    @Mapping(
            target = "anoReferencia",
            source = "registroAnual.anoReferencia"
    )
    InfraestruturaEscolaResponseDTO converter(InfraestruturaEscola infraestrutura);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registroAnual", ignore = true)
    InfraestruturaEscola converter(InfraestruturaEscolaRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registroAnual", ignore = true)
    void atualizar(InfraestruturaEscolaRequestDTO dto, @MappingTarget InfraestruturaEscola infraestrutura);
}