package ac.gov.br.sigei_backend.DTOs.request;

import jakarta.validation.constraints.NotNull;

public record EquipeEscolaRequestDTO(

        Long id,

        Long registroAnualId,

        Boolean possuiDiretor,
        Boolean possuiCoordenador,
        Boolean possuiSecretario,
        Integer quantidadeProfessores,
        Integer quantidadeFuncionarios

) {
}