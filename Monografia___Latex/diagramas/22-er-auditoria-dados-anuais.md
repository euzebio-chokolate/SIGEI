# Modelo relacional - auditoria dos dados anuais

Versões dos anos de referência, registros escolares, equipes, infraestruturas e matrículas.

Atualizado em **06/10/2026**, considerando o estado resultante das migrações **V1 a V6** e os mapeamentos atuais das entidades.

```plantuml
@startuml 22_er_auditoria_dados_anuais
title Modelo relacional - auditoria dos dados anuais
skinparam backgroundColor white
skinparam shadowing false
skinparam defaultFontName Arial
skinparam defaultFontSize 12
skinparam classAttributeIconSize 0
skinparam monochrome true
hide methods
entity revisao {
  {field} * id : bigint <<PK>>
  {field} * data_hora : timestamp(6) with time zone
  {field} usuario_id : bigint
  {field} usuario_email : varchar(255)
  {field} usuario_nome : varchar(255)
}
entity ano_referencia_aud {
  {field} * id : bigint <<PK>>
  {field} * rev : bigint <<PK, FK>>
  {field} ano : integer
  {field} revtype : smallint
  --
  {field} PK (id, rev)
}
entity registro_anual_aud {
  {field} * id : bigint <<PK>>
  {field} * rev : bigint <<PK, FK>>
  {field} revtype : smallint
  {field} total_alunos : integer
  {field} ano_referencia_id : bigint
  {field} escola_id : bigint
  {field} local_funcionamento : varchar(255)
  {field} rede : varchar(255)
  --
  {field} PK (id, rev)
}
entity equipe_escola_aud {
  {field} * id : bigint <<PK>>
  {field} * rev : bigint <<PK, FK>>
  {field} necessita_transporte : boolean
  {field} revtype : smallint
  {field} tem_barqueiro : boolean
  {field} tem_equipe_gestora : boolean
  {field} tem_merendeira : boolean
  {field} tem_professor_responsavel : boolean
  {field} tem_zeladora : boolean
  {field} ano_referencia_id : bigint
  {field} registro_anual_id : bigint
  --
  {field} PK (id, rev)
}
entity infraestrutura_escola_aud {
  {field} * id : bigint <<PK>>
  {field} * rev : bigint <<PK, FK>>
  {field} ano_construcao : integer
  {field} latitude : float(53)
  {field} longitude : float(53)
  {field} num_salas : integer
  {field} revtype : smallint
  {field} tem_internet : boolean
  {field} tem_predio : boolean
  {field} tem_registro_fotografico : boolean
  {field} registro_anual_id : bigint
  {field} foto_public_id : varchar(255)
  {field} foto_url : varchar(255)
  {field} situacao : varchar(255)
  {field} tipo_acesso : varchar(255)
  {field} tipo_agua : varchar(255)
  {field} tipo_energia : varchar(255)
  --
  {field} PK (id, rev)
}
entity matricula_nivel_aud {
  {field} * id : bigint <<PK>>
  {field} * rev : bigint <<PK, FK>>
  {field} qtd_feminino : integer
  {field} qtd_masculino : integer
  {field} revtype : smallint
  {field} total : integer
  {field} registro_id : bigint
  {field} nivel : varchar(255)
  --
  {field} PK (id, rev)
}
entity matricula_serie_aud {
  {field} * id : bigint <<PK>>
  {field} * rev : bigint <<PK, FK>>
  {field} qtd_feminino : integer
  {field} qtd_masculino : integer
  {field} revtype : smallint
  {field} total : integer
  {field} registro_id : bigint
  {field} denominacao : varchar(255)
  {field} etapa_ensino : varchar(255)
  --
  {field} PK (id, rev)
}
revisao ||--o{ ano_referencia_aud : rev
revisao ||--o{ registro_anual_aud : rev
revisao ||--o{ equipe_escola_aud : rev
revisao ||--o{ infraestrutura_escola_aud : rev
revisao ||--o{ matricula_nivel_aud : rev
revisao ||--o{ matricula_serie_aud : rev
note top of revisao
  Autoria copiada, sem FK para usuario.
  PK das versões: (id, rev).
  Apenas rev referencia fisicamente revisao.id.
end note
legend bottom
  Asterisco (*) = NOT NULL | PK = chave primária | FK = chave estrangeira física
  Círculo = zero; barra = um; ramificação = muitos
endlegend
@enduml
```

## Leitura e limites

**Convenções:** `*` indica coluna obrigatória (NOT NULL, incluindo a chave primária); ausência de `*` indica coluna anulável. `PK` = chave primária; `FK` = chave estrangeira física; `unique`/`UNIQUE` = unicidade. No pé de galinha, círculo = zero, barra = um e ramificação = muitos. Toda FK referencia a coluna `id` da tabela na outra extremidade. Campos de auditoria que apenas copiam identificadores não são marcados como FK.

- Cada linha de auditoria é identificada por **PK (id, rev)**: `id` identifica o objeto versionado e `rev` identifica a revisão. Uma revisão pode conter alterações em várias tabelas; cada linha auditada pertence a exatamente uma revisão.
- Nas tabelas `_aud`, apenas `rev` tem FK física. Os demais identificadores são valores históricos, sem FK para tabelas atuais ou outras tabelas auditadas. Por isso o diagrama não desenha relações físicas para esses campos.
- `revtype` é `smallint` anulável no DDL, com os códigos usados pelo Envers: 0 = criação, 1 = alteração, 2 = exclusão. Não há CHECK desses códigos na V4.
- `revisao.usuario_id`, `usuario_email` e `usuario_nome` são cópias do autor, sem FK. Ausência de autor identifica operações do sistema.
- A V4 cria uma revisão inicial com uma fotografia dos dados existentes. Ela não recupera datas, autores ou alterações anteriores à implantação da auditoria.
- `store_data_at_delete=true` preserva os valores na exclusão. A tabela `revisao` repetida nas figuras 21, 22 e 23 é a mesma tabela física.
- Os campos booleanos de `equipe_escola_aud` são anuláveis, diferentemente dos campos correspondentes de `equipe_escola`. As unicidades das tabelas atuais também não são copiadas para as tabelas auditadas.
- A V4 cria índices de busca em `registro_anual_aud(escola_id)`, `equipe_escola_aud(registro_anual_id)`, `infraestrutura_escola_aud(registro_anual_id)`, `matricula_nivel_aud(registro_id)` e `matricula_serie_aud(registro_id)`. São índices não únicos, sem novas FKs.
- V5 e V6 acrescentam revisões do sistema para as normalizações de infraestrutura. Versões anteriores podem conservar os textos anteriores à padronização; as opções atuais não são restrições retroativas nas tabelas `_aud`.

## Fontes no projeto

- [V4__trilha_auditoria.sql](../../sigei-backend/src/main/resources/db/migration/V4__trilha_auditoria.sql)
- [Revisao.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/models/Revisao.java)
- [RevisaoListener.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/config/auditoria/RevisaoListener.java)
- [application.properties](../../sigei-backend/src/main/resources/application.properties)
- [V5__padroniza_situacao_e_acesso.sql](../../sigei-backend/src/main/resources/db/migration/V5__padroniza_situacao_e_acesso.sql)
- [V6__padroniza_agua_e_energia.sql](../../sigei-backend/src/main/resources/db/migration/V6__padroniza_agua_e_energia.sql)
- [RegistroAnual.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/models/RegistroAnual.java)
