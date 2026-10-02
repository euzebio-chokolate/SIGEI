# Sequência de renovação automática da sessão

Tratamento de 401 pelo interceptor e rotação do refresh token.

```plantuml
@startuml 12_sequencia_renovacao
title Sequência de renovação automática da sessão
skinparam backgroundColor white
skinparam shadowing false
skinparam defaultFontName Arial
skinparam defaultFontSize 12
skinparam ArrowColor #334155
skinparam packageStyle rectangle
skinparam classAttributeIconSize 0
skinparam monochrome true
autonumber
participant "Página Angular" as P
participant "authInterceptor / AuthService" as F
participant "API / Spring Security" as API
participant "AuthController / AuthService" as A
participant RefreshTokenService as R
database PostgreSQL as DB
P -> F : requisição de recurso protegido
F -> API : requisição com cookie de acesso
API --> F : 401
alt existe usuário no estado local
  F -> A : POST /api/auth/refresh\ncookie de renovação + X-XSRF-TOKEN
  note over F : Renovações simultâneas na mesma instância\ncompartilham um Observable (shareReplay).
  A -> R : consumir(token)
  R -> DB : buscar SHA-256 do token
  DB --> R : sessão e usuário
  alt token ativo, não expirado e usuário ativo
    R -> DB : revogarSeAtivo(id) de forma condicional
    R --> A : usuário
    A -> R : emitir novo refresh token
    R -> DB : persistir novo hash
    A --> F : 200 + novos cookies + dados do usuário
  else token recusado
    opt reutilização após tolerância de 30 s ou usuário inativo
      R -> DB : revogar todos os refresh tokens do usuário
    end
    R --> A : TokenInvalidoException
    A --> F : 401 + limpar cookies
  end
  F -> API : repetir requisição original uma única vez\ninclusive se o refresh retornou 401
  alt repetição aceita
    API --> P : resposta do recurso
  else repetição retorna 401
    API --> F : 401
    F -> F : limpar estado local e navegar para /login
  end
else sem estado local de sessão
  F --> P : propagar 401
end
@enduml
```

## Leitura e limites

O retry após refresh recusado tenta acomodar renovação por outra aba. A tolerância de 30 segundos não aceita novamente o token consumido: recusa a renovação sem revogar todas as sessões. Falhas diferentes de 401 são propagadas conforme o interceptor. A revogação de refresh tokens não é uma lista de revogação dos JWTs já emitidos.

## Fontes no projeto

- [auth.interceptor.ts](../../sigei-frontend/src/app/core/auth/auth.interceptor.ts)
- [auth.service.ts](../../sigei-frontend/src/app/core/auth/auth.service.ts)
- [RefreshTokenService.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/service/auth/RefreshTokenService.java)
- [AuthController.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/controllers/auth/AuthController.java)
