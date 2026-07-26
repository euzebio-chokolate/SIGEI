package ac.gov.br.sigei_backend.DTOs.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EscolaRequestDTO(
        Long id,

        @NotBlank(message = "O código não pode ser vazio!")
        String codigo,

        @NotBlank(message = "O nome não pode ser vazio!")
        String nome,

        @NotBlank(message = "O tipo da unidade não pode ser vazio!")
        String tipoUnidade,

        Long escolaSedeId,

        @NotNull(message = "A aldeia é obrigatória!")
        Long aldeiaId
        ) {
}
