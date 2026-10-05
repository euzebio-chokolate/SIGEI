# Análise do projeto SIGEI para a monografia

Análise estática do repositório `SIGEI/` (backend, frontend, migrações, configuração, documentação, diagramas, histórico do Git e testes), feita em 05/10/2026 no ramo `docs/documentacao-sistema` (último commit `e399305`). Serve de base para a escrita do TCC. Nenhum capítulo foi redigido ainda.

> **TCC de referência.** O PDF `TCC-ALOMA-FINAL.pdf` (Downloads) foi lido por inteiro em 05/10/2026. A seção 10 compara a estrutura dele com a do SIGEI, e a seção 9 traz o sumário já ajustado a essa referência. Da referência foram aproveitados apenas estrutura, organização, extensão e forma de apresentar figuras e quadros; nenhum texto dela foi copiado.

---

## 1. Resumo do sistema

O SIGEI (Sistema de Gerenciamento Escolar Indígena) é uma aplicação web que centraliza as informações das escolas indígenas do Acre: localização, município, terra indígena, povo, aldeia, vínculo sede/anexo, equipe escolar, infraestrutura, fotografia e matrículas. Os dados de cada escola são organizados por ano de referência, de modo a manter um histórico anual.

O sistema tem duas áreas:

- **Mapa público**, sem login: escolas georreferenciadas sobre o mapa do Acre, com busca, filtros, camadas de fundo e janela com foto e dados resumidos.
- **Área administrativa**, com login: painel de indicadores, cadastros territoriais, cadastro de escolas em etapas, histórico anual, abertura de novo ano, usuários e preferências.

Há dois perfis de usuário: `ADMIN` (leitura e escrita) e `USUARIO` (somente leitura). O primeiro acesso exige troca de senha.

O repositório é um monorepo com três partes:

| Pasta | Conteúdo |
| --- | --- |
| `sigei-backend/` | API REST em Java 21 / Spring Boot 4.0.5 |
| `sigei-frontend/` | SPA em Angular 21 / TypeScript 5.9 / Tailwind CSS 4 / Leaflet 1.9 |
| `Monografia___Latex/` | Pré-projeto (`tcc.tex`), capítulo de desenvolvimento (`desenvolvimento.tex`), `refs.bib`, 20 diagramas PlantUML e imagens |

---

## 2. Funcionalidades encontradas

Situação conferida no código. "Implementado" significa que há tela no frontend e endpoint no backend, salvo indicação.

### 2.1 Área pública

| Funcionalidade | Situação | Evidência |
| --- | --- | --- |
| Mapa público das escolas (página inicial `/`) | Implementado | `features/mapa-publico/pages/mapa-publico`; consome `GET /infraestrutura` (rota pública) |
| Marcador verde (em funcionamento) ou vermelho (`PARALISADA`) | Implementado | `assets/markers/escola-marker-green.svg` / `-red.svg` |
| Quatro camadas de fundo: OpenStreetMap, Esri World Imagery, OpenTopoMap, CARTO Dark | Implementado | `mapa-publico.component.ts`, linhas 41–53 |
| Busca por nome ou código INEP, sem acentos | Implementado | `ui/mapa-busca` |
| Filtros por situação, tipo de acesso, internet e prédio (no navegador) | Implementado | `ui/mapa-filtros` |
| *Popup* com foto, nome, INEP, situação, acesso, internet e prédio | Implementado | `mapa-publico.component.ts` |

### 2.2 Autenticação e sessão

| Funcionalidade | Situação | Evidência |
| --- | --- | --- |
| Login por e-mail e senha (BCrypt) | Implementado | `/login`, `POST /auth/login`, `AuthService` |
| Tokens em cookies `HttpOnly`/`SameSite=Strict` (acesso 15 min, renovação 7 dias) | Implementado | `CookiesSessao`, `JwtService`, `RefreshTokenService` |
| Renovação automática da sessão após `401` | Implementado | `core/auth/auth.interceptor.ts`, `POST /auth/refresh` |
| Rotação de *refresh token* e revogação em caso de reuso | Implementado | `RefreshTokenService`, `RefreshTokenFluxoTest` |
| Proteção CSRF (*double submit cookie*) | Implementado | `SecurityConfig` (`csrf.spa()`) |
| Troca obrigatória de senha no primeiro acesso / alteração de senha | Implementado | `/alterar-senha`, `PUT /auth/alterar-senha`, autoridade `ROLE_TROCAR_SENHA` |
| Logout com revogação | Implementado | `POST /auth/logout` |
| Guardas de rota por perfil | Implementado | `authGuard`, `adminGuard`, `authenticatedGuard`, `fallbackGuard` |

### 2.3 Área administrativa

