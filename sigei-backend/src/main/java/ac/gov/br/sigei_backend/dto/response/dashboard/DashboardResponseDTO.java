package ac.gov.br.sigei_backend.dto.response.dashboard;

import java.util.List;

public record DashboardResponseDTO(
        Integer anoReferencia,
        DashboardResumoDTO resumo,
        List<DashboardEvolucaoDTO> evolucaoMatriculas,
        List<DashboardEvolucaoDTO> evolucaoEscolas,
        List<DashboardItemDTO> matriculasPorNivel,
        DashboardInfraestruturaDTO infraestrutura,
        List<DashboardItemDTO> escolasPorMunicipio,
        List<DashboardItemDTO> situacaoEscolas
) {
}
