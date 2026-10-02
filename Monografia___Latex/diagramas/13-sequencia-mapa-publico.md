# Sequência de consulta ao mapa público

Carregamento de dados, tiles e aplicação de filtros no navegador.

```plantuml
@startuml 13_sequencia_mapa_publico
title Sequência de consulta ao mapa público
skinparam backgroundColor white
skinparam shadowing false
skinparam defaultFontName Arial
skinparam defaultFontSize 12
skinparam ArrowColor #334155
skinparam packageStyle rectangle
skinparam classAttributeIconSize 0
skinparam monochrome true
autonumber
actor Visitante as U
participant MapaPublicoComponent as F
participant "Provedor de tiles" as T
participant "InfraestruturaEscolaController / Service" as A
database "PostgreSQL\nvia InfraestruturaEscolaRepository" as DB
participant "Origem da foto\nCloudinary quando configurado" as C
U -> F : abre /
F -> F : carregar preferências e iniciar Leaflet
F -> T : solicitar tiles da camada selecionada
T --> F : imagens do mapa
F -> A : GET /api/infraestrutura (público)
A -> DB : findAll()
DB --> A : infraestruturas e associações
A --> F : lista de DTOs com dados da escola
F -> F : aplicar filtros e criar marcadores\na partir das coordenadas aceitas
U -> F : pesquisar nome/código ou alterar filtros
F -> F : filtrar lista em memória\nsituação, acesso, internet e prédio
U -> F : selecionar escola / abrir popup
opt foto habilitada e fotoUrl disponível
  F -> C : buscar imagem da URL
  C --> F : fotografia
end
F --> U : mapa e informações da escola
@enduml
```

## Leitura e limites

A consulta atual usa `findAll()` e não seleciona o último ano. Com mais de um registro anual, uma escola pode aparecer mais de uma vez. Filtros e busca não geram nova consulta à API. Não há serviço de geocodificação neste fluxo.

## Fontes no projeto

- [mapa-publico.component.ts](../../sigei-frontend/src/app/features/mapa-publico/pages/mapa-publico/mapa-publico.component.ts)
- [infraestrutura.service.ts](../../sigei-frontend/src/app/domains/escolas/data-access/infraestrutura.service.ts)
- [InfraestruturaEscolaService.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/service/InfraestruturaEscolaService.java)
- [SecurityConfig.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/config/security/SecurityConfig.java)
