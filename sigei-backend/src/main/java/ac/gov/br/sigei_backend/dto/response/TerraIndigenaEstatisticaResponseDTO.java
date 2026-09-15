package ac.gov.br.sigei_backend.dto.response;

import ac.gov.br.sigei_backend.dto.response.resumo.MunicipioResumoDTO;

public record TerraIndigenaEstatisticaResponseDTO(
        Long id,
        String nome,
        MunicipioResumoDTO municipio,
        Long quantidadeAldeias,
        Long quantidadeEscolas
) {
}