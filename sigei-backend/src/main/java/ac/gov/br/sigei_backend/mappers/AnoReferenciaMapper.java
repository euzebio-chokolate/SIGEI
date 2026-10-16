package ac.gov.br.sigei_backend.mappers;

import ac.gov.br.sigei_backend.dto.request.AnoReferenciaRequestDTO;
import ac.gov.br.sigei_backend.dto.response.AnoReferenciaResponseDTO;
import ac.gov.br.sigei_backend.dto.response.resumo.AnoReferenciaResumoDTO;
import ac.gov.br.sigei_backend.models.AnoReferencia;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AnoReferenciaMapper {

    AnoReferenciaResponseDTO converter(AnoReferencia anoReferencia);

    AnoReferenciaResumoDTO converterResumo(AnoReferencia anoReferencia);

    @Mapping(target = "id", ignore = true)
    AnoReferencia converter(AnoReferenciaRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    void atualizar(AnoReferenciaRequestDTO dto,
                   @MappingTarget AnoReferencia anoReferencia);
}