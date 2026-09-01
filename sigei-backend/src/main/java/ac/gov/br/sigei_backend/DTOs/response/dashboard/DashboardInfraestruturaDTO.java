package ac.gov.br.sigei_backend.DTOs.response.dashboard;

public record DashboardInfraestruturaDTO(
        Long totalAvaliado,
        Long comInternet,
        Double percentualInternet,
        Long comEnergia,
        Double percentualEnergia,
        Long comPredio,
        Double percentualPredio
) {
}
