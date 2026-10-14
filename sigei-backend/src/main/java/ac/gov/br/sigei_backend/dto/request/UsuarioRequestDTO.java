package ac.gov.br.sigei_backend.dto.request;

import ac.gov.br.sigei_backend.models.enums.PerfilUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioRequestDTO(

        Long id,

        @NotBlank(message = "Nome não pode ser vazio")
        @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
        String nome,

        @NotBlank(message = "E-mail não pode ser vazio")
        @Email(message = "E-mail inválido")
        @Size(max = 255, message = "E-mail deve ter no máximo 255 caracteres")
        String email,

        // Vazia na edição mantém a senha atual (o cadastro exige no service). Informada,
        // segue a troca de senha: de 8 a 72 caracteres (limite do BCrypt)
        @Pattern(regexp = "\\s*|.{8,72}", message = "Senha deve ter entre 8 e 72 caracteres")
        String senha,

        @NotNull(message = "Perfil é obrigatório")
        PerfilUsuario perfil,

        Boolean ativo
) {
}