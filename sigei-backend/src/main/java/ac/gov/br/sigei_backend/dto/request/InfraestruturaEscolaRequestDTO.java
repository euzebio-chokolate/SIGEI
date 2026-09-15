package ac.gov.br.sigei_backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record InfraestruturaEscolaRequestDTO(
        Long id,

        @NotNull(message = "O registro anual é obrigatório")
        Long registroAnualId,

        Integer anoConstrucao,
        Integer numSalas,
        Double latitude,
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