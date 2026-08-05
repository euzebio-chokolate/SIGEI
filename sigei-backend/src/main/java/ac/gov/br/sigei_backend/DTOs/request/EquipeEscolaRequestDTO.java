package ac.gov.br.sigei_backend.DTOs.request;

import jakarta.validation.constraints.NotNull;

public record EquipeEscolaRequestDTO(

        Long id,
        Long registroAnualId,

        boolean temProfessorResponsavel,
        boolean temEquipeGestora,
        boolean temMerendeira,
        boolean temZeladora,
        boolean temBarqueiro,
        boolean necessitaTransporte

) {
}