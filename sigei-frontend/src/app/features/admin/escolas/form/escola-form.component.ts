import {
  ChangeDetectorRef,
  Component,
  OnInit,
  inject
} from '@angular/core';

import {
  FormArray,
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  RouterLink
} from '@angular/router';

import { EscolaService } from '../../../../core/services/escola.service';

import {
  AldeiaResponse,
  AldeiaService
} from '../../../../core/services/aldeia.service';

import {
  AnoReferencia,
  AnoReferenciaService
} from '../../../../core/services/ano-referencia.service';

@Component({
  selector: 'app-escola-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './escola-form.component.html',
})
export class EscolaFormComponent implements OnInit {

  private readonly fb = inject(FormBuilder);

  private readonly escolaService =
    inject(EscolaService);

  private readonly aldeiaService =
    inject(AldeiaService);

  private readonly anoReferenciaService =
    inject(AnoReferenciaService);

  private readonly cdr =
    inject(ChangeDetectorRef);

  etapaAtual = 1;

  aldeias: AldeiaResponse[] = [];

  anosReferencia: AnoReferencia[] = [];

  escolasSede: {
    id: number;
    codigo: string;
    nome: string;
  }[] = [];

  carregandoAldeias = false;
  carregandoAnos = false;

  erroMensagem = '';

  // ==========================
  // ETAPA 1 - ESCOLA
  // ==========================

  form = this.fb.group({

    codigo: this.fb.nonNullable.control(
      '',
      [
        Validators.required,
        Validators.minLength(2)
      ]
    ),

    nome: this.fb.nonNullable.control(
      '',
      [
        Validators.required,
        Validators.minLength(2)
      ]
    ),

    tipoUnidade: this.fb.nonNullable.control(
      '',
      Validators.required
    ),

    aldeiaId: this.fb.nonNullable.control(
      0,
      [
        Validators.required,
        Validators.min(1)
      ]
    ),

    escolaSedeId:
      this.fb.control<number | null>(null)

  });

  // ==========================
  // ETAPA 2 - REGISTRO ANUAL
  // ==========================

  registroForm = this.fb.nonNullable.group({

    anoReferenciaId: [
      0,
      [
        Validators.required,
        Validators.min(1)
      ]
    ],

    rede: [
      '',
      Validators.required
    ],

    localFuncionamento: [
      '',
      Validators.required
    ]

  });

  // ==========================
  // ETAPA 3 - EQUIPE
  // ==========================

  equipeForm = this.fb.nonNullable.group({

    temProfessorResponsavel: [false],

    temEquipeGestora: [false],

    temMerendeira: [false],

    temZeladora: [false],

    temBarqueiro: [false],

    necessitaTransporte: [false]

  });

  // ==========================
// ETAPA 4 - INFRAESTRUTURA
// ==========================

infraestruturaForm = this.fb.group({

  anoConstrucao: this.fb.control<number | null>(
    null
  ),

  numSalas: this.fb.control<number | null>(
    null
  ),

  latitude: this.fb.control<number | null>(
    null
  ),

  longitude: this.fb.control<number | null>(
    null
  ),

  situacao: this.fb.nonNullable.control(
    '',
    Validators.required
  ),

  tipoAcesso: this.fb.nonNullable.control(
    '',
    Validators.required
  ),

  temPredio: this.fb.nonNullable.control(
    false
  ),

  tipoAgua: this.fb.nonNullable.control(
    '',
    Validators.required
  ),

  temInternet: this.fb.nonNullable.control(
    false
  ),

  tipoEnergia: this.fb.nonNullable.control(
    '',
    Validators.required
  ),

  temRegistroFotografico:
    this.fb.nonNullable.control(
      false
    )

});

  ngOnInit(): void {

    this.carregarAldeias();

    this.carregarEscolasSede();

    this.carregarAnosReferencia();

  }

  carregarAldeias(): void {

    this.carregandoAldeias = true;

    this.aldeiaService
      .listar()
      .subscribe({

        next: (dados) => {

          this.aldeias =
            [...(dados ?? [])]
              .sort(
                (a, b) =>
                  a.nome.localeCompare(b.nome)
              );

          this.carregandoAldeias = false;

          this.cdr.markForCheck();

        },

        error: (erro) => {

          console.error(
            'Erro ao carregar aldeias:',
            erro
          );

          this.erroMensagem =
            'Não foi possível carregar as aldeias.';

          this.carregandoAldeias = false;

          this.cdr.markForCheck();

        }

      });

  }

  carregarEscolasSede(): void {

    this.escolaService
      .listar()
      .subscribe({

        next: (dados) => {

          this.escolasSede =
            (dados ?? [])
              .filter(
                escola =>
                  escola.tipoUnidade === 'SEDE'
              )
              .map(escola => ({
                id: escola.id,
                codigo: escola.codigo,
                nome: escola.nome
              }))
              .sort(
                (a, b) =>
                  a.nome.localeCompare(b.nome)
              );

          this.cdr.markForCheck();

        },

        error: (erro) => {

          console.error(
            'Erro ao carregar escolas sede:',
            erro
          );

        }

      });

  }

  carregarAnosReferencia(): void {

    this.carregandoAnos = true;

    this.anoReferenciaService
      .listar()
      .subscribe({

        next: (dados) => {

          this.anosReferencia =
            [...(dados ?? [])]
              .sort(
                (a, b) =>
                  b.ano - a.ano
              );

          this.carregandoAnos = false;

          this.cdr.markForCheck();

        },

        error: (erro) => {

          console.error(
            'Erro ao carregar anos de referência:',
            erro
          );

          this.erroMensagem =
            'Não foi possível carregar os anos de referência.';

          this.carregandoAnos = false;

          this.cdr.markForCheck();

        }

      });

  }

