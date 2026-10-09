package ac.gov.br.sigei_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Sem professorId, a lotação é uma vaga em aberto. Sem escolaId, é uma lotação no
 * núcleo de educação do município e municipioId é obrigatório; com escola, o
 * município é o da escola. O número do contrato é sensível.
 */
public record LotacaoProfessorRequestDTO(

        Long id,

        Long professorId,

        @NotNull(message = "Ano de referência é obrigatório")
        Long anoReferenciaId,

        Long escolaId,

        Long municipioId,

        @Size(max = 255, message = "Cargo deve ter no máximo 255 caracteres")
        String cargo,

        @Size(max = 255, message = "Segmento deve ter no máximo 255 caracteres")
        String segmento,

        @Size(max = 30, message = "Número do contrato deve ter no máximo 30 caracteres")
        String numeroContrato,

        LocalDate inicioContrato,

        @Size(max = 255, message = "Amparo legal deve ter no máximo 255 caracteres")
        String amparoLegal,

        String tipoContrato

) {
}
