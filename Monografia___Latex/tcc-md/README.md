# Texto do TCC em Markdown

Primeira versão completa do texto do TCC sobre o SIGEI, escrita conforme o sumário aprovado em 05/10/2026 (seção 9 de `../00-analise-do-projeto.md`). Os capítulos descrevem o sistema no estado do commit `026840e` (06/10/2026), depois das correções do incremento 7.

| Arquivo | Conteúdo |
| --- | --- |
| `00-pre-textuais.md` | Capa, folha de rosto, agradecimentos (placeholder), resumo, *abstract*, listas e sumário |
| `01-introducao.md` | Capítulo 1 |
| `02-referencial-teorico.md` | Capítulo 2 |
| `03-metodologia.md` | Capítulo 3 |
| `04-sigei.md` | Capítulo 4 |
| `05-conclusoes.md` | Capítulo 5 |
| `06-referencias.md` | Referências citadas |
| `referencias-a-pesquisar.md` | Fontes que ainda precisam ser encontradas |

Total aproximado: 14 mil palavras nos capítulos, 46 figuras e 9 quadros.

## Marcadores usados no texto

| Marcador | Significado |
| --- | --- |
| `[INSERIR FIGURA N – ...]` | Captura de tela ou diagrama a inserir. A legenda e a fonte ficam logo abaixo. |
| `[REFERÊNCIA NECESSÁRIA: ...]` | Afirmação que precisa de fonte (ver `referencias-a-pesquisar.md`). |
| `[DADO A CONFIRMAR: ...]` | Número ou fato que não pode entrar no texto sem fonte. |
| `[CONFIRMAR: ...]` | Informação que só o autor pode dar (reuniões, validação, origem das planilhas, implantação). |
| `[ATENÇÃO — trecho que depende de correção no sistema: ...]` | Comportamento atual do sistema que o autor pretende corrigir. Revisar o texto depois da correção. |
| `[A PREENCHER]` | Resultado de teste que só pode ser registrado após a execução. |

Para listar todos os pendentes: `grep -n "\[\(INSERIR\|REFERÊNCIA\|DADO\|CONFIRMAR\|ATENÇÃO\|A PREENCHER\|AJUSTAR\|CONFERIR\)" *.md`.

## Pendências que dependem de informações do autor

1. Titulação atual do orientador (Me. ou Dr.).
2. Se houve reuniões com a SEE/AC: datas, formato e temas (seção 3.2.1).
3. Origem das planilhas e autorização de uso dos dados (capítulo 1, seções 2.2 e 3.2.1).
4. Como a demanda chegou ao autor (seção 3.2.1).
5. Se houve apresentação ou validação do sistema com a SEE/AC (seções 3.1, 3.4 e 4.3.3; capítulo 5).
6. Se o sistema foi implantado em algum servidor (seção 4.2.7).
7. Setor da SEE/AC que seria o público-alvo (início do capítulo 4).

## Trechos a revisar depois das correções no sistema

O autor informou que fará correções no sistema. Os trechos abaixo descrevem o comportamento atual e devem ser revistos depois delas:

| Comportamento atual | Onde aparece |
| --- | --- |
| Sete preferências da tela de configurações não têm efeito | 4.1.13; cap. 5 |
| A senha mínima é de 4 caracteres no cadastro de usuário e de 8 na troca de senha | 4.1.2 e 4.1.12 (o texto cita apenas os 8 da troca) |
| Coleção do Bruno anterior aos *cookies* e ao CSRF | 4.3.1 |
| *Front-end* sem testes automatizados | 4.3; cap. 5 |
| A trilha de auditoria não tem tela; é consultada só pela API | 4.1.10 e 4.2.6; cap. 5 |

Se alguma correção mudar contagens (testes, *endpoints*, requisitos), atualize também o resumo, o *abstract*, o Quadro 8 e o capítulo 5.

### Correções já incorporadas ao texto (incremento 7, 06/10/2026)

| Correção | Commit | Onde o texto foi atualizado |
| --- | --- | --- |
| Vínculo sede/anexo unificado em `escola.escola_sede_id`; tabela e rota `/anexo` removidas | `f294923` | 3.2.5, 3.2.6, 4.1.9, 4.2.3; cap. 5 |
| Trilha de auditoria com Hibernate Envers e `GET /escola/{id}/historico` | `588541e` | Resumo, *abstract*, RNF11, 3.3.1, 4.1.10, 4.2.2, Quadro 7, nova seção 4.2.6; cap. 5 |
| Desfazimento do cadastro completo em `IOException` | `d396a9d` | 4.1.9 |
| Abertura de novo ano copia `totalAlunos`, `MatriculaSerie.total` e o ano da equipe | `a8ed010` | 4.1.11; CT10 |
| Um marcador por escola no mapa (`GET /infraestrutura/maisRecentes`) | `b0921dc` | 4.1.1; Quadro 7 |
| *Login*: mesma resposta 401 para e-mail inexistente e senha incorreta; 403 para usuário desativado | `0c62937` | 4.1.2, 4.2.2; CT02 e novo CT15 |
| Opções únicas de situação, acesso, água e energia (sem "aéreo" e "extinta") | `8919c6b`, `9594849`, `026840e` | 4.1.1, 4.1.9, 4.1.14; cap. 5 |

Contagens atualizadas: 236 casos em 34 classes de teste (Quadro 8), 16 controladores e 70 operações (4.2.2 e cap. 5), 13 tabelas de domínio e autenticação mais as de auditoria (3.2.6 e 4.2.3). A coleção do Bruno continua com 51 requisições: saíram as duas de `/anexo` e entraram as de `/escola/{id}/historico` e `/infraestrutura/maisRecentes`.

### Diagramas desatualizados

Alguns diagramas de `../diagramas/` ainda mostram a tabela e a classe `Anexo`. Atualize-os antes de exportar as figuras correspondentes:

- `02-casos-de-uso.md` (Figura 1): caso de uso "Manter municípios e vínculos da tabela anexo";
- `06-classes-territorio.md` (Figura 2): classe `Anexo` e seus relacionamentos;
- `08-er-territorio.md` (Figura 4): tabela `anexo`;
- `README.md` da pasta: descrição do `06` e o item 3 das observações, que trata a duplicidade como atual.

As tabelas da trilha de auditoria não precisam entrar nos diagramas, pois o texto as descreve na seção 4.2.6.

## Próximos passos para fechar o texto

1. Responder às pendências acima e substituir os marcadores `[CONFIRMAR]`.
2. Pesquisar as referências de `referencias-a-pesquisar.md` e escrever a seção 2.7 (trabalhos relacionados).
3. Exportar os diagramas PlantUML de `../diagramas/` (figuras 1 a 6, 29, 34, 39 a 41, 43 e 44).
4. Fazer as capturas de tela com dados de demonstração, sem dados pessoais nem a senha real do administrador.
5. Executar a suíte de testes com o JDK 21 e o Docker ativo e atualizar o Quadro 8, a seção 4.3.1, o resumo e o *abstract*.
6. Executar o roteiro do Quadro 9 e preencher a coluna de resultados.
7. Converter o texto para o `tcc.tex`/`desenvolvimento.tex`, substituindo o conteúdo do pré-projeto. Corrigir no preâmbulo o curso (`\programa` e `\natureza`), a titulação do orientador e o tipo de trabalho (`\objeto`), e acrescentar ao `refs.bib` as referências novas.
