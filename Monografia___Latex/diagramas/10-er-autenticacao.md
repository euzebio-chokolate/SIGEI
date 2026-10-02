# Modelo relacional - usuários e renovação de sessão

Terceira parte do modelo físico, incluindo a migração V2.

```plantuml
@startuml 10_er_autenticacao
title Modelo relacional - usuários e renovação de sessão
skinparam backgroundColor white
skinparam shadowing false
skinparam defaultFontName Arial
skinparam defaultFontSize 12
skinparam ArrowColor #334155
skinparam packageStyle rectangle
skinparam classAttributeIconSize 0
skinparam monochrome true
entity usuario {
  alterar_senha : boolean default true not null
  ativo : boolean not null
  * id : bigint <<PK>>
  email : varchar(255) not null unique
  nome : varchar(255) not null
  perfil : varchar(255) not null
  senha : varchar(255) not null
}
entity refresh_token {
  revogado : boolean not null
  criado_em : timestamp(6) with time zone not null
  expira_em : timestamp(6) with time zone not null
  * id : bigint <<PK>>
  revogado_em : timestamp(6) with time zone
  usuario_id : bigint not null <<FK>>
  token_hash : varchar(64) not null unique
}
usuario ||--o{ refresh_token : usuario_id
note bottom of usuario
  CHECK perfil IN ('ADMIN', 'USUARIO').
  senha guarda o hash produzido pelo PasswordEncoder.
end note
note bottom of refresh_token
  token_hash: SHA-256 do valor entregue ao navegador.
  ON DELETE CASCADE na FK usuario_id.
  Token de acesso JWT não possui tabela própria.
end note
@enduml
```

## Leitura e limites

Modelo físico derivado das migrações Flyway. `PK` = chave primária; `FK` = chave estrangeira; `unique` = unicidade; `not null` = preenchimento obrigatório. No pé de galinha, círculo significa zero, barra significa um e ramificação significa muitos. A ausência de `not null` indica coluna anulável. Tabelas de apoio repetidas entre figuras são a mesma tabela física.

## Fontes no projeto

- [V1__esquema_inicial.sql](../../sigei-backend/src/main/resources/db/migration/V1__esquema_inicial.sql)
- [V2__refresh_token.sql](../../sigei-backend/src/main/resources/db/migration/V2__refresh_token.sql)
