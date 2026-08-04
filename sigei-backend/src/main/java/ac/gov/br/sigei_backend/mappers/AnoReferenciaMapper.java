package ac.gov.br.sigei_backend.mappers;

import ac.gov.br.sigei_backend.DTOs.request.AnoReferenciaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.AnoReferenciaResponseDTO;
import ac.gov.br.sigei_backend.DTOs.response.resumo.AnoReferenciaResumoDTO;
import ac.gov.br.sigei_backend.models.AnoReferencia;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AnoReferenciaMapper {

    AnoReferenciaResponseDTO converter(AnoReferencia anoReferencia);

    AnoReferenciaResumoDTO converterResumo(AnoReferencia anoReferencia);

    AnoReferencia converter(AnoReferenciaRequestDTO dto);

    void atualizar(AnoReferenciaRequestDTO dto,
                   @MappingTarget AnoReferencia anoReferencia);
}