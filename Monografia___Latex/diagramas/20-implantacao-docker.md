# Implantação com Docker Compose

Topologia descrita pelos arquivos de implantação do repositório, com serviços de frontend e backend em composições separadas.

```plantuml
@startuml 20_implantacao_docker
title Implantação com Docker Compose
skinparam backgroundColor white
skinparam shadowing false
skinparam defaultFontName Arial
skinparam defaultFontSize 12
skinparam ArrowColor #334155
skinparam packageStyle rectangle
skinparam classAttributeIconSize 0
skinparam monochrome true
left to right direction
node "Computador do usuário" {
  artifact "Navegador\nSPA Angular" as BROWSER
}
node "Host Docker" {
  node "Compose do frontend" {
    node "frontend\nNginx 1.27 Alpine\nporta interna 80" as WEB {
      artifact "dist/sigei-frontend/browser\nHTML, CSS e JavaScript" as STATIC
    }
  }
  node "Compose do backend" {
    node "api\nTemurin Java 21 JRE\nporta interna 8080" as API {
      artifact "app.jar" as JAR
      artifact "planilhas/*.xlsx" as XLS
    }
    database "postgres\nPostgreSQL 17 Alpine\nporta interna 5432" as PG
    storage "Volume postgres_data" as VOL
  }
}
cloud "Cloudinary" as CL
cloud "Provedores de tiles" as TILES
BROWSER --> WEB : HTTP / porta FRONTEND_PORT\npadrão 4200
WEB --> API : /api/* via API_UPSTREAM\npadrão Compose: host.docker.internal:8080
API --> PG : JDBC\npostgres:5432
PG --> VOL : persistência
API --> CL : HTTPS / upload de foto
BROWSER --> CL : HTTPS / fotoUrl
BROWSER --> TILES : HTTPS / tiles
note bottom of API
  Publicação da API: PORT (padrão Compose 8080).
  .env.example usa PORT=8081: ajustar API_UPSTREAM.
  API aguarda healthcheck do PostgreSQL.
  Flyway migra; Hibernate valida o esquema.
end note
note bottom of WEB
  Os Compose não definem uma rede compartilhada.
  O Dockerfile tem API_UPSTREAM=http://api:8080,
  mas o Compose do frontend sobrescreve esse padrão.
  TLS não é configurado nestes arquivos.
end note
@enduml
```

## Leitura e limites

Variáveis e credenciais são fornecidas pelo ambiente; os diagramas não contêm segredos. O PostgreSQL também publica `POSTGRES_PORT` (padrão 5432) no host. Healthchecks existentes: `/healthz` no frontend e `/actuator/health` na API. O build do Angular usa Node 22; o build Java usa JDK 21 e Maven Wrapper.

## Fontes no projeto

- [docker-compose.yml](../../sigei-backend/docker-compose.yml)
- [Dockerfile](../../sigei-backend/Dockerfile)
- [.env.example](../../sigei-backend/.env.example)
- [compose.yml](../../sigei-frontend/compose.yml)
- [Dockerfile](../../sigei-frontend/Dockerfile)
- [default.conf.template](../../sigei-frontend/nginx/default.conf.template)
