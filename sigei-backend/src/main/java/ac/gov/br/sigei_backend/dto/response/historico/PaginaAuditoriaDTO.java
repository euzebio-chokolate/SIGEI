package ac.gov.br.sigei_backend.dto.response.historico;

import java.util.List;

/**
 * Uma página da auditoria geral. A paginação é por alteração (registro alterado
 * em uma revisão), não por revisão: a revisão inicial da migração V4 sozinha
 * contém todos os registros existentes na época.
 */
public record PaginaAuditoriaDTO(
        List<AlteracaoHistoricoDTO> alteracoes,
        int pagina,
        int tamanho,
        long totalAlteracoes,
        int totalPaginas
) {
}
