import { Directive, TemplateRef, ViewContainerRef, inject } from '@angular/core';

import { AuthService } from './auth.service';

/**
 * Renderiza o elemento só para administradores: `<button *seAdmin>Excluir</button>`.
 *
 * O backend só aceita escrita do perfil ADMIN (SecurityConfig); para o perfil
 * USUARIO, as ações de cadastro, edição e exclusão nem chegam ao DOM, em vez
 * de aparecerem e terminarem em 403. O perfil não muda durante a sessão, então
 * a decisão é tomada uma única vez, na criação da view.
 */
@Directive({
  selector: '[seAdmin]',
  standalone: true
})
export class SeAdminDirective {
  constructor() {
    if (inject(AuthService).ehAdmin()) {
      inject(ViewContainerRef).createEmbeddedView(inject(TemplateRef));
    }
  }
}
