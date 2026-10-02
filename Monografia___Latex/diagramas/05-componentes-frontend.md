# Organização do frontend Angular

Separação entre funcionalidades, serviços de domínio, autenticação e componentes compartilhados.

```plantuml
@startuml 05_componentes_frontend
title Organização do frontend Angular
skinparam backgroundColor white
skinparam shadowing false
skinparam defaultFontName Arial
skinparam defaultFontSize 12
skinparam ArrowColor #334155
skinparam packageStyle rectangle
skinparam classAttributeIconSize 0
skinparam monochrome true
top to bottom direction
component "App / Router\napp.routes.ts" as ROOT
package "features" {
  component "mapa-publico\nmapa, busca e filtros" as MAP
  component "auth\nlogin e alterar-senha" as LOGIN
  component "admin\nshell e rotas carregadas sob demanda" as ADMIN
  component "Páginas de cadastros\nterritórios, escolas e usuários" as CRUD
  component "Dashboard e histórico" as HIST
  component "Configurações locais" as CONF
}
package "domains" {
  component "escolas\nmodels / data-access / ui" as ESC
  component "territorios\nmodels / data-access" as TERR
  component "usuarios\nmodels / data-access" as USER
}
package "core/auth" {
  component "AuthService" as AUTH
  component "Guards de navegação" as GUARD
  component "authInterceptor\nrenovação após 401" as INT
}
component "shared/ui\nheader e modais" as UI
component "HttpClient\nsuporte XSRF" as HTTP
database "localStorage" as LOCAL
component "API REST (/api)" as API
ROOT --> MAP
ROOT --> LOGIN
ROOT --> ADMIN
ROOT --> GUARD
GUARD --> AUTH
ADMIN --> CRUD
ADMIN --> HIST
ADMIN --> CONF
MAP --> ESC
CRUD --> ESC
CRUD --> TERR
CRUD --> USER
HIST --> ESC
HIST --> HTTP : serviço do dashboard
LOGIN --> AUTH
AUTH --> HTTP
ESC --> HTTP
TERR --> HTTP
USER --> HTTP
HTTP --> INT
INT ..> AUTH : renovarSessao()
INT --> API
CONF --> LOCAL
AUTH --> LOCAL : dados do usuário
MAP --> LOCAL : lê preferências
ADMIN --> UI
MAP --> UI
@enduml
```

## Leitura e limites

As páginas são componentes standalone. Os guards controlam a navegação e a exibição de ações; as regras efetivas de segurança são aplicadas pela API. Nem toda preferência armazenada implica comportamento implementado em todas as telas.

## Fontes no projeto

- [app.routes.ts](../../sigei-frontend/src/app/app.routes.ts)
- [app.config.ts](../../sigei-frontend/src/app/app.config.ts)
- [admin.routes.ts](../../sigei-frontend/src/app/features/admin/admin.routes.ts)
- [auth.interceptor.ts](../../sigei-frontend/src/app/core/auth/auth.interceptor.ts)
