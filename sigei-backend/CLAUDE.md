# Contexto do projeto — SIGEI Backend

Este arquivo orienta o trabalho nesta API. Descreve a implementação existente; ao alterar contratos ou regras, confira o código e atualize este contexto. Responda e escreva documentação em português do Brasil.

## Objetivo e tecnologias

API de gestão da educação escolar indígena do Acre: municípios, terras indígenas, povos, aldeias, escolas, registros anuais, infraestrutura, equipes, matrículas, indicadores e usuários.

- Java 21 e Maven Wrapper (`mvnw` / `mvnw.cmd`).
- Spring Boot 4.0.5, Spring MVC, Spring Data JPA, Bean Validation, Spring Security e Actuator.
- PostgreSQL na aplicação; H2 nos testes com perfil `test`.
- MapStruct 1.6.3 para conversões; Apache POI 5.4.1 para Excel.
- JWT com JJWT 0.12.6; senhas com BCrypt.
- Cloudinary 2.0.0 para imagens; Springdoc 2.8.13 declarado no `pom.xml` para OpenAPI/Swagger.
- Flyway 11 (gerenciado pelo Spring Boot) para versionar o esquema; Testcontainers 2 nos testes de migração.
- Hibernate Envers (versão do Hibernate gerenciada pelo Spring Boot) para a trilha de auditoria.
- JUnit Jupiter e Mockito nos testes.

As versões acima são as declaradas no repositório, não uma afirmação de compatibilidade validada entre todas as dependências.

## Organização e responsabilidades

Pacote principal: `src/main/java/ac/gov/br/sigei_backend`. Entrada: `SigeiBackendApplication`.

| Pacote | Responsabilidade |
| --- | --- |
| `controllers` | Rotas HTTP, entrada, validação e respostas; subpacotes `auth`, `dashboard`, `importacao`. |
| `service` | Regras de negócio e transações; subpacotes `auth`, `armazenamento`, `importacao`. |
| `repository` | Persistência JPA, consultas e agregações; projeções em `projection`. |
| `models` | Entidades JPA; enumerações em `enums`. |
| `dto/request` | Contratos de entrada, incluindo IDs dos relacionamentos. |
| `dto/response` | Contratos de saída; especializações em `resumo`, `detalhe`, `dashboard` e `historico`. |
| `mappers` | Interfaces MapStruct, com componentes Spring e atualizações via `@MappingTarget`. |
| `config/security` | Autorização HTTP, filtro JWT e codificação de senhas. |
| `config/initializer` | Administrador, municípios e carga inicial de planilhas. |
| `config/cloudinary` | Cliente de armazenamento de imagens. |
| `config/auditoria` | `RevisaoListener`, que grava o autor das revisões do Envers. |
| `exceptions/handler` | Tratamento global de erros HTTP. |
| `utils` | Leitura/normalização de Excel, coordenadas e contadores de importação. |

Fluxo usual: controller → service → repository; mappers convertem entidades e DTOs. Relacionamentos recebidos por ID são resolvidos nos services. Há injeção por campo e por construtor no código atual; mantenha coerência com a classe alterada.

## Domínio e relacionamentos

Consulte [docs/BANCO_DE_DADOS.md](docs/BANCO_DE_DADOS.md) para o diagrama e o dicionário completo das tabelas. Atualize essa documentação ao alterar os mapeamentos JPA.

