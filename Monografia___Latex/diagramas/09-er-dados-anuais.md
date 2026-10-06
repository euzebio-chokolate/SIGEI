# Modelo relacional - dados anuais

Dados escolares por ano, incluindo os domínios de valores da infraestrutura. A tabela escola é repetida integralmente para explicitar a origem do vínculo.

Atualizado em **06/10/2026**, considerando o estado resultante das migrações **V1 a V6** e os mapeamentos atuais das entidades.

```plantuml
@startuml 09_er_dados_anuais
title Modelo relacional - dados anuais
skinparam backgroundColor white
skinparam shadowing false
skinparam defaultFontName Arial
skinparam defaultFontSize 12
skinparam classAttributeIconSize 0
skinparam monochrome true
hide methods
entity escola {
  {field} * id : bigint <<PK>>
  {field} aldeia_id : bigint unique <<FK>>
  {field} escola_sede_id : bigint <<FK>>
  {field} codigo : varchar(255)
  {field} nome : varchar(255)
  {field} tipo_unidade : varchar(255)
}
entity ano_referencia {
  {field} * id : bigint <<PK>>
  {field} * ano : integer unique
}
entity registro_anual {
  {field} * id : bigint <<PK>>
  {field} total_alunos : integer
  {field} ano_referencia_id : bigint <<FK>>
  {field} escola_id : bigint <<FK>>
  {field} local_funcionamento : varchar(255)
  {field} rede : varchar(255)
  --
  {field} UNIQUE (escola_id, ano_referencia_id)
}
entity equipe_escola {
  {field} * id : bigint <<PK>>
  {field} * necessita_transporte : boolean
  {field} * tem_barqueiro : boolean
  {field} * tem_equipe_gestora : boolean
  {field} * tem_merendeira : boolean
  {field} * tem_professor_responsavel : boolean
  {field} * tem_zeladora : boolean
  {field} ano_referencia_id : bigint <<FK>>
  {field} registro_anual_id : bigint unique <<FK>>
}
entity infraestrutura_escola {
  {field} * id : bigint <<PK>>
  {field} ano_construcao : integer
  {field} latitude : float(53)
  {field} longitude : float(53)
  {field} num_salas : integer
  {field} tem_internet : boolean
  {field} tem_predio : boolean
  {field} tem_registro_fotografico : boolean
  {field} registro_anual_id : bigint unique <<FK>>
  {field} foto_public_id : varchar(255)
  {field} foto_url : varchar(255)
  {field} situacao : varchar(255)
  {field} tipo_acesso : varchar(255)
  {field} tipo_agua : varchar(255)
  {field} tipo_energia : varchar(255)
}
entity matricula_nivel {
  {field} * id : bigint <<PK>>
  {field} qtd_feminino : integer
  {field} qtd_masculino : integer
  {field} total : integer
  {field} registro_id : bigint <<FK>>
  {field} nivel : varchar(255)
}
entity matricula_serie {
  {field} * id : bigint <<PK>>
  {field} qtd_feminino : integer
  {field} qtd_masculino : integer
  {field} total : integer
  {field} registro_id : bigint <<FK>>
  {field} denominacao : varchar(255)
  {field} etapa_ensino : varchar(255)
}
escola |o--o{ escola : escola_sede_id
ano_referencia |o--o{ registro_anual : ano_referencia_id
escola |o--o{ registro_anual : escola_id
ano_referencia |o--o{ equipe_escola : ano_referencia_id
registro_anual |o--o| equipe_escola : registro_anual_id
registro_anual |o--o| infraestrutura_escola : registro_anual_id
registro_anual |o--o{ matricula_nivel : registro_id
registro_anual |o--o{ matricula_serie : registro_id
note bottom of infraestrutura_escola
  Opções da aplicação (não são CHECKs no banco):
  situacao: FUNCIONANDO, PARALISADA, DESATIVADA
  tipo_acesso: TERRESTRE, FLUVIAL, MISTO
  tipo_agua: REDE_PUBLICA, POCO, CACIMBA, RIO,
    IGARAPE, VERTENTE, RIO_VERTENTE, OUTRO, NAO
  tipo_energia: REDE_PUBLICA, GERADOR, SOLAR, OUTRO, NAO
  Os quatro campos também admitem nulo (não informado).
end note
legend bottom
  Asterisco (*) = NOT NULL | PK = chave primária | FK = chave estrangeira física
  Círculo = zero; barra = um; ramificação = muitos
endlegend
@enduml
```

## Leitura e limites

**Convenções:** `*` indica coluna obrigatória (NOT NULL, incluindo a chave primária); ausência de `*` indica coluna anulável. `PK` = chave primária; `FK` = chave estrangeira física; `unique`/`UNIQUE` = unicidade. No pé de galinha, círculo = zero, barra = um e ramificação = muitos. Toda FK referencia a coluna `id` da tabela na outra extremidade. Campos de auditoria que apenas copiam identificadores não são marcados como FK.

- `UNIQUE (escola_id, ano_referencia_id)` impede duplicar a combinação de escola e ano quando ambos estão preenchidos. As FKs continuam anuláveis no SQL.
- As FKs únicas de equipe e infraestrutura permitem no máximo uma linha de cada uma por registro anual. O campo `equipe_escola.ano_referencia_id` permanece no esquema.
- Não há UNIQUE por nível ou série no SQL. A existência de validações da aplicação não é representada como restrição física inexistente.
- `SituacaoEscola`, `TipoAcesso`, `TipoAgua` e `TipoEnergia` definem opções na aplicação. Os quatro atributos continuam `String` na entidade e `varchar(255)` anulável no PostgreSQL; não existem tabelas auxiliares, tipos ENUM nativos nem CHECK dessas opções nas migrações V1–V6.
- `OpcoesInfraestrutura.validar` normaliza entradas e rejeita valores inválidos na gravação pela API. Vazio é convertido para nulo. Na importação, valores não reconhecidos são convertidos para nulo.
- V5 normaliza situação/acesso e V6 normaliza água/energia, registrando as correções em `infraestrutura_escola_aud`. As tabelas temporárias dessas migrações não fazem parte do modelo persistente.
- As versões dos dados anuais estão no [ER de auditoria anual](22-er-auditoria-dados-anuais.md).

## Fontes no projeto

- [V1__esquema_inicial.sql](../../sigei-backend/src/main/resources/db/migration/V1__esquema_inicial.sql)
- [V5__padroniza_situacao_e_acesso.sql](../../sigei-backend/src/main/resources/db/migration/V5__padroniza_situacao_e_acesso.sql)
- [V6__padroniza_agua_e_energia.sql](../../sigei-backend/src/main/resources/db/migration/V6__padroniza_agua_e_energia.sql)
- [InfraestruturaEscola.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/models/InfraestruturaEscola.java)
- [SituacaoEscola.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/models/enums/SituacaoEscola.java)
- [TipoAcesso.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/models/enums/TipoAcesso.java)
- [TipoAgua.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/models/enums/TipoAgua.java)
- [TipoEnergia.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/models/enums/TipoEnergia.java)
- [OpcoesInfraestrutura.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/utils/OpcoesInfraestrutura.java)
