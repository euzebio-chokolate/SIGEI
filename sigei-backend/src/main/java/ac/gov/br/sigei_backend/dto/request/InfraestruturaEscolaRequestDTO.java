package ac.gov.br.sigei_backend.dto.request;

import ac.gov.br.sigei_backend.dto.validacao.AnoValido;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record InfraestruturaEscolaRequestDTO(
        Long id,

        @NotNull(message = "O registro anual é obrigatório")
        Long registroAnualId,

        @AnoValido(message = "Ano de construção deve estar entre 1900 e o ano atual")
        Integer anoConstrucao,

        @PositiveOrZero(message = "Número de salas não pode ser negativo")
        Integer numSalas,

        @DecimalMin(value = "-90", message = "Latitude deve estar entre -90 e 90")
        @DecimalMax(value = "90", message = "Latitude deve estar entre -90 e 90")
        Double latitude,

        @DecimalMin(value = "-180", message = "Longitude deve estar entre -180 e 180")
        @DecimalMax(value = "180", message = "Longitude deve estar entre -180 e 180")
        Double longitude,

        String situacao,
        String tipoAcesso,
        Boolean temPredio,
        String tipoAgua,
        Boolean temInternet,
        String tipoEnergia,
        Boolean temRegistroFotografico
) {
}