package ac.gov.br.sigei_backend.DTOs.response;

import ac.gov.br.sigei_backend.DTOs.response.resumo.RegistroAnualResumoDTO;

public record MatriculaNivelResponseDTO(
        Long id,
        String nivel,
        Integer qtdMasculino,
        Integer qtdFeminino,
        Integer total
) {
}