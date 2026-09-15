package ac.gov.br.sigei_backend.mappers;

import ac.gov.br.sigei_backend.dto.request.UsuarioRequestDTO;
import ac.gov.br.sigei_backend.dto.response.UsuarioResponseDTO;
import ac.gov.br.sigei_backend.models.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioResponseDTO converter(Usuario usuario);

    @Mapping(target = "senha", ignore = true)
    Usuario converter(UsuarioRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "senha", ignore = true)
    void atualizar(
            UsuarioRequestDTO dto,
            @MappingTarget Usuario usuario
    );
}