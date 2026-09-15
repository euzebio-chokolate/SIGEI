package ac.gov.br.sigei_backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PovoRequestDTO(

        Long id,

        @NotBlank(message = "Nome não pode ser vazio")
        String nome

) {
}