| Funcionalidade | Situação | Evidência |
| --- | --- | --- |
| Painel de indicadores com seleção de ano | Implementado | `/admin/dashboard`, `GET /dashboard?ano=` |
| Municípios: consulta com estatísticas | Implementado (somente leitura na interface) | `/admin/municipios`, `GET /municipio/estatisticas`; commit `da629ea` "torna municipios somente para consulta" |
| Terras indígenas: listar, cadastrar, editar, excluir, estatísticas | Implementado | `/admin/terras-indigenas`, `/terra` |
| Povos indígenas: listar, cadastrar, editar, excluir | Implementado | `/admin/povos`, `/povo` |
| Aldeias: listar, cadastrar, editar, excluir | Implementado | `/admin/aldeias`, `/aldeia` |
| Escolas: listagem com busca, paginação e exclusão | Implementado | `/admin/escolas` |
| Cadastro/edição de escola em 7 etapas, gravado em uma única transação | Implementado | `escola-form` + `ui/form-steps`; `POST /escola/salvarCompleto`, `PUT /escola/editarCompleto`, `CadastroEscolaService` |
| Distinção sede/anexo, com escolha da escola-sede | Implementado | `escola-etapa.component.html` (campo `escolaSedeId` quando `ANEXO`) |
| Equipe escolar (etapa 3) | Implementado como etapa do formulário, não como tela própria | `equipe-etapa.component` |
| Infraestrutura com coordenadas e foto (etapa 4) | Implementado como etapa do formulário | `infraestrutura-etapa.component`; Cloudinary |
| Matrículas por nível (6 níveis) e por série/etapa (14 itens) | Implementado como etapas 5 e 6 | `escola-form.config.ts` |
| Histórico por ano e por escola, com período | Implementado | `/admin/historico-escolas`; `GET /registroAnual/buscarPorAno`, `GET /registroAnual/historico` |
| Cadastro retroativo de ano anterior e edição de um ano específico | Implementado | `escola-form` com `?ano=` ou `?novoAno=true` |
| Abertura de novo ano (copia o último registro de cada escola) | Implementado | Modal na lista de escolas; `POST /atualizacao-anual/iniciar`, `AtualizacaoAnualService` |
| Usuários: listar, cadastrar, editar, excluir, ativar/desativar | Implementado | `/admin/usuarios`, `/usuario` |
| Configurações (preferências locais) | Parcial: só 3 de 10 preferências têm efeito, todas no mapa | `/admin/configuracoes`; `docs/FRONTEND.md` |
| Importação de planilhas de infraestrutura e matrículas | Só pela API, sem tela | `POST /importacoes/infraestrutura`, `POST /importacoes/matriculas` |
| Carga inicial automática (municípios, planilhas, administrador) | Implementado | `MunicipioInitializer`, `DadosIniciaisInitializer`, `AdminInitializer` |
| Documentação interativa da API (Swagger/OpenAPI) | Implementado | Springdoc; `/swagger-ui.html` |

### 2.4 Funcionalidades do pedido que não existem como tal

| Item do sumário sugerido | O que existe de fato |
| --- | --- |
| 4.1.15 Relatórios | Não há geração nem exportação de relatórios (PDF, CSV, planilha ou impressão). O que existe é o painel de indicadores. A busca por `exportar`, `csv`, `pdf`, `relatorio` e `print` no frontend não retornou ocorrências. O próprio `diagramas/README.md` classifica o RF08 do pré-projeto como "atendimento parcial: painel de indicadores, sem exportação". |
| 4.1.9 Matrículas, 4.1.10 Equipe, 4.1.11 Infraestrutura como telas | São etapas do formulário de escola. Os endpoints individuais existem, mas o frontend usa o cadastro completo. |
| 4.1.8 Registro anual como tela | O registro anual é a etapa 2 do formulário e a base da tela "Histórico escolar". |
| 4.1.13 Filtros e consultas | Existem, mas espalhados: filtros do mapa, busca textual e paginação nas listagens, consulta do histórico por ano/escola/período e seleção de ano no painel. |

---

## 3. Arquitetura identificada

### 3.1 Visão geral

```text
Navegador
  └─ SPA Angular (servida pelo Nginx em Docker; ng serve em desenvolvimento)
       │  HTTP/JSON pela mesma origem, prefixo /api
       │  cookies de sessão HttpOnly + cabeçalho X-XSRF-TOKEN
       ▼
  Nginx (proxy /api → API_UPSTREAM) | proxy.conf.json em desenvolvimento
       ▼
  API REST Spring Boot
    JwtFilter → Controller → Service → Repository (Spring Data JPA/Hibernate)
       │                         │
       ▼                         ▼
  PostgreSQL (Flyway)        Cloudinary (fotos)
```

Mapa: os *tiles* vêm diretamente do navegador aos servidores do OpenStreetMap, Esri, OpenTopoMap e CARTO.

### 3.2 Backend (`ac.gov.br.sigei_backend`)

| Pacote | Quantidade | Papel |
| --- | --- | --- |
| `controllers` | 17 classes (inclui `auth`, `dashboard`, `importacao`) | Rotas HTTP, validação com `@Valid`, códigos de status, anotações OpenAPI |
| `service` | 23 classes (inclui `auth`, `armazenamento`, `importacao`) | Regras de negócio e transações |
| `repository` | 15 interfaces + 1 projeção | Spring Data JPA, consultas JPQL e agregações do painel |
| `models` | 14 entidades + 2 enums | Mapeamento JPA |
| `dto/request`, `dto/response` | 16 + 32 classes/records | Contratos da API; entidades nunca são expostas |
| `mappers` | 14 interfaces MapStruct | Conversão entidade ↔ DTO gerada em compilação |
| `config/security` | 4 classes | `SecurityConfig`, `JwtFilter`, `CookiesSessao`, `PasswordConfig` |
| `config/initializer` | 3 classes | Municípios, carga de planilhas, administrador |
| `exceptions/handler` | `ManipuladorGlobalException` | Converte exceções em respostas HTTP |
| `utils` | 4 classes | Leitura de Excel, coordenadas, contadores de importação |

Fluxo de uma requisição: o `JwtFilter` lê o token (cookie `sigei_acesso` ou `Authorization: Bearer`), consulta o usuário no banco e define as autoridades; o `SecurityConfig` aplica as regras; o controller valida o DTO; o service resolve os relacionamentos por ID, aplica a regra de negócio e chama o repository; o mapper converte a entidade em DTO de resposta, devolvido em JSON.

Regras de autorização, na ordem em que são avaliadas: `OPTIONS` e `/actuator/health` públicos; `/auth/login|refresh|logout` públicos; Swagger público; `/auth/alterar-senha` exige autenticação; `GET /infraestrutura/**` público (alimenta o mapa); demais `GET` exigem `ADMIN` ou `USUARIO`; todas as outras requisições exigem `ADMIN`.

### 3.3 Frontend (`sigei-frontend/src/app`)

| Pasta | Papel |
| --- | --- |
| `core/auth` | `AuthService`, interceptor (renovação após `401`), guardas, modelos |
| `domains/escolas`, `domains/territorios`, `domains/usuarios` | Serviços HTTP e modelos TypeScript por domínio; modais de ano e de detalhe do histórico |
| `features/mapa-publico` | Página do mapa, busca e filtros |
| `features/auth` | Login e alteração de senha |
| `features/admin` | *Shell* com menu lateral e as telas administrativas (carregadas sob demanda) |
| `shared/ui` | Cabeçalho, modais de confirmação, sucesso e lista |