- `Municipio` contém o cadastro territorial; `TerraIndigena` referencia um município.
- `Aldeia` referencia uma terra indígena e um `Povo`; `Escola` referencia uma aldeia.
- `Escola` possui código, nome, tipo de unidade e referência opcional `escolaSede`. O campo da entidade é `String`, mas `VinculoSedeService` aceita apenas os valores do enum `TipoUnidade` (`SEDE`, `ANEXO`), sem diferenciar maiúsculas, e grava em maiúsculas.
- `Escola.escolaSede` é a única representação do vínculo entre anexo e escola-sede. A antiga entidade `Anexo` e a rota `/anexo` foram removidas, e a tabela foi apagada pela migração V3. `VinculoSedeService`, usado por `EscolaService` e `CadastroEscolaService`, aplica as regras com `400`: um `ANEXO` exige uma sede do tipo `SEDE`, diferente de si mesmo; uma `SEDE` não tem sede; e uma escola com anexos não pode passar a `ANEXO`. A importação de Excel grava o vínculo diretamente.
- `AnoReferencia` representa o ano civil. Não confunda seu ID com o valor do ano, como `2025`.
- `RegistroAnual` vincula escola e ano, com restrição de unicidade `(escola_id, ano_referencia_id)`. Contém rede, local de funcionamento e total de alunos.
- Cada registro anual possui uma `EquipeEscola`, uma `InfraestruturaEscola` e coleções de `MatriculaNivel` e `MatriculaSerie`.
- Infraestrutura reúne construção, salas, coordenadas, situação, acesso, água, energia, internet e fotografia. Equipe registra disponibilidade de profissionais e necessidade de transporte.
- Matrículas armazenam quantidades por sexo e totais, agrupadas por nível ou série/etapa.
- `Usuario` possui nome, e-mail único, senha codificada, perfil (`ADMIN` ou `USUARIO`), estado ativo e indicador `alterarSenha`.

`Escola.registros` e os relacionamentos filhos de `RegistroAnual` usam cascata e remoção de órfãos. Exclusões e substituições de coleções podem remover dados relacionados. Preserve os dois lados dos vínculos ao construir entidades.

## Contratos HTTP

Não há prefixo global `/api` configurado. Preserve a grafia existente, incluindo camelCase nas rotas.

Na tabela, **CRUD base** significa: `POST /salvar`, `GET` na raiz do recurso, `PUT /editar` e `DELETE /excluir/{id}`. Nas edições, o ID normalmente vem no corpo do DTO. A tabela lista apenas rotas implementadas.

| Recurso | Operações |
| --- | --- |
| `/aldeia` | CRUD base e `GET /{id}`. |
| `/povo` | CRUD base e `GET /{id}`. |
| `/municipio` | CRUD base e `GET /estatisticas`. |
| `/terra` | CRUD base, `GET /{id}` e `GET /estatisticas`. |
| `/escola` | CRUD base, `GET /{id}` com detalhes e registro anual mais recente da escola, `GET /{id}/historico` (trilha de auditoria, só `ADMIN`), `POST /salvarCompleto` e `PUT /editarCompleto` (multipart: parte `dados` em JSON e parte opcional `foto`). |
| `/anos` | CRUD base. |
| `/registroAnual` | CRUD base, `GET /buscarPorAno?ano=2025` e `GET /buscarPorEscolaEAno?escolaId=1&ano=2025`. |
| `/infraestrutura` | CRUD base, `GET /buscarPorAno?ano=2025`, `GET /maisRecentes` (uma por escola, do registro mais recente que tenha infraestrutura; usada pelo mapa público) e `POST /{id}/foto` (multipart, campo `foto`). |
| `/equipe` | Apenas `POST /salvar` e `PUT /editar`. |
| `/matriculaNivel` | CRUD base. |
| `/matriculaSerie` | CRUD base. |
| `/usuario` | CRUD base e `GET /{id}`. |
| `/auth` | `POST /login`, `POST /refresh`, `POST /logout` e `PUT /alterar-senha`. |
| `/dashboard` | `GET`, com parâmetro opcional `ano`. |
| `/atualizacao-anual` | `POST /iniciar`, sem corpo. |
| `/importacoes` | `POST /infraestrutura` e `POST /matriculas`, multipart com campo `arquivo`. |

Os DTOs são a fonte dos nomes de campos, tipos e validações. Não suponha paginação, um envelope único de resposta ou CRUD completo em todos os controllers. Criações normalmente retornam `201`; consultas e edições, `200`. Exclusões variam entre `200` e `204`; preserve o contrato de cada controller.

