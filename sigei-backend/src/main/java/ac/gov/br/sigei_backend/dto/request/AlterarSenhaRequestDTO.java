package ac.gov.br.sigei_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlterarSenhaRequestDTO(

        @NotBlank(message = "Senha atual não pode ser vazia")
        String senhaAtual,

        @NotBlank(message = "Nova senha não pode ser vazia")
        // O BCrypt não aceita mais de 72 bytes
        @Size(min = 8, max = 72, message = "Nova senha deve ter entre 8 e 72 caracteres")
        String novaSenha,

        @NotBlank(message = "Confirmação da senha não pode ser vazia")
        String confirmacaoSenha
) {
}
