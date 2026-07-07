package ac.gov.br.sigei_backend.mappers;

import ac.gov.br.sigei_backend.DTOs.request.EscolaRequestDTO;
import ac.gov.br.sigei_backend.DTOs.response.EscolaResponseDTO;
import ac.gov.br.sigei_backend.models.Escola;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring",
        uses = {
            MunicipioMapper.class,
            AldeiaMapper.class
        }
)
public interface EscolaMapper {
    public EscolaResponseDTO converter(Escola escola);

    @Mapping(target = "municipio", ignore = true)
    @Mapping(target = "terraIndigena", ignore = true)
    @Mapping(target = "aldeia", ignore = true)
    @Mapping(target = "escolaSede", ignore = true)
    Escola converter(EscolaRequestDTO dto);

    @Mapping(target = "municipio", ignore = true)
    @Mapping(target = "terraIndigena", ignore = true)
    @Mapping(target = "aldeia", ignore = true)
    @Mapping(target = "escolaSede", ignore = true)
    public void atualizar(EscolaRequestDTO dto, @MappingTarget Escola escola);
}
