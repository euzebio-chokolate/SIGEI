import { Component, DestroyRef, OnInit, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, ParamMap, Router, RouterLink } from '@angular/router';
import { BehaviorSubject, catchError, combineLatest, map, of, switchMap, tap } from 'rxjs';

import { mensagemErroCarregamento } from '@core/http/erro-http';
import { AuditoriaService } from '@domains/auditoria/data-access/auditoria.service';
import {
  ENTIDADES_AUDITADAS,
  FiltroAuditoria,
  OPERACOES_HISTORICO,
  OperacaoHistorico,
  PaginaAuditoria,
  agruparPorRevisao
} from '@domains/auditoria/models/auditoria.models';
import { LinhaDoTempoAuditoriaComponent } from '@domains/auditoria/ui/linha-do-tempo/linha-do-tempo-auditoria.component';
import { UsuarioResponse, UsuarioService } from '@domains/usuarios/data-access/usuario.service';
import { EstadoErroComponent } from '@shared/ui/estado-erro/estado-erro.component';

const TAMANHO_PAGINA = 20;
/** Valor do filtro de autor para as alterações sem usuário (inicializadores, cargas, migrações). */
const AUTOR_SISTEMA = 'sistema';

/**
 * Auditoria central: quem criou, alterou ou excluiu o quê, em todo o sistema.
 * Os filtros e a página ficam na URL, então voltar, avançar e compartilhar o
 * link reproduzem a mesma consulta.
 */
@Component({
  selector: 'app-auditoria',
  standalone: true,
  imports: [FormsModule, RouterLink, LinhaDoTempoAuditoriaComponent, EstadoErroComponent],
  templateUrl: './auditoria.component.html'
})
export class AuditoriaComponent implements OnInit {
  private readonly auditoriaService = inject(AuditoriaService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly entidades = ENTIDADES_AUDITADAS;
  readonly operacoes = OPERACOES_HISTORICO;
  readonly autorSistema = AUTOR_SISTEMA;
  readonly hoje = new Date().toLocaleDateString('sv-SE');

  // Campos do formulário de filtros, espelhando a URL
  entidade = '';
  operacao: OperacaoHistorico | '' = '';
  autor = '';
  de = '';
  ate = '';

  readonly usuarios = signal<UsuarioResponse[]>([]);
  readonly carregando = signal(true);
  readonly erro = signal('');
  /** Problema nos filtros informados, mostrado junto a eles (não é falha de carregamento). */
  readonly erroFiltro = signal('');
  readonly pagina = signal<PaginaAuditoria | null>(null);

  readonly revisoes = computed(() => agruparPorRevisao(this.pagina()?.alteracoes ?? []));
  readonly temFiltros = computed(() => this.filtrosAtivos() > 0);
  private readonly filtrosAtivos = signal(0);
  private readonly recarga$ = new BehaviorSubject<void>(undefined);

  ngOnInit(): void {
    this.usuarioService.listar().subscribe({
      next: usuarios => this.usuarios.set([...usuarios].sort((a, b) => a.nome.localeCompare(b.nome))),
      // Sem a lista, o filtro por autor fica só com "Sistema"; a auditoria continua funcionando
      error: erro => console.error('Erro ao carregar usuários para o filtro:', erro)
    });

    // Cada mudança na URL (filtro, página, voltar do navegador) refaz a consulta; a anterior é cancelada
    combineLatest([this.route.queryParamMap, this.recarga$]).pipe(
      map(([params]) => params),
      tap(params => this.lerFiltros(params)),
      switchMap(params => {
        this.carregando.set(true);
        this.erro.set('');
        return this.auditoriaService.buscar(this.filtroDaUrl(params), this.paginaDaUrl(params), TAMANHO_PAGINA).pipe(
          catchError(erro => {
            console.error('Erro ao carregar a auditoria:', erro);
            this.erro.set(mensagemErroCarregamento(erro, 'Não foi possível carregar a auditoria.'));
            return of(null);
          })
        );
      }),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(resultado => {
      if (resultado) this.pagina.set(resultado);
      this.carregando.set(false);
    });
  }

  /** Aplica os filtros do formulário, voltando à primeira página. */
  aplicarFiltros(): void {
    if (this.de && this.ate && this.de > this.ate) {
      this.erroFiltro.set('A data inicial não pode ser posterior à data final.');
      return;
    }
    this.erroFiltro.set('');
    this.navegar(0);
  }

  limparFiltros(): void {
    this.entidade = '';
    this.operacao = '';
    this.autor = '';
    this.de = '';
    this.ate = '';
    this.erroFiltro.set('');
    this.navegar(0);
  }

  irParaPagina(pagina: number): void {
    this.navegar(pagina);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  /** Repete a consulta atual (ex.: depois de uma falha), sem mudar a URL. */
  recarregar(): void {
    this.recarga$.next();
  }

  private navegar(pagina: number): void {
    const queryParams: Record<string, string | number | null> = {
      entidade: this.entidade || null,
      operacao: this.operacao || null,
      autor: this.autor || null,
      de: this.de || null,
      ate: this.ate || null,
      pagina: pagina > 0 ? pagina + 1 : null
    };
    this.router.navigate([], { relativeTo: this.route, queryParams });
  }

  private lerFiltros(params: ParamMap): void {
    this.entidade = params.get('entidade') ?? '';
    this.operacao = (params.get('operacao') as OperacaoHistorico | null) ?? '';
    this.autor = params.get('autor') ?? '';
    this.de = params.get('de') ?? '';
    this.ate = params.get('ate') ?? '';
    this.filtrosAtivos.set([this.entidade, this.operacao, this.autor, this.de, this.ate].filter(Boolean).length);
  }

  private filtroDaUrl(params: ParamMap): FiltroAuditoria {
    const autor = params.get('autor');
    const usuarioId = autor && autor !== AUTOR_SISTEMA ? Number(autor) : null;
    return {
      entidade: params.get('entidade'),
      operacao: params.get('operacao') as OperacaoHistorico | null,
      usuarioId: usuarioId && usuarioId > 0 ? usuarioId : null,
      sistema: autor === AUTOR_SISTEMA ? true : null,
      de: params.get('de'),
      ate: params.get('ate')
    };
  }

  /** Na URL a página começa em 1; na API, em 0. */
  private paginaDaUrl(params: ParamMap): number {
    const pagina = Number(params.get('pagina'));
    return Number.isInteger(pagina) && pagina > 1 ? pagina - 1 : 0;
  }
}
