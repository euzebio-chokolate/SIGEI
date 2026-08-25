package ac.gov.br.sigei_backend.DTOs.request;

public record MatriculaSerieRequestDTO(
        Long id,
        Long registroId,
        String denominacao,
        String etapaEnsino,
        Integer qtdMasculino,
        Integer qtdFeminino,
        Integer total
) {
}
