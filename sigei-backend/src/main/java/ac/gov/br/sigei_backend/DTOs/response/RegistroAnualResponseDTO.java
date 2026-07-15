package ac.gov.br.sigei_backend.DTOs.response;

import ac.gov.br.sigei_backend.DTOs.response.resumo.EscolaResumoDTO;

public record RegistroAnualResponseDTO(
        Long id,
        EscolaResumoDTO escola,
        Integer ano,
        String rede,
        String localFuncionamento
) {
}