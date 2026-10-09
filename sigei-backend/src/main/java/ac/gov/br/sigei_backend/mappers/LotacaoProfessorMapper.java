package ac.gov.br.sigei_backend.mappers;

import ac.gov.br.sigei_backend.dto.request.LotacaoProfessorRequestDTO;
import ac.gov.br.sigei_backend.dto.response.LotacaoProfessorResponseDTO;
import ac.gov.br.sigei_backend.models.LotacaoProfessor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
        componentModel = "spring",
        uses = {
                ProfessorMapper.class,
                AnoReferenciaMapper.class,
                EscolaMapper.class,
                MunicipioMapper.class
        }
)
public interface LotacaoProfessorMapper {

    LotacaoProfessorResponseDTO converter(LotacaoProfessor lotacao);

    // Relacionamentos e tipo de contrato são resolvidos e validados pelo service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "professor", ignore = true)
    @Mapping(target = "anoReferencia", ignore = true)
    @Mapping(target = "escola", ignore = true)
    @Mapping(target = "municipio", ignore = true)
    @Mapping(target = "tipoContrato", ignore = true)
    void atualizar(LotacaoProfessorRequestDTO dto, @MappingTarget LotacaoProfessor lotacao);
}
