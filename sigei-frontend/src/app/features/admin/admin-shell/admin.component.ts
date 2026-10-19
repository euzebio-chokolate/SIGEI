import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from '@core/auth/auth.service';
import { SeAdminDirective } from '@core/auth/se-admin.directive';
import { PreferenciasService } from '@core/preferencias/preferencias.service';
import { LembreteAnoLetivoComponent } from './lembrete-ano-letivo/lembrete-ano-letivo.component';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, SeAdminDirective, LembreteAnoLetivoComponent],
  templateUrl: './admin.component.html'
})
export class AdminComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly preferencias = inject(PreferenciasService).preferencias;

  usuario = this.authService.getUsuario();
  ehAdmin = this.authService.ehAdmin();
  menuMobileAberto = false;

  /** No computador, o menu mostra só os ícones (o menu do celular continua completo). */
  readonly menuCompacto = computed(() => this.preferencias().menuCompacto);

  abrirMenuMobile(): void {
    this.menuMobileAberto = true;
  }

  fecharMenuMobile(): void {
    this.menuMobileAberto = false;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