Componentes *standalone*, rotas com `loadComponent` (*lazy loading*), Tailwind CSS 4 e ícones Font Awesome 6.5.1 (CDN). Os gráficos do painel são barras em HTML/CSS, sem biblioteca de gráficos.

### 3.4 Banco de dados

PostgreSQL, com esquema versionado pelo Flyway (`V1__esquema_inicial.sql`, `V2__refresh_token.sql`) e validado pelo Hibernate (`ddl-auto=validate`). São 14 tabelas de domínio e autenticação, além de `flyway_schema_history`. Nos testes, H2 em memória.

### 3.5 Implantação

Dockerfiles para backend e frontend; `docker-compose.yml` do backend com PostgreSQL 17 (`postgres:17-alpine`) e volume persistente; `compose.yml` do frontend com Nginx. O Nginx define cabeçalhos de segurança (CSP, `X-Frame-Options`, etc.) e o proxy `/api`. Não há terminação TLS na configuração fornecida nem evidência no repositório de implantação em servidor da SEE/AC.

---

## 4. Entidades e relacionamentos

Cardinalidades conferidas nas anotações JPA e na migração `V1`.

| Entidade (tabela) | Atributos principais | Relacionamentos |
| --- | --- | --- |
| `Municipio` | `nome` | 1 município → N terras indígenas |
| `TerraIndigena` (`terra_indigena`) | `nome` | N:1 com `Municipio` (`municipio_id`, anulável) |
| `Povo` | `nome` | 1 povo → N aldeias (`mappedBy = "povo"`) |
| `Aldeia` | `nome` | N:1 com `TerraIndigena` (obrigatória) e N:1 com `Povo` (obrigatório) |
| `Escola` | `codigo` (INEP), `nome`, `tipoUnidade` (`String`) | **1:1 com `Aldeia`** (`aldeia_id` único e anulável); N:1 autorrelacionamento `escolaSede`; 1 escola → N registros anuais (cascata e remoção de órfãos) |
| `Anexo` | — | N:1 com `Escola` (`escola`) e N:1 com `Escola` (`escolaAnexo`); par único |
| `AnoReferencia` (`ano_referencia`) | `ano` (único, obrigatório) | 1 ano → N registros anuais |
| `RegistroAnual` (`registro_anual`) | `rede`, `localFuncionamento`, `totalAlunos` | N:1 com `Escola` e com `AnoReferencia`; par `(escola_id, ano_referencia_id)` único; 1:1 com equipe e infraestrutura; 1:N com matrículas por nível e por série (todas com cascata e remoção de órfãos) |
| `EquipeEscola` (`equipe_escola`) | 6 booleanos: professor responsável, equipe gestora, merendeira, zeladora, barqueiro, necessita transporte | 1:1 com `RegistroAnual` (`registro_anual_id` único); N:1 com `AnoReferencia` |
| `InfraestruturaEscola` | ano de construção, nº de salas, latitude, longitude, situação, tipo de acesso, prédio, água, internet, energia, registro fotográfico, `fotoUrl`, `fotoPublicId` | 1:1 com `RegistroAnual` (`registro_anual_id` único) |
| `MatriculaNivel` | `nivel`, `qtdMasculino`, `qtdFeminino`, `total` | N:1 com `RegistroAnual` |
| `MatriculaSerie` | `denominacao`, `etapaEnsino`, `qtdMasculino`, `qtdFeminino`, `total` | N:1 com `RegistroAnual` |
| `Usuario` | `nome`, `email` (único), `senha` (BCrypt), `perfil` (`ADMIN`/`USUARIO`), `ativo`, `alterarSenha` | 1 usuário → N refresh tokens |
| `RefreshToken` (`refresh_token`) | `tokenHash` (SHA-256, único), `criadoEm`, `expiraEm`, `revogado`, `revogadoEm` | N:1 com `Usuario` (exclusão em cascata no banco) |

Cadeia territorial: `Municipio 1—N TerraIndigena 1—N Aldeia N—1 Povo`, e `Aldeia 1—0..1 Escola`.
Cadeia anual: `Escola 1—N RegistroAnual N—1 AnoReferencia`, e `RegistroAnual 1—1 EquipeEscola`, `1—1 InfraestruturaEscola`, `1—N MatriculaNivel`, `1—N MatriculaSerie`.

Pontos que precisam aparecer corretamente no texto:

1. **Escola–Aldeia é 1:1 no esquema.** Cada aldeia pode ter no máximo uma escola. O diagrama ER antigo do pré-projeto (`imagens/SIGEI - ER2-...png`) e o texto da seção "Diagrama Entidade-Relacionamento" do `tcc.tex` descrevem outra estrutura (Escola ligada a Município e uma entidade `Matricula`), que não corresponde ao código.
2. **Sede/anexo tem duas representações**: `Escola.escolaSede` (usada pelo formulário) e a tabela `anexo` (com endpoint próprio, mas não usada pelo frontend). Elas não são sincronizadas.
3. **`Escola` não tem coordenadas.** Latitude e longitude ficam na infraestrutura de cada registro anual.
4. **`tipoUnidade` é `String`**, embora exista o enum `TipoUnidade`.
5. `TerraIndigena.municipio` é anulável; uma terra indígena só pode ser associada a **um** município no modelo.

---

## 5. Tecnologias

