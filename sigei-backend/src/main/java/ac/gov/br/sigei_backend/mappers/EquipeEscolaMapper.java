package ac.gov.br.sigei_backend.mappers;

import ac.gov.br.sigei_backend.DTOs.request.EquipeEscolaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.EquipeEscolaResponseDTO;
import ac.gov.br.sigei_backend.models.EquipeEscola;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring",
        uses = {EscolaMapper.class}
)
public interface EquipeEscolaMapper {
    EquipeEscolaResponseDTO converter(EquipeEscola equipeEscola);

    @Mapping(target = "escola", ignore = true)
    EquipeEscola converter(EquipeEscolaRequestDTO equipeEscolaRequestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "escola", ignore = true)
    void atualizar(EquipeEscolaRequestDTO equipeEscolaRequestDTO,
                   @MappingTarget EquipeEscola equipeEscola);
}
