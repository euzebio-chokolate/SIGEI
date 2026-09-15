package ac.gov.br.sigei_backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(

        @NotBlank(message = "E-mail não pode ser vazio")
        @Email(message = "E-mail inválido")
        String email,

        @NotBlank(message = "Senha não pode ser vazia")
        String senha

) {
}