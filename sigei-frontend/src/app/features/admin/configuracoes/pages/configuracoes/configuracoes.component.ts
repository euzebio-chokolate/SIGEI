import { Component, OnDestroy, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { AuthService } from '@core/auth/auth.service';
import {
  CAMADAS_MAPA,
  PAGINAS_INICIAIS,
  PREFERENCIAS_PADRAO,
  PreferenciasService
} from '@core/preferencias/preferencias.service';

/**
 * Preferências deste navegador. Todas têm efeito: página inicial (login e /admin),
 * mapa público, menu compacto, animações e lembrete de ano letivo. Salvar aplica
 * na hora, sem recarregar a página.
 */
@Component({
  selector: 'app-configuracoes',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './configuracoes.component.html',
})
export class ConfiguracoesComponent implements OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly preferenciasService = inject(PreferenciasService);
  private readonly authService = inject(AuthService);

  readonly paginasIniciais = PAGINAS_INICIAIS;
  readonly camadasMapa = CAMADAS_MAPA;
  readonly usuario = this.authService.getUsuario();
  readonly ehAdmin = this.authService.ehAdmin();

  readonly salvoComSucesso = signal(false);
  private temporizador?: ReturnType<typeof setTimeout>;

  readonly form = this.fb.nonNullable.group({ ...this.preferenciasService.preferencias() });

  salvar(): void {
    this.preferenciasService.salvar(this.form.getRawValue());
    this.form.markAsPristine();
    this.avisarSalvo();
  }

  restaurarPadrao(): void {
    this.preferenciasService.restaurarPadrao();
    this.form.reset({ ...PREFERENCIAS_PADRAO });
    this.avisarSalvo();
  }

  ngOnDestroy(): void {
    clearTimeout(this.temporizador);
  }

  private avisarSalvo(): void {
    this.salvoComSucesso.set(true);
    clearTimeout(this.temporizador);
    this.temporizador = setTimeout(() => this.salvoComSucesso.set(false), 3000);
  }
}
