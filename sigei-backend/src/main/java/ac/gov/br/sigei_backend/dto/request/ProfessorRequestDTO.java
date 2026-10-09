package ac.gov.br.sigei_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * CPF e matrícula são sensíveis e opcionais: o CPF aceita pontuação e é gravado só
 * com os dígitos. Na edição, os valores informados substituem os gravados; vazio
 * limpa o campo.
 */
public record ProfessorRequestDTO(

        Long id,

        @NotBlank(message = "Nome não pode ser vazio")
        @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
        String nome,

        @Size(max = 14, message = "CPF deve ter 11 dígitos")
        String cpf,

        @Size(max = 30, message = "Matrícula deve ter no máximo 30 caracteres")
        String matricula,

        @Size(max = 255, message = "Etnia deve ter no máximo 255 caracteres")
        String etnia,

        @Pattern(regexp = "[MF]", message = "Sexo deve ser M ou F")
        String sexo,

        @Size(max = 255, message = "Formação deve ter no máximo 255 caracteres")
        String formacao

) {
}
