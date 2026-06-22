package ac.gov.br.sigei_backend.DTOs.response;

public record EscolaResponseDTO(
        Long id,
        String codigo,
        String nome,
        String tipoUnidade,

        EscolaResumoResponseDTO escolaSede,

        MunicipioResponseDTO municipio,
        TerraIndigenaResponseDTO terraIndigena,
        AldeiaResponseDTO aldeia
) {
}
