package ac.gov.br.sigei_backend.repository.projection;

/** Escolas e matrículas de um ano em um município (nulo quando a escola não tem município). */
public record EvolucaoRelatorio(
        Integer ano,
        Long municipioId,
        Long escolas,
        Long matriculas
) {
}
