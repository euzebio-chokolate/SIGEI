# Contexto do projeto — SIGEI Backend

Este arquivo orienta o trabalho nesta API. Descreve a implementação existente; ao alterar contratos ou regras, confira o código e atualize este contexto. Responda e escreva documentação em português do Brasil.

## Objetivo e tecnologias

API de gestão da educação escolar indígena do Acre: municípios, terras indígenas, povos, aldeias, escolas, registros anuais, infraestrutura, equipes, matrículas, professores e suas lotações, indicadores e usuários.

- Java 21 e Maven Wrapper (`mvnw` / `mvnw.cmd`).
- Spring Boot 4.0.5, Spring MVC, Spring Data JPA, Bean Validation, Spring Security e Actuator.
- PostgreSQL na aplicação; H2 nos testes com perfil `test`.
- MapStruct 1.6.3 para conversões; Apache POI 5.4.1 para Excel; OpenPDF 2.0.3 para os relatórios em PDF.
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
| `dto/request` | Contratos de entrada, incluindo IDs dos relacionamentos; restrições próprias em `dto/validacao`. |
| `dto/response` | Contratos de saída; especializações em `resumo`, `detalhe`, `dashboard` e `historico`. |
| `mappers` | Interfaces MapStruct, com componentes Spring e atualizações via `@MappingTarget`. |
| `config/security` | Autorização HTTP, filtro JWT e codificação de senhas. |
| `config/initializer` | Administrador e municípios. |
| `config/cloudinary` | Cliente de armazenamento de imagens. |
| `config/auditoria` | `RevisaoListener`, que grava o autor das revisões do Envers. |
| `config/criptografia` | Cifra dos dados sensíveis dos professores (`CriptografiaDadosSensiveis`, `DadoSensivelConverter`). |
| `exceptions/handler` | Tratamento global de erros HTTP. |
| `utils` | Leitura/normalização de Excel, coordenadas e contadores de importação. |

Fluxo usual: controller → service → repository; mappers convertem entidades e DTOs. Relacionamentos recebidos por ID são resolvidos nos services. Há injeção por campo e por construtor no código atual; mantenha coerência com a classe alterada.

## Domínio e relacionamentos

Consulte [docs/BANCO_DE_DADOS.md](docs/BANCO_DE_DADOS.md) para o diagrama e o dicionário completo das tabelas. Atualize essa documentação ao alterar os mapeamentos JPA.

- `Municipio` contém o cadastro territorial; `TerraIndigena` referencia um município.
- `Aldeia` referencia uma terra indígena e um `Povo`; `Escola` referencia uma aldeia (`@ManyToOne`). Uma aldeia pode ter mais de uma escola, como Belo Monte (Feijó), com duas escolas-sede.
- `Escola` possui código, nome, tipo de unidade e referência opcional `escolaSede`. O campo da entidade é `String`, mas `VinculoSedeService` aceita apenas os valores do enum `TipoUnidade` (`SEDE`, `ANEXO`), sem diferenciar maiúsculas, e grava em maiúsculas.
- `Escola.escolaSede` é a única representação do vínculo entre anexo e escola-sede. A antiga entidade `Anexo` e a rota `/anexo` foram removidas, e a tabela foi apagada pela migração V3. `VinculoSedeService`, usado por `EscolaService` e `CadastroEscolaService`, aplica as regras com `400`: um `ANEXO` exige uma sede do tipo `SEDE`, diferente de si mesmo; uma `SEDE` não tem sede; e uma escola com anexos não pode passar a `ANEXO`. A importação de Excel grava o vínculo diretamente.
- `AnoReferencia` representa o ano civil. Não confunda seu ID com o valor do ano, como `2025`.
- `RegistroAnual` vincula escola e ano, com restrição de unicidade `(escola_id, ano_referencia_id)`. Contém rede, local de funcionamento e total de alunos. `rede` e `localFuncionamento` são `String`, mas a API só aceita os valores dos enums `Rede` (`ESTADUAL`, `MUNICIPAL`) e `LocalFuncionamento` (`RURAL`, `URBANA`), validados por `utils/OpcoesRegistroAnual` (`400` fora das opções; grava o nome da opção, sem diferenciar maiúsculas e acentos). A importação grava `ESTADUAL`/`RURAL`, e a abertura de ano copia o valor existente.
- Cada registro anual possui uma `EquipeEscola`, uma `InfraestruturaEscola` e coleções de `MatriculaNivel` e `MatriculaSerie`.
- Infraestrutura reúne construção, salas, coordenadas, situação, acesso, água, energia, internet e fotografia. Equipe registra disponibilidade de profissionais e necessidade de transporte.
- `situacao`, `tipoAcesso`, `tipoAgua` e `tipoEnergia` são `String`, mas só aceitam os valores dos enums `SituacaoEscola`, `TipoAcesso`, `TipoAgua` e `TipoEnergia`.
  - `utils/OpcoesInfraestrutura` valida nas gravações pela API (`400` fora das opções) e grava o nome da opção. A comparação ignora acentos, maiúsculas e separadores (`Rio/Vertente` → `RIO_VERTENTE`); vazio vira nulo.
  - Na importação, valores fora das opções viram nulo. Na energia, `REDE ELETRICA` vira `REDE_PUBLICA` e `SIM` vira `OUTRO`; esses sinônimos valem só na importação.
  - No frontend, `domains/escolas/models/infraestrutura-opcoes.ts` é a fonte única das opções e dos rótulos (`rotuloOpcao`) do formulário, do filtro do mapa, da revisão do cadastro e do modal de histórico. Ao mudar uma opção, altere os dois lados.
