package ac.gov.br.sigei_backend.DTOs.response;

import ac.gov.br.sigei_backend.models.Aldeia;
import ac.gov.br.sigei_backend.models.Municipio;
import ac.gov.br.sigei_backend.models.TerraIndigena;

public record EscolaResponseDTO(
        Long id,
        String codigo,
        String nome,
        Municipio municipio,
        TerraIndigena terraIndigena,
        Aldeia aldeia

) {
}
