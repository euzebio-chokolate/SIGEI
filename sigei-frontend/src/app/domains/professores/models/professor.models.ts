import type { AnoReferenciaResumo, EscolaResumo, MunicipioResumo } from '@domains/escolas/models/escola.models';

/**
 * CPF e matrícula são dados sensíveis: o backend só os devolve ao perfil ADMIN
 * (para os demais perfis, chegam nulos) e os grava criptografados.
 */
export interface ProfessorResponse {
  id: number;
  nome: string;
  cpf: string | null;
  matricula: string | null;
  etnia: string | null;
  sexo: Sexo | null;
  formacao: string | null;
}

export interface ProfessorRequest {
  id?: number;
  nome: string;
  cpf: string | null;
  matricula: string | null;
  etnia: string | null;
  sexo: Sexo | null;
  formacao: string | null;
}

export interface ProfessorResumo {
  id: number;
  nome: string;
  etnia: string | null;
  sexo: Sexo | null;
  formacao: string | null;
}

/**
 * Lotação de um professor em um ano. professor nulo indica vaga em aberto;
 * escola nula, lotação no núcleo de educação do município. numeroContrato é
 * sensível e só chega ao perfil ADMIN.
 */
export interface LotacaoProfessorResponse {
  id: number;
  professor: ProfessorResumo | null;
  anoReferencia: AnoReferenciaResumo;
  escola: EscolaResumo | null;
  municipio: MunicipioResumo;
  cargo: string | null;
  segmento: string | null;
  numeroContrato: string | null;
  inicioContrato: string | null;
  amparoLegal: string | null;
  tipoContrato: TipoContrato | null;
}

export interface LotacaoProfessorRequest {
  id?: number;
  professorId: number | null;
  anoReferenciaId: number;
  escolaId: number | null;
  municipioId: number | null;
  cargo: string | null;
  segmento: string | null;
  numeroContrato: string | null;
  inicioContrato: string | null;
  amparoLegal: string | null;
  tipoContrato: TipoContrato | null;
}

export interface ImportacaoProfessoresResultado {
  linhasLidas: number;
  professoresCriados: number;
  professoresAtualizados: number;
  lotacoesCriadas: number;
  lotacoesAtualizadas: number;
  vagasEmAberto: number;
  lotacoesNoNucleo: number;
  linhasIgnoradas: number;
  erros: string[];
}

export type Sexo = 'M' | 'F';

/** Mesmas opções do enum TipoContrato do backend. */
export type TipoContrato = 'EFETIVO' | 'PSS' | 'CONTRATO_DIRETO' | 'PERMUTA';

export const OPCOES_TIPO_CONTRATO: { valor: TipoContrato; rotulo: string }[] = [
  { valor: 'EFETIVO', rotulo: 'Efetivo' },
  { valor: 'PSS', rotulo: 'PSS (contrato temporário)' },
  { valor: 'CONTRATO_DIRETO', rotulo: 'Contrato direto' },
  { valor: 'PERMUTA', rotulo: 'Permuta' }
];

export const OPCOES_SEXO: { valor: Sexo; rotulo: string }[] = [
  { valor: 'F', rotulo: 'Feminino' },
  { valor: 'M', rotulo: 'Masculino' }
];

export function rotuloTipoContrato(valor: string | null | undefined): string {
  return OPCOES_TIPO_CONTRATO.find(opcao => opcao.valor === valor)?.rotulo ?? 'Não informado';
}

export function rotuloSexo(valor: string | null | undefined): string {
  return OPCOES_SEXO.find(opcao => opcao.valor === valor)?.rotulo ?? 'Não informado';
}

export function somenteDigitos(valor: string | null | undefined): string {
  return (valor ?? '').replace(/\D/g, '');
}

/** 52998224725 → 529.982.247-25 (aplica a máscara aos dígitos já digitados). */
export function formatarCpf(valor: string | null | undefined): string {
  const digitos = somenteDigitos(valor).slice(0, 11);

  return digitos
    .replace(/^(\d{3})(\d)/, '$1.$2')
    .replace(/^(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
    .replace(/\.(\d{3})(\d{1,2})$/, '.$1-$2');
}

/** Exibe só os dígitos centrais, como recomendado para listagens: ***.982.247-** */
export function mascararCpf(valor: string | null | undefined): string {
  const digitos = somenteDigitos(valor);
  if (digitos.length !== 11) return '';
  return `***.${digitos.slice(3, 6)}.${digitos.slice(6, 9)}-**`;
}

/** Mesma validação do backend (CpfUtils): 11 dígitos e dígitos verificadores. */
export function cpfValido(valor: string | null | undefined): boolean {
  const cpf = somenteDigitos(valor);

  if (cpf.length !== 11 || /^(\d)\1{10}$/.test(cpf)) return false;

  const digito = (quantidade: number): number => {
    let soma = 0;
    for (let i = 0; i < quantidade; i++) {
      soma += Number(cpf[i]) * (quantidade + 1 - i);
    }
    const resto = (soma * 10) % 11;
    return resto === 10 ? 0 : resto;
  };

  return digito(9) === Number(cpf[9]) && digito(10) === Number(cpf[10]);
}

/** 2026-02-10 → 10/02/2026 */
export function formatarData(valor: string | null | undefined): string {
  if (!valor) return '';
  const [ano, mes, dia] = valor.split('-');
  return dia && mes && ano ? `${dia}/${mes}/${ano}` : valor;
}