  aoAlterarTipoUnidade(): void {

    const tipo =
      this.form.controls.tipoUnidade.value;

    const escolaSedeControl =
      this.form.controls.escolaSedeId;

    if (tipo === 'ANEXO') {

      escolaSedeControl.setValidators([
        Validators.required
      ]);

    } else {

      escolaSedeControl.clearValidators();

      escolaSedeControl.setValue(null);

    }

    escolaSedeControl
      .updateValueAndValidity();

  }

  avancar(): void {

    this.erroMensagem = '';

    if (this.etapaAtual === 1) {

      if (this.form.invalid) {

        this.form.markAllAsTouched();

        return;

      }

      this.etapaAtual = 2;

      this.cdr.markForCheck();

      return;

    }

    if (this.etapaAtual === 2) {

      if (this.registroForm.invalid) {

        this.registroForm
          .markAllAsTouched();

        return;

      }

      this.etapaAtual = 3;

      this.cdr.markForCheck();

      return;

    }

    if (this.etapaAtual === 3) {

  this.etapaAtual = 4;

  this.cdr.markForCheck();

  return;

}

// ==========================
// ETAPA 4
// ==========================

if (this.etapaAtual === 4) {

  if (this.infraestruturaForm.invalid) {

    this.infraestruturaForm
      .markAllAsTouched();

    return;

  }

  this.etapaAtual = 5;

  this.cdr.markForCheck();

  return;

}

// ==========================
// ETAPA 5
// ==========================

if (this.etapaAtual === 5) {

  if (this.matriculasNivelForm.invalid) {

    this.matriculasNivelForm
      .markAllAsTouched();

    return;

  }

  console.log(
    'Escola:',
    this.form.getRawValue()
  );

  console.log(
    'Registro Anual:',
    this.registroForm.getRawValue()
  );

  console.log(
    'Equipe:',
    this.equipeForm.getRawValue()
  );

  console.log(
    'Infraestrutura:',
    this.infraestruturaForm.getRawValue()
  );

  console.log(
    'Matrículas por Nível:',
    this.montarMatriculasNivel()
  );

  /*
   * Próxima:
   * Etapa 6 - Matrículas por Série
   */

  return;

}



  }
  montarMatriculasNivel() {

  const matriculas =
    this.matriculasNivel.controls.map(
      (grupo, index) => ({
        nivel:
          grupo.get('nivel')?.value,

        qtdMasculino:
          grupo.get('qtdMasculino')?.value,

        qtdFeminino:
          grupo.get('qtdFeminino')?.value,

        total:
          this.calcularTotalNivel(index)
      })
    );

  return [

    {
      nivel: 'TOTAL_GERAL',

      qtdMasculino:
        this.calcularTotalMasculino(),

      qtdFeminino:
        this.calcularTotalFeminino(),

      total:
        this.calcularTotalGeral()
    },

    ...matriculas

  ];

}
  

  // ==========================
// ETAPA 5 - MATRÍCULAS POR NÍVEL
// ==========================

readonly niveisMatricula = [
  {
    nivel: 'ENSINO_INFANTIL',
    descricao: 'Ensino Infantil'
  },
  {
    nivel: 'FUNDAMENTAL_ANOS_INICIAIS',
    descricao: 'Fundamental - Anos Iniciais'
  },
  {
    nivel: 'FUNDAMENTAL_ANOS_FINAIS',
    descricao: 'Fundamental - Anos Finais'
  },
  {
    nivel: 'ENSINO_MEDIO',
    descricao: 'Ensino Médio'
  },
  {
    nivel: 'EJA',
    descricao: 'EJA'
  },
  {
    nivel: 'AEE',
    descricao: 'AEE'
  }
];

matriculasNivelForm = this.fb.group({

  matriculas: this.fb.array(
    this.niveisMatricula.map(
      item =>
        this.fb.group({

          nivel:
            this.fb.nonNullable.control(
              item.nivel
            ),

          qtdMasculino:
            this.fb.control<number | null>(
              null,
              [
                Validators.min(0)
              ]
            ),

          qtdFeminino:
            this.fb.control<number | null>(
              null,
              [
                Validators.min(0)
              ]
            )

        })
    )
  )

});

get matriculasNivel(): FormArray {
  return this.matriculasNivelForm.controls.matriculas;
}

getDescricaoNivel(index: number): string {

  return this.niveisMatricula[index]?.descricao ?? '';

}

calcularTotalNivel(index: number): number {

  const grupo =
    this.matriculasNivel.at(index);

  const masculino =
    Number(
      grupo.get('qtdMasculino')?.value ?? 0
    );

  const feminino =
    Number(
      grupo.get('qtdFeminino')?.value ?? 0
    );

  return masculino + feminino;

}

calcularTotalMasculino(): number {

  return this.matriculasNivel.controls
    .reduce(
      (total, grupo) =>
        total +
        Number(
          grupo.get('qtdMasculino')?.value ?? 0
        ),
      0
    );

}

calcularTotalFeminino(): number {

  return this.matriculasNivel.controls
    .reduce(
      (total, grupo) =>
        total +
        Number(
          grupo.get('qtdFeminino')?.value ?? 0
        ),
      0
    );

}

calcularTotalGeral(): number {

  return (
    this.calcularTotalMasculino() +
    this.calcularTotalFeminino()
  );

}

  voltarEtapa(): void {

    if (this.etapaAtual > 1) {

      this.etapaAtual--;

      this.erroMensagem = '';

      this.cdr.markForCheck();

    }

  }

}