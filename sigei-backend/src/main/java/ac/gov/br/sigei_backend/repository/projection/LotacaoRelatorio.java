package ac.gov.br.sigei_backend.repository.projection;

import java.time.LocalDate;

/**
 * Lotação de um ano, sem os dados sensíveis (CPF, matrícula e número do contrato),
 * que nunca entram nos relatórios.
 */
public record LotacaoRelatorio(
        Long municipioId,
        String municipio,
        String escola,
        Long professorId,
        String professor,
        String etnia,
        String sexo,
        String formacao,
        String cargo,
        String segmento,
        String tipoContrato,
        LocalDate inicioContrato
) {
}
