package ac.gov.br.sigei_backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

/**
 * Cadastro completo de uma escola em um ano: dados da escola, registro anual,
 * equipe, infraestrutura e matrículas, gravados em uma única transação.
 * Os vínculos com o registro anual são resolvidos no service, por isso os
 * itens internos não recebem IDs.
 */
public record CadastroEscolaRequestDTO(

        @NotNull(message = "Os dados da escola são obrigatórios")
        @Valid
        EscolaRequestDTO escola,

        @NotNull(message = "Ano de referencia é obrigatório")
        Long anoReferenciaId,

        @NotBlank(message = "Rede é obrigatória")
        String rede,

        @NotBlank(message = "Local de funcionamento é obrigatório")
        String localFuncionamento,

        @PositiveOrZero(message = "Total de alunos não pode ser negativo")
        Integer totalAlunos,

        @NotNull(message = "A equipe escolar é obrigatória")
        @Valid
        EquipeDTO equipe,

        @NotNull(message = "A infraestrutura é obrigatória")
        @Valid
        InfraestruturaDTO infraestrutura,

        List<@Valid MatriculaNivelDTO> matriculasNivel,

        List<@Valid MatriculaSerieDTO> matriculasSerie,

        /** Remove a foto atual da infraestrutura quando nenhuma nova foto é enviada. */
        boolean removerFoto
) {

    public record EquipeDTO(
            boolean temProfessorResponsavel,
            boolean temEquipeGestora,
            boolean temMerendeira,
            boolean temZeladora,
            boolean temBarqueiro,
            boolean necessitaTransporte
    ) {
    }

    public record InfraestruturaDTO(
            Integer anoConstrucao,
            Integer numSalas,
            Double latitude,
            Double longitude,
            String situacao,
            String tipoAcesso,
            Boolean temPredio,
            String tipoAgua,
            Boolean temInternet,
            String tipoEnergia,
            Boolean temRegistroFotografico
    ) {
    }

    public record MatriculaNivelDTO(
            @NotBlank(message = "Nível é obrigatório")
            String nivel,

            @PositiveOrZero(message = "Quantidade não pode ser negativa")
            Integer qtdMasculino,

            @PositiveOrZero(message = "Quantidade não pode ser negativa")
            Integer qtdFeminino,

            @PositiveOrZero(message = "Total não pode ser negativo")
            Integer total
    ) {
    }

    public record MatriculaSerieDTO(
            @NotBlank(message = "Denominação é obrigatória")
            String denominacao,

            String etapaEnsino,

            @PositiveOrZero(message = "Quantidade não pode ser negativa")
            Integer qtdMasculino,

            @PositiveOrZero(message = "Quantidade não pode ser negativa")
            Integer qtdFeminino,

            @PositiveOrZero(message = "Total não pode ser negativo")
            Integer total
    ) {
    }
}
