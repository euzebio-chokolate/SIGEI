package ac.gov.br.sigei_backend.DTOs.response;

import ac.gov.br.sigei_backend.DTOs.response.resumo.AnoReferenciaResumoDTO;

import java.util.List;

public record RegistroAnualResponseDTO(
        Long id,
        AnoReferenciaResumoDTO anoReferencia,
        String rede,
        String localFuncionamento,
        EquipeEscolaResponseDTO equipe,
        InfraestruturaEscolaResponseDTO infraestrutura,
        List<MatriculaNivelResponseDTO>matriculasNivel,
        List<MatriculaSerieResponseDTO> matriculasSerie
) {
}