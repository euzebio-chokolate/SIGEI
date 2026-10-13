package ac.gov.br.sigei_backend.dto.response.dashboard;

import java.util.List;

/**
 * Lotações de professores no ano. {@code porTipoContrato} traz o valor gravado
 * (EFETIVO, PSS...), com nulo para lotações sem tipo informado.
 */
public record DashboardProfessoresDTO(
        Long totalProfessores,
        Long totalLotacoes,
        Long vagasEmAberto,
        Long lotacoesNoNucleo,
        List<DashboardItemDTO> porTipoContrato
) {
}
