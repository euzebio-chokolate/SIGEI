package ac.gov.br.sigei_backend.DTOs.request;

import jakarta.validation.constraints.NotNull;

public record InfraestruturaEscolaRequestDTO(
        Long id,

        @NotNull
        Long registroAnualId,

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