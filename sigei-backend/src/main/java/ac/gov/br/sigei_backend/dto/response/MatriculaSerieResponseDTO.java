package ac.gov.br.sigei_backend.dto.response;

import ac.gov.br.sigei_backend.dto.response.resumo.RegistroAnualResumoDTO;

public record MatriculaSerieResponseDTO(
        Long id,
        String denominacao,
        String etapaEnsino,
        Integer qtdMasculino,
        Integer qtdFeminino,
        Integer total

) {
}
