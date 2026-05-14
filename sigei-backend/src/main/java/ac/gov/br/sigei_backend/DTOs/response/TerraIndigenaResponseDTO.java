package ac.gov.br.sigei_backend.DTOs.response;

public record TerraIndigenaResponseDTO(
        Long id,
        String nome,

        MunicipioResponseDTO municipio
) {
}