Swagger: `/swagger-ui.html` e `/swagger-ui/**`; OpenAPI: `/v3/api-docs`. As anotações dos controllers documentam operações, mas o comportamento efetivo depende também dos services e de `SecurityConfig`.

## Segurança e autenticação

Fonte principal: `config/security/SecurityConfig.java`. A aplicação é stateless, sem form login ou HTTP Basic. O `JwtFilter` executa antes de `UsernamePasswordAuthenticationFilter`.

Os tokens trafegam em cookies `HttpOnly`, `SameSite=Strict` e `Path=/` gravados por `CookiesSessao`: `sigei_acesso` (JWT) e `sigei_renovacao` (refresh token). Nenhum token segue no corpo das respostas. O atributo `Secure` vem de `SIGEI_COOKIE_SECURE` (padrão `true`). Por causa de `SameSite=Strict`, o frontend precisa acessar a API pela mesma origem, via proxy `/api` (Nginx em produção, `proxy.conf.json` em desenvolvimento).

CSRF usa `csrf.spa()` (double submit cookie): o cookie `XSRF-TOKEN`, legível por JavaScript, é gravado em toda resposta e deve voltar no cabeçalho `X-XSRF-TOKEN` em métodos não seguros. O `HttpClient` do Angular faz isso automaticamente para URLs relativas. Ficam dispensados `/auth/login` e requisições com `Authorization: Bearer`, que não dependem de cookies. Falha de CSRF gera `403`.

As regras são avaliadas nesta ordem:

| Requisição | Acesso |
| --- | --- |
| `OPTIONS /**` | Público. |
| `/actuator/health` e descendentes | Público. |
| `/auth/login`, `/auth/refresh`, `/auth/logout` | Público. |
| Rotas Swagger/OpenAPI configuradas | Público. |
| `/auth/alterar-senha` | Qualquer usuário autenticado. |
| `GET /infraestrutura` e descendentes | Público. |
| `GET /escola/*/historico` | `ADMIN`. |
| Demais `GET /**` | `ADMIN` ou `USUARIO`. |
| Demais requisições | `ADMIN`. |

Esses matchers não criam endpoints. A exceção pública de infraestrutura não libera uploads, edições ou exclusões. A regra geral de leitura também permite a `USUARIO` consultar `/usuario`; não presuma restrição exclusiva de administrador nessa consulta.

Fluxo de autenticação:

1. `POST /auth/login` recebe `email` e `senha`; o service consulta o usuário, verifica se está ativo e compara a senha com BCrypt.
2. A resposta grava os cookies de sessão e devolve no corpo apenas `id`, `nome`, `email`, `perfil` e `alterarSenha`.
3. O navegador envia `sigei_acesso` automaticamente. O filtro aceita também `Authorization: Bearer <token>` (com precedência) para clientes de API, mas a API não expõe mais o token. O subject é o e-mail; há claims de ID, nome e perfil. Expiração padrão do token de acesso: `900000` milissegundos (15 minutos).
4. O filtro valida o JWT e consulta novamente o usuário no banco. A autorização usa o perfil e o estado atuais do cadastro.
5. Se `alterarSenha=true`, a única autoridade atribuída é `ROLE_TROCAR_SENHA`. O usuário pode alterar a própria senha e acessar rotas públicas, mas não recebe os privilégios normais do perfil.
6. `PUT /auth/alterar-senha` recebe `senhaAtual`, `novaSenha` e `confirmacaoSenha`; valida as senhas, impede reutilizar a atual, grava BCrypt, define `alterarSenha=false`, revoga todos os refresh tokens do usuário e remove os cookies de sessão. Retorna `204`.
7. `POST /auth/refresh` lê o cookie `sigei_renovacao`, grava novos cookies e devolve o mesmo corpo do login. `RefreshTokenService` grava apenas o hash SHA-256, valida por 7 dias (`jwt.refresh-expiration`) e rotaciona: o token usado é revogado por update condicional, de modo que duas renovações simultâneas não geram duas sessões. Reapresentar um token revogado há mais de 30 segundos (`TOLERANCIA_CONCORRENCIA`) revoga todas as sessões do usuário; dentro desse intervalo, que cobre abas renovando ao mesmo tempo com o mesmo cookie, o token é só recusado. Cookie ausente ou token desconhecido, expirado, revogado ou de usuário inativo resulta em `401` e na remoção dos cookies; a transação não é desfeita nesses casos, para manter as revogações.
8. `POST /auth/logout` revoga o refresh token do cookie, remove os cookies e retorna `204` mesmo sem sessão.

