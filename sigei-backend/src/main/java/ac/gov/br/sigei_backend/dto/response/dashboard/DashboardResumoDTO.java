package ac.gov.br.sigei_backend.dto.response.dashboard;

public record DashboardResumoDTO(
        Long totalEscolas,
        Long totalMatriculas,
        Long totalMunicipios,
        Long totalTerrasIndigenas
) {
}
