# Diagramas do SIGEI para o TCC

Conjunto de **20 diagramas em PlantUML**, elaborado a partir do backend, frontend, migrações SQL e arquivos de implantação disponíveis em 02/10/2026. Cada arquivo contém objetivo, código independente, referências à implementação e, quando necessário, observações de leitura.

Os diagramas documentam o código encontrado no repositório. Não representam uma infraestrutura em produção verificada, nem funcionalidades futuras. A análise foi estática; a aplicação e o banco não foram alterados.

## Índice e uso na monografia

| Arquivo | Conteúdo | Local sugerido |
| --- | --- | --- |
| [01-contexto.md](01-contexto.md) | Participantes e integrações externas | Visão geral da solução |
| [02-casos-de-uso.md](02-casos-de-uso.md) | Funcionalidades por perfil | Requisitos / análise do sistema |
| [03-arquitetura-containers.md](03-arquitetura-containers.md) | SPA, servidor web, API e banco | Arquitetura implementada |
| [04-componentes-backend.md](04-componentes-backend.md) | Camadas e dependências Spring | Arquitetura do backend |
| [05-componentes-frontend.md](05-componentes-frontend.md) | Features, domains, core e shared | Arquitetura do frontend |
| [06-classes-territorio.md](06-classes-territorio.md) | Município, terra, povo, aldeia, escola e anexo | Modelagem de domínio / apêndice |
| [07-classes-registro-anual.md](07-classes-registro-anual.md) | Agregado anual, equipe e matrículas | Modelagem de domínio |
| [08-er-territorio.md](08-er-territorio.md) | Esquema físico territorial | Modelo de dados |
| [09-er-dados-anuais.md](09-er-dados-anuais.md) | Esquema físico dos dados por ano | Modelo de dados |
| [10-er-autenticacao.md](10-er-autenticacao.md) | Usuários e refresh tokens | Modelo de dados / segurança |
| [11-sequencia-login.md](11-sequencia-login.md) | Login e senha temporária | Segurança e controle de acesso |
| [12-sequencia-renovacao.md](12-sequencia-renovacao.md) | 401, rotação e repetição da chamada | Segurança / apêndice |
| [13-sequencia-mapa-publico.md](13-sequencia-mapa-publico.md) | Consulta pública, filtros e tiles | Mapa público interativo |
| [14-sequencia-cadastro-completo.md](14-sequencia-cadastro-completo.md) | Transação de cadastro e fotografia | Cadastro de escolas |
| [15-atividade-formulario.md](15-atividade-formulario.md) | Sete etapas do cadastro/edição | Funcionalidades / apêndice |
| [16-atividade-abertura-ano.md](16-atividade-abertura-ano.md) | Criação do próximo ano e cópias | Histórico anual e abertura de ano |
| [17-sequencia-historico.md](17-sequencia-historico.md) | Consulta por escola e período | Histórico anual / apêndice |
| [18-sequencia-dashboard.md](18-sequencia-dashboard.md) | Agregações e séries históricas | Painel de indicadores / apêndice |
| [19-atividade-importacao.md](19-atividade-importacao.md) | Carga automática e importação manual | Carga inicial e importação |
| [20-implantacao-docker.md](20-implantacao-docker.md) | Topologia dos Compose e portas | Implantação |

Uma seleção para o texto principal é **02, 03, 05, 08, 09, 10, 11, 14, 16 e 20**. As outras figuras podem detalhar a explicação ou integrar apêndices. Classes e modelo relacional têm propósitos diferentes: as classes descrevem objetos e associações; o modelo relacional explicita tabelas, tipos, nulabilidade e restrições. Não é necessário repetir todas as figuras em um mesmo capítulo.

O modelo relacional foi dividido em três figuras para preservar a leitura. Juntas, elas abrangem as 14 tabelas de negócio/autenticação das migrações V1 e V2. `flyway_schema_history` é uma tabela técnica do Flyway e não integra esse domínio.

## Convenções

- Cada bloco começa com `@startuml` e termina com `@enduml`; não há dependências de temas ou includes remotos.
- Os diagramas usam fundo branco e traços monocromáticos para impressão.
- Os endpoints `/api/...` correspondem ao acesso do frontend. O Nginx remove `/api` antes de encaminhar ao backend.
- `ADMIN` e `USUARIO` são os perfis persistidos. `ROLE_TROCAR_SENHA` é uma autoridade temporária atribuída pelo filtro, não um terceiro valor do enum de perfil.
- As cardinalidades dos modelos acompanham o esquema efetivo; regras de formulário mais restritas estão descritas em notas.
- Os exemplos não usam conteúdo do `.env` real. A configuração foi examinada a partir de `.env.example`, propriedades e Compose.

## Relação com os requisitos existentes

Os identificadores abaixo são os já usados em [desenvolvimento.tex](../desenvolvimento.tex), sem criar uma nova numeração de requisitos.

| Requisito | Diagramas de apoio | Cobertura |
| --- | --- | --- |
| RF01 e RF02 — cadastrar/editar escola | 02, 06, 07, 14, 15 | Formulário e gravação completa |
| RF03 — excluir escola | 02, 04, 07 | Ação administrativa e relações de persistência; vínculos podem impedir exclusão |
| RF04 e RF10 — mapa | 01, 03, 13 | Consulta, filtros locais, camadas e fotografias |
| RF05 — aldeias | 02, 06, 08 | Cadastro e vínculos territoriais |
| RF06 — municípios | 02, 06, 08, 19 | Consulta na interface, manutenção na API e uso na importação |
| RF07 — terras indígenas | 02, 06, 08 | Cadastro e relações |
| RF08 — relatórios | 18 | Atendimento parcial: painel de indicadores, sem exportação |
| RF09 — matrículas | 07, 09, 14, 16, 19 | Dados anuais, gravação e importação |
| RNF02 — acesso por navegador | 03, 05, 20 | Arquitetura web |
| RNF03 — controle de acesso | 02, 10, 11, 12 | Perfis, cookies, JWT e renovação |
| RNF06 — integridade | 08, 09, 10, 14 | Chaves, unicidade, validação e transação |

