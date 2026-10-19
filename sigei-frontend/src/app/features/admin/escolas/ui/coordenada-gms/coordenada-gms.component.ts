import { Component, computed, DestroyRef, inject, input, OnDestroy, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ValidatorFn } from '@angular/forms';

import {
  CoordenadaGms,
  decimalParaGms,
  Direcao,
  DIRECOES,
  gmsParaDecimal,
  TipoCoordenada
} from '@core/forms/coordenadas';
import { ErroCampoComponent } from '@shared/ui/erro-campo/erro-campo.component';

/**
 * Entrada de uma coordenada em graus, minutos e segundos. O controle continua
 * guardando graus decimais, que é o que a API recebe; enquanto os campos têm
 * um valor inválido, o controle fica nulo e com o erro `gms`.
 */
@Component({
  selector: 'app-coordenada-gms',
  standalone: true,
  imports: [ErroCampoComponent],
  templateUrl: './coordenada-gms.component.html'
})
export class CoordenadaGmsComponent implements OnInit, OnDestroy {
  private readonly destroyRef = inject(DestroyRef);

  readonly controle = input.required<FormControl<number | null>>();
  readonly tipo = input.required<TipoCoordenada>();
  readonly rotulo = input.required<string>();

  readonly gms = signal<CoordenadaGms>({ graus: null, minutos: null, segundos: null, direcao: 'S' });
  readonly erro = signal<string | null>(null);
  readonly direcoes = computed(() => DIRECOES[this.tipo()]);
  readonly mensagens = { gms: (mensagem: string) => mensagem };

  /** Último valor gravado por este componente, para reconhecer mudanças externas. */
  private valorGravado: number | null | undefined;

  private readonly validador: ValidatorFn = () => {
    const erro = this.erro();
    return erro ? { gms: erro } : null;
  };

  ngOnInit(): void {
    const controle = this.controle();
    this.preencher(controle.value);
    controle.addValidators(this.validador);

    // Um valor carregado depois (edição de escola) atualiza os campos.
    controle.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(valor => {
        if (valor !== this.valorGravado) {
          this.erro.set(null);
          this.preencher(valor);
        }
      });
  }

  ngOnDestroy(): void {
    const controle = this.controle();
    controle.removeValidators(this.validador);
    controle.updateValueAndValidity({ emitEvent: false });
  }

  alterarNumero(campo: 'graus' | 'minutos' | 'segundos', evento: Event): void {
    const texto = (evento.target as HTMLInputElement).value.trim().replace(',', '.');
    const numero = texto === '' ? null : Number(texto);
    this.atualizar({ ...this.gms(), [campo]: numero !== null && Number.isNaN(numero) ? NaN : numero });
  }

  alterarDirecao(evento: Event): void {
    this.atualizar({ ...this.gms(), direcao: (evento.target as HTMLSelectElement).value as Direcao });
  }

  marcarTocado(): void {
    this.controle().markAsTouched();
  }

  textoSegundos(): string {
    const segundos = this.gms().segundos;
    return segundos === null || Number.isNaN(segundos) ? '' : String(segundos).replace('.', ',');
  }

  private atualizar(gms: CoordenadaGms): void {
    this.gms.set(gms);
    const { valor, erro } = gmsParaDecimal(gms, this.tipo());
    this.erro.set(erro);
    this.valorGravado = valor;
    this.controle().setValue(valor);
  }

  private preencher(valor: number | null): void {
    this.valorGravado = valor;
    this.gms.set(decimalParaGms(valor, this.tipo()));
  }
}
