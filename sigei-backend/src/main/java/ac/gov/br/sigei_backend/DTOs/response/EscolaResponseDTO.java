package ac.gov.br.sigei_backend.DTOs.response;

public record EscolaResponseDTO(
        Long id,
        String nome,
        Double latitude,
        Double longitude,
        int qtdAlunos
) {
}
