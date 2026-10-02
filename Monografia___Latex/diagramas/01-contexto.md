# Contexto do SIGEI

Visão do sistema e de seus participantes externos. Adequada à apresentação da solução.

```plantuml
@startuml 01_contexto
title Contexto do SIGEI
skinparam backgroundColor white
skinparam shadowing false
skinparam defaultFontName Arial
skinparam defaultFontSize 12
skinparam ArrowColor #334155
skinparam packageStyle rectangle
skinparam classAttributeIconSize 0
skinparam monochrome true
left to right direction
actor "Visitante" as V
actor "Usuário de consulta\nUSUARIO" as U
actor "Administrador\nADMIN" as A
rectangle "SIGEI\nSistema de Gerenciamento Escolar Indígena" as S
cloud "Serviços de mapas\nOpenStreetMap, Esri,\nOpenTopoMap e CARTO" as M
cloud "Cloudinary" as C
artifact "Planilhas Excel\ninfraestrutura e censo 2025" as X
V --> S : consulta mapa público
U --> S : consulta cadastros,\nhistórico e indicadores
A --> S : mantém cadastros\ne inicia novo ano
S --> M : navegador solicita tiles
S --> C : API envia imagens;\nnavegador consulta fotos
X --> S : importação pela API\nou carga inicial
note bottom of S
  Perfis autenticados também acessam o mapa público.
  Cloudinary é opcional para uso sem upload de foto.
end note
@enduml
```

## Fontes no projeto

- [app.routes.ts](../../sigei-frontend/src/app/app.routes.ts)
- [SecurityConfig.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/config/security/SecurityConfig.java)
- [CloudinaryService.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/service/armazenamento/CloudinaryService.java)
- [mapa-publico.component.ts](../../sigei-frontend/src/app/features/mapa-publico/pages/mapa-publico/mapa-publico.component.ts)
