package ac.gov.br.sigei_backend.DTOs.request;

import ac.gov.br.sigei_backend.models.Escola;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EscolaRequestDTO(
        Long id,

        @NotBlank(message = "O nome não pode ser vazio!")
        @NotNull(message = "")
        String nome,

        Double latitude,
        Double longitude

        ) {

    public Escola ConverterParaEntidade() {
        Escola escola = new Escola();
        escola.setId(id);
        escola.setNome(nome);
        escola.setLatitude(latitude);
        escola.setLongitude(longitude);
        return escola;
    }
}
