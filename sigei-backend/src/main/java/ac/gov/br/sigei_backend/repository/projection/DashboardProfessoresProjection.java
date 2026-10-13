package ac.gov.br.sigei_backend.repository.projection;

public interface DashboardProfessoresProjection {

    Long getTotalProfessores();

    Long getTotalLotacoes();

    Long getVagasEmAberto();

    Long getLotacoesNoNucleo();
}
