/**
 * Rótulos dos níveis de matrícula (MatriculaNivel.nivel) e das etapas das
 * séries (MatriculaSerie.etapaEnsino), que a API grava como códigos
 * (FUNDAMENTAL_ANOS_INICIAIS). Fonte única para as telas que os exibem.
 */
const ROTULOS_NIVEL: Record<string, string> = {
  TOTAL_GERAL: 'Total Geral',
  ENSINO_INFANTIL: 'Ensino Infantil',
  FUNDAMENTAL_ANOS_INICIAIS: 'Fundamental - Anos Iniciais',
  FUNDAMENTAL_ANOS_FINAIS: 'Fundamental - Anos Finais',
  ENSINO_MEDIO: 'Ensino Médio',
  EJA: 'EJA',
  AEE: 'AEE'
};

/** Código desconhecido aparece sem os sublinhados: NOVO_NIVEL → NOVO NIVEL. */
export function rotuloNivel(valor: string | null | undefined): string {
  if (!valor) return '-';
  return ROTULOS_NIVEL[valor] ?? valor.replace(/_/g, ' ');
}
