package ac.gov.br.sigei_backend.DTOs.request;

import jakarta.validation.constraints.NotBlank;

public record MunicipioRequestDTO(
        Long id,

        @NotBlank(message = "Nome não pode ser vazio")
        String nome
) {
}
