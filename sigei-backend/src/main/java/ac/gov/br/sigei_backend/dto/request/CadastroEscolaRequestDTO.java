package ac.gov.br.sigei_backend.dto.request;

import ac.gov.br.sigei_backend.dto.validacao.AnoValido;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

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
        @Size(max = 255, message = "Rede deve ter no máximo 255 caracteres")
        String rede,

        @NotBlank(message = "Local de funcionamento é obrigatório")
        @Size(max = 255, message = "Local de funcionamento deve ter no máximo 255 caracteres")
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
            @AnoValido(message = "Ano de construção deve estar entre 1900 e o ano atual")
            Integer anoConstrucao,

            @PositiveOrZero(message = "Número de salas não pode ser negativo")
            Integer numSalas,

            @DecimalMin(value = "-90", message = "Latitude deve estar entre -90 e 90")
            @DecimalMax(value = "90", message = "Latitude deve estar entre -90 e 90")
            Double latitude,

            @DecimalMin(value = "-180", message = "Longitude deve estar entre -180 e 180")
            @DecimalMax(value = "180", message = "Longitude deve estar entre -180 e 180")
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
            @Size(max = 255, message = "Nível deve ter no máximo 255 caracteres")
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
            @Size(max = 255, message = "Denominação deve ter no máximo 255 caracteres")
            String denominacao,

            @Size(max = 255, message = "Etapa de ensino deve ter no máximo 255 caracteres")
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
