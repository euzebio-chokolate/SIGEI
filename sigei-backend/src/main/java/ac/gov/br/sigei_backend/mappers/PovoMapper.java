package ac.gov.br.sigei_backend.mappers;

import ac.gov.br.sigei_backend.dto.request.PovoRequestDTO;
import ac.gov.br.sigei_backend.dto.response.PovoResponseDTO;
import ac.gov.br.sigei_backend.dto.response.resumo.PovoResumoDTO;
import ac.gov.br.sigei_backend.models.Povo;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring"
)
public interface PovoMapper {
    PovoResponseDTO converter(Povo povo);
    PovoResumoDTO converterResumo(Povo povo);
    Povo converter(PovoRequestDTO dto);
}