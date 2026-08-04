package ac.gov.br.sigei_backend.DTOs.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegistroAnualRequestDTO(

        Long id,

        @NotNull(message = "Escola é obrigatória")
        Long escolaId,

        @NotNull(message = "Ano de referencia é obrigatório")
        Long anoReferenciaId,

        @NotBlank(message = "Rede é obrigatória")
        String rede,

        @NotBlank(message = "Local de funcionamento é obrigatório")
        String localFuncionamento

) {
}