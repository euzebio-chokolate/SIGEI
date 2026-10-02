# Arquitetura geral da aplicação

Visão lógica da SPA, do servidor HTTP, da API e da persistência. Os elementos representam unidades de execução, sem pressupor uma infraestrutura de nuvem específica.

```plantuml
@startuml 03_arquitetura_containers
title Arquitetura geral da aplicação
skinparam backgroundColor white
skinparam shadowing false
skinparam defaultFontName Arial
skinparam defaultFontSize 12
skinparam ArrowColor #334155
skinparam packageStyle rectangle
skinparam classAttributeIconSize 0
skinparam monochrome true
left to right direction
actor Pessoa
node "Navegador" {
  component "SPA Angular 21\nTypeScript, Tailwind CSS\nLeaflet e RxJS" as SPA
  database "Armazenamento local\ndados de exibição e preferências" as LOCAL
  component "Cookies de sessão\nHttpOnly" as COOKIE
}
node "Servidor web" {
  component "Nginx\narquivos estáticos e proxy /api" as WEB
}
node "Servidor da aplicação" {
  component "API REST\nJava 21 / Spring Boot 4.0.5\nSpring Security / JPA" as API
  component "Flyway\nmigrações SQL" as FLY
}
database "PostgreSQL" as DB
cloud "Provedores de tiles" as MAP
cloud Cloudinary as IMG
Pessoa --> SPA
SPA --> WEB : HTTP /api/*\nJSON ou multipart
WEB --> SPA : HTML, CSS e JavaScript
WEB --> API : encaminha sem /api
SPA --> LOCAL
SPA ..> COOKIE : navegador envia automaticamente
API --> DB : JDBC / Hibernate
FLY --> DB : migração do esquema
SPA --> MAP : HTTPS / tiles
API --> IMG : HTTPS / upload
SPA --> IMG : HTTPS / fotoUrl
@enduml
```

## Leitura e limites

Os tokens não são armazenados no `localStorage`. O estado local orienta a navegação; o backend decide a autorização. O Nginx fornecido atende HTTP; terminação TLS depende do ambiente de implantação.

## Fontes no projeto

- [package.json](../../sigei-frontend/package.json)
- [default.conf.template](../../sigei-frontend/nginx/default.conf.template)
- [pom.xml](../../sigei-backend/pom.xml)
- [application.properties](../../sigei-backend/src/main/resources/application.properties)