| Camada | Tecnologia (versão declarada) | Papel no SIGEI |
| --- | --- | --- |
| Backend | Java 21 | Linguagem da API |
| | Spring Boot 4.0.5 (Spring MVC) | Estrutura da API, injeção de dependências, configuração |
| | Spring Data JPA + Hibernate | Persistência e consultas; Hibernate valida o esquema |
| | Bean Validation | Validação dos DTOs de entrada |
| | Spring Security + JJWT 0.12.6 | Autenticação *stateless*, JWT em cookies, CSRF, autorização por perfil |
| | MapStruct 1.6.3 | Conversão entre entidades e DTOs |
| | Apache POI 5.4.1 | Leitura das planilhas Excel do censo e da infraestrutura |
| | Cloudinary 2.0.0 | Armazenamento das fotografias das escolas |
| | Springdoc OpenAPI 2.8.13 | Documentação interativa da API |
| | Flyway (gerido pelo Spring Boot) | Versionamento do esquema |
| | Spring Boot Actuator | *Health check* |
| Banco | PostgreSQL (17 no Compose) | Banco relacional da aplicação |
| | H2 | Banco em memória nos testes |
| Frontend | Angular 21.2 (*standalone*, sem Zone.js segundo o README) | SPA |
| | TypeScript 5.9 | Tipagem estática |
| | Tailwind CSS 4.3 | Estilização |
| | Leaflet 1.9.4 | Mapa interativo e marcadores |
| | RxJS 7.8 | Chamadas HTTP assíncronas |
| | Font Awesome 6.5.1 (CDN) | Ícones |
| Testes | JUnit Jupiter, Mockito 5.11, `@DataJpaTest`, MockMvc, Testcontainers | Testes automatizados do backend |
| | Bruno | Coleção de requisições manuais da API |
| | Vitest 4 | Configurado no frontend, sem testes escritos |
| Implantação | Docker, Docker Compose, Nginx | Empacotamento e execução |
| Apoio | Maven Wrapper, Git/GitHub | Build e versionamento |

Correções em relação ao pré-projeto (`tcc.tex`): Java 21 (não 17), Angular 21 (não 16), Leaflet usado diretamente (não `ngx-leaflet`), esquema criado pelo Flyway (não por `ddl-auto`). O `desenvolvimento.tex` já registra a maior parte dessas mudanças.

---

## 6. Evidências encontradas no código

### 6.1 Endpoints (72 operações em 17 controllers)

| Recurso | Operações |
| --- | --- |
| `/escola` | CRUD, `GET /{id}`, `POST /salvarCompleto`, `PUT /editarCompleto` |
| `/registroAnual` | CRUD, `GET /buscarPorAno`, `GET /buscarPorEscolaEAno`, `GET /historico` |
| `/infraestrutura` | CRUD, `GET /buscarPorAno`, `POST /{id}/foto` |
| `/aldeia`, `/povo`, `/usuario` | CRUD e `GET /{id}` |
| `/terra` | CRUD, `GET /{id}`, `GET /estatisticas` |
| `/municipio` | CRUD, `GET /estatisticas` |
| `/anexo`, `/anos`, `/matriculaNivel`, `/matriculaSerie` | CRUD |
| `/equipe` | `POST /salvar`, `PUT /editar` |
| `/auth` | `POST /login`, `POST /refresh`, `POST /logout`, `PUT /alterar-senha` |
| `/dashboard` | `GET` (com `ano` opcional) |
| `/atualizacao-anual` | `POST /iniciar` |
| `/importacoes` | `POST /infraestrutura`, `POST /matriculas` |

O endpoint `GET /registroAnual/historico` está implementado e documentado no Swagger, mas falta na tabela de contratos do `sigei-backend/CLAUDE.md`.

### 6.2 Regras de negócio relevantes

- **Cadastro completo** (`CadastroEscolaService`): uma transação grava escola, registro do ano, equipe, infraestrutura, matrículas e foto. Matrículas são casadas por `nivel` e por `denominacao` + `etapaEnsino`; chaves repetidas geram `400`. O *upload* ocorre após o *flush*; se a transação for desfeita, a imagem é removida por compensação.
- **Abertura de ano** (`AtualizacaoAnualService.iniciarNovoAno`): cria o maior ano + 1 (recusa se já existir) e, para cada escola com histórico, copia rede, local de funcionamento, equipe, infraestrutura (inclusive URL da foto) e matrículas. Não copia `totalAlunos` nem `fotoPublicId`. Escolas sem registro são ignoradas.
- **Painel** (`DashboardService`): ano solicitado ou o maior cadastrado. Retorna resumo (escolas, matrículas, municípios, terras), evolução de matrículas e escolas por ano, matrículas por nível, percentuais de internet, energia e prédio, escolas por município e situação das escolas.
- **Importação** (`ImportacaoExcelService`, `ImportacaoMatriculasService`): abas `Planilha4` e `Censo_2025`, colunas fixas e ano fixo **2025**. Retornam contadores e lista de erros.
- **Carga inicial**: 22 municípios do Acre (`MunicipioInitializer`), as duas planilhas da pasta `planilhas/` quando não há registro anual e o administrador inicial com troca de senha obrigatória.

### 6.3 Testes automatizados

- **Backend:** 32 classes de teste em `src/test/java`, além do arquivo de *fixtures*:
  - 11 testes de serviço com Mockito (`AldeiaServiceTest`, `RegistroAnualServiceTest` etc.), com 99 casos;
  - 14 testes de repositório com `@DataJpaTest` e H2, com *fixtures* em `RepositoryTestFixtures`;
  - 4 testes de integração com H2: `CadastroEscolaServiceTest` (transação e foto), `RefreshTokenFluxoTest` (sessões), `RegistroAnualHistoricoTest` e `AuthSessaoHttpTest` (HTTP: cookies, CSRF, perfis), com 39 casos;
  - `MunicipioInitializerTest`, `SigeiBackendApplicationTests` (contexto) e `FlywayMigracoesTest` (PostgreSQL real via Testcontainers; ignorado sem Docker).
- **Resultado da execução em 05/10/2026:** ver seção 6.5.
- **Frontend:** Vitest configurado, **nenhum arquivo `.spec.ts`**. Não há testes de interface nem *end-to-end*.
- **Testes manuais:** coleção Bruno em `sigei-backend/tests - bruno/Sample API Collection/` com 51 requisições `.yml` para os recursos principais, login e importações. A coleção tem senha de exemplo gravada no arquivo de login e é anterior à mudança para cookies + CSRF; precisa ser revista antes de servir de evidência.
- Não há relatórios de cobertura (JaCoCo) nem integração contínua (nenhum *workflow* de CI no repositório).

### 6.4 Histórico de desenvolvimento (Git)

