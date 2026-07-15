package ac.gov.br.sigei_backend.mappers;

import ac.gov.br.sigei_backend.DTOs.request.RegistroAnualRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.RegistroAnualResponseDTO;
import ac.gov.br.sigei_backend.DTOs.response.resumo.RegistroAnualResumoDTO;
import ac.gov.br.sigei_backend.models.RegistroAnual;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(
        componentModel = "spring",
        uses = {EscolaMapper.class}
)
public interface RegistroAnualMapper {

    RegistroAnualResponseDTO converter(RegistroAnual registro);

    RegistroAnualResumoDTO converterResumo(RegistroAnual registro);

    @Mapping(target = "escola", ignore = true)
    RegistroAnual converter(RegistroAnualRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "escola", ignore = true)
    void atualizar(RegistroAnualRequestDTO dto, @MappingTarget RegistroAnual registro);
}