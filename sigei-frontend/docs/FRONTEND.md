# Documentação técnica do frontend

Descreve a implementação atual do `sigei-frontend`: organização do código,
rotas, autenticação, comunicação com a API e funcionamento das telas. Os
contratos da API estão documentados no backend (`sigei-backend/CLAUDE.md` e
Swagger em `/swagger-ui.html`).

## Stack

| Item | Versão declarada | Uso |
| --- | --- | --- |
| Angular | 21.2 | Componentes standalone, roteamento com carregamento sob demanda, formulários reativos e `HttpClient`. |
| TypeScript | 5.9 | Modo `strict` habilitado. |
| Tailwind CSS | 4.3 | Estilização por classes utilitárias (via `@tailwindcss/postcss`). |
| Leaflet | 1.9.4 | Mapa público. Usado diretamente, sem `ngx-leaflet`. |
| @ng-icons/heroicons | 34 | Ícones. |
| RxJS | 7.8 | Composição das chamadas HTTP. |
| Vitest | 4.0 | Executor de testes configurado. Não há arquivos `.spec.ts` no momento. |

A aplicação usa detecção de mudanças **sem Zone.js**
(`provideZonelessChangeDetection` em `app.config.ts`). Por isso os componentes
chamam `ChangeDetectorRef.markForCheck()` depois de receber dados assíncronos.
Mantenha esse padrão ao criar telas novas; sem ele, a tela não atualiza.

## Estrutura de pastas

```text
src/
├── app/
│   ├── app.config.ts        # providers globais (router, HttpClient, interceptor)
│   ├── app.routes.ts        # rotas de primeiro nível
│   ├── core/auth/           # AuthService, guards, interceptor e modelos de sessão
│   ├── domains/             # acesso a dados e modelos, agrupados por domínio
│   │   ├── escolas/         # escola, registro anual, equipe, infraestrutura, matrículas, anos
│   │   │   ├── data-access/ # serviços HTTP
│   │   │   ├── models/      # interfaces de request/response
│   │   │   └── ui/          # componentes reutilizáveis do domínio (modais)
│   │   ├── territorios/     # município, terra indígena, aldeia, povo
│   │   └── usuarios/
│   ├── features/            # telas, agrupadas por área da aplicação
│   │   ├── mapa-publico/    # página inicial pública
│   │   ├── auth/            # login e troca de senha
│   │   └── admin/           # área administrativa (shell + páginas)
│   └── shared/ui/           # header, modais de confirmação, sucesso e listagem
├── assets/                  # logo e marcadores SVG do mapa
├── environments/            # URL da API por ambiente
└── styles.css
```

Regra de dependência: `features` usa `domains`, `core` e `shared`; `domains`
não importa de `features`. Um serviço HTTP usado por mais de uma tela fica em
`domains/<domínio>/data-access`. A exceção é o `DashboardService`, que só a
tela de dashboard usa e fica em `features/admin/dashboard/data-access`.

Aliases configurados em `tsconfig.json`:

| Alias | Caminho |
| --- | --- |
| `@core/*` | `src/app/core/*` |
| `@domains/*` | `src/app/domains/*` |
| `@features/*` | `src/app/features/*` |
| `@shared/*` | `src/app/shared/*` |
| `@environments/*` | `src/environments/*` |

## Rotas

Todas as páginas usam `loadComponent`/`loadChildren`: cada tela é um bloco
JavaScript separado, baixado somente quando acessado.

| Rota | Tela | Guard |
| --- | --- | --- |
| `/` | Mapa público | — (pública) |
| `/login` | Login | — |
| `/alterar-senha` | Troca de senha | `authenticatedGuard` |
| `/admin` | Shell administrativo (redireciona para `/admin/dashboard`) | `authGuard` |
| `/admin/dashboard` | Indicadores | herda `authGuard` |
| `/admin/municipios` | Municípios com estatísticas (somente leitura) | herda |
| `/admin/terras-indigenas` | Lista de terras indígenas | herda |
| `/admin/terras-indigenas/novo`, `/editar/:id` | Formulário | `adminGuard` |
| `/admin/aldeias` | Lista de aldeias | herda |
| `/admin/aldeias/novo`, `/editar/:id` | Formulário | `adminGuard` |
| `/admin/povos` | Lista de povos | herda |
| `/admin/povos/novo`, `/editar/:id` | Formulário | `adminGuard` |
| `/admin/escolas` | Lista de escolas e abertura de novo ano | herda |
| `/admin/escolas/novo`, `/editar/:id` | Cadastro em etapas | `adminGuard` |
| `/admin/historico-escolas` | Histórico anual | herda |
| `/admin/importacoes` | Importação de planilhas de escolas, matrículas e professores | `adminGuard` |
| `/admin/usuarios` | Lista de usuários | herda |
| `/admin/usuarios/novo`, `/editar/:id` | Formulário | `adminGuard` |
| `/admin/configuracoes` | Preferências locais | herda |
| `**` | Redireciona conforme a sessão | `fallbackGuard` |

