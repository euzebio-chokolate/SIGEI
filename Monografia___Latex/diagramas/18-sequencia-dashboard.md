# Sequência de consulta aos indicadores

Consolidação de indicadores por ano e séries de evolução.

```plantuml
@startuml 18_sequencia_dashboard
title Sequência de consulta aos indicadores
skinparam backgroundColor white
skinparam shadowing false
skinparam defaultFontName Arial
skinparam defaultFontSize 12
skinparam ArrowColor #334155
skinparam packageStyle rectangle
skinparam classAttributeIconSize 0
skinparam monochrome true
autonumber
actor "Usuário autenticado" as U
participant "DashboardComponent /\nDashboardService Angular" as F
participant DashboardController as C
participant DashboardService as S
database "PostgreSQL\nAnoReferenciaRepository / DashboardRepository" as DB
U -> F : abrir painel ou selecionar ano
F -> C : GET /api/dashboard?ano=...\n(parâmetro opcional)
C -> S : buscar(ano)
group Transação somente leitura
S -> DB : validar ano informado\nou buscar maior ano cadastrado
DB --> S : ano selecionado
  S -> DB : buscar resumo, matrículas por nível,\ninfraestrutura, municípios e situações do ano
  DB --> S : agregações do ano
  S -> DB : buscar evolução de matrículas e escolas
  DB --> S : séries de todos os anos
  S -> S : calcular percentuais\ne normalizar situações
end
S --> C : DashboardResponseDTO
C --> F : JSON consolidado
F --> U : cartões, distribuições e gráficos
@enduml
```

## Leitura e limites

As séries de evolução não se restringem ao ano selecionado. O ano informado deve existir; sem anos cadastrados a consulta falha. O painel exibe indicadores, sem endpoint de exportação de relatório em arquivo.

## Fontes no projeto

- [DashboardService.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/service/DashboardService.java)
- [DashboardRepository.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/repository/DashboardRepository.java)
- [DashboardController.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/controllers/dashboard/DashboardController.java)
- [dashboard.service.ts](../../sigei-frontend/src/app/features/admin/dashboard/data-access/dashboard.service.ts)
