package ac.gov.br.sigei_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AldeiaRequestDTO(

        Long id,

        @NotBlank(message = "Nome não pode ser vazio")
        String nome,

        @NotNull(message = "A terra indígena é obrigatória")
        Long terraIndigenaId,

        @NotNull(message = "O povo é obrigatório")
        Long povoId

) {
}