package ac.gov.br.sigei_backend.dto.request;

import ac.gov.br.sigei_backend.dto.validacao.AnoValido;
import jakarta.validation.constraints.NotNull;

public record AnoReferenciaRequestDTO(
        Long id,

        // Até o ano seguinte: a abertura de ano e a importação também criam o próximo ano
        @NotNull(message = "Ano é obrigatório")
        @AnoValido(anosAFrente = 1, message = "Ano deve estar entre 1900 e o próximo ano")
        Integer ano
) {
}