- Matrículas armazenam quantidades por sexo e totais, agrupadas por nível ou série/etapa.
- `Professor` guarda nome, etnia (texto livre, inclusive `NÃO INDÍGENA`), sexo (`M`/`F`), formação e os dados sensíveis CPF e matrícula. `LotacaoProfessor` é a lotação de um professor em um `AnoReferencia`: escola, município, cargo, segmento, número do contrato (sensível), início, amparo legal e tipo de contrato (`String` validada pelo enum `TipoContrato` em `utils/OpcoesProfessor`: `EFETIVO`, `PSS`, `CONTRATO_DIRETO`, `PERMUTA`).
  - Lotação sem professor é uma vaga em aberto; sem escola, é uma lotação no núcleo de educação do município e exige `municipioId`. Com escola, o município é o da escola. A lotação pertence ao ano, não ao `RegistroAnual`.
  - Não há unicidade por professor/escola/ano: o mesmo professor pode ter várias lotações (segmentos diferentes). Excluir o professor exclui as lotações (`CascadeType.REMOVE`); excluir a escola pela API remove antes as lotações nela.
  - **Dados sensíveis** (CPF, matrícula, número do contrato): cifrados com AES-256-GCM pelo `DadoSensivelConverter` (`@Convert`), também nas tabelas `_aud`; chave mestra `SIGEI_CHAVE_DADOS_SENSIVEIS`, obrigatória. O CPF é gravado só com dígitos (`utils/CpfUtils` valida os dígitos verificadores) e tem índice cego `cpfHash` (HMAC, `@NotAudited`) para unicidade e busca. As respostas só trazem esses campos ao `ADMIN` (`AcessoDadosSensiveis` + `semDadosSensiveis()` dos DTOs); para `USUARIO`, vêm nulos. Mensagens de erro não repetem os valores. Nunca registre esses valores em logs.
- `Usuario` possui nome, e-mail único, senha codificada, perfil (`ADMIN` ou `USUARIO`), estado ativo e indicador `alterarSenha`. O e-mail é gravado e buscado normalizado (`utils/EmailUtils`: sem espaços nas pontas e em minúsculas), no cadastro, na edição, no login e no `JwtFilter`; `Joao@x.com` e `joao@x.com` são o mesmo usuário.

`Escola.registros` e os relacionamentos filhos de `RegistroAnual` usam cascata e remoção de órfãos. Exclusões e substituições de coleções podem remover dados relacionados. Preserve os dois lados dos vínculos ao construir entidades.

## Contratos HTTP

Não há prefixo global `/api` configurado. As rotas seguem REST: recursos no plural e em kebab-case, sem verbos no caminho, e o ID do registro no caminho. Mantenha esse padrão em rotas novas.

Na tabela, **CRUD base** significa: `POST` na coleção (`201`), `GET` na coleção, `PUT /{id}` (`200`) e `DELETE /{id}` (`204`, sem corpo). Na edição, o ID do caminho prevalece: um `id` no corpo é ignorado, e o cadastro também ignora o `id` do corpo (os mappers de entrada têm `@Mapping(target = "id", ignore = true)`), para que um `POST` nunca sobrescreva um registro. Os controllers retornam `ResponseEntity` tipado. A tabela lista apenas rotas implementadas.

| Recurso | Operações |
| --- | --- |
| `/aldeias` | CRUD base e `GET /{id}`. |
| `/povos` | CRUD base e `GET /{id}`. |
| `/municipios` | CRUD base e `GET /estatisticas`. |
| `/terras-indigenas` | CRUD base, `GET /{id}` e `GET /estatisticas`. |
| `/escolas` | CRUD base, `GET /{id}` com detalhes e registro anual mais recente da escola, `GET /{id}/historico` (trilha de auditoria, só `ADMIN`), `GET /{id}/registros-anuais` (registros da escola em ordem crescente de ano; `anoInicial` e `anoFinal` opcionais), `GET /{id}/registros-anuais/{ano}` (registro do ano civil), `POST /cadastro-completo` e `PUT /{id}/cadastro-completo` (multipart: parte `dados` em JSON e parte opcional `foto`). |
| `/anos-referencia` | CRUD base (sem `GET /{id}`) e `POST /proximo`, sem corpo, que abre o ano seguinte (ver "Abertura de ano") e responde `201` com o ano criado. |
| `/anos/{ano}` | Listas completas de um ano civil, sem paginação, usadas pelas telas: `GET /registros-anuais`, `GET /infraestruturas` (pública) e `GET /lotacoes`. `{ano}` é o valor do ano (`2025`), não o ID de `AnoReferencia`; por isso fica fora de `/anos-referencia/{id}` (`AnoController`). |
| `/registros-anuais` | CRUD base, com o `GET` da coleção paginado (`PaginaDTO`; filtros opcionais `ano`, `escolaId` e `municipioId`). |
| `/infraestruturas` | CRUD base, `GET /mais-recentes` (uma por escola, do registro mais recente que tenha infraestrutura; usada pelo mapa público) e `POST /{id}/foto` (multipart, campo `foto`). |
| `/equipes` | Apenas `POST` e `PUT /{id}`. |
| `/matriculas-nivel` | CRUD base. |
| `/matriculas-serie` | CRUD base. |
| `/usuarios` | CRUD base e `GET /{id}`. |
| `/professores` | CRUD base, `GET /{id}` e `GET /{id}/lotacoes` (todas as lotações do professor, da mais recente à mais antiga). A exclusão remove as lotações. CPF e matrícula só aparecem para `ADMIN`. |
| `/lotacoes` | CRUD base, com o `GET` da coleção paginado (`PaginaDTO`; filtros opcionais `ano`, `professorId`, `escolaId` e `municipioId`), e `GET /{id}`. Número do contrato só aparece para `ADMIN`. |
| `/auth` | `POST /login`, `POST /refresh`, `POST /logout` e `PUT /senha` (troca da própria senha). |
| `/dashboard` | `GET`, com parâmetro opcional `ano`. Inclui o bloco `professores` (lotados, lotações, vagas, núcleo e lotações por tipo de contrato). |
| `/auditoria` | `GET`, só `ADMIN`: alterações de todas as entidades auditadas, paginadas (`pagina` a partir de 0, `tamanho` até 100) e filtráveis por `entidade`, `operacao`, `usuarioId`, `sistema`, `de` e `ate` (datas do Acre). Responde `PaginaAuditoriaDTO`. |
| `/importacoes` | `POST /infraestrutura`, `POST /matriculas` e `POST /professores`, multipart com campo `arquivo` e parâmetro opcional `ano` (sem ele, o mais recente); `GET /layouts` (colunas aceitas por tipo) e `GET /modelos/{tipo}` (planilha modelo `.xlsx`). |
| `/relatorios` | `GET /{tipo}` (`painel`, `escolas`, `matriculas`, `professores`), com `formato` (`xlsx` padrão ou `pdf`), `ano` e `municipioId` opcionais; responde o arquivo como anexo. |

