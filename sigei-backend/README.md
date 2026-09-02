# SIGEI Backend

API do SIGEI em Java 21, Spring Boot e PostgreSQL.

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

Use o `Dockerfile` e um PostgreSQL gerenciado. Configure:

- `DATABASE_URL`, no formato `jdbc:postgresql://host:5432/banco`
- `DATABASE_USERNAME` e `DATABASE_PASSWORD`
- `JWT_SECRET`, com pelo menos 32 caracteres aleatórios
- `SIGEI_ADMIN_PASSWORD`
- `CORS_ALLOWED_ORIGINS`, com a URL HTTPS pública do frontend

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
