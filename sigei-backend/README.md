# SIGEI Backend

API do SIGEI em Java 21, Spring Boot e PostgreSQL.

## Documentação

- API HTTP: Swagger em `/swagger-ui.html` e OpenAPI em `/v3/api-docs` com a aplicação em execução.
- [Histórico anual das escolas](docs/HISTORICO_ESCOLAS.md): cadastro retroativo, consulta por período e integração com gráficos.
- [Banco de dados](docs/BANCO_DE_DADOS.md): diagrama de relacionamentos, dicionário das 13 tabelas, chaves, nulabilidade e cascatas, com base nas entidades JPA.
- [Modelo ER gerado automaticamente](docs/MODELO_ER.md): tabelas, colunas e relacionamentos extraídos das entidades JPA.

Para atualizar o modelo ER após alterar as entidades, execute na raiz:

```powershell
.\scripts\gerar-modelo-er.ps1
# Apenas verificar se o arquivo versionado está atualizado:
.\scripts\gerar-modelo-er.ps1 -Check
```

O gerador usa PowerShell, sem iniciar a API ou acessar o banco. O arquivo contém
Mermaid para visualização em um renderizador compatível. É um extrator dos
mapeamentos usados neste projeto, não um interpretador completo de JPA:
herança, chaves compostas, tabelas de junção e estratégias de nomes personalizadas
exigem ampliar o script. A geração é executada pelo comando, não pelo build Maven.

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
do Acre e importa as planilhas da pasta `planilhas`. Reinícios posteriores não
repetem a carga quando já existem registros anuais.

Para parar sem apagar os dados, execute `docker compose down`. Para apagar o
banco local e testar uma carga nova, execute `docker compose down -v`.

## Hospedagem

Para a demonstração do TCC com orçamento zero e acesso mesmo com o computador
desligado, veja [hospedagem gratuita com Vercel, Render e Neon](docs/HOSPEDAGEM_GRATUITA.md).
O `render.yaml` prepara a API no plano Free; o guia inclui o modelo do proxy do frontend.
O Render pode solicitar cartão para verificar a conta; nesse caso essa alternativa
não atende à exigência de publicar sem cartão.

Como alternativa para uma apresentação ao vivo com o computador ligado, veja
[o acesso temporário sem cartão](docs/HOSPEDAGEM_SEM_CARTAO.md).

Use o `Dockerfile` e um PostgreSQL gerenciado. Configure:

- `DATABASE_URL`, no formato `jdbc:postgresql://host:5432/banco`
- `DATABASE_USERNAME` e `DATABASE_PASSWORD`
- `JWT_SECRET`, com pelo menos 32 caracteres aleatórios
- `SIGEI_CHAVE_DADOS_SENSIVEIS`, com 32 bytes aleatórios em Base64 (`openssl rand -base64 32`), guardada em local seguro: é a chave da criptografia de CPF, matrícula e número do contrato dos professores
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

## Execução sem Docker

Configure as mesmas variáveis e execute `./mvnw spring-boot:run`. Java 21 é
obrigatório.
