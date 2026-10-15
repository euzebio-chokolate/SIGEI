/** Espelha o PaginaDTO do backend (listagens paginadas de todos os anos). */
export interface Pagina<T> {
  itens: T[];
  pagina: number;
  tamanho: number;
  totalItens: number;
  totalPaginas: number;
}
