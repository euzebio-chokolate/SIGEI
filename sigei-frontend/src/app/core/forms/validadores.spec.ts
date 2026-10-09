import { FormControl } from '@angular/forms';

import { textoObrigatorio, validarArquivo } from './validadores';

describe('textoObrigatorio', () => {
  it('recusa texto só com espaços, como o @NotBlank', () => {
    expect(new FormControl('   ', textoObrigatorio).errors).toEqual({ required: true });
  });

  it('recusa vazio e nulo', () => {
    expect(new FormControl('', textoObrigatorio).errors).toEqual({ required: true });
    expect(new FormControl(null, textoObrigatorio).errors).toEqual({ required: true });
  });

  it('aceita texto preenchido', () => {
    expect(new FormControl(' Huni Kui ', textoObrigatorio).errors).toBeNull();
  });
});

describe('validarArquivo', () => {
  const arquivo = (nome: string, bytes: number) => new File([new Uint8Array(bytes)], nome);

  it('aceita a planilha no formato e tamanho esperados', () => {
    expect(validarArquivo(arquivo('Quadro.XLSX', 10), { extensoes: ['.xlsx'] })).toBeNull();
  });

  it('recusa outra extensão', () => {
    expect(validarArquivo(arquivo('quadro.xls', 10), { extensoes: ['.xlsx'] }))
      .toBe('O arquivo deve estar no formato .xlsx.');
  });

  it('recusa arquivo acima do limite do backend', () => {
    expect(validarArquivo(arquivo('quadro.xlsx', 2 * 1024 * 1024), { extensoes: ['.xlsx'], tamanhoMaximoMb: 1 }))
      .toBe('O arquivo deve ter no máximo 1 MB.');
  });

  it('recusa arquivo vazio', () => {
    expect(validarArquivo(arquivo('quadro.xlsx', 0), { extensoes: ['.xlsx'] })).toBe('O arquivo está vazio.');
  });
});
