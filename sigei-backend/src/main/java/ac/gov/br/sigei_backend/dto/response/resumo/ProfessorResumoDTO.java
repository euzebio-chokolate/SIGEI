package ac.gov.br.sigei_backend.dto.response.resumo;

public record ProfessorResumoDTO(
        Long id,
        String nome,
        String etnia,
        String sexo,
        String formacao
) {
}
