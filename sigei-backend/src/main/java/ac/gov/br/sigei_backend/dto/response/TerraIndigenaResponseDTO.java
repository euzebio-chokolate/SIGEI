package ac.gov.br.sigei_backend.dto.response;

public record TerraIndigenaResponseDTO(
        Long id,
        String nome,

        MunicipioResponseDTO municipio
) {
}
