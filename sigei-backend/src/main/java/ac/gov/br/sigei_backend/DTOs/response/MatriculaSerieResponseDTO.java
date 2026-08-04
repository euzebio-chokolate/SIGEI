package ac.gov.br.sigei_backend.DTOs.response;

import ac.gov.br.sigei_backend.DTOs.response.resumo.RegistroAnualResumoDTO;

public record MatriculaSerieResponseDTO(
        Long id,
        String denominacao,
        String etapaEnsino,
        Integer qtdMasculino,
        Integer qtdFeminino

) {
}
