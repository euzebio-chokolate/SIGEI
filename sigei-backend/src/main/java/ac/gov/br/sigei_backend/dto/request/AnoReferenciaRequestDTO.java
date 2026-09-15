package ac.gov.br.sigei_backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record AnoReferenciaRequestDTO(
        Long id,

        @NotNull
        Integer ano
) {
}
