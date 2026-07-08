package ac.gov.br.sigei_backend.mappers;

import ac.gov.br.sigei_backend.DTOs.request.PovoRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.PovoResponseDTO;
import ac.gov.br.sigei_backend.DTOs.response.resumo.PovoResumoDTO;
import ac.gov.br.sigei_backend.models.Povo;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        uses = {AldeiaMapper.class}
)
public interface PovoMapper {

    PovoResponseDTO converter(Povo povo);

    PovoResumoDTO converterResumo(Povo povo);

    Povo converter(PovoRequestDTO dto);
}