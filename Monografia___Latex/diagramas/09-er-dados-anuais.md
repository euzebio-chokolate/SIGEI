# Modelo relacional - dados anuais

Segunda parte do modelo físico. A tabela escola é repetida de forma resumida para mostrar a ligação com o território.

```plantuml
@startuml 09_er_dados_anuais
title Modelo relacional - dados anuais
skinparam backgroundColor white
skinparam shadowing false
skinparam defaultFontName Arial
skinparam defaultFontSize 12
skinparam ArrowColor #334155
skinparam packageStyle rectangle
skinparam classAttributeIconSize 0
skinparam monochrome true
entity escola {
  * id : bigint <<PK>>
}
entity ano_referencia {
  ano : integer not null unique
  * id : bigint <<PK>>
}
entity registro_anual {
  total_alunos : integer
  ano_referencia_id : bigint <<FK>>
  escola_id : bigint <<FK>>
  * id : bigint <<PK>>
  local_funcionamento : varchar(255)
  rede : varchar(255)
  --
  unique (escola_id, ano_referencia_id)
}
entity equipe_escola {
  necessita_transporte : boolean not null
  tem_barqueiro : boolean not null
  tem_equipe_gestora : boolean not null
  tem_merendeira : boolean not null
  tem_professor_responsavel : boolean not null
  tem_zeladora : boolean not null
  ano_referencia_id : bigint <<FK>>
  * id : bigint <<PK>>
  registro_anual_id : bigint unique <<FK>>
}
entity infraestrutura_escola {
  ano_construcao : integer
  latitude : float(53)
  longitude : float(53)
  num_salas : integer
  tem_internet : boolean
  tem_predio : boolean
  tem_registro_fotografico : boolean
  * id : bigint <<PK>>
  registro_anual_id : bigint unique <<FK>>
  foto_public_id : varchar(255) <<FK>>
  foto_url : varchar(255)
  situacao : varchar(255)
  tipo_acesso : varchar(255)
  tipo_agua : varchar(255)
  tipo_energia : varchar(255)
}
entity matricula_nivel {
  qtd_feminino : integer
  qtd_masculino : integer
  total : integer
  * id : bigint <<PK>>
  registro_id : bigint <<FK>>
  nivel : varchar(255)
}
entity matricula_serie {
  qtd_feminino : integer
  qtd_masculino : integer
  total : integer
  * id : bigint <<PK>>
  registro_id : bigint <<FK>>
  denominacao : varchar(255)
  etapa_ensino : varchar(255)
}
escola |o--o{ registro_anual
ano_referencia |o--o{ registro_anual
ano_referencia |o--o{ equipe_escola
registro_anual |o--o| equipe_escola
registro_anual |o--o| infraestrutura_escola
registro_anual |o--o{ matricula_nivel
registro_anual |o--o{ matricula_serie
@enduml
```

## Leitura e limites

Modelo físico derivado das migrações Flyway. `PK` = chave primária; `FK` = chave estrangeira; `unique` = unicidade; `not null` = preenchimento obrigatório. No pé de galinha, círculo significa zero, barra significa um e ramificação significa muitos. A ausência de `not null` indica coluna anulável. Tabelas de apoio repetidas entre figuras são a mesma tabela física. Não existem restrições UNIQUE para nível ou série no SQL; a validação de itens repetidos no cadastro completo é feita pelo serviço.

## Fontes no projeto

- [V1__esquema_inicial.sql](../../sigei-backend/src/main/resources/db/migration/V1__esquema_inicial.sql)
- [V2__refresh_token.sql](../../sigei-backend/src/main/resources/db/migration/V2__refresh_token.sql)