137 commits, todos do mesmo autor, entre 20/04/2026 e 02/10/2026:

| Mês | Commits | Foco observado nas mensagens |
| --- | --- | --- |
| Abr/2026 | 7 | Commit inicial, primeiros DTOs e validação, início do frontend |
| Mai/2026 | 4 | "Começa o backend verdadeiro", DTOs, MapStruct |
| Jun/2026 | 11 | CRUD de terra, escola e aldeia, com testes |
| Jul/2026 | 29 | Uma entidade por vez, cada uma com testes: Povo, Equipe, Infraestrutura, RegistroAnual, Matrículas, Anexo; depois controllers |
| Ago/2026 | 58 | Frontend, Cloudinary, histórico anual, atualização anual, autenticação JWT, usuários, mapa público, painel, carga inicial, preparação para hospedagem |
| Set/2026 | 25 | Ajustes para rede local, Swagger, ciclos da EJA, histórico escolar, documentação, cadastro transacional e cookies |
| Out/2026 | 3 | Flyway, diagramas PlantUML |

O histórico mostra desenvolvimento em pequenos incrementos: o backend foi construído entidade por entidade, cada uma acompanhada de testes, e o frontend e a segurança vieram depois. **Não há evidência** de Scrum, Kanban, *sprints*, quadro de tarefas, *issues* ou *pull requests* como processo. O pré-projeto declara "metodologia incremental"; o histórico é compatível com isso, mas não comprova uma metodologia formal. O texto deve descrever as etapas que o histórico mostra, sem atribuir rituais que não aparecem.

### 6.5 Execução dos testes

Executado em 05/10/2026 nesta análise, com `.\mvnw clean test -Dmaven.compiler.proc=full` (JDK 24.0.1 da máquina, sem Docker disponível). Resultado lido dos relatórios do Surefire (`target/surefire-reports`):

| Executados | Falhas | Erros | Ignorados |
| --- | --- | --- | --- |
| 217 | 0 | 0 | 3 |

Os 3 ignorados são os de `FlywayMigracoesTest`, que dependem do Docker (Testcontainers); portanto, **a aplicação das migrações em PostgreSQL real não foi verificada nesta execução**. Os demais testes rodam sobre H2, que não garante compatibilidade de todas as consultas com o PostgreSQL. Para usar esse resultado no TCC, repita a execução com o JDK 21 do projeto e, se possível, com Docker ativo, e capture a saída como evidência.

---

## 7. Funcionalidades aparentemente incompletas ou com divergências

| # | Ponto | Efeito / observação | Local |
| --- | --- | --- | --- |
| 1 | Sem módulo de relatórios | Não há exportação ou impressão; apenas o painel | Frontend inteiro |
| 2 | Mapa usa todos os anos | `GET /infraestrutura` não filtra o ano; uma escola com vários anos aparece com vários marcadores | `mapa-publico.component.ts` |
| 3 | Importação sem tela e presa a 2025 | Só via API, com aba, colunas e ano fixos | `service/importacao` |
| 4 | Configurações parciais | 7 de 10 preferências são salvas mas não lidas por nenhuma tela | `configuracoes.component.ts` |
| 5 | `excluirFoto()` chama `DELETE /infraestrutura/{id}/foto`, inexistente | Código morto: o fluxo atual usa `removerFoto` no cadastro completo | `infraestrutura.service.ts` |
| 6 | `MunicipioService.listar()` usa `/municipio/buscarTudo`, inexistente | Código morto | `municipio.service.ts` |
| 7 | Sede/anexo duplicado | `escolaSede` e tabela `anexo` não sincronizados | `Escola`, `Anexo` |
| 8 | Abertura de ano incompleta em alguns campos | Não copia `totalAlunos` nem `fotoPublicId`; equipe sem `anoReferencia` | `AtualizacaoAnualService` |
| 9 | Erros de login | Senha incorreta ou usuário inativo lançam `RuntimeException`, sem tratamento específico, e podem resultar em `500` | `AuthService`, `ManipuladorGlobalException` |
| 10 | Proxy de desenvolvimento na porta 8081 e backend em 8080 | Exige ajuste para rodar localmente | `proxy.conf.json` |
| 11 | Frontend sem testes | Vitest configurado, sem specs | `sigei-frontend` |
| 12 | Histórico sem auditoria | Guarda um registro por ano; edições sobrescrevem o registro do ano | `RegistroAnual` |

**Documentação desatualizada no repositório** (corrigir antes de citar):

- `README.md` da raiz, seção "Limitações conhecidas", diz que não há renovação de token (sessão de 24 h) e que o cadastro não é transacional. Os dois pontos foram resolvidos no commit `0afef9a`.
- `sigei-frontend/docs/FRONTEND.md`, seção "Escolas: cadastro em etapas", descreve o encadeamento de várias requisições com `forkJoin`. O formulário atual usa `salvarCompleto`/`editarCompleto`. A tabela de divergências também cita `environment.production.ts` com URL direta, mas o arquivo atual usa `/api`.
- `Monografia___Latex/ALTERACOES_MONOGRAFIA.md` lista os trechos do `desenvolvimento.tex` ainda desatualizados (sessão de 24 h, cadastro em várias requisições, `localStorage`, `ddl-auto`).
- O pré-projeto (`tcc.tex`) descreve versões, a estrutura de dados e um "Consulente" diferentes da implementação atual.

---

## 8. Informações que não consegui confirmar

Estes pontos aparecem no pré-projeto ou no pedido, mas o repositório não os comprova. Não devem entrar no TCC como fato sem fonte ou confirmação sua.

