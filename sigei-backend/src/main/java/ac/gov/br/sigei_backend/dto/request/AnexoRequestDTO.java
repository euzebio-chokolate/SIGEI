package ac.gov.br.sigei_backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record AnexoRequestDTO(

        Long id,

        @NotNull(message = "Escola é obrigatória")
        Long escolaId,

        @NotNull(message = "Escola anexo é obrigatória")
        Long escolaAnexoId

) {
}