package ac.gov.br.sigei_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record RegistroAnualRequestDTO(

        Long id,

        @NotNull(message = "Escola é obrigatória")
        Long escolaId,

        @NotNull(message = "Ano de referencia é obrigatório")
        Long anoReferenciaId,

        @NotBlank(message = "Rede é obrigatória")
        String rede,

        @NotBlank(message = "Local de funcionamento é obrigatório")
        String localFuncionamento,

        @PositiveOrZero(message = "Total de alunos não pode ser negativo")
        Integer totalAlunos

) {
}
