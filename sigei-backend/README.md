# SIGEI Backend

API do SIGEI em Java 21, Spring Boot e PostgreSQL.

## Documentação

API HTTP: Swagger em `/swagger-ui.html` e OpenAPI em `/v3/api-docs` com a
aplicação em execução. O esquema do banco está nas migrações do Flyway, em
`src/main/resources/db/migration`.

## Organização do código

Os pacotes em `src/main/java/ac/gov/br/sigei_backend` seguem uma estrutura por camadas:

- `config`: configurações, com `security`, `cloudinary` e `initializer`.
- `controllers`: endpoints HTTP, com subpacotes `auth`, `dashboard` e `importacao`.
- `dto`: contratos de entrada em `request` e de saída em `response`; respostas especializadas em `dashboard`, `detalhe` e `resumo`.
- `exceptions`: exceções da aplicação e tratamento HTTP em `handler`.
- `mappers`: conversões entre entidades e DTOs.
- `models`: entidades e enumerações em `enums`.
- `repository`: acesso aos dados e projeções em `projection`.
- `service`: regras de negócio, com subpacotes `auth`, `armazenamento` e `importacao`.
- `utils`: auxiliares de Excel, coordenadas e contadores de importação.

Os testes em `src/test/java` espelham os pacotes de produção. Use o sufixo
`Test` para novas classes de teste, permitindo sua descoberta automática pelo
Maven. Auxiliares exclusivos dos testes de repositório ficam em `repository`.
Em testes com muitos cenários, agrupe as operações com `@Nested`, como em
`RegistroAnualServiceTest`.

Com Java 21 configurado, execute a suíte completa com `./mvnw clean test`
(no PowerShell, `./mvnw.cmd clean test`).

## Execução com Docker

```bash
cp .env.example .env
# Edite .env e troque todas as senhas e segredos.
docker compose up --build -d
docker compose logs -f api
```

A API estará em `http://localhost:8080` e o health check em
`http://localhost:8080/actuator/health`.

Na primeira inicialização de um banco vazio, a aplicação cadastra os municípios
do Acre e o administrador inicial. Escolas, matrículas e professores são
importados pela tela de importações; as planilhas da pasta `planilhas` servem de
referência.

Para parar sem apagar os dados, execute `docker compose down`. Para apagar o
banco local e testar uma carga nova, execute `docker compose down -v`.

## Hospedagem

Use o `Dockerfile` e um PostgreSQL gerenciado. Configure:

- `DATABASE_URL`, no formato `jdbc:postgresql://host:5432/banco`
- `DATABASE_USERNAME` e `DATABASE_PASSWORD`
- `JWT_SECRET`, com pelo menos 32 caracteres aleatórios
- `SIGEI_CHAVE_DADOS_SENSIVEIS`, com 32 bytes aleatórios em Base64 (`openssl rand -base64 32`), guardada em local seguro: é a chave mestra da criptografia de CPF, matrícula e número do contrato dos professores
- `SIGEI_CHAVE_RECUPERACAO_PUBLICA` (recomendada), a chave pública que permite recuperar esses dados se a chave mestra se perder; veja [Chaves dos dados sensíveis](#chaves-dos-dados-sensíveis)
- `SIGEI_ADMIN_PASSWORD`
- `CORS_ALLOWED_ORIGINS`, com a URL HTTPS pública do frontend
- `SIGEI_COOKIE_SECURE=true` (padrão), para que os cookies de sessão só trafeguem por HTTPS

A sessão usa cookies `HttpOnly` e `SameSite=Strict`, portanto o frontend deve
acessar a API pela mesma origem, pelo proxy `/api` do Nginx do frontend ou da Vercel.

O esquema do banco é criado e atualizado pelo Flyway na inicialização
(`src/main/resources/db/migration`). Bancos já existentes, criados pelo Hibernate
antes do Flyway, recebem baseline automaticamente e só executam as migrações novas.
Faça backup do banco antes da primeira implantação com o Flyway.

Para fotografias, configure `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY` e
`CLOUDINARY_API_SECRET`. O health check da hospedagem deve usar
`/actuator/health`.

Essas três credenciais também são necessárias no `.env` local para usar o
endpoint de upload. Elas estão disponíveis no painel da conta Cloudinary. Sem
elas, o restante da API continua funcionando, mas o upload responde com HTTP
`503 Service Unavailable`.

Alguns provedores entregam uma URL `postgresql://usuario:senha@host/banco`; ela
deve ser convertida para o formato JDBC acima.

## Chaves dos dados sensíveis

CPF, matrícula e número do contrato dos professores são gravados cifrados
(AES-256-GCM), também nas tabelas de auditoria. A cifra é em envelope: os valores
são cifrados por uma chave de dados, guardada na tabela `chave_dados` cifrada pela
chave mestra (`SIGEI_CHAVE_DADOS_SENSIVEIS`) e pela chave pública de recuperação
(`SIGEI_CHAVE_RECUPERACAO_PUBLICA`). A chave privada de recuperação fica fora do
servidor. A aplicação não sobe com uma chave que não abre os dados.

**Configurar a recuperação (uma vez).** Num computador confiável, gere o par e
configure a chave pública em uma linha:

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:4096 -aes256 -out sigei-recuperacao-privada.pem
openssl pkey -in sigei-recuperacao-privada.pem -pubout -outform DER | base64 -w0
```

Reinicie; o log mostra "Cópia de recuperação da chave de dados atualizada".
Guarde o `.pem` e sua senha separados, com mais de um responsável, e nunca no
repositório, no servidor ou nos backups do banco.

**Trocar a chave mestra.** Configure a nova em `SIGEI_CHAVE_DADOS_SENSIVEIS` e a
antiga em `SIGEI_CHAVE_DADOS_SENSIVEIS_ANTERIOR`, reinicie, remova a antiga e
reinicie de novo. Os valores gravados não mudam; só o registro de `chave_dados`
é recifrado.

**Recuperar após perder a chave mestra.** Configure uma nova chave mestra e, em
`SIGEI_CHAVE_RECUPERACAO_PRIVADA`, a chave privada em uma linha
(`openssl pkcs8 -topk8 -nocrypt -in sigei-recuperacao-privada.pem -outform DER | base64 -w0`).
Reinicie, remova `SIGEI_CHAVE_RECUPERACAO_PRIVADA` imediatamente e reinicie de
novo. Como a chave privada passou pelo servidor, gere depois um novo par de
recuperação.

**Backups.** Para restaurar um backup com os dados sensíveis legíveis, é preciso a
chave mestra vigente na data do backup ou a chave privada de recuperação. Guarde
a chave mestra com os segredos da hospedagem, nunca junto com os backups.

## Execução sem Docker

Configure as mesmas variáveis e execute `./mvnw spring-boot:run`. Java 21 é
obrigatório.
