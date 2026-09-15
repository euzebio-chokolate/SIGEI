import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DashboardEvolucao, DashboardItem, DashboardResponse, DashboardService } from '@features/admin/dashboard/data-access/dashboard.service';

interface EvolucaoPorAno { ano: number; matriculas: number; escolas: number; }

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  private readonly dashboardService = inject(DashboardService);
  private readonly cdr = inject(ChangeDetectorRef);

  dados: DashboardResponse | null = null;
  carregando = true;
  erroMensagem = '';
  anoSelecionado: number | null = null;
  anosDisponiveis: number[] = [];
  evolucao: EvolucaoPorAno[] = [];

  ngOnInit(): void { this.carregar(); }

  carregar(ano?: number): void {
    this.carregando = true;
    this.erroMensagem = '';
    this.dashboardService.buscar(ano).subscribe({
      next: dados => {
        this.dados = dados;
        this.anoSelecionado = dados.anoReferencia;
        this.evolucao = this.agruparEvolucao(dados.evolucaoMatriculas ?? [], dados.evolucaoEscolas ?? []);
        this.anosDisponiveis = Array.from(new Set([dados.anoReferencia, ...this.evolucao.map(item => item.ano)])).sort((a, b) => b - a);
        this.carregando = false;
        this.cdr.markForCheck();
      },
      error: erro => {
        this.carregando = false;
        this.erroMensagem = erro?.status === 404 ? 'Não há dados cadastrados para o ano selecionado.' : 'Não foi possível carregar os indicadores do dashboard.';
        this.cdr.markForCheck();
      }
    });
  }

  alterarAno(): void { if (this.anoSelecionado !== null) this.carregar(this.anoSelecionado); }

  percentual(valor: number, itens: DashboardItem[]): number {
    const maior = Math.max(0, ...itens.map(item => item.total));
    return maior === 0 ? 0 : Math.max(2, (valor / maior) * 100);
  }

  alturaEvolucao(valor: number, campo: 'matriculas' | 'escolas'): number {
    const maior = Math.max(0, ...this.evolucao.map(item => item[campo]));
    return maior === 0 ? 0 : Math.max(5, (valor / maior) * 100);
  }

  formatarNumero(valor: number | null | undefined): string { return new Intl.NumberFormat('pt-BR').format(valor ?? 0); }
  formatarPercentual(valor: number | null | undefined): string { return `${new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 2 }).format(valor ?? 0)}%`; }

  private agruparEvolucao(matriculas: DashboardEvolucao[], escolas: DashboardEvolucao[]): EvolucaoPorAno[] {
    const anos = new Set([...matriculas.map(item => item.ano), ...escolas.map(item => item.ano)]);
    return Array.from(anos).sort((a, b) => a - b).map(ano => ({
      ano,
      matriculas: matriculas.find(item => item.ano === ano)?.total ?? 0,
      escolas: escolas.find(item => item.ano === ano)?.total ?? 0
    }));
  }
}