No frontend, os tokens nunca passam pelo JavaScript: o `localStorage` guarda só os dados de exibição do usuário (`sigei_usuario`), usados pelos guards. Após um `401`, `authInterceptor` renova a sessão com uma única chamada compartilhada e repete a requisição; se ela ainda falhar com `401`, limpa a sessão local e redireciona ao login.

Token ausente ou inválido segue pelo filtro sem autenticar; uma rota protegida responde `401`. Falta de permissão para usuário autenticado resulta em `403`.

CORS usa `CORS_ALLOWED_ORIGINS` (padrão `http://localhost:4200`), aceita origens separadas por vírgula, permite credenciais e os métodos GET/POST/PUT/DELETE/PATCH/OPTIONS. Headers permitidos: `Authorization`, `Content-Type`, `Accept`, `Origin`, `X-XSRF-TOKEN`; `Authorization` é exposto à resposta. Com o proxy `/api`, o navegador não faz requisições entre origens; o CORS serve a acessos diretos à API.

## Fluxos importantes

### Abertura de ano

`AtualizacaoAnualService.iniciarNovoAno()` é transacional. Cria o maior ano cadastrado + 1 e percorre as escolas. Para cada escola com histórico, usa seu registro mais recente e copia para novas entidades:

- do registro: rede, local de funcionamento e `totalAlunos`;
- a equipe, com `anoReferencia` apontando para o novo ano;
- a infraestrutura;
- as matrículas por nível e por série, inclusive os totais.

Escolas sem registro anterior são ignoradas. O dashboard abre no maior ano, então o novo ano já mostra os totais copiados até que sejam editados.

Da foto, só a URL é copiada. O `fotoPublicId` fica apenas no registro que fez o upload, porque a imagem é compartilhada entre os anos e uma remoção no Cloudinary não deve apagar a foto de outro ano. `AtualizacaoAnualServiceTest` cobre a cópia campo a campo; ao acrescentar um campo a essas entidades, inclua-o na cópia e no teste.

### Cadastro completo de escola

`CadastroEscolaService` grava, em uma única transação, escola, registro do ano informado, equipe, infraestrutura, matrículas e foto (`CadastroEscolaRequestDTO`). É o fluxo usado pelo formulário de escolas do frontend; os endpoints individuais de cada entidade continuam existindo.

- `salvarCompleto` exige `escola.id` nulo e retorna `201`; `editarCompleto` exige `escola.id` e retorna `200`. Ambos retornam `CadastroEscolaResponseDTO` (`escola` e `registro`).
- O registro é localizado por escola + ano: se existir, é atualizado; senão, é criado. Registros de outros anos não são alterados.
- Matrículas são casadas por `nivel` e por `denominacao` + `etapaEnsino`; itens ausentes na requisição são preservados e chaves repetidas geram `400`.
- `removerFoto=true` limpa a referência da foto. O upload ao Cloudinary ocorre após o flush do banco; se a transação for desfeita depois do upload, a imagem é removida por compensação.
- `salvar` e `editar` usam `@Transactional(rollbackFor = IOException.class)`. Uma falha de E/S no upload também desfaz tudo; sem isso, o Spring confirmaria a transação, porque `IOException` é verificada. Mantenha o `rollbackFor` em métodos transacionais que declarem exceções verificadas.

### Trilha de auditoria

