import { Component, OnInit, computed, inject, signal } from '@angular/core';

import { AuthService } from '@core/auth/auth.service';
import { interpretarErro } from '@core/http/erro-http';
import { ToastService } from '@core/notificacao/toast.service';
import { PreferenciasService } from '@core/preferencias/preferencias.service';
import { AnoReferenciaService } from '@domains/escolas/data-access/ano-referencia.service';
import { ConfirmModalComponent } from '@shared/ui/confirm-modal/confirm-modal.component';

const CHAVE_DISPENSADO = 'sigei-lembrete-ano-dispensado';

/** Ano civil no Acre, como o backend (AnoValido) usa para validar os anos. */
export function anoAtualNoAcre(agora = new Date()): number {
  return Number(new Intl.DateTimeFormat('en-US', { timeZone: 'America/Rio_Branco', year: 'numeric' }).format(agora));
}

/**
 * Lembrete de ano letivo (preferência "Lembrete de ano letivo"): aparece no topo
 * da área administrativa quando o ano atual ainda não foi aberto no sistema. O
 * administrador pode abrir o ano seguinte ao mais recente, que copia os últimos
 * registros das escolas e as lotações (POST /anos-referencia/proximo). Dispensado,
 * só volta numa nova sessão do navegador.
 */
@Component({
  selector: 'app-lembrete-ano-letivo',
  standalone: true,
  imports: [ConfirmModalComponent],
  templateUrl: './lembrete-ano-letivo.component.html'
})
export class LembreteAnoLetivoComponent implements OnInit {
  private readonly anoReferenciaService = inject(AnoReferenciaService);
  private readonly preferencias = inject(PreferenciasService).preferencias;
  private readonly toast = inject(ToastService);

  readonly ehAdmin = inject(AuthService).ehAdmin();
  readonly anoAtual = anoAtualNoAcre();

  /** Maior ano cadastrado; nulo enquanto carrega ou se não houver anos. */
  readonly ultimoAno = signal<number | null>(null);
  readonly dispensado = signal(lerDispensado() === this.anoAtual);
  readonly confirmando = signal(false);
  readonly abrindo = signal(false);

  readonly visivel = computed(() => {
    const ultimo = this.ultimoAno();
    return this.preferencias().avisarAtualizacaoAnual
      && !this.dispensado()
      && ultimo !== null
      && ultimo < this.anoAtual;
  });

  readonly proximoAno = computed(() => (this.ultimoAno() ?? 0) + 1);

  ngOnInit(): void {
    if (!this.preferencias().avisarAtualizacaoAnual || this.dispensado()) return;
    this.carregar();
  }

  dispensar(): void {
    this.dispensado.set(true);
    try {
      sessionStorage.setItem(CHAVE_DISPENSADO, String(this.anoAtual));
    } catch {
      // Sem armazenamento: o lembrete some só até recarregar
    }
  }

  abrirAno(): void {
    if (this.abrindo()) return;
    this.abrindo.set(true);

    this.anoReferenciaService.iniciarProximo().subscribe({
      next: ano => {
        this.abrindo.set(false);
        this.confirmando.set(false);
        this.toast.sucesso(`O ano letivo de ${ano.ano} foi aberto com os dados de ${ano.ano - 1}.`, 'Ano aberto');
        this.ultimoAno.set(ano.ano);
      },
      error: erro => {
        this.abrindo.set(false);
        this.confirmando.set(false);
        this.toast.erro(interpretarErro(erro, 'Não foi possível abrir o ano letivo.').mensagem);
      }
    });
  }

  private carregar(): void {
    this.anoReferenciaService.listar().subscribe({
      next: anos => {
        const maior = Math.max(...(anos ?? []).map(item => item.ano));
        this.ultimoAno.set(Number.isFinite(maior) ? maior : null);
      },
      // O lembrete é acessório: uma falha aqui não deve atrapalhar a tela
      error: () => this.ultimoAno.set(null)
    });
  }
}

function lerDispensado(): number | null {
  try {
    return Number(sessionStorage.getItem(CHAVE_DISPENSADO)) || null;
  } catch {
    return null;
  }
}
