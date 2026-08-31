import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

interface PreferenciasSistema {
  nomeInstituicao: string;
  siglaInstituicao: string;
  paginaInicial: string;
  camadaMapa: string;
  abrirPopupAoPassarMouse: boolean;
  exibirFotosNoMapa: boolean;
  menuCompacto: boolean;
  reduzirAnimacoes: boolean;
  confirmarExclusoes: boolean;
  avisarAtualizacaoAnual: boolean;
}

@Component({
  selector: 'app-configuracoes',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './configuracoes.component.html',
})
export class ConfiguracoesComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly chaveArmazenamento = 'sigei-preferencias';

  salvando = false;
  salvoComSucesso = false;

  readonly form = this.fb.nonNullable.group({
    nomeInstituicao: ['Sistema de Gerenciamento Escolar Indígena'],
    siglaInstituicao: ['SIGEI'],
    paginaInicial: ['dashboard'],
    camadaMapa: ['padrao'],
    abrirPopupAoPassarMouse: [true],
    exibirFotosNoMapa: [true],
    menuCompacto: [false],
    reduzirAnimacoes: [false],
    confirmarExclusoes: [true],
    avisarAtualizacaoAnual: [true],
  });

  ngOnInit(): void {
    const preferencias = localStorage.getItem(this.chaveArmazenamento);
    if (!preferencias) return;

    try {
      this.form.patchValue(JSON.parse(preferencias) as Partial<PreferenciasSistema>);
    } catch {
      localStorage.removeItem(this.chaveArmazenamento);
    }
  }

  salvar(): void {
    this.salvando = true;
    this.salvoComSucesso = false;
    localStorage.setItem(this.chaveArmazenamento, JSON.stringify(this.form.getRawValue()));

    window.setTimeout(() => {
      this.salvando = false;
      this.salvoComSucesso = true;
      this.cdr.markForCheck();

      window.setTimeout(() => {
        this.salvoComSucesso = false;
        this.cdr.markForCheck();
      }, 3000);
    }, 350);
  }

  restaurarPadrao(): void {
    this.form.reset({
      nomeInstituicao: 'Sistema de Gerenciamento Escolar Indígena',
      siglaInstituicao: 'SIGEI',
      paginaInicial: 'dashboard',
      camadaMapa: 'padrao',
      abrirPopupAoPassarMouse: true,
      exibirFotosNoMapa: true,
      menuCompacto: false,
      reduzirAnimacoes: false,
      confirmarExclusoes: true,
      avisarAtualizacaoAnual: true,
    });
    localStorage.removeItem(this.chaveArmazenamento);
    this.salvoComSucesso = false;
  }
}
