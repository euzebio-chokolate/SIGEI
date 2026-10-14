package ac.gov.br.sigei_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MatriculaNivelRequestDTO(

        Long id,

        @NotNull(message = "Registro anual é obrigatório")
        Long registroId,

        @NotBlank(message = "Nível é obrigatório")
        @Size(max = 255, message = "Nível deve ter no máximo 255 caracteres")
        String nivel,

        Integer qtdMasculino,
        Integer qtdFeminino,
        Integer total

) {
}