Os DTOs são a fonte dos nomes de campos, tipos e validações. Não suponha paginação, um envelope único de resposta ou CRUD completo em todos os controllers.

Paginação: só as listagens de todos os anos, que crescem com o histórico (`GET /registros-anuais` e `GET /lotacoes`), e a auditoria. As duas primeiras recebem `pagina` (a partir de 0) e `tamanho` (padrão 20, até 100; `utils/Paginacao`, `400` fora disso), ordenam na consulta do repository e respondem `PaginaDTO` (`itens`, `pagina`, `tamanho`, `totalItens`, `totalPaginas`). As demais listas devolvem tudo, porque têm o tamanho do cadastro ou de um ano (cerca de 250 escolas e 600 lotações por ano), e o frontend pagina e filtra na tela; as telas usam as rotas por ano (`/anos/{ano}/...`), não as paginadas. A exceção é o modal de detalhes da escola, que lista os professores lotados nela no ano com `GET /lotacoes?escolaId=&ano=&tamanho=100`. Ao criar uma listagem que cresça a cada ano, pagine-a da mesma forma. Criações retornam `201`; consultas e edições, `200`; exclusões, `204`.

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
| `/auth/senha` | Qualquer usuário autenticado. |
| `GET /infraestruturas` e descendentes, e `GET /anos/*/infraestruturas` | Público. |
| `GET /escolas/*/historico` | `ADMIN`. |
| `GET /auditoria` e descendentes | `ADMIN`. |
| `GET /usuarios` e descendentes | `ADMIN`. |
| Demais `GET /**` | `ADMIN` ou `USUARIO`. |
| Demais requisições | `ADMIN`. |

Esses matchers não criam endpoints. A exceção pública de infraestrutura não libera uploads, edições ou exclusões. Leituras que expõem dados de pessoas ou de todo o sistema (`/usuarios`, com nomes, e-mails e perfis; `/auditoria`; histórico da escola) têm matcher `ADMIN` antes da regra geral de leitura; esconder a tela no frontend não basta. `UsuarioAcessoHttpTest` cobre `/usuarios`, inclusive a raiz (o padrão `/usuarios/*` não a incluiria). Já `/professores` e `/lotacoes` (inclusive `/professores/{id}/lotacoes` e `/anos/{ano}/lotacoes`) têm a leitura liberada a `USUARIO`, e é o service (`AcessoDadosSensiveis`) que omite CPF, matrícula e número do contrato para quem não é `ADMIN`.

Fluxo de autenticação:

1. `POST /auth/login` recebe `email` e `senha`. O service consulta o usuário e compara a senha com BCrypt; sem usuário, compara com um hash fictício, para que o tempo de resposta não revele quais e-mails existem. E-mail inexistente e senha incorreta resultam no mesmo `401` (`CredenciaisInvalidasException`). Só quem acerta a senha de um usuário inativo recebe `403` (`UsuarioDesativadoException`), e o frontend mostra essa mensagem específica.
2. A resposta grava os cookies de sessão e devolve no corpo apenas `id`, `nome`, `email`, `perfil` e `alterarSenha`.
3. O navegador envia `sigei_acesso` automaticamente. O filtro aceita também `Authorization: Bearer <token>` (com precedência) para clientes de API, mas a API não expõe mais o token. O subject é o e-mail; há claims de ID, nome e perfil. Expiração padrão do token de acesso: `900000` milissegundos (15 minutos).
4. O filtro valida o JWT e consulta novamente o usuário no banco. A autorização usa o perfil e o estado atuais do cadastro.
5. Se `alterarSenha=true`, a única autoridade atribuída é `ROLE_TROCAR_SENHA`. O usuário pode alterar a própria senha e acessar rotas públicas, mas não recebe os privilégios normais do perfil.
6. `PUT /auth/senha` recebe `senhaAtual`, `novaSenha` e `confirmacaoSenha`; valida as senhas (de 8 a 72 caracteres, o limite do BCrypt; o mesmo vale para a senha de `/usuarios`, que pode vir vazia na edição para manter a atual), impede reutilizar a atual, grava BCrypt, define `alterarSenha=false`, revoga todos os refresh tokens do usuário e remove os cookies de sessão. Retorna `204`.
7. `POST /auth/refresh` lê o cookie `sigei_renovacao`, grava novos cookies e devolve o mesmo corpo do login. `RefreshTokenService` grava apenas o hash SHA-256, valida por 7 dias (`jwt.refresh-expiration`) e rotaciona: o token usado é revogado por update condicional, de modo que duas renovações simultâneas não geram duas sessões. Reapresentar um token revogado há mais de 30 segundos (`TOLERANCIA_CONCORRENCIA`) revoga todas as sessões do usuário; dentro desse intervalo, que cobre abas renovando ao mesmo tempo com o mesmo cookie, o token é só recusado. Cookie ausente ou token desconhecido, expirado, revogado ou de usuário inativo resulta em `401` e na remoção dos cookies; a transação não é desfeita nesses casos, para manter as revogações.
8. `POST /auth/logout` revoga o refresh token do cookie, remove os cookies e retorna `204` mesmo sem sessão.

