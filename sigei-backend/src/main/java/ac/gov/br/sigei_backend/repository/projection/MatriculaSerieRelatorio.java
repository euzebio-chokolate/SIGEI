package ac.gov.br.sigei_backend.repository.projection;

public record MatriculaSerieRelatorio(
        Long registroId,
        String denominacao,
        String etapaEnsino,
        Integer total
) {
}
