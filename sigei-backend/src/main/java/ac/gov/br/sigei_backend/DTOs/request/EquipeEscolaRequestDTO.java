package ac.gov.br.sigei_backend.DTOs.request;

import jakarta.validation.constraints.NotNull;

public record EquipeEscolaRequestDTO(
        Long id,

        @NotNull(message = "Escola é obrigatória")
        Long escolaId,

        @NotNull(message = "Ano de referência é obrigatório")
        int anoReferencia,

        boolean temProfessorResponsavel,
        boolean temEquipeGestora,
        boolean temMerendeira,
        boolean temZeladora,
        boolean temBarqueiro,
        boolean necessitaTransporte
) {
}
