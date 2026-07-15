package ac.gov.br.sigei_backend.DTOs.request;

import jakarta.validation.constraints.NotNull;

public record InfraestruturaEscolaRequestDTO(
        Long id,

        @NotNull(message = "Escola é obrigatória")
        Long escolaId,

        @NotNull(message = "Ano de referência é obrigatório")
        int anoReferencia,

        int anoConstrucao,
        int numSalas,
        Double latitude,
        Double longitude,
        String situacao,
        String tipoAcesso,
        Boolean temPredio,
        Boolean temAgua,
        Boolean temInternet,
        Boolean temEnergia,
        Boolean temRegistroFotografico

) {
}