1. **Nome do curso — resolvido em 05/10/2026.** O autor confirmou: Curso Superior de Tecnologia em **Sistemas para Internet**. O `tcc.tex` (`\programa`, `\natureza`, justificativa) e o `README.md` ainda dizem "Análise e Desenvolvimento de Sistemas" e precisam ser corrigidos.
1a. **Titulação do orientador.** O `tcc.tex` usa "Prof. Me."; o TCC de referência (2025) usa "Professor Dr". Confirme a titulação atual do Prof. Marlon Amaro Coelho Teixeira.
2. **Nome da secretaria.** O `tcc.tex` usa "Secretaria de Estado de Educação, Cultura e Esportes (SEE/AC)"; o pedido usa "Secretaria de Estado de Educação do Acre". Confirmar a denominação oficial vigente.
3. **"149 escolas indígenas e 19 anexos"** (introdução do `tcc.tex`): não há fonte citada. Os dados podem estar nas planilhas, mas a contagem não foi feita nesta análise e precisaria de fonte oficial.
4. **Reuniões com a SEE/AC.** O pré-projeto afirma que os requisitos vieram de "reuniões com representantes da SEE/AC" e "observação dos processos". Não há atas, registros ou outras evidências no repositório. Confirme se as reuniões ocorreram, quando e com quem (sem nomes, se preferir).
5. **Origem e autorização das planilhas** (`Censo_Escolas_Indígenas_2025_...xlsx` e `Quadro_Geral_Construção_Escolas_2025_...xlsx`). Quem forneceu e se o uso dos dados no TCC foi autorizado.
6. **Validação com usuários ou com a demandante.** Não há evidência de testes de aceitação, questionários ou avaliação de usabilidade.
7. **Implantação em produção.** Há preparação para hospedagem (commits `a197182`, `cc7c33f`), mas nada indica se o sistema está em uso pela SEE/AC.
8. **Afirmações sobre FUNAI, número de etnias e acesso fluvial** na introdução do `tcc.tex`: citam `brasil1988` (a Constituição), que não sustenta esses dados. Precisam de fonte adequada.
9. **Requisitos não funcionais com metas** (ex.: "mapa em até cinco segundos", RNF04): não há medição no repositório.
10. **Referências do `refs.bib`.** Estão no formato correto, mas não verifiquei edição, ano e editora de cada obra. Alguns pontos merecem conferência: `postgresql2023` cita "PostgreSQL 18 documentation" com ano 2023; `secretariaacre2022` cita o "Plano Estadual de Educação do Acre 2015–2025" com ano 2022; `paladino2012`, com local e evento a confirmar.
11. **Uso sem Zone.js.** O README afirma, mas não conferi a configuração (`app.config.ts`).

---

## 9. Proposta de sumário para o TCC

A numeração dos capítulos segue o TCC de referência: 1 Introdução, 2 Referencial teórico, 3 Metodologia (3.1 Processo, 3.2 Modelagem, 3.3 Tecnologias, 3.4 Validação), 4 Sistema (4.1 Funcionalidades, 4.2 Arquitetura, 4.3 Testes) e 5 Conclusões e trabalhos futuros. Dentro dessa estrutura, as seções foram adaptadas ao que existe no SIGEI. Itens entre colchetes são observações para você, não títulos.

**Elementos pré-textuais**
Capa · Folha de rosto · Dedicatória [opcional, placeholder] · Agradecimentos [placeholder] · Epígrafe [opcional] · Resumo e palavras-chave · *Abstract* e *keywords* · Lista de figuras · Lista de quadros · Lista de abreviaturas e siglas · Sumário
[A referência não traz folha de aprovação nem ficha catalográfica. Confirme no modelo do IFAC (`abntspiifac.cls`) se elas são exigidas na versão final.]

**1 INTRODUÇÃO**
Texto corrido: contextualização da educação escolar indígena no Acre, dispersão das informações, papel da SEE/AC, motivação e problema de pesquisa.
1.1 Objetivos
 1.1.1 Objetivo geral
 1.1.2 Objetivos específicos
1.2 Justificativa
[A organização dos capítulos fica no último parágrafo, como na referência. A referência não tem subseções na introdução; as duas propostas aqui apenas destacam os itens que você pediu.]

**2 REFERENCIAL TEÓRICO**
Parágrafo de abertura do capítulo.
2.1 Educação escolar indígena no Brasil: marco legal
2.2 Educação escolar indígena no Acre
2.3 Gestão da informação educacional e o Censo Escolar
2.4 Sistemas de informação na gestão pública educacional
2.5 Georreferenciamento e mapas na web
2.6 Aplicações web e APIs REST
2.7 Trabalhos relacionados [a pesquisar; nenhum consta no repositório]

**3 METODOLOGIA**
Parágrafo de abertura do capítulo.
3.1 Processo de Desenvolvimento
 [Modelo incremental, sustentado pelo Quadro de incrementos derivado do Git (seção 10.3). Opcionalmente, um parágrafo inicial classificando a pesquisa como aplicada, se o orientador pedir.]
3.2 Levantamento e Modelagem do Sistema
 3.2.1 Contexto da demanda e levantamento de requisitos
 3.2.2 Requisitos funcionais (Quadro)
 3.2.3 Requisitos não funcionais (Quadro) [a referência não tem; incluir só os verificáveis]
 3.2.4 Atores e casos de uso (Figura)
 3.2.5 Diagrama de classes (Figura)
 3.2.6 Modelo do banco de dados (Figura ER)
3.3 Tecnologias
 3.3.1 *Back-end*: Java, Spring Boot, Spring Data JPA e Hibernate
 3.3.2 Segurança: Spring Security e JWT
 3.3.3 Banco de dados: PostgreSQL e Flyway
 3.3.4 *Front-end*: Angular, TypeScript e Tailwind CSS
 3.3.5 Mapas: Leaflet
 3.3.6 Ferramentas de apoio: Cloudinary, Docker, Git, Swagger/OpenAPI, JUnit/Mockito e Bruno
3.4 Validação
 [Quadro "Processo de validação", no formato da referência, com: testes automatizados (JUnit/Mockito/H2), testes manuais de API (Bruno/Swagger), verificação das funcionalidades na interface e conferência dos dados importados. Validação com a SEE/AC só se for confirmada.]

