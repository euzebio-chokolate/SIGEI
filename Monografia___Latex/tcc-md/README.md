# Texto do TCC em Markdown

Primeira versão completa do texto do TCC sobre o SIGEI, escrita conforme o sumário aprovado em 05/10/2026 (seção 9 de `../00-analise-do-projeto.md`). Os capítulos descrevem o sistema no estado do commit `e399305` (02/10/2026).

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
6. Se o sistema foi implantado em algum servidor (seção 4.2.6).
7. Setor da SEE/AC que seria o público-alvo (início do capítulo 4).

## Trechos a revisar depois das correções no sistema

O autor informou que fará correções no sistema. Os trechos abaixo descrevem o comportamento atual e devem ser revistos depois delas:

| Comportamento atual | Onde aparece |
| --- | --- |
| `GET /infraestrutura` retorna todos os anos; uma escola pode ter vários marcadores no mapa | 4.1.1; limitações no cap. 5 |
| A abertura de novo ano não copia `totalAlunos` nem `fotoPublicId` | 4.1.11; cap. 5 |
| `@Transactional` sem desfazimento para `IOException` no cadastro completo | 4.1.9; cap. 5 |
| Senha incorreta ou usuário inativo no *login* podem resultar em erro 500 | Caso CT02 (Quadro 9); cap. 5 |
| A situação no formulário (funcionando, paralisada, desativada) e no filtro do mapa (funcionando, paralisada, extinta) divergem, assim como o tipo de acesso (aéreo existe só no formulário) | 4.1.1 e 4.1.9; cap. 5 |
| Sete preferências da tela de configurações não têm efeito | 4.1.13; cap. 5 |
| Vínculo sede/anexo duplicado (`escola.escola_sede_id` e tabela `anexo`) | 4.2.3; cap. 5 |
| A senha mínima é de 4 caracteres no cadastro de usuário e de 8 na troca de senha | 4.1.2 e 4.1.12 (o texto cita apenas os 8 da troca) |
| Coleção do Bruno anterior aos *cookies* e ao CSRF | 4.3.1 |
| *Front-end* sem testes automatizados | 4.3; cap. 5 |

Se alguma correção mudar contagens (testes, *endpoints*, requisitos), atualize também o resumo, o *abstract*, o Quadro 8 e o capítulo 5.

## Próximos passos para fechar o texto

1. Responder às pendências acima e substituir os marcadores `[CONFIRMAR]`.
2. Pesquisar as referências de `referencias-a-pesquisar.md` e escrever a seção 2.7 (trabalhos relacionados).
3. Exportar os diagramas PlantUML de `../diagramas/` (figuras 1 a 6, 29, 34, 39 a 41, 43 e 44).
4. Fazer as capturas de tela com dados de demonstração, sem dados pessoais nem a senha real do administrador.
5. Executar a suíte de testes com o JDK 21 e o Docker ativo e atualizar o Quadro 8, a seção 4.3.1, o resumo e o *abstract*.
6. Executar o roteiro do Quadro 9 e preencher a coluna de resultados.
7. Converter o texto para o `tcc.tex`/`desenvolvimento.tex`, substituindo o conteúdo do pré-projeto. Corrigir no preâmbulo o curso (`\programa` e `\natureza`), a titulação do orientador e o tipo de trabalho (`\objeto`), e acrescentar ao `refs.bib` as referências novas.
