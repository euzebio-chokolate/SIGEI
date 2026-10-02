package ac.gov.br.sigei_backend.mappers;

import ac.gov.br.sigei_backend.dto.request.CadastroEscolaRequestDTO;
import ac.gov.br.sigei_backend.models.EquipeEscola;
import ac.gov.br.sigei_backend.models.InfraestruturaEscola;
import ac.gov.br.sigei_backend.models.MatriculaNivel;
import ac.gov.br.sigei_backend.models.MatriculaSerie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CadastroEscolaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registroAnual", ignore = true)
    @Mapping(target = "anoReferencia", ignore = true)
    void atualizar(CadastroEscolaRequestDTO.EquipeDTO dto, @MappingTarget EquipeEscola equipe);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registroAnual", ignore = true)
    @Mapping(target = "fotoUrl", ignore = true)
    @Mapping(target = "fotoPublicId", ignore = true)
    void atualizar(CadastroEscolaRequestDTO.InfraestruturaDTO dto, @MappingTarget InfraestruturaEscola infraestrutura);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registro", ignore = true)
    void atualizar(CadastroEscolaRequestDTO.MatriculaNivelDTO dto, @MappingTarget MatriculaNivel matricula);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registro", ignore = true)
    void atualizar(CadastroEscolaRequestDTO.MatriculaSerieDTO dto, @MappingTarget MatriculaSerie matricula);
}
