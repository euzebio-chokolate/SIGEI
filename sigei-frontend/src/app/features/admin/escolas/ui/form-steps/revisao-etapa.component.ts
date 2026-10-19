import {
  Component,
  EventEmitter,
  Input,
  Output
} from '@angular/core';

import {
  rotuloOpcao,
  SITUACOES_ESCOLA,
  TIPOS_ACESSO,
  TIPOS_AGUA,
  TIPOS_ENERGIA
} from '@domains/escolas/models/infraestrutura-opcoes';
import { formatarGms } from '@core/forms/coordenadas';

@Component({
  selector: 'app-revisao-etapa',
  standalone: true,
  imports: [],
  templateUrl: './revisao-etapa.component.html'
})
export class RevisaoEtapaComponent {

  @Input({ required: true })
  escola!: {
    codigo: string;
    nome: string;
    tipoUnidade: string;
    aldeiaId: number;
    escolaSedeId: number | null;
  };

  @Input({ required: true })
  registro!: {
    anoReferenciaId: number;
    rede: string;
    localFuncionamento: string;
    totalAlunos: number | null;
  };

  @Input({ required: true })
  equipe!: {
    temProfessorResponsavel: boolean;
    temEquipeGestora: boolean;
    temMerendeira: boolean;
    temZeladora: boolean;
    temBarqueiro: boolean;
    necessitaTransporte: boolean;
  };

  @Input({ required: true })
  infraestrutura!: {
    anoConstrucao: number | null;
    numSalas: number | null;
    latitude: number | null;
    longitude: number | null;
    situacao: string;
    tipoAcesso: string;
    temPredio: boolean;
    tipoAgua: string;
    temInternet: boolean;
    tipoEnergia: string;
    temRegistroFotografico: boolean;
  };

  @Input()
  nomeAldeia = '-';

  @Input()
  nomeEscolaSede = '-';

  @Input()
  anoReferencia = '-';

  @Input()
  totalMatriculas = 0;

  @Input()
  totalMasculino = 0;

  @Input()
  totalFeminino = 0;

  @Input()
  salvando = false;

  @Input()
  erroMensagem = '';

  @Output()
  voltar = new EventEmitter<void>();

  @Output()
  salvar = new EventEmitter<void>();

  emitirVoltar(): void {
    this.voltar.emit();
  }

  emitirSalvar(): void {
    this.salvar.emit();
  }

  simNao(valor: boolean): string {
    return valor ? 'Sim' : 'Não';
  }

  readonly gms = formatarGms;

  readonly situacoes = SITUACOES_ESCOLA;
  readonly tiposAcesso = TIPOS_ACESSO;
  readonly tiposAgua = TIPOS_AGUA;
  readonly tiposEnergia = TIPOS_ENERGIA;
  readonly rotulo = rotuloOpcao;

}