O Hibernate Envers registra cada criação, alteração e exclusão das entidades com `@Audited`: todas as de domínio e `Usuario`, sem a `senha` (`@NotAudited`). `RefreshToken` não é auditado. Cada transação gera uma `Revisao` com data e hora, e o `RevisaoListener` copia para ela ID, e-mail e nome do `Usuario` autenticado. Sem autenticação (inicializadores, carga de planilhas), o autor fica nulo e a revisão é atribuída ao sistema. Os valores de cada versão ficam nas tabelas `<entidade>_aud`. A configuração está em `application.properties` (`org.hibernate.envers.*`): exclusões guardam os últimos valores, e mudanças em coleções não geram revisão do pai.

`HistoricoEscolaService` monta `GET /escola/{id}/historico`, com as alterações da escola, de seus registros anuais e, em cada registro, de equipe, infraestrutura e matrículas. Compara cada versão com a anterior da mesma entidade e devolve apenas os campos alterados. Relacionamentos aparecem como `<campo>Id`. A ordem vai da revisão mais recente à mais antiga, e cada item traz o `ano` do registro. Uma escola excluída mantém o histórico; uma escola sem revisões resulta em `404`.

- As revisões só são gravadas no commit. Testes que verificam auditoria precisam de transações reais, como em `HistoricoEscolaServiceTest`, com `Propagation.NOT_SUPPORTED`.
- Alterações por SQL direto não são registradas.
- Ao adicionar ou mudar um campo de entidade auditada, a migração deve alterar também a tabela `_aud`.

### Importação de Excel

`ImportacaoExcelService` lê a aba `Planilha4`; `ImportacaoMatriculasService` lê `Censo_2025`. Ambos possuem ano fixo `2025` no código. Não são importadores genéricos de qualquer layout ou ano.

Preserve os índices de colunas, a normalização de nomes, o tratamento de coordenadas e as regras de identificação de escolas. Os retornos `ImportacaoResultadoDTO` e `ImportacaoMatriculasResultadoDTO` incluem contadores e erros; uma resposta `200` não significa que todas as linhas foram importadas. Consulte os services e os utilitários antes de modificar esses fluxos.

### Dashboard

`DashboardService` usa o ano solicitado ou o maior ano cadastrado. Retorna resumo, evolução histórica de matrículas e escolas, matrículas por nível, infraestrutura, escolas por município e situação das escolas. As séries históricas abrangem os anos, enquanto os indicadores selecionados usam o ano resolvido. Consultas ficam em `DashboardRepository`; confira agregações para evitar duplicar contagens em joins.

### Imagens

`CloudinaryService` envia imagens para `sigei/infraestrutura`. Credenciais ausentes, vazias ou com valor `disabled` provocam `ArmazenamentoImagemNaoConfiguradoException`, convertida em `503`. A API pode funcionar sem Cloudinary, mas o upload não. Limites multipart configurados: 20 MB por arquivo e por requisição.

## Configuração e inicialização

Fonte: `src/main/resources/application.properties`. O arquivo importa opcionalmente `.env` da raiz como properties. Use `.env.example` como referência; nunca copie credenciais reais para documentação, testes, respostas ou commits.