Guards (`core/auth/auth.guard.ts`):

- `authGuard`: exige sessão; se a troca de senha estiver pendente, envia para `/alterar-senha`.
- `authenticatedGuard`: exige apenas sessão, para permitir a troca de senha.
- `adminGuard`: exige sessão, troca de senha concluída e perfil `ADMIN`. Um `USUARIO` é enviado para `/admin/dashboard`.
- `fallbackGuard`: sem sessão → `/login`; troca pendente → `/alterar-senha`; demais casos → `/admin/dashboard`.

Os guards só controlam a navegação. Quem garante as permissões é o backend
(`SecurityConfig`). O shell administrativo também esconde botões de escrita
para quem não é administrador (`ehAdmin`).

## Autenticação e sessão

Fluxo implementado em `core/auth/auth.service.ts`:

1. `login()` apaga uma sessão anterior e chama `POST /auth/login`.
2. O backend grava o token de acesso (15 min) e o refresh token (7 dias) em
   cookies `HttpOnly` e `SameSite=Strict`. Os tokens nunca passam pelo
   JavaScript; o corpo da resposta traz só os dados do usuário, salvos em
   `localStorage['sigei_usuario']` para os guards e o menu.
3. O navegador envia os cookies automaticamente, porque a API é acessada pela
   mesma origem (`/api`). Em métodos não seguros, o `HttpClient` copia o
   cookie `XSRF-TOKEN` para o cabeçalho `X-XSRF-TOKEN` (proteção CSRF nativa
   do Angular, que só atua em URLs relativas).
4. Ao receber `401`, `authInterceptor` chama `POST /auth/refresh` (uma única
   chamada compartilhada entre requisições simultâneas) e repete a requisição.
   Se ela falhar de novo com `401`, a sessão local é apagada e o usuário vai
   para o login.
5. Se `alterarSenha` for verdadeiro, a senha digitada no login fica em
   `sessionStorage['sigei_senha_temporaria']` apenas para preencher o campo
   "senha atual" da tela de troca. Ela é removida assim que essa tela lê o
   valor, e também no logout.
6. `logout()` chama `POST /auth/logout`, que revoga a sessão e remove os
   cookies, e apaga as chaves locais.

`estaLogado()` indica apenas que houve login neste navegador, pois os cookies
`HttpOnly` não são legíveis. Quem decide a autorização é sempre o backend.

## Comunicação com a API

`environment.apiUrl` define a base das chamadas:

| Ambiente | Arquivo | `apiUrl` | Encaminhamento |
| --- | --- | --- | --- |
| Desenvolvimento | `environment.ts` | `/api` | `proxy.conf.json` remove `/api` e encaminha para `http://127.0.0.1:8081`. |
| Produção | `environment.production.ts` | `/api` | O Nginx encaminha para `API_UPSTREAM`. |

> A mesma origem é obrigatória: os cookies de sessão usam `SameSite=Strict` e
> a proteção CSRF do Angular só atua em URLs relativas. Não aponte `apiUrl`
> diretamente para outro domínio.

> O proxy de desenvolvimento aponta para a porta **8081**, e o backend usa
> **8080** por padrão. Ajuste `proxy.conf.json` ou a variável `PORT` do
> backend para que coincidam.

Serviços HTTP e rotas consumidas:

| Serviço | Base | Operações usadas |
| --- | --- | --- |
| `EscolaService` | `/escola` | listar, `GET /{id}`, salvar, editar, excluir |
| `RegistroAnualService` | `/registroAnual` | CRUD, `buscarPorAno`, `buscarPorEscolaEAno`, `historico` |
| `EquipeEscolaService` | `/equipe` | salvar, editar |
| `InfraestruturaService` | `/infraestrutura` | listar, salvar, editar, excluir, `POST /{id}/foto`, `DELETE /{id}/foto` |
| `MatriculaNivelService` | `/matriculaNivel` | CRUD |
| `MatriculaSerieService` | `/matriculaSerie` | CRUD |
| `AnoReferenciaService` | `/anos` | listar, salvar |
| `AtualizacaoAnualService` | `/atualizacao-anual` | `POST /iniciar` (resposta em texto) |
| `MunicipioService` | `/municipio` | `GET /estatisticas` |
| `TerraIndigenaService` | `/terra` | `GET /estatisticas`, `GET /{id}`, salvar, editar, excluir |
| `AldeiaService` | `/aldeia` | CRUD e `GET /{id}` |
| `PovoService` | `/povo` | CRUD e `GET /{id}` |
| `UsuarioService` | `/usuario` | CRUD e `GET /{id}` |
| `DashboardService` | `/dashboard` | `GET` com `ano` opcional |
| `AuthService` | `/auth` | `POST /login`, `POST /refresh`, `POST /logout`, `PUT /alterar-senha` |

