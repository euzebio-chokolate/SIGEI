package ac.gov.br.sigei_backend.DTOs.response;

import ac.gov.br.sigei_backend.DTOs.response.resumo.EscolaResumoDTO;

public record EquipeEscolaResponseDTO(
        Long id,
        EscolaResumoDTO escola,
        int anoReferencia,
        boolean temProfessorResponsavel,
        boolean temEquipeGestora,
        boolean temMerendeira,
        boolean temZeladora,
        boolean temBarqueiro,
        boolean necessitaTransporte
) {
}
