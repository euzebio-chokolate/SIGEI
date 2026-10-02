# Componentes e camadas do backend

Responsabilidades e dependências principais do monólito Spring Boot.

```plantuml
@startuml 04_componentes_backend
title Componentes e camadas do backend
skinparam backgroundColor white
skinparam shadowing false
skinparam defaultFontName Arial
skinparam defaultFontSize 12
skinparam ArrowColor #334155
skinparam packageStyle rectangle
skinparam classAttributeIconSize 0
skinparam monochrome true
top to bottom direction
component "Clientes HTTP" as CLIENT
package "Entrada e segurança" {
  component "Spring Security\nCSRF, CORS, regras por perfil\nJwtFilter" as SEC
  component "Controllers REST\nauth, cadastros, dashboard, importação" as CTR
  component "DTOs de entrada\nBean Validation" as IN
  component "ManipuladorGlobalException" as ERR
}
package "Aplicação" {
  component "Services de domínio\nterritórios, escolas e dados anuais" as SVC
  component "CadastroEscolaService\nAtualizacaoAnualService" as ORQ
  component "AuthService\nJwtService / RefreshTokenService" as AUTH
  component "DashboardService" as DASH
  component "Importação Excel\nApache POI / utils" as IMP
  component "Mappers MapStruct\nDTOs de resposta" as MAP
}
package "Persistência e integrações" {
  component "Repositories\nSpring Data JPA / consultas" as REP
  component "Models\nentidades JPA" as ENT
  component "CloudinaryService" as CL
  component "Initializers\nadmin, municípios e planilhas" as INIT
}
database PostgreSQL as DB
cloud Cloudinary as CLOUD
CLIENT --> SEC
SEC --> CTR
SEC --> AUTH : valida JWT
SEC --> REP : consulta usuário
CTR --> IN
CTR --> SVC
CTR --> ORQ
CTR --> AUTH
CTR --> DASH
CTR --> IMP
ERR ..> CTR : respostas de erro
SVC --> REP
ORQ --> REP
AUTH --> REP
DASH --> REP
IMP --> REP
SVC --> MAP
ORQ --> MAP
REP --> ENT
REP --> DB
ORQ --> CL
SVC --> CL : fotografia
CL --> CLOUD
INIT --> IMP
INIT --> REP
@enduml
```

## Leitura e limites

Agrupamentos representam camadas e conjuntos de classes, não microserviços. `CadastroEscolaService` coordena a gravação do agregado escolar; `DashboardService` consulta projeções e agregações.

## Fontes no projeto

- [EscolaController.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/controllers/EscolaController.java)
- [CadastroEscolaService.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/service/CadastroEscolaService.java)
- [DashboardService.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/service/DashboardService.java)
- [SecurityConfig.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/config/security/SecurityConfig.java)
- [ManipuladorGlobalException.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/exceptions/handler/ManipuladorGlobalException.java)
