package ac.gov.br.sigei_backend.dto.response.historico;

import java.time.Instant;
import java.util.List;

/**
 * Uma alteração da trilha de auditoria: o que mudou em uma entidade, em qual
 * revisão, quando e por quem. Usuário nulo indica alteração feita pelo sistema.
 * {@code descricao} identifica o registro para leitura (nome, série, nível ou,
 * na auditoria geral, a escola ou o professor a que pertence); pode ser nula.
 * {@code ano} é o ano do registro anual a que a entidade pertence (nulo quando não se aplica).
 */
public record AlteracaoHistoricoDTO(
        Long revisao,
        Instant dataHora,
        Long usuarioId,
        String usuarioNome,
        String usuarioEmail,
        String entidade,
        Long entidadeId,
        String descricao,
        Integer ano,
        OperacaoHistorico operacao,
        List<CampoAlteradoDTO> campos
) {
}
