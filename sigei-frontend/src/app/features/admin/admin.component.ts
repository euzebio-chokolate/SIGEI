import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './admin.component.html'
})
export class AdminComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  usuario = this.authService.getUsuario();
  menuMobileAberto = false;

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
