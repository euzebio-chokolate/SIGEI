package ac.gov.br.sigei_backend.service.auditoria;

import ac.gov.br.sigei_backend.dto.response.historico.OperacaoHistorico;

import java.time.LocalDate;

/**
 * Filtros opcionais da auditoria geral; nulo significa "todos".
 *
 * @param entidade  nome da entidade auditada (ex.: {@code Escola}, {@code Professor})
 * @param usuarioId autor das alterações
 * @param sistema   true para só as alterações sem autor (inicializadores, cargas, migrações)
 * @param de        primeiro dia do período, inclusive (horário do Acre)
 * @param ate       último dia do período, inclusive (horário do Acre)
 */
public record FiltroAuditoria(
        String entidade,
        OperacaoHistorico operacao,
        Long usuarioId,
        Boolean sistema,
        LocalDate de,
        LocalDate ate
) {
}
