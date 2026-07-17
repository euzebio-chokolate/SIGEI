package ac.gov.br.sigei_backend.DTOs.request;

import jakarta.validation.constraints.NotNull;

public record AnexoRequestDTO(

        Long id,

        @NotNull(message = "Escola é obrigatória")
        Long escolaId,

        @NotNull(message = "Escola anexo é obrigatória")
        Long escolaAnexoId

) {
}