## Telas

### Mapa público

`features/mapa-publico`. É a página inicial e não exige login.

- Carrega `GET /infraestrutura`, rota pública, porque cada registro de
  infraestrutura traz as coordenadas, a situação e os dados resumidos da escola.
- Centro inicial: `[-8.77, -70.55]`, zoom 7 (estado do Acre).
- Camadas de fundo: OpenStreetMap (padrão), Esri World Imagery (satélite),
  OpenTopoMap (terreno) e CARTO Dark (escuro).
- Marcador verde para escolas em funcionamento e vermelho para as de situação
  `PARALISADA`.
- Filtros (aplicados no navegador): texto por nome ou código INEP, situação,
  tipo de acesso, "com internet" e "com prédio". A busca ignora acentos.
- O *popup* mostra foto, nome, INEP, situação, acesso e selos de internet e
  prédio. Os textos são escapados antes de entrar no HTML.
- Ao escolher uma escola na busca, o mapa centraliza nela e abre o *popup*.
- Registros com coordenadas ausentes ou fora do intervalo válido não geram
  marcador.

Preferências lidas de `localStorage['sigei-preferencias']`: `camadaMapa`,
`abrirPopupAoPassarMouse` e `exibirFotosNoMapa`.

### Dashboard

Consome `GET /dashboard?ano=`. Exibe totais (escolas, matrículas, municípios,
terras indígenas), evolução de matrículas e de escolas por ano, matrículas por
nível, percentuais de internet, energia e prédio, escolas por município e
situação das escolas. Os gráficos são barras desenhadas com HTML e Tailwind
(largura/altura via `[style]`), sem biblioteca de gráficos.

### Cadastros territoriais

- **Municípios:** somente leitura, com estatísticas por município. Os
  municípios do Acre são cadastrados pelo backend na inicialização.
- **Terras indígenas, aldeias e povos:** listagem com busca e paginação, e
  formulário único para criação e edição (`/novo` e `/editar/:id`). A exclusão
  pede confirmação (`ConfirmModalComponent`).

### Escolas: cadastro em etapas

`features/admin/escolas/pages/escola-form`. O formulário é dividido em sete
etapas, cada uma um componente em `ui/form-steps`:

| Etapa | Componente | Conteúdo |
| --- | --- | --- |
| 1 | `EscolaEtapaComponent` | Código INEP, nome, tipo de unidade (sede/anexo), aldeia e escola-sede |
| 2 | `RegistroAnualEtapaComponent` | Ano de referência, rede, local de funcionamento, total de alunos |
| 3 | `EquipeEtapaComponent` | Professor responsável, equipe gestora, merendeira, zeladora, barqueiro, transporte |
| 4 | `InfraestruturaEtapaComponent` | Ano de construção, salas, coordenadas, situação, acesso, água, energia, internet, prédio, foto |
| 5 | `MatriculasNivelEtapaComponent` | Matrículas por nível (Infantil, Fundamental I e II, Médio, EJA, AEE) por sexo |
| 6 | `MatriculasSerieEtapaComponent` | Matrículas do 1º ano à 3ª série do Médio e ciclos 1 e 2 da EJA |
| 7 | `RevisaoEtapaComponent` | Resumo antes de salvar |

As listas de níveis e séries ficam em `escola-form.config.ts`. Cada etapa é
validada ao avançar. Na etapa 4, marcar "registro fotográfico" exige uma foto.

Ao salvar, as chamadas são encadeadas assim:

```text
escola (salvar/editar)
  └─ registro anual (salvar/editar)
       └─ em paralelo (forkJoin):
            ├─ equipe
            ├─ infraestrutura ─→ envio ou remoção da foto
            ├─ N × matrícula por nível
            └─ N × matrícula por série
```

Esse fluxo **não é atômico**. Se uma chamada intermediária falhar, as
anteriores já foram gravadas. Para completar, basta editar a escola e salvar de
novo, pois os IDs existentes são reaproveitados.

**Edição de um ano específico.** A partir do histórico, a tela abre com
`?ano=AAAA` (edita o registro daquele ano) ou `?novoAno=true` (cadastra um ano
anterior). Nesses casos ela começa na etapa 2, e ao trocar o ano carrega o
registro correspondente, se houver. Os demais anos não são alterados.

### Abertura de novo ano

Na lista de escolas, o administrador confirma um modal que chama
`POST /atualizacao-anual/iniciar`. O backend cria o ano seguinte e copia o
último registro de cada escola (ver `AtualizacaoAnualService` no backend).

### Histórico de escolas

`features/admin/historico-escolas`. Tem dois modos:

- **Por ano:** lista os registros de um ano (`buscarPorAno`), com filtro de texto e paginação.
- **Por escola:** consulta `GET /registroAnual/historico?escolaId=&anoInicial=&anoFinal=`
  e mostra a evolução ano a ano. O `escolaId` fica na URL, então voltar de uma
  edição reabre a mesma consulta.

`HistoricoEscolaModalComponent` (`domains/escolas/ui`) mostra o detalhe de um
registro. O total de alunos usa `totalAlunos` e, se ele estiver vazio, o nível
`TOTAL_GERAL` das matrículas.

### Importação de planilhas

A tela `/admin/importacoes` (menu "Importação de planilhas", só administradores) envia as
planilhas de escolas e infraestrutura, de matrículas e do quadro de professores para o ano
escolhido (`POST /importacoes/{tipo}?ano=`). Ela lista as colunas aceitas de cada tipo
(`GET /importacoes/layouts`), com os títulos alternativos, e baixa um modelo `.xlsx`
(`GET /importacoes/modelos/{tipo}`). A importação de escolas aceita um ano novo, que o
backend cria; as demais só oferecem anos cadastrados. O resultado mostra os contadores e
as linhas não importadas. A tela de professores não importa mais planilhas; a lista vazia
aponta para esta tela. Serviço e modelos ficam em `domains/importacoes`.

### Usuários

Listagem e formulário (nome, e-mail, perfil `ADMIN`/`USUARIO`, ativo). Somente
administradores criam e editam usuários.

### Configurações

Preferências gravadas apenas neste navegador, em
`localStorage['sigei-preferencias']`. Hoje só três delas têm efeito, todas no
mapa: `camadaMapa`, `abrirPopupAoPassarMouse` e `exibirFotosNoMapa`. Os campos
`nomeInstituicao`, `siglaInstituicao`, `paginaInicial`, `menuCompacto`,
`reduzirAnimacoes`, `confirmarExclusoes` e `avisarAtualizacaoAnual` são salvos,
mas nenhuma tela os lê.

## Componentes compartilhados

| Componente | Local | Uso |
| --- | --- | --- |
| `HeaderComponent` | `shared/ui/header` | Cabeçalho do mapa público |
| `ConfirmModalComponent` | `shared/ui/confirm-modal` | Confirmação de exclusão |
| `SucessoModalComponent` | `shared/ui/sucesso-modal` | Mensagem após salvar |
| `ListModalComponent` | `shared/ui/list-modal` | Lista em modal (usada na lista de povos) |
| `AnoModalComponent` | `domains/escolas/ui/ano-modal` | Cadastro rápido de ano de referência |
| `HistoricoEscolaModalComponent` | `domains/escolas/ui/historico-escola-modal` | Detalhe de um registro anual |

## Divergências conhecidas

Pontos em que o frontend e o backend não estão alinhados, verificados no
código em 29/09/2026:

| Ponto | Efeito | Local |
| --- | --- | --- |
| `DELETE /infraestrutura/{id}/foto` não existe no backend | Remover a foto de uma escola já cadastrada e salvar dá erro, depois de escola, registro e demais dados já terem sido gravados | `infraestrutura.service.ts`, `escola-form.component.ts` |
| `GET /infraestrutura` devolve os registros de todos os anos | Uma escola com mais de um ano cadastrado aparece com vários marcadores no mapa público | `mapa-publico.component.ts` |
| `MunicipioService.listar()` usa `/municipio/buscarTudo` e `buscarPorId()` usa `/municipio/{id}`, rotas que não existem | Nenhum, porque os métodos não são chamados; são código morto | `municipio.service.ts` |
| Porta do proxy (8081) diferente da porta padrão do backend (8080) | `npm start` não alcança a API sem ajuste | `proxy.conf.json` |
| `environment.production.ts` usa a URL do backend, mas o README descreve o uso de `/api` via Nginx | No Docker, o proxy `/api` do Nginx não é usado pelo build de produção | `environment.production.ts`, `README.md` |

## Execução

Veja o [README](../README.md) para desenvolvimento local, build e Docker.

```bash
npm ci
npm start          # http://localhost:4200
npm run build      # dist/sigei-frontend/browser
npm test           # Vitest (ainda sem testes)
```
