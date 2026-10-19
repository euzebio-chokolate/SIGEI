/**
 * Apoio aos testes de componente: interage com o DOM como o usuário, disparando
 * os mesmos eventos que o navegador (input, change, blur), para que formulários
 * reativos e bindings de evento reajam como na tela.
 */

export function campo<T extends HTMLElement = HTMLInputElement>(raiz: HTMLElement, seletor: string): T {
  const elemento = raiz.querySelector<T>(seletor);
  if (!elemento) throw new Error(`Elemento não encontrado: ${seletor}`);
  return elemento;
}

export function digitar(elemento: HTMLInputElement | HTMLTextAreaElement, valor: string): void {
  elemento.value = valor;
  elemento.dispatchEvent(new Event('input', { bubbles: true }));
  elemento.dispatchEvent(new Event('blur'));
}

export function escolher(elemento: HTMLSelectElement, valor: string): void {
  elemento.value = valor;
  elemento.dispatchEvent(new Event('change', { bubbles: true }));
}

/** Botão pelo texto visível, ignorando espaços extras. */
export function botao(raiz: HTMLElement, texto: string): HTMLButtonElement {
  const encontrado = Array.from(raiz.querySelectorAll('button'))
    .find(item => normalizar(item.textContent).includes(texto));
  if (!encontrado) throw new Error(`Botão não encontrado: ${texto}`);
  return encontrado;
}

export function texto(raiz: HTMLElement): string {
  return normalizar(raiz.textContent);
}

/** Simula a escolha de um arquivo num input type="file". */
export function selecionarArquivo(elemento: HTMLInputElement, arquivo: File): void {
  Object.defineProperty(elemento, 'files', { value: [arquivo], configurable: true });
  elemento.dispatchEvent(new Event('change', { bubbles: true }));
}

function normalizar(valor: string | null): string {
  return (valor ?? '').replace(/\s+/g, ' ').trim();
}
