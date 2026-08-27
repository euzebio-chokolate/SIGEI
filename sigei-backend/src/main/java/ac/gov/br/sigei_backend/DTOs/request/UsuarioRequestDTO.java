package ac.gov.br.sigei_backend.DTOs.request;

import ac.gov.br.sigei_backend.models.enums.PerfilUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioRequestDTO(

        Long id,

        @NotBlank(message = "Nome não pode ser vazio")
        String nome,

        @NotBlank(message = "E-mail não pode ser vazio")
        @Email(message = "E-mail inválido")
        String email,

        @NotBlank(message = "Senha não pode ser vazia")
        String senha,

        @NotNull(message = "Perfil é obrigatório")
        PerfilUsuario perfil,

        Boolean ativo
) {
}