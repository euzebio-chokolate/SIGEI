package ac.gov.br.sigei_backend.dto.request;

import jakarta.validation.constraints.Size;

public record MatriculaSerieRequestDTO(
        Long id,
        Long registroId,
        @Size(max = 255, message = "Denominação deve ter no máximo 255 caracteres")
        String denominacao,
        @Size(max = 255, message = "Etapa de ensino deve ter no máximo 255 caracteres")
        String etapaEnsino,
        Integer qtdMasculino,
        Integer qtdFeminino,
        Integer total
) {
}
