package ac.gov.br.sigei_backend.DTOs.response;

import ac.gov.br.sigei_backend.DTOs.response.resumo.RegistroAnualResumoDTO;

public record EquipeEscolaResponseDTO(
        Long id,
        boolean temProfessorResponsavel,
        boolean temEquipeGestora,
        boolean temMerendeira,
        boolean temZeladora,
        boolean temBarqueiro,
        boolean necessitaTransporte
) {
}