No frontend, os tokens nunca passam pelo JavaScript: o `localStorage` guarda só os dados de exibição do usuário (`sigei_usuario`), usados pelos guards. Após um `401`, `authInterceptor` renova a sessão com uma única chamada compartilhada e repete a requisição; se ela ainda falhar com `401`, limpa a sessão local e redireciona ao login.

Token ausente ou inválido segue pelo filtro sem autenticar; uma rota protegida responde `401`. Falta de permissão para usuário autenticado resulta em `403`.

CORS usa `CORS_ALLOWED_ORIGINS` (padrão `http://localhost:4200`), aceita origens separadas por vírgula, permite credenciais e os métodos GET/POST/PUT/DELETE/PATCH/OPTIONS. Headers permitidos: `Authorization`, `Content-Type`, `Accept`, `Origin`, `X-XSRF-TOKEN`; `Authorization` é exposto à resposta. Com o proxy `/api`, o navegador não faz requisições entre origens; o CORS serve a acessos diretos à API.

## Fluxos importantes

### Mapa público

`GET /infraestruturas/mais-recentes` seleciona diretamente `InfraestruturaEscolaResponseDTO`
no repository, incluindo os resumos de escola e ano por um construtor de projeção.
Isso evita materializar entidades e carregar relacionamentos EAGER por escola.
Preserve uma infraestrutura por escola, do ano mais recente que tenha infraestrutura;
o teste de repository também verifica uma única consulta e nenhuma entidade carregada.

### Abertura de ano

`POST /anos-referencia/proximo` chama `AtualizacaoAnualService.iniciarNovoAno()`, que é transacional e devolve o ano criado. Cria o maior ano cadastrado + 1 e percorre as escolas. Para cada escola com histórico, usa seu registro mais recente e copia para novas entidades:

- do registro: rede, local de funcionamento e `totalAlunos`;
- a equipe, com `anoReferencia` apontando para o novo ano;
- a infraestrutura;
- as matrículas por nível e por série, inclusive os totais.

Escolas sem registro anterior são ignoradas. O dashboard abre no maior ano, então o novo ano já mostra os totais copiados até que sejam editados.

As lotações de professores do último ano também são copiadas para o novo, com todos os campos, inclusive vagas em aberto, lotações no núcleo e o número do contrato.

Da foto, só a URL é copiada. O `fotoPublicId` fica apenas no registro que fez o upload, porque a imagem é compartilhada entre os anos e uma remoção no Cloudinary não deve apagar a foto de outro ano. `AtualizacaoAnualServiceTest` cobre a cópia campo a campo; ao acrescentar um campo a essas entidades, inclua-o na cópia e no teste.

### Cadastro completo de escola

`CadastroEscolaService` grava, em uma única transação, escola, registro do ano informado, equipe, infraestrutura, matrículas e foto (`CadastroEscolaRequestDTO`). É o fluxo usado pelo formulário de escolas do frontend; os endpoints individuais de cada entidade continuam existindo.

- `POST /escolas/cadastro-completo` exige `escola.id` nulo e retorna `201`; `PUT /escolas/{id}/cadastro-completo` usa o ID do caminho (ignora `escola.id`) e retorna `200`. Ambos retornam `CadastroEscolaResponseDTO` (`escola` e `registro`).
- O registro é localizado por escola + ano: se existir, é atualizado; senão, é criado. Registros de outros anos não são alterados.
- Matrículas são casadas por `nivel` e por `denominacao` + `etapaEnsino`; itens ausentes na requisição são preservados e chaves repetidas geram `400`.
- `removerFoto=true` limpa a referência da foto. O upload ao Cloudinary ocorre após o flush do banco; se a transação for desfeita depois do upload, a imagem é removida por compensação.
- `salvar` e `editar` usam `@Transactional(rollbackFor = IOException.class)`. Uma falha de E/S no upload também desfaz tudo; sem isso, o Spring confirmaria a transação, porque `IOException` é verificada. Mantenha o `rollbackFor` em métodos transacionais que declarem exceções verificadas.

### Trilha de auditoria

O Hibernate Envers registra cada criação, alteração e exclusão das entidades com `@Audited`: todas as de domínio e `Usuario`, sem a `senha` (`@NotAudited`). `RefreshToken` não é auditado. Cada transação gera uma `Revisao` com data e hora, e o `RevisaoListener` copia para ela ID, e-mail e nome do `Usuario` autenticado. Sem autenticação (inicializadores), o autor fica nulo e a revisão é atribuída ao sistema. Os valores de cada versão ficam nas tabelas `<entidade>_aud`. A configuração está em `application.properties` (`org.hibernate.envers.*`): exclusões guardam os últimos valores, e mudanças em coleções não geram revisão do pai.

`HistoricoEscolaService` monta `GET /escolas/{id}/historico`, com as alterações da escola, de seus registros anuais e, em cada registro, de equipe, infraestrutura e matrículas. Compara cada versão com a anterior da mesma entidade e devolve apenas os campos alterados. Relacionamentos aparecem como `<campo>Id`. A ordem vai da revisão mais recente à mais antiga, e cada item traz o `ano` do registro. Uma escola excluída mantém o histórico; uma escola sem revisões resulta em `404`.

`AuditoriaService` monta `GET /auditoria`, a auditoria geral. Lista primeiro uma projeção leve (revisão, ID) de cada tabela `_aud` para ordenar e contar, e só então carrega e compara os registros da página. A paginação é por alteração, não por revisão, porque a revisão inicial da `V4` contém todos os registros existentes na época. Cada item traz uma `descricao` (nome do registro ou, para partes do registro anual e lotações, a escola ou o professor), buscada nos dados atuais e, para registros excluídos, na última versão auditada.

As duas trilhas usam `service/auditoria/VersoesAuditadas` para ler e comparar versões. O Envers decifra CPF, matrícula e número do contrato ao ler as tabelas `_aud`; `VersoesAuditadas` compara os valores reais, mas devolve esses campos (os anotados com `DadoSensivelConverter`) como `(dado protegido)`. Ao criar outro campo sensível, use o mesmo conversor para que ele seja protegido também na auditoria.

- As revisões só são gravadas no commit. Testes que verificam auditoria precisam de transações reais, como em `HistoricoEscolaServiceTest` e `AuditoriaServiceTest`, com `Propagation.NOT_SUPPORTED`. As revisões se acumulam no banco H2 entre testes; filtre pelo autor do teste, como faz `AuditoriaServiceTest`.
- Alterações por SQL direto não são registradas.
- Ao adicionar ou mudar um campo de entidade auditada, a migração deve alterar também a tabela `_aud`.

### Importação de Excel

As três importações recebem o ano (`ano`) e localizam as colunas pelo cabeçalho, e não pela posição. Cada service declara um enum `Coluna` (implementa `utils/ColunaPlanilha`: título, obrigatoriedade, descrição e sinônimos) e um `LayoutPlanilha` estático (`LAYOUT`), também usado pela tela (`GET /importacoes/layouts`) e pelo modelo (`GET /importacoes/modelos/{tipo}`).

- `LayoutPlanilha.localizar` procura, nas 10 primeiras linhas de cada aba, a primeira linha com todas as colunas obrigatórias; aba e ordem das colunas são livres e colunas extras são ignoradas. Sem elas, `400` listando as que faltam.
- A comparação ignora acentos, maiúsculas e pontuação (`Nº DE SALAS` = `N DE SALAS`). Títulos exatos têm prioridade; depois vale um título que começa pelo nome conhecido (`Nº DE ALUNOS 2025`).
- Cabeçalho em duas linhas: onde a primeira está vazia ou tem um título desconhecido (grupo mesclado, como `Matrículas`), vale a linha seguinte, e os dados começam depois dela.
- Coluna opcional ausente tem posição -1, que `ExcelUtils` lê como célula vazia. Na importação de escolas, colunas opcionais ausentes não alteram os valores gravados.
- Ao acrescentar uma coluna, inclua-a no enum (com sinônimos das planilhas reais) e no teste do service. As planilhas de `planilhas/` (referência para a importação pela tela) precisam continuar sendo lidas com o mesmo resultado.

`ImportacaoExcelService` (escolas, infraestrutura e equipe) cria o ano informado se ainda não existir (de 2000 ao ano atual + 1), sem copiar dados de outro ano. O anexo vem da coluna `TIPO DE UNIDADE` ou, no formato antigo, de `ANEXO` na coluna de ordem; sem `LATITUDE`/`LONGITUDE`, aceita `COORDENADAS` com a longitude na coluna seguinte. Coordenadas podem estar em graus decimais ou em graus, minutos e segundos. Com dois anos no ano de construção (`2014 e 2025`, construção e reforma), vale o primeiro (`ExcelUtils.lerAno`). Erros de banco de uma linha aparecem no resultado com uma mensagem neutra, sem o SQL, e vão para o log. `ImportacaoMatriculasService` exige o ano cadastrado e o registro anual de cada escola nele; `EJA` e `AEE` são opcionais (zero).

Preserve a normalização de nomes, o tratamento de coordenadas e as regras de identificação de escolas. Os retornos `ImportacaoResultadoDTO`, `ImportacaoMatriculasResultadoDTO` e `ImportacaoProfessoresResultadoDTO` incluem contadores e erros; uma resposta `200` não significa que todas as linhas foram importadas. Consulte os services e os utilitários antes de modificar esses fluxos.

`ImportacaoProfessoresService` lê o quadro de escolas e professores indígenas para o ano informado, que precisa estar iniciado (senão, `400`). As colunas obrigatórias são `MUNICÍPIO`, `LOTAÇÃO` e `NOME`. Cada linha é uma lotação:

- `NECESSIDADE` no nome ou no tipo de contrato vira vaga em aberto; lotação iniciada por `NEE` (`NEE SEE`, `NEE CRUZEIRO DO SUL`) vira lotação no núcleo do município.
- A planilha não tem código INEP. A escola é procurada no município, entre unidades do mesmo tipo (sede/anexo; o anexo é reconhecido pelo nome ou pelo `tipoUnidade`), nesta ordem: nome completo, nome sem o sufixo de anexo, mesma aldeia com alguma palavra em comum, e nome mais semelhante. Prefixos (`ESC.`, `IND.`), preposições e pontuação são ignorados, `DR` equivale a `DOUTOR`, e palavras com uma letra de diferença contam como iguais. Empates são decididos pela aldeia; se persistirem, a linha vai para os erros, em vez de arriscar a escola errada.
- Professores são localizados pelo CPF (índice cego) ou pelo nome normalizado, desempatado pela etnia; lotações, pelo professor, escola/município e segmento. Por isso a importação pode ser repetida sem duplicar.
- Células vazias nunca apagam valores gravados: CPF, matrícula e número do contrato vêm em branco na planilha e são preenchidos depois pelo sistema. CPF numérico tem os zeros à esquerda restaurados e é validado.
- Com a planilha de 2026 e a base de 2025, 595 das 607 linhas são importadas; as 12 restantes são de escolas que não estão cadastradas.

No frontend, a tela `/admin/importacoes` (só `ADMIN`) faz as três importações para qualquer ano, mostra as colunas aceitas e baixa o modelo. A tela de professores não tem mais importação própria.

### Dashboard

`DashboardService` usa o ano solicitado ou o maior ano cadastrado. Retorna resumo, evolução histórica de matrículas e escolas, matrículas por nível, infraestrutura, escolas por município e situação das escolas, além das lotações de professores do ano (`professores`: professores distintos, lotações, vagas em aberto, lotações no núcleo e distribuição por tipo de contrato, com o valor gravado). As séries históricas abrangem os anos, enquanto os indicadores selecionados usam o ano resolvido. Consultas ficam em `DashboardRepository`; confira agregações para evitar duplicar contagens em joins.

### Relatórios exportáveis

`RelatorioService` (`service/relatorio`) monta, para um ano e opcionalmente um município, os relatórios `painel` (indicadores consolidados), `escolas`, `matriculas` e `professores`. O conteúdo é um modelo independente do formato (`Relatorio`: seções com indicadores e tabelas), convertido por `GeradorPlanilhaRelatorio` (.xlsx: aba Resumo e uma aba por tabela, números como números) e `GeradorPdfRelatorio` (A4, paisagem nas tabelas largas, cabeçalho repetido e rodapé com emissão e página).

- As consultas ficam em `RelatorioRepository` e selecionam projeções (`repository/projection/*Relatorio`), sem carregar entidades; o filtro por município é aplicado no service. Ano ou município inexistente resulta em `404`; formato inválido, em `400`.
- Os números seguem o painel: matrículas pelo `totalAlunos` do registro e energia presente quando o tipo não é `NAO`.
- CPF, matrícula e número do contrato nunca entram nos relatórios: as projeções não selecionam esses campos, e por isso os relatórios ficam liberados também a `USUARIO`. `RelatorioServiceTest` verifica que esses valores não aparecem na planilha nem no PDF; preserve isso ao acrescentar colunas.

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
| `SIGEI_CHAVE_DADOS_SENSIVEIS` | Obrigatório; chave mestra (≥ 32 bytes em Base64, `openssl rand -base64 32`) da cifra de CPF, matrícula e número do contrato. Sem ela a aplicação não sobe; trocá-la exige recifrar os dados, e perdê-la os torna irrecuperáveis. |
| `JWT_EXPIRATION` | Validade do token de acesso em milissegundos; padrão 15 minutos. |
| `JWT_REFRESH_EXPIRATION` | Validade do refresh token em milissegundos; padrão 7 dias. |
| `SIGEI_COOKIE_SECURE` | Atributo `Secure` dos cookies de sessão; padrão `true`. Use `false` só em acesso HTTP sem TLS por IP da rede. |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas do frontend. |
| `SIGEI_ADMIN_NAME`, `SIGEI_ADMIN_EMAIL`, `SIGEI_ADMIN_PASSWORD` | Administrador inicial. |
| `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` | Armazenamento de imagens. |
| `PORT` | Porta local da API; padrão 8080. No Compose, controla a porta publicada, com a API interna em 8080. |

`spring.jpa.open-in-view=false`: mantenha o acesso a relacionamentos necessários ao DTO dentro do contexto transacional. `hibernate.default_batch_fetch_size=100` carrega em lote os relacionamentos que a consulta não trouxe (aldeia, terra, povo, matrículas), em vez de uma consulta por registro. O lote não alcança o lado inverso de um `@OneToOne` (`RegistroAnual.equipe` e `infraestrutura`): inclua esses campos na consulta (`join fetch` ou `@EntityGraph`), como fazem as listagens de `RegistroAnualRepository`. `ListagensHttpTest` limita o número de consultas das listagens. O esquema é versionado pelo Flyway em `src/main/resources/db/migration`, e o Hibernate apenas o valida (`ddl-auto=validate`):

- `V1__esquema_inicial.sql` reproduz exatamente o DDL que o Hibernate gerava antes do Flyway, inclusive os nomes das constraints.
- `V2__refresh_token.sql` cria `refresh_token` de forma idempotente, pois a tabela pode já existir em bancos de desenvolvimento.
- `V3__unifica_vinculo_sede_anexo.sql` copia para `escola.escola_sede_id` os vínculos válidos e não ambíguos da tabela `anexo` e depois a remove.
- `V4__trilha_auditoria.sql` cria `revisao` e as tabelas `<entidade>_aud` do Envers, com índices para o histórico por escola. Também grava uma revisão inicial do sistema com o estado dos dados existentes.
- `V5__padroniza_situacao_e_acesso.sql` padroniza `situacao` e `tipo_acesso` com as opções do sistema; valores fora delas viram nulo. Os ajustes ficam registrados numa revisão do sistema. Migrações que alterem dados devem fazer o mesmo, porque SQL direto não passa pelo Envers.
- `V6__padroniza_agua_e_energia.sql` faz o mesmo com `tipo_agua` e `tipo_energia`, convertendo também as grafias da planilha (`POÇO`, `RIO/VERTENTE`, `NÃO`, `REDE ELETRICA`, `SIM`).
- `V7__professores_e_lotacoes.sql` cria `professor` e `lotacao_professor`, com as tabelas `_aud`. As colunas sensíveis só recebem valores cifrados pela aplicação; não grave CPF, matrícula ou contrato por SQL direto.
- `V8__normaliza_email_usuario.sql` normaliza os e-mails de `usuario` já gravados, registrando o ajuste numa revisão do sistema. E-mails que coincidiriam com o de outro usuário não são alterados; a migração emite um aviso com os IDs, para resolução manual.
- `V9__varias_escolas_por_aldeia.sql` remove a restrição única de `escola.aldeia_id` (procurada pela coluna, porque o nome varia) e cria o índice `idx_escola_aldeia`. Não altera dados.
- Bancos criados antes do Flyway, sem `flyway_schema_history`, recebem baseline na versão 1 (`spring.flyway.baseline-on-migrate`) e executam só as migrações seguintes.

Para mudar o esquema: altere a entidade, crie `V<n>__descricao.sql` com o SQL PostgreSQL correspondente e nunca edite uma migração já aplicada. Para obter o DDL que o Hibernate espera, gere o script com `jakarta.persistence.schema-generation.scripts.action=create` e o dialeto PostgreSQL. `FlywayMigracoesTest` aplica as migrações em um PostgreSQL real (Testcontainers) e sobe o contexto com `validate`. Ele exige Docker e é ignorado quando o Docker não está disponível.

Na inicialização:

- `MunicipioInitializer` (`@Order(1)`) cadastra os municípios do Acre ausentes, comparando nomes normalizados.
- Não há carga automática de dados: escolas, matrículas e professores são importados pela tela `/admin/importacoes` (`/importacoes`).
- `AdminInitializer` cria o administrador apenas se o e-mail ainda não existir, com troca de senha obrigatória. Sem senha configurada, gera uma temporária e a registra no log. Configurar novas credenciais não altera automaticamente um usuário já existente.

Actuator expõe `health` e `info`; apenas health tem liberação pública explícita. O Dockerfile faz build em Java 21, pula os testes e executa o JAR com usuário sem privilégios. O Compose inclui PostgreSQL 17 e volume persistente.

### Demonstração pública gratuita

O Git local tem a raiz em `SIGEI`, contendo backend e frontend. No Render, use
Blueprint Path `sigei-backend/render.yaml`; o arquivo define `rootDir: sigei-backend`.
Na Vercel, use Root Directory `sigei-frontend`.
`render.yaml` configura somente a API no Render Free, com PostgreSQL externo no
Neon, heap Java de 256 MB e pool de três conexões. Esses limites precisam de validação no ambiente hospedado.
`docs/deploy/vercel.json.example` é um modelo para o repositório do frontend:
substitua o endereço de exemplo pela API real antes de usá-lo como `vercel.json`.
O proxy `/api` da Vercel mantém a mesma origem da sessão; a API não deve entrar
em cache compartilhado. Os arquivos não publicam os serviços automaticamente.

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

Testes em `src/test/java` espelham os pacotes principais. Services usam Mockito; repositories usam `@DataJpaTest` com `@ActiveProfiles("test")`. Há teste de carregamento do contexto, teste do inicializador de municípios e `FlywayMigracoesTest` (PostgreSQL via Testcontainers). O perfil `test` configura H2 em memória, schema `create-drop`, Flyway desligado (as migrações usam SQL do PostgreSQL), e segredo JWT de teste.

Use sufixo `Test` para descoberta pelo Maven e `@Nested` quando ajudar a organizar cenários, seguindo `RegistroAnualServiceTest`. Fixtures de persistência ficam em `repository/RepositoryTestFixtures.java`. Requisições manuais estão em `tests - bruno/Sample API Collection/`; elas não substituem testes automatizados. `RotasRestHttpTest` cobre o contrato REST (ID do caminho na edição, `id` do corpo ignorado no cadastro, `204` na exclusão, abertura de ano e as listas por ano). Testes H2 não garantem compatibilidade de toda consulta com PostgreSQL.

## Erros e cuidados ao modificar

Toda resposta de erro é um **ProblemDetail** (RFC 9457, `application/problem+json`): `status`, `title` (tipo do erro, fixo por status: "Conflito", "Requisição inválida"…), `detail` (mensagem para o usuário, em português), `instance` (rota) e, na validação, `errors` com as mensagens por campo. `exceptions/handler/Problemas` monta os corpos; não devolva texto puro nem mapas avulsos em respostas de erro.

`exceptions/handler/ManipuladorGlobalException.java` estende `ResponseEntityExceptionHandler`, que trata as exceções do Spring MVC (JSON mal formado, parâmetro ausente ou de tipo errado, método não aceito, rota inexistente, upload acima do limite); `handleExceptionInternal` troca os textos em inglês por mensagens em português:

- Valor do JSON que não converte para o tipo do campo (texto num número, opção fora de um enum, nulo num booleano) responde `400` como a validação, com o campo em `errors` ("Valor inválido para o campo perfil. Use um destes: ADMIN, USUARIO."). JSON mal formado, sem campo identificável, só tem o `detail`.
- Tipo errado na URL (`/povo/abc`): "Valor inválido para id.".
- Upload acima de `spring.servlet.multipart.max-file-size`: `413` com o limite na mensagem. `server.tomcat.max-swallow-size=100MB` faz o Tomcat descartar o corpo recusado em vez de fechar a conexão; com o padrão (2 MB), o cliente não recebia resposta. `FormatoErroHttpTest` cobre esses casos.

Os tratamentos próprios:

