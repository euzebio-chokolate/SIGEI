package ac.gov.br.sigei_backend.config.auditoria;

import ac.gov.br.sigei_backend.models.Revisao;
import ac.gov.br.sigei_backend.models.Usuario;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Preenche o autor de cada revisão com o usuário autenticado pelo {@code JwtFilter}.
 * Sem autenticação (inicializadores, carga de planilhas na subida), o autor fica
 * nulo e a revisão é atribuída ao sistema.
 */
public class RevisaoListener implements RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        Revisao revisao = (Revisao) revisionEntity;
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao != null && autenticacao.getPrincipal() instanceof Usuario usuario) {
            revisao.setUsuarioId(usuario.getId());
            revisao.setUsuarioEmail(usuario.getEmail());
            revisao.setUsuarioNome(usuario.getNome());
        }
    }
}
