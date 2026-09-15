package ac.gov.br.sigei_backend.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;

public record MunicipioRequestDTO(
        Long id,

        @NotBlank(message = "Nome não pode ser vazio")
        @Column(unique = true)
        String nome
) {
}
