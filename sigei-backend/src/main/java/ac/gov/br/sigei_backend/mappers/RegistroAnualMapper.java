package ac.gov.br.sigei_backend.mappers;

import ac.gov.br.sigei_backend.dto.request.RegistroAnualRequestDTO;
import ac.gov.br.sigei_backend.dto.response.RegistroAnualResponseDTO;
import ac.gov.br.sigei_backend.dto.response.resumo.RegistroAnualResumoDTO;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
        componentModel = "spring",
        uses = {
                EquipeEscolaMapper.class,
                InfraestruturaEscolaMapper.class,
                MatriculaNivelMapper.class,
                MatriculaSerieMapper.class,
                AnoReferenciaMapper.class
        }
)
public interface RegistroAnualMapper {

    RegistroAnualResponseDTO converter(RegistroAnual registro);

    RegistroAnualResumoDTO converterResumo(RegistroAnual registro);

    @Mapping(target = "escola", ignore = true)
    @Mapping(target = "anoReferencia", ignore = true)
    RegistroAnual converter(RegistroAnualRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "escola", ignore = true)
    @Mapping(target = "anoReferencia", ignore = true)
    void atualizar(RegistroAnualRequestDTO dto,
                   @MappingTarget RegistroAnual registro);
}