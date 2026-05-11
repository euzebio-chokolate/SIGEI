package ac.gov.br.sigei_backend.DTOs.request;

import ac.gov.br.sigei_backend.models.Aldeia;
import ac.gov.br.sigei_backend.models.Escola;
import ac.gov.br.sigei_backend.models.Municipio;
import ac.gov.br.sigei_backend.models.TerraIndigena;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EscolaRequestDTO(
        Long id,

        @NotBlank(message = "O código não pode ser vazio!")
        @NotNull(message = "")
        String codigo,

        @NotBlank(message = "O nome não pode ser vazio!")
        @NotNull(message = "")
        String nome,
        Municipio municipio,
        TerraIndigena terraIndigena,
        Aldeia aldeia
        ) {
}