**4 SIGEI – SISTEMA DE GERENCIAMENTO ESCOLAR INDÍGENA**
Parágrafo de apresentação: finalidade, público-alvo e as duas áreas (pública e administrativa).
4.1 Funcionalidades
 4.1.1 Mapa público interativo
 4.1.2 Autenticação e troca obrigatória de senha
 4.1.3 Tela inicial: painel de indicadores
 4.1.4 Municípios
 4.1.5 Terras indígenas
 4.1.6 Povos indígenas
 4.1.7 Aldeias
 4.1.8 Gerenciamento de escolas
 4.1.9 Cadastro de escolas em etapas
  [Dentro desta seção, em sequência e com uma figura por etapa: dados da escola e vínculo sede/anexo; registro anual; equipe escolar; infraestrutura e fotografia; matrículas por nível e por série; revisão. A referência descreve o formulário de inventário da mesma forma, como uma sequência de etapas dentro de uma única subseção.]
 4.1.10 Histórico escolar
 4.1.11 Abertura de novo ano letivo
 4.1.12 Gerenciamento de usuários
 4.1.13 Configurações
 4.1.14 Carga inicial e importação de planilhas [sem tela; descrever pela API]
4.2 Arquitetura do Sistema
 4.2.1 *Front-end*
 4.2.2 *Back-end*
 4.2.3 Banco de dados [descrição conceitual das entidades e relações]
 4.2.4 Comunicação entre *front-end* e *back-end*
 4.2.5 Segurança e controle de acesso
 4.2.6 Implantação
4.3 Testes
 4.3.1 Testes da API [automatizados (JUnit) e manuais (Bruno/Swagger)]
 4.3.2 Testes das funcionalidades
 4.3.3 Validação do sistema

**5 CONCLUSÕES E TRABALHOS FUTUROS**
[Texto corrido, como na referência, cobrindo conclusões, limitações e trabalhos futuros. Pode ser dividido em 5.1/5.2 se o orientador preferir.]

**REFERÊNCIAS**
**APÊNDICES** [opcional: diagramas de sequência e atividade, tabela de *endpoints*]

### 9.1 Mudanças em relação à estrutura do pedido

- **Relatórios (4.1.15) removido** como seção própria: não existe no sistema. O painel é tratado em 4.1.3, e relatórios exportáveis vão para trabalhos futuros. A referência tem relatórios em PDF, mas isso não deve ser imitado no SIGEI.
- **Matrículas, equipe, infraestrutura e registro anual** ficam dentro do cadastro em etapas (4.1.9), que é como aparecem no sistema.
- **Filtros e consultas (4.1.13)** foram distribuídos: filtros do mapa em 4.1.1, busca e paginação nas listagens e consultas do histórico em 4.1.10.
- **Modelagem (diagrama de classes e ER)** saiu do capítulo 4 e foi para 3.2, como na referência. A descrição conceitual das entidades fica em 4.2.3, atendendo ao item 7 do pedido sem repetir figuras.
- **"Atendimento aos requisitos"** saiu como seção própria; a relação entre RFs e funcionalidades pode ser citada na conclusão.
- **Acrescentados:** histórico escolar, abertura de novo ano, configurações, carga inicial/importação, segurança e implantação.
- **Mapa** na primeira posição, por ser a página inicial e a única área pública.

### 9.2 Requisitos funcionais propostos (rascunho para o Quadro da seção 3.2.2)

Baseados apenas no que está implementado. Substituem a lista do pré-projeto, que incluía "gerar relatórios" e "gerenciar municípios" com escrita na interface.

| Código | Descrição |
| --- | --- |
| RF01 | Autenticar usuário por e-mail e senha. |
| RF02 | Manter e renovar a sessão do usuário e permitir o encerramento da sessão. |
| RF03 | Exigir a troca da senha no primeiro acesso e permitir a alteração da própria senha. |
| RF04 | Gerenciar usuários, com perfis de administrador e de consulta e indicação de usuário ativo. |
| RF05 | Consultar os municípios do Acre com estatísticas associadas. |
| RF06 | Gerenciar terras indígenas, associadas a um município. |
| RF07 | Gerenciar povos indígenas. |
| RF08 | Gerenciar aldeias, associadas a uma terra indígena e a um povo. |
| RF09 | Cadastrar escola com código INEP, nome, tipo de unidade (sede ou anexo), aldeia e escola-sede. |
| RF10 | Registrar, para cada escola e ano, rede, local de funcionamento e total de alunos. |
| RF11 | Registrar a equipe escolar do ano. |
| RF12 | Registrar a infraestrutura do ano, incluindo coordenadas geográficas e fotografia. |
| RF13 | Registrar matrículas por nível e por série/etapa, separadas por sexo. |
| RF14 | Editar os dados de uma escola e de um ano específico, inclusive anos anteriores. |
| RF15 | Excluir escolas e demais cadastros, mediante confirmação. |
| RF16 | Consultar o histórico das escolas por ano e por escola, com período opcional. |
| RF17 | Abrir um novo ano letivo, copiando o último registro de cada escola. |
| RF18 | Exibir publicamente as escolas em mapa interativo, com situação indicada por cor. |
| RF19 | Buscar escolas no mapa por nome ou código INEP e filtrá-las por situação, acesso, internet e prédio. |
| RF20 | Exibir painel de indicadores por ano de referência. |
| RF21 | Importar planilhas de infraestrutura e de matrículas (por meio da API). |
| RF22 | Realizar a carga inicial de municípios, dados das planilhas e administrador. |
| RF23 | Guardar preferências de exibição do mapa no navegador. |

Atores: **Visitante** (mapa público), **Usuário** (`USUARIO`, consulta), **Administrador** (`ADMIN`, consulta e escrita). O "Consulente" e o "Gestor" do pré-projeto correspondem a Usuário e Administrador; convém unificar os nomes.

### 9.3 Figuras disponíveis e a produzir

| Material | Situação |
| --- | --- |
| 20 diagramas PlantUML em `diagramas/` (contexto, casos de uso, containers, componentes, classes, ER, sequências, atividades, implantação) | Prontos em código; falta exportar para imagem |
| `imagens/tela-mapa.png`, `lista-escolas.png`, `cadastro-aldeias.png`, `cadastro-terras.png` | Capturas existentes; verificar se correspondem à interface atual |
| `imagens/diagrama-casos-de-uso.drawio.png`, `diagrama-arquitetura.png`, `SIGEI - ER2-...png` | Do pré-projeto; o ER está desatualizado |
| Capturas a produzir | Login, troca de senha, painel, municípios, povos, cada etapa do formulário de escola, histórico (por ano e por escola), modal de detalhe, abertura de ano, usuários, configurações, Swagger, execução dos testes, requisições no Bruno |