Diagramas não demonstram, por si, conformidade com metas de desempenho ou usabilidade. RNF01, RNF04 e RNF05 devem ser sustentados por evidências adequadas da interface e de avaliação, conforme o texto da monografia.

## Pontos da implementação que afetam a leitura

1. **Mapa:** `GET /infraestrutura` retorna todos os registros, sem filtrar o ano. Uma escola pode ter vários marcadores quando houver vários anos.
2. **Relação escola–aldeia:** `escola.aldeia_id` é único e anulável. O banco não modela várias escolas para a mesma aldeia.
3. **Sede/anexo:** coexistem `escola.escola_sede_id` e a tabela `anexo`. O cadastro completo usa a primeira relação, sem sincronizar automaticamente a segunda.
4. **Abertura de ano:** copia o registro mais recente de cada escola, preservando quantitativos de matrícula. Não copia `totalAlunos`, `fotoPublicId` nem atribui o ano da equipe. A figura 16 registra exatamente esse comportamento.
5. **Histórico:** são registros por ano; não há trilha de auditoria que preserve cada edição de um registro.
6. **Fotos e transação:** há compensação de upload quando a transação é desfeita. A anotação transacional não declara rollback para `IOException`; a figura 14 não promete atomicidade para qualquer erro possível.
7. **Segurança:** consultas de infraestrutura são públicas; demais consultas de negócio exigem perfil. Alterações exigem `ADMIN`, com exceção da troca da própria senha por usuário autenticado e das operações de sessão.
8. **Importação:** os layouts e o ano 2025 são fixos. A carga inicial executa infraestrutura antes de matrículas, sem transação única envolvendo ambas.
9. **Implantação:** o Compose do frontend usa `host.docker.internal` para alcançar a API. O `PORT=8081` do exemplo exige ajustar `API_UPSTREAM`; não há terminação TLS no Nginx fornecido.

Essas notas registram diferenças relevantes entre a implementação e uma eventual descrição idealizada. Não foram feitas correções no software como parte deste trabalho.

## Visualizar e exportar

Copie o conteúdo de um bloco `plantuml`, incluindo `@startuml` e `@enduml`, para um editor compatível ou arquivo `.puml`. O Markdown permanece como fonte principal solicitada para a documentação.

Para extrair todos os blocos localmente, execute no PowerShell, **dentro desta pasta**:

```powershell
$saidaDiagramas = Join-Path (Get-Location) 'exportados'
New-Item -ItemType Directory -Path $saidaDiagramas -Force | Out-Null
$utf8Diagramas = New-Object System.Text.UTF8Encoding($false)
Get-ChildItem -File -Filter '*.md' | Where-Object Name -Match '^\d{2}-' | ForEach-Object {
    $conteudoDiagrama = Get-Content -LiteralPath $_.FullName -Raw -Encoding UTF8
    $blocoDiagrama = [regex]::Match($conteudoDiagrama, '(?s)```plantuml\s*\r?\n(.*?)```')
    if (-not $blocoDiagrama.Success) { throw "Bloco PlantUML ausente: $($_.Name)" }
    $arquivoDiagrama = Join-Path $saidaDiagramas ($_.BaseName + '.puml')
    [System.IO.File]::WriteAllText($arquivoDiagrama, $blocoDiagrama.Groups[1].Value, $utf8Diagramas)
}
```

Com Java e o JAR do PlantUML disponíveis, ajuste o caminho do JAR e renderize:

```powershell
java -jar 'C:\ferramentas\plantuml.jar' -charset UTF-8 -tsvg 'exportados/*.puml'
java -jar 'C:\ferramentas\plantuml.jar' -charset UTF-8 -tpng -Sdpi=200 'exportados/*.puml'
```

O nome declarado em `@startuml` usa sublinhados; por isso as imagens geradas terão nomes como `03_arquitetura_containers.png`. Prefira SVG para edição vetorial; para inclusão direta com `graphicx` no LaTeX, utilize PNG ou uma exportação em PDF. Figuras largas ficam mais legíveis em página paisagem; figuras altas devem respeitar também a altura útil da página.

Exemplo de inclusão, após copiar a imagem para `Monografia___Latex/imagens`:

```latex
\begin{figure}[htbp]
    \centering
    \caption{Arquitetura geral do SIGEI}
    \includegraphics[width=\textwidth,height=0.80\textheight,keepaspectratio]
        {imagens/03_arquitetura_containers.png}
    \label{fig:arquitetura-sigei}
    \par\small Fonte: Elaborado pelo autor (2026).
\end{figure}
```

Adapte a apresentação da fonte ao padrão da classe da monografia. Os arquivos `.tex` e as imagens já existentes não foram substituídos.

## Validação realizada

- Extração e verificação de um bloco PlantUML completo por arquivo.
- Conferência de existência das referências locais citadas.
- Renderização local das 20 fontes para SVG com PlantUML 1.2026.2 e Graphviz, sem erros de sintaxe.
- Conferência do modelo físico com as migrações e revisão das sequências contra services, controllers e componentes Angular.

Essa validação verifica a documentação e a sintaxe; não equivale à execução de testes funcionais do SIGEI.
