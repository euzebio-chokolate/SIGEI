# Sequência de consulta ao histórico escolar

Consulta por escola e intervalo opcional, com validação e ordenação.

```plantuml
@startuml 17_sequencia_historico
title Sequência de consulta ao histórico escolar
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
participant "Página / modal de histórico" as F
participant RegistroAnualController as C
participant RegistroAnualService as S
database "PostgreSQL\nvia repositories" as DB
U -> F : selecionar escola e período
F -> C : GET /api/registroAnual/historico\n?escolaId=...&anoInicial=...&anoFinal=...
C -> S : buscarHistorico(escolaId, anoInicial, anoFinal)
S -> S : validar ID, anos positivos\ne ordem do intervalo
S -> DB : existsById(escolaId)
alt escola existe e período válido
  S -> DB : buscarHistorico com limites opcionais
  DB --> S : registros ordenados por ano
  S -> S : mapear registro e dados associados
  S --> C : lista de RegistroAnualResponseDTO
  C --> F : histórico (pode ser vazio)
  F --> U : comparação de anos e detalhes
else erro de validação ou escola ausente
  S --> C : exceção
  C --> F : 400 ou 404
  F --> U : mensagem de erro
end
@enduml
```

## Leitura e limites

Trata-se de histórico por ano de referência, não trilha de auditoria de alterações. A edição de um ano modifica seu registro; não cria automaticamente uma versão anterior dos valores alterados.

## Fontes no projeto

- [RegistroAnualService.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/service/RegistroAnualService.java)
- [RegistroAnualRepository.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/repository/RegistroAnualRepository.java)
- [historico-escolas.component.ts](../../sigei-frontend/src/app/features/admin/historico-escolas/pages/historico-escolas/historico-escolas.component.ts)
- [historico-escola-modal.component.ts](../../sigei-frontend/src/app/domains/escolas/ui/historico-escola-modal/historico-escola-modal.component.ts)