| Variáveis | Finalidade / padrão |
| --- | --- |
| `DATABASE_URL` | URL JDBC; padrão `jdbc:postgresql://localhost:5432/sigei`. |
| `DATABASE_USERNAME`, `DATABASE_PASSWORD` | Credenciais; fallback para `POSTGRES_USER` / `POSTGRES_PASSWORD`. |
| `DATABASE_POOL_SIZE`, `DATABASE_POOL_MIN_IDLE` | Pool Hikari; padrões 10 e 1. |
| `JPA_DDL_AUTO`, `JPA_SHOW_SQL` | Padrões `validate` e `false`. Não use `update`: o esquema é responsabilidade do Flyway. |
| `JWT_SECRET` | Obrigatório; use segredo aleatório com pelo menos 32 bytes para a chave HMAC. |
| `JWT_EXPIRATION` | Validade do token de acesso em milissegundos; padrão 15 minutos. |
| `JWT_REFRESH_EXPIRATION` | Validade do refresh token em milissegundos; padrão 7 dias. |
| `SIGEI_COOKIE_SECURE` | Atributo `Secure` dos cookies de sessão; padrão `true`. Use `false` só em acesso HTTP sem TLS por IP da rede. |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas do frontend. |
| `SIGEI_ADMIN_NAME`, `SIGEI_ADMIN_EMAIL`, `SIGEI_ADMIN_PASSWORD` | Administrador inicial. |
| `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` | Armazenamento de imagens. |
| `SIGEI_DADOS_INICIAIS_ENABLED` | Carga automática de planilhas; padrão `true`. |
| `SIGEI_PLANILHA_INFRAESTRUTURA`, `SIGEI_PLANILHA_MATRICULAS` | Resources das planilhas; padrões apontam para `planilhas/`. |
| `PORT` | Porta local da API; padrão 8080. No Compose, controla a porta publicada, com a API interna em 8080. |

`spring.jpa.open-in-view=false`: mantenha o acesso a relacionamentos necessários ao DTO dentro do contexto transacional. O esquema é versionado pelo Flyway em `src/main/resources/db/migration`, e o Hibernate apenas o valida (`ddl-auto=validate`):

- `V1__esquema_inicial.sql` reproduz exatamente o DDL que o Hibernate gerava antes do Flyway, inclusive os nomes das constraints.
- `V2__refresh_token.sql` cria `refresh_token` de forma idempotente, pois a tabela pode já existir em bancos de desenvolvimento.
- `V3__unifica_vinculo_sede_anexo.sql` copia para `escola.escola_sede_id` os vínculos válidos e não ambíguos da tabela `anexo` e depois a remove.
- `V4__trilha_auditoria.sql` cria `revisao` e as tabelas `<entidade>_aud` do Envers, com índices para o histórico por escola. Também grava uma revisão inicial do sistema com o estado dos dados existentes.
- Bancos criados antes do Flyway, sem `flyway_schema_history`, recebem baseline na versão 1 (`spring.flyway.baseline-on-migrate`) e executam só as migrações seguintes.

Para mudar o esquema: altere a entidade, crie `V<n>__descricao.sql` com o SQL PostgreSQL correspondente e nunca edite uma migração já aplicada. Para obter o DDL que o Hibernate espera, gere o script com `jakarta.persistence.schema-generation.scripts.action=create` e o dialeto PostgreSQL. `FlywayMigracoesTest` aplica as migrações em um PostgreSQL real (Testcontainers) e sobe o contexto com `validate`. Ele exige Docker e é ignorado quando o Docker não está disponível.

Na inicialização:

- `MunicipioInitializer` (`@Order(1)`) cadastra os municípios do Acre ausentes, comparando nomes normalizados.
- `DadosIniciaisInitializer` (`@Order(2)`) só executa quando habilitado e não existe nenhum registro anual. Valida os dois arquivos e importa infraestrutura antes de matrículas. Arquivos indisponíveis podem impedir a inicialização.
- `AdminInitializer` cria o administrador apenas se o e-mail ainda não existir, com troca de senha obrigatória. Sem senha configurada, gera uma temporária e a registra no log. Configurar novas credenciais não altera automaticamente um usuário já existente.

Actuator expõe `health` e `info`; apenas health tem liberação pública explícita. O Dockerfile faz build em Java 21, pula os testes e executa o JAR com usuário sem privilégios. O Compose inclui PostgreSQL 17 e volume persistente.

## Execução e verificação

Execute na raiz com JDK 21 configurado. No PowerShell:

```powershell
# Compilar
.\mvnw.cmd compile

# Executar testes
.\mvnw.cmd clean test

# Executar um teste específico
.\mvnw.cmd "-Dtest=RegistroAnualServiceTest" test

# Empacotar / iniciar localmente (configure o banco e o ambiente antes)
.\mvnw.cmd package
.\mvnw.cmd spring-boot:run
```

