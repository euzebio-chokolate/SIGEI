package ac.gov.br.sigei_backend.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MunicipioRequestDTO(
        Long id,

        @NotBlank(message = "Nome não pode ser vazio")
        @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
        @Column(unique = true)
        String nome
) {
}
