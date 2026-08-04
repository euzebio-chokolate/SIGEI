package ac.gov.br.sigei_backend.DTOs.request;

import jakarta.validation.constraints.NotNull;

public record AnoReferenciaRequestDTO(
        Long id,
        @NotNull Integer ano
) {
}
