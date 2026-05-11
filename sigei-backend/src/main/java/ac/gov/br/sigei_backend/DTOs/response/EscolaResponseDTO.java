package ac.gov.br.sigei_backend.DTOs.response;

public record EscolaResponseDTO(
        Long id,
        String codigo,
        String nome,

        ac.gov.br.sigei_backend.models.Municipio municipioResponseDTO,
        ac.gov.br.sigei_backend.models.TerraIndigena terraIndigenaResponseDTO,
        ac.gov.br.sigei_backend.models.Aldeia aldeiaResponseDTO
) {
}
