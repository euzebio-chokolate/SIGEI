package ac.gov.br.sigei_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MatriculaNivelRequestDTO(

        Long id,

        @NotNull(message = "Registro anual é obrigatório")
        Long registroId,

        @NotBlank(message = "Nível é obrigatório")
        String nivel,

        Integer qtdMasculino,
        Integer qtdFeminino,
        Integer total

) {
}