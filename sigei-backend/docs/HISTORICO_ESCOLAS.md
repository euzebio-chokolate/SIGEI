# Histórico anual das escolas

Uma escola mantém o mesmo `escolaId` em todos os anos. Cada combinação de escola
e ano possui um `RegistroAnual` independente, com matrículas, equipe e
infraestrutura próprias. Não é necessária migração de banco para esta funcionalidade:
as tabelas e a coluna `totalAlunos` já existem nas entidades.

## Cadastro de um ano anterior

As operações de escrita exigem autenticação com perfil `ADMIN`.

1. Consulte `GET /anos-referencia` e reutilize o ID do ano desejado. Se ainda não existir,
   cadastre com `POST /anos-referencia`, enviando `{"ano": 1990}`.
2. Crie o registro com `POST /registros-anuais`. Exemplo para escola de ID 1
   e ano 1990 cadastrado com ID 5:

```json
{
  "escolaId": 1,
  "anoReferenciaId": 5,
  "rede": "Estadual",
  "localFuncionamento": "Rural",
  "totalAlunos": 40
}
```

3. Use o `id` retornado para cadastrar as informações daquele ano nos endpoints
   existentes de matrículas, equipe e infraestrutura. Matrículas recebem
   `registroId`; equipe e infraestrutura recebem `registroAnualId`.
4. Repita para outros anos. O cadastro de uma combinação escola/ano já existente
   retorna `409`. Para corrigir esse registro, use `PUT /registros-anuais/{id}`
   com o seu `id` no caminho.

`totalAlunos` é opcional e deve ser maior ou igual a zero. Representa o total
consolidado informado para aquele ano, inclusive quando não há detalhamento por
nível ou série. Não é calculado automaticamente a partir dessas listas; ao
alterar matrículas manualmente, atualize também o total consolidado quando
necessário. Não some matrículas por nível com matrículas por série, pois podem
representar os mesmos alunos.

Na criação, omitir o total mantém o valor desconhecido (`null`). Na edição,
omitir ou enviar `null` preserva o total existente, mantendo compatibilidade com
clientes anteriores. Enviar `0` registra explicitamente zero alunos.
Rede e local de funcionamento continuam obrigatórios.

## Consulta do histórico

Exige autenticação com perfil `ADMIN` ou `USUARIO`.

```http
GET /escolas/1/registros-anuais
GET /escolas/1/registros-anuais?anoInicial=1990&anoFinal=2026
GET /escolas/1/registros-anuais?anoInicial=1990
GET /escolas/1/registros-anuais?anoFinal=1992
```

Os limites são opcionais e inclusivos. A resposta é uma lista de registros
anuais ordenada do menor para o maior ano. Cada item contém `id`,
`anoReferencia` (`id` e `ano`), `rede`, `localFuncionamento`, `totalAlunos`,
`equipe`, `infraestrutura`, `matriculasNivel` e `matriculasSerie`.
O campo `totalAlunos` também passa a fazer parte das respostas existentes de
registros anuais.

- `200`: histórico encontrado ou lista vazia se a escola não tiver registros no período.
- `400`: ID da escola não positivo, ano não positivo, formato inválido
  ou ano inicial maior que o final.
- `404`: escola inexistente.

Anos sem registro não aparecem na lista. Totais e infraestrutura desconhecidos
não são convertidos para zero. Para gráficos, use `anoReferencia.ano` no eixo
horizontal e `totalAlunos` ou `infraestrutura.numSalas` no eixo vertical.
Apresente lacunas como dados não informados; não presuma crescimento anual entre
anos não consecutivos nem calcule variação percentual com base igual a zero.

Para consultar apenas um ano (valor do ano civil, não o ID):

```http
GET /escolas/1/registros-anuais/1990
```

O detalhe `GET /escolas/{id}` continua selecionando o maior ano de referência.
Cadastrar 1990 depois de 2026 não muda essa seleção.

## Limites do histórico

Nome, aldeia, tipo de unidade e vínculo com escola sede continuam pertencendo ao
cadastro atual da escola. Esta implementação preserva dados anuais, sem criar
versões históricas desses campos cadastrais.

Se a equipe de um ano for desconhecida, não crie o respectivo cadastro: os campos
booleanos da equipe existente representam apenas sim/não. A ausência da equipe
é retornada como `null`.
