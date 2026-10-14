package ac.gov.br.sigei_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EscolaRequestDTO(
        Long id,

        @NotBlank(message = "O código não pode ser vazio!")
        @Size(max = 255, message = "Código deve ter no máximo 255 caracteres")
        String codigo,

        @NotBlank(message = "O nome não pode ser vazio!")
        @Size(max = 255, message = "Nome deve ter no máximo 255 caracteres")
        String nome,

        @NotBlank(message = "O tipo da unidade não pode ser vazio!")
        String tipoUnidade,

        Long escolaSedeId,

        @NotNull(message = "A aldeia é obrigatória!")
        Long aldeiaId
        ) {
}
