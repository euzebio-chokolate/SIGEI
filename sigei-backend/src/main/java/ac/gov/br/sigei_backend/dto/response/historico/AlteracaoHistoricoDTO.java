package ac.gov.br.sigei_backend.dto.response.historico;

import java.time.Instant;
import java.util.List;

/**
 * Uma alteração da trilha de auditoria: o que mudou em uma entidade, em qual
 * revisão, quando e por quem. Usuário nulo indica alteração feita pelo sistema.
 * {@code ano} é o ano do registro anual a que a entidade pertence (nulo para a escola).
 */
public record AlteracaoHistoricoDTO(
        Long revisao,
        Instant dataHora,
        Long usuarioId,
        String usuarioNome,
        String usuarioEmail,
        String entidade,
        Long entidadeId,
        Integer ano,
        OperacaoHistorico operacao,
        List<CampoAlteradoDTO> campos
) {
}
