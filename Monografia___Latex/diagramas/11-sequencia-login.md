# Sequência de autenticação

Fluxo de entrada e encaminhamento conforme a necessidade de trocar a senha.

```plantuml
@startuml 11_sequencia_login
title Sequência de autenticação
skinparam backgroundColor white
skinparam shadowing false
skinparam defaultFontName Arial
skinparam defaultFontSize 12
skinparam ArrowColor #334155
skinparam packageStyle rectangle
skinparam classAttributeIconSize 0
skinparam monochrome true
autonumber
actor Pessoa as U
participant "LoginComponent / AuthService\nAngular" as F
participant "AuthController" as C
participant "AuthService\nSpring" as S
database "PostgreSQL\nvia repositories" as DB
participant "JwtService /\nRefreshTokenService" as T
U -> F : informa e-mail e senha
F -> F : limpa estado local anterior
F -> C : POST /api/auth/login
note over F,C : Nginx remove /api; login dispensa CSRF.
C -> S : login(dto)
S -> DB : buscar usuário por e-mail
DB --> S : usuário e senha codificada
S -> S : verificar ativo e PasswordEncoder.matches
alt credenciais aceitas
  S -> T : gerar JWT e emitir refresh token
  T -> DB : persistir SHA-256, criação e expiração
  T --> S : valores dos tokens
  S --> C : SessaoAutenticada
  C --> F : dados do usuário + Set-Cookie\nsigei_acesso / sigei_renovacao
  F -> F : salvar dados de exibição no localStorage
  alt alterarSenha = true
    F --> U : encaminhar para /alterar-senha
  else senha definitiva
    F --> U : encaminhar para área administrativa
  end
else credenciais recusadas
  S --> C : exceção
  C --> F : resposta de erro
  F --> U : informar falha de autenticação
end
@enduml
```

## Leitura e limites

Os cookies de sessão usam HttpOnly e SameSite=Strict; Secure é configurável. O código atual distingue erros: e-mail não encontrado resulta em 404, enquanto senha incorreta ou usuário desativado lançam RuntimeException. O diagrama não normaliza esses casos para um 401 inexistente no fluxo atual.

## Fontes no projeto

- [auth.service.ts](../../sigei-frontend/src/app/core/auth/auth.service.ts)
- [login.component.ts](../../sigei-frontend/src/app/features/auth/pages/login/login.component.ts)
- [AuthController.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/controllers/auth/AuthController.java)
- [AuthService.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/service/auth/AuthService.java)
- [RefreshTokenService.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/service/auth/RefreshTokenService.java)
