import { AbstractControl, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';

/** Tamanho das colunas de texto sem @Column(length): o varchar(255) padrão do JPA. */
export const TAMANHO_TEXTO = 255;

/** Senhas: de 8 a 72 caracteres, como no backend (o BCrypt não aceita mais de 72 bytes). */
export const TAMANHO_MINIMO_SENHA = 8;
export const TAMANHO_MAXIMO_SENHA = 72;

/** Limite de upload do backend (spring.servlet.multipart.max-file-size). */
export const TAMANHO_MAXIMO_UPLOAD_MB = 20;

/**
 * Equivale ao @NotBlank do backend: só espaços contam como vazio.
 * O Validators.required aceita "   ", que o backend recusa com 400.
 */
export const textoObrigatorio: ValidatorFn = (controle: AbstractControl): ValidationErrors | null =>
  typeof controle.value === 'string' && controle.value.trim() === ''
    ? { required: true }
    : Validators.required(controle);

/**
 * Confere um arquivo antes do envio, para não gastar o upload com algo que o
 * backend vai recusar.
 * @returns a mensagem do problema, ou null se o arquivo é aceitável.
 */
export function validarArquivo(
  arquivo: File,
  { extensoes, tamanhoMaximoMb = TAMANHO_MAXIMO_UPLOAD_MB }: { extensoes: string[]; tamanhoMaximoMb?: number }
): string | null {
  const nome = arquivo.name.toLowerCase();
  if (!extensoes.some(extensao => nome.endsWith(extensao))) {
    return `O arquivo deve estar no formato ${extensoes.join(' ou ')}.`;
  }
  if (arquivo.size > tamanhoMaximoMb * 1024 * 1024) {
    return `O arquivo deve ter no máximo ${tamanhoMaximoMb} MB.`;
  }
  if (arquivo.size === 0) {
    return 'O arquivo está vazio.';
  }
  return null;
}