| Exceção | HTTP / `detail` |
| --- | --- |
| `MethodArgumentNotValidException` | `400`; `errors` com campo → mensagens, e `detail` com a mensagem do campo (ou "Alguns campos precisam de correção." se houver mais de um). |
| `IllegalArgumentException` | `400`, mensagem da exceção. |
| `NoSuchElementException` | `404`, mensagem da exceção. |
| `RegraNegocioException` (e a subclasse `RegistroEmUsoException`) | `409`, mensagem do service. |
| `DataIntegrityViolationException` | `409`, mensagem genérica; a mensagem do banco (SQL, constraints) não vai para a resposta, e o log registra só o nome da constraint. |
| `ArmazenamentoImagemNaoConfiguradoException` | `503`, mensagem da exceção. |
| `TokenInvalidoException` | `401`, mensagem da exceção; em `/auth/refresh` o controller remove os cookies e relança a exceção. |
| `CredenciaisInvalidasException` | `401`, "E-mail ou senha inválidos." (login). |
| `UsuarioDesativadoException` | `403`, "Usuário desativado. Procure o administrador do sistema." (login com a senha correta). |
| Qualquer outra `Exception` | `500`, mensagem genérica; a exceção vai só para o log. |

As mensagens de `IllegalArgumentException`, `NoSuchElementException` e das exceções próprias chegam à tela; escreva-as para o usuário, sem detalhes técnicos nem dados sensíveis.

Conflitos previsíveis são conferidos nos services antes de gravar, para que o `409` explique o motivo, e o frontend mostra essa mensagem sem deduzir nada:

- Exclusões com dependentes lançam `RegistroEmUsoException`, montada por `utils/Vinculos` ("Não é possível excluir o povo: ele tem 2 aldeias vinculadas."): povo e terra indígena com aldeias; município com terras indígenas ou lotações; aldeia com escolas; escola com anexos; ano com registros anuais ou lotações. O que sai em cascata (registros da escola, lotações, sessões do usuário) não bloqueia.
- Duplicidades lançam `RegraNegocioException`: e-mail de usuário, ano já cadastrado, registro anual da mesma escola e ano e segunda equipe ou infraestrutura do mesmo registro anual.
- `ConflitoHttpTest` cobre esses casos. Ao criar uma chave estrangeira ou uma restrição única, acrescente a verificação no service; a resposta genérica da `DataIntegrityViolationException` fica só para o que não dá para antecipar, como gravações simultâneas.

Erros gerados fora dos controllers são encaminhados para `/error`: `401` sem autenticação (o entry point de `SecurityConfig` usa `sendError`), `403` sem permissão ou com token CSRF inválido (`AccessDeniedHandler` padrão) e exceções nos filtros. `controllers/ErroController` substitui o `BasicErrorController` do Spring Boot e responde nesse encaminhamento com o mesmo ProblemDetail, com a mensagem padrão do status (no `403` de CSRF, uma mensagem própria), sem detalhes da exceção. `/error` é liberado em `SecurityConfig` (`DispatcherType.ERROR`): o `JwtFilter` não roda no encaminhamento, e sem a liberação o usuário autenticado recebia `401`, que o frontend trata como sessão expirada e encerra a sessão. A autorização continua valendo na requisição original (rota protegida sem token: `401`). `ErroEncaminhadoHttpTest` e `FormatoErroHttpTest` cobrem isso com servidor real, porque o MockMvc não faz o encaminhamento. Trate os erros esperados com exceções próprias e `@ExceptionHandler`, e não com `RuntimeException`.

No Swagger, as respostas de erro dos controllers são documentadas como `application/problem+json` com o schema `ProblemDetail`.

No frontend, `core/http/erro-http.ts` (`interpretarErro`) lê `detail` e `errors`; falhas de conexão e `5xx` ficam com mensagens genéricas. Downloads com `responseType: 'blob'` recebem também o erro como Blob e usam `lerErroDoDownload` (`core/http/download.ts`) para recuperar o ProblemDetail.

Ao trabalhar no projeto:

- Preserve contratos usados pelo frontend: rotas, DTOs, nomes JSON e códigos HTTP. Cadastros e edições validam o corpo com `@Valid` (`EdicaoValidacaoHttpTest`); mantenha isso em novos endpoints. Campos de texto livre têm `@Size` igual ao tamanho da coluna (`varchar(255)`, salvo indicação na entidade), para que textos longos resultem em `400` e não em erro de banco (`TamanhoCamposHttpTest`); campos restritos a opções (`tipoUnidade`, `rede`, `localFuncionamento`, `situacao`, `tipoAcesso`, `tipoAgua`, `tipoEnergia`, `tipoContrato`) são recusados pelo service. Faixas numéricas também ficam nos DTOs, iguais às do frontend: latitude de -90 a 90, longitude de -180 a 180, salas ≥ 0 e ano de construção de 1900 ao ano atual (`RegrasValidacaoHttpTest`). Anos que dependem da data usam `dto/validacao/AnoValido` (fuso do Acre); o ano de referência vai de 1900 ao ano seguinte, que a abertura de ano e a importação também criam. Nem todo DTO tem campos obrigatórios (`EquipeEscolaRequestDTO` não tem restrições, e `MatriculaSerieRequestDTO` só limita o tamanho dos textos), então não presuma validação uniforme.
- Mantenha regras de negócio nos services e consultas nos repositories. Retorne DTOs e use os mappers existentes.
- Use imports `jakarta.*` para JPA/validação. Não introduza Lombok ou outra estrutura sem necessidade: as entidades atuais têm métodos explícitos.
- Não edite implementações MapStruct geradas em `target/`; altere as interfaces em `mappers`.
- Ao mudar segurança, confira a ordem dos matchers e os casos público, sem token, `USUARIO`, `ADMIN` e troca obrigatória de senha, além de cookies e CSRF (`AuthSessaoHttpTest`).
- Ao mudar persistência, revise unicidade, cascatas, transações, carregamento de relações e reflexos nos indicadores, nas importações e na trilha de auditoria, e crie a migração Flyway correspondente, incluindo as tabelas `_aud`.
- Verifique alterações de lógica com os testes relevantes e informe o que foi efetivamente executado. Não trate o build Docker como execução da suíte.
- Atualize este arquivo quando houver mudanças de arquitetura, configuração, endpoints ou regras centrais.