---

## 10. Comparação com o TCC de referência

Referência: LIMA, M. A. S. *Safest: uma ferramenta para o cálculo de excesso de risco conforme a NR-3*. TCC (Tecnologia em Sistemas para Internet), IFAC, Campus Rio Branco, 2025. Orientador: Marlon Amaro Coelho Teixeira.

### 10.1 Estrutura e extensão da referência

| Aspecto | Referência |
| --- | --- |
| Total | 44 páginas, cerca de 8.600 palavras |
| Pré-textuais | Capa, folha de rosto, dedicatória, agradecimentos, epígrafe, resumo (1 parágrafo, 5 palavras-chave), *abstract*, listas de figuras, quadros e siglas, sumário |
| 1 Introdução | 2 páginas, texto corrido, sem subseções; objetivo geral no meio do texto, sem objetivos específicos; organização no último parágrafo |
| 2 Referencial teórico | Cerca de 7 páginas, 6 seções; a última é "Trabalhos relacionados" |
| 3 Metodologia | 3.1 Processo de desenvolvimento (incremental, com Quadro de incrementos); 3.2 Modelagem (Quadro de RF, casos de uso, diagrama de classes); 3.3 Tecnologias (lista curta); 3.4 Validação (Quadro do processo de validação e critérios) |
| 4 Sistema | 4.1 Funcionalidades (7 subseções, cada uma com 1 a 4 capturas de tela); 4.2 Arquitetura (*front-end*, *back-end*, comunicação); 4.3 Testes (testes de API no Postman, validação pela demandante) |
| 5 Conclusões | Uma página, texto corrido, com trabalhos futuros |
| Elementos gráficos | 29 figuras (cerca de 20 capturas de tela, 2 diagramas UML, 5 capturas do Postman) e 5 quadros |
| Referências | 10 |
| Legendas | "Figura N – Título" acima da imagem e "Fonte: A autora" abaixo |

### 10.2 O que muda no SIGEI em relação à referência

| Ponto | Referência | SIGEI |
| --- | --- | --- |
| Tamanho do sistema | Uma funcionalidade central (inventário e relatório) | Cerca de 14 funcionalidades, área pública e área administrativa |
| Extensão estimada | 31 páginas textuais | 40 a 50 páginas textuais. O capítulo 4 cresce com as telas; os demais podem ficar perto da referência. |
| Figuras | 29 | Cerca de 35 a 45. É possível limitar a uma captura por funcionalidade e levar diagramas extras para os apêndices. |
| Quadros | 5 | Cerca de 7: incrementos, RF, RNF, tecnologias, validação, perfis de acesso, *endpoints* (este no apêndice) |
| Objetivos específicos | Não tem | Ter, porque o pedido exige e a conclusão deve retomá-los |
| Tecnologias | Lista de 4 itens | Explicação do papel de cada tecnologia, conforme o pedido |
| Testes | Postman, chamados de "testes unitários" | Testes unitários reais (JUnit/Mockito), testes de repositório, testes HTTP e testes manuais com Bruno. Cada tipo deve ser chamado pelo nome correto. |
| Validação | Feita pela demandante (PO) | Não há evidência. Descrever apenas o que ocorreu ou apresentar como etapa prevista. |
| Demandante | Professora identificada, com reuniões descritas | SEE/AC; as reuniões precisam ser confirmadas (seção 8, item 4) |
| Legenda | "Fonte: A autora" | "Fonte: Elaborado pelo autor" (conforme o pedido) |

### 10.3 Quadro de incrementos proposto (equivalente ao Quadro 3 da referência)

Derivado das datas e mensagens dos commits. Os períodos são aproximados e se sobrepõem.

| Incremento | Período | Conteúdo (conforme os commits) |
| --- | --- | --- |
| 1 | 20/04 a 21/07/2026 | Estrutura do *back-end*: entidades, DTOs, validação, MapStruct e CRUD de cada entidade (terra, escola, aldeia, município, povo, equipe, infraestrutura, registro anual, matrículas, anexo), cada uma com testes automatizados |
| 2 | 24/07 a 03/08/2026 | Testes manuais de *endpoints* com Bruno; início do *front-end* (páginas e formulários); ano de referência passa a ser tabela |
| 3 | 05/08 a 18/08/2026 | Consultas por ano; importação das planilhas de infraestrutura e de matrículas |
| 4 | 19/08 a 26/08/2026 | Integração do *front-end* com a API; Swagger; formulário de escolas com fotos no Cloudinary; histórico e abertura de novo ano na interface |
| 5 | 27/08 a 02/09/2026 | Autenticação JWT, usuários e perfis; mapa público; configurações; troca obrigatória de senha; painel de indicadores; carga inicial; preparação para hospedagem |
| 6 | 14/09 a 02/10/2026 | Atualização dos testes; documentação da API; ciclos da EJA; histórico por escola; cadastro em transação única; sessão em *cookies* com renovação e CSRF; Flyway; documentação e diagramas |

O histórico não registra validação de cada incremento com a demandante. A referência afirma isso para o Safest; no SIGEI, essa afirmação só pode entrar se você confirmar.

---

## Próximo passo

**Atualização de 05/10/2026:** o sumário da seção 9 foi aprovado e o curso, confirmado. Os capítulos estão em `tcc-md/`. O autor informou que fará correções no sistema depois; os trechos que dependem dessas correções estão listados em `tcc-md/README.md`.

Itens ainda pendentes:

1. aprovação (ou ajustes) do sumário da seção 9;
2. as respostas aos itens 1 a 7 da seção 8, sobretudo: curso, titulação do orientador, reuniões com a SEE/AC, origem das planilhas e se houve alguma validação com a secretaria.