O `pom.xml` não declara `annotationProcessorPaths`; com JDK 23 ou superior o MapStruct não é executado implicitamente e os testes falham por falta dos mappers. Nesse caso, acrescente `"-Dmaven.compiler.proc=full"` aos comandos.

Em Linux/macOS, use `./mvnw` nos mesmos comandos. Com `.env` configurado, `docker compose up --build -d` inicia os serviços; `docker compose logs -f api` acompanha a aplicação. `docker compose down` para os serviços mantendo o volume. Não use `down -v` para uma parada comum: remove os dados persistidos.

API local: `http://localhost:8080`; health: `/actuator/health`.

Testes em `src/test/java` espelham os pacotes principais. Services usam Mockito; repositories usam `@DataJpaTest` com `@ActiveProfiles("test")`. Há teste de carregamento do contexto, teste do inicializador de municípios e `FlywayMigracoesTest` (PostgreSQL via Testcontainers). O perfil `test` configura H2 em memória, schema `create-drop`, Flyway desligado (as migrações usam SQL do PostgreSQL), segredo JWT de teste e desabilita a carga de planilhas.

Use sufixo `Test` para descoberta pelo Maven e `@Nested` quando ajudar a organizar cenários, seguindo `RegistroAnualServiceTest`. Fixtures de persistência ficam em `repository/RepositoryTestFixtures.java`. Requisições manuais estão em `tests - bruno/Sample API Collection/`; elas não substituem testes automatizados. Testes H2 não garantem compatibilidade de toda consulta com PostgreSQL.

## Erros e cuidados ao modificar

`exceptions/handler/ManipuladorGlobalException.java` define os tratamentos atuais:

| Exceção | HTTP / corpo |
| --- | --- |
| `MethodArgumentNotValidException` | `400`, mapa de campos para listas de mensagens. |
| `IllegalArgumentException` | `400`, mensagem textual. |
| `NoSuchElementException` | `404`, mensagem textual. |
| `DataIntegrityViolationException` | `409`, mensagem textual. |
| `ArmazenamentoImagemNaoConfiguradoException` | `503`, mensagem textual. |
| `TokenInvalidoException` | `401`, mensagem textual; em `/auth/refresh` é tratada no próprio controller, que também remove os cookies. |

Não existe um formato universal de erro. O login lança `NoSuchElementException` para e-mail não encontrado e `RuntimeException` para senha incorreta/usuário inativo; estes últimos não têm tratamento específico nesse advice e podem resultar em `500`. Não documente todos os erros de login como `401` sem implementar essa mudança.

Ao trabalhar no projeto:

- Preserve contratos usados pelo frontend: rotas, DTOs, nomes JSON e códigos HTTP. Algumas edições não têm `@Valid`; não presuma validação uniforme.
- Mantenha regras de negócio nos services e consultas nos repositories. Retorne DTOs e use os mappers existentes.
- Use imports `jakarta.*` para JPA/validação. Não introduza Lombok ou outra estrutura sem necessidade: as entidades atuais têm métodos explícitos.
- Não edite implementações MapStruct geradas em `target/`; altere as interfaces em `mappers`.
- Ao mudar segurança, confira a ordem dos matchers e os casos público, sem token, `USUARIO`, `ADMIN` e troca obrigatória de senha, além de cookies e CSRF (`AuthSessaoHttpTest`).
- Ao mudar persistência, revise unicidade, cascatas, transações, carregamento de relações e reflexos nos indicadores, nas importações e na trilha de auditoria, e crie a migração Flyway correspondente, incluindo as tabelas `_aud`.
- Verifique alterações de lógica com os testes relevantes e informe o que foi efetivamente executado. Não trate o build Docker como execução da suíte.
- Atualize este arquivo quando houver mudanças de arquitetura, configuração, endpoints ou regras centrais.
