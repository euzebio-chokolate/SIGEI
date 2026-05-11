package ac.gov.br.sigei_backend.DTOs.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TerraIndigenaRequestDTO(
        Long id,

        @NotBlank(message = "O nome não pode ser vazio")
        String nome,

        @NotNull(message = "O município é obrigatório")
        Long municipioId
) {
}
