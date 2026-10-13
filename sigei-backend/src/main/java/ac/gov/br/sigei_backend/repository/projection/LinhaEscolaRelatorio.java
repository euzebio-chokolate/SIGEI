package ac.gov.br.sigei_backend.repository.projection;

/** Escola com o registro e a infraestrutura de um ano, para os relatórios. */
public record LinhaEscolaRelatorio(
        Long registroId,
        Long municipioId,
        String municipio,
        String terraIndigena,
        String aldeia,
        String povo,
        String codigo,
        String escola,
        String tipoUnidade,
        String sede,
        Integer totalAlunos,
        String situacao,
        String tipoAcesso,
        Boolean temPredio,
        Integer numSalas,
        Boolean temInternet,
        String tipoAgua,
        String tipoEnergia,
        Long infraestruturaId
) {
}
