# Modelo relacional - território e escolas

Primeira parte do modelo físico, com todas as colunas das tabelas apresentadas.

```plantuml
@startuml 08_er_territorio
title Modelo relacional - território e escolas
skinparam backgroundColor white
skinparam shadowing false
skinparam defaultFontName Arial
skinparam defaultFontSize 12
skinparam ArrowColor #334155
skinparam packageStyle rectangle
skinparam classAttributeIconSize 0
skinparam monochrome true
entity municipio {
  * id : bigint <<PK>>
  nome : varchar(255)
}
entity terra_indigena {
  * id : bigint <<PK>>
  municipio_id : bigint <<FK>>
  nome : varchar(255)
}
entity povo {
  * id : bigint <<PK>>
  nome : varchar(255)
}
entity aldeia {
  * id : bigint <<PK>>
  povo_id : bigint not null <<FK>>
  terra_indigena_id : bigint not null <<FK>>
  nome : varchar(255)
}
entity escola {
  aldeia_id : bigint unique <<FK>>
  escola_sede_id : bigint <<FK>>
  * id : bigint <<PK>>
  codigo : varchar(255)
  nome : varchar(255)
  tipo_unidade : varchar(255)
}
entity anexo {
  escola_anexo_id : bigint <<FK>>
  escola_id : bigint <<FK>>
  * id : bigint <<PK>>
  --
  unique (escola_id, escola_anexo_id)
}
municipio |o--o{ terra_indigena
terra_indigena ||--o{ aldeia
povo ||--o{ aldeia
aldeia |o--o| escola
escola |o--o{ escola : escola_sede_id
escola |o--o{ anexo : escola_id
escola |o--o{ anexo : escola_anexo_id
@enduml
```

## Leitura e limites

Modelo físico derivado das migrações Flyway. `PK` = chave primária; `FK` = chave estrangeira; `unique` = unicidade; `not null` = preenchimento obrigatório. No pé de galinha, círculo significa zero, barra significa um e ramificação significa muitos. A ausência de `not null` indica coluna anulável. Tabelas de apoio repetidas entre figuras são a mesma tabela física.

## Fontes no projeto

- [V1__esquema_inicial.sql](../../sigei-backend/src/main/resources/db/migration/V1__esquema_inicial.sql)
- [V2__refresh_token.sql](../../sigei-backend/src/main/resources/db/migration/V2__refresh_token.sql)
