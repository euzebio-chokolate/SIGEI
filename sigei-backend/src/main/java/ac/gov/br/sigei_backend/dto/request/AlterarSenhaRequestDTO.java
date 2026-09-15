package ac.gov.br.sigei_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlterarSenhaRequestDTO(

        @NotBlank(message = "Senha atual não pode ser vazia")
        String senhaAtual,

        @NotBlank(message = "Nova senha não pode ser vazia")
        @Size(min = 8, message = "Nova senha deve ter pelo menos 8 caracteres")
        String novaSenha,

        @NotBlank(message = "Confirmação da senha não pode ser vazia")
        String confirmacaoSenha
) {
}
