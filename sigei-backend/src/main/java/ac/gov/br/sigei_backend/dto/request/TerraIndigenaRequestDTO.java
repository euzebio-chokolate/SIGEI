package ac.gov.br.sigei_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TerraIndigenaRequestDTO(
        Long id,

        @NotBlank(message = "O nome não pode ser vazio")
        @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
        String nome,

        @NotNull(message = "O município é obrigatório")
        Long municipioId
) {
}
