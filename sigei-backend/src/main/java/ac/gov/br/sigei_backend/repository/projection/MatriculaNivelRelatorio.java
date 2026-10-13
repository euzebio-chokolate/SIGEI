package ac.gov.br.sigei_backend.repository.projection;

public record MatriculaNivelRelatorio(
        Long registroId,
        String nivel,
        Integer total,
        Integer masculino,
        Integer feminino
) {
}
