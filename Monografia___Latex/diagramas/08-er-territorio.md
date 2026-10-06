# Modelo relacional - território e escolas

Estrutura territorial e vínculo entre escolas-sede e anexos, com todas as colunas das cinco tabelas.

Atualizado em **06/10/2026**, considerando o estado resultante das migrações **V1 a V6** e os mapeamentos atuais das entidades.

```plantuml
@startuml 08_er_territorio
title Modelo relacional - território e escolas
skinparam backgroundColor white
skinparam shadowing false
skinparam defaultFontName Arial
skinparam defaultFontSize 12
skinparam classAttributeIconSize 0
skinparam monochrome true
hide methods
entity municipio {
  {field} * id : bigint <<PK>>
  {field} nome : varchar(255)
}
entity terra_indigena {
  {field} * id : bigint <<PK>>
  {field} municipio_id : bigint <<FK>>
  {field} nome : varchar(255)
}
entity povo {
  {field} * id : bigint <<PK>>
  {field} nome : varchar(255)
}
entity aldeia {
  {field} * id : bigint <<PK>>
  {field} * povo_id : bigint <<FK>>
  {field} * terra_indigena_id : bigint <<FK>>
  {field} nome : varchar(255)
}
entity escola {
  {field} * id : bigint <<PK>>
  {field} aldeia_id : bigint unique <<FK>>
  {field} escola_sede_id : bigint <<FK>>
  {field} codigo : varchar(255)
  {field} nome : varchar(255)
  {field} tipo_unidade : varchar(255)
}
municipio |o--o{ terra_indigena : municipio_id
povo ||--o{ aldeia : povo_id
terra_indigena ||--o{ aldeia : terra_indigena_id
aldeia |o--o| escola : aldeia_id
escola |o--o{ escola : escola_sede_id
note bottom of escola
  escola_sede_id: vínculo único de representação sede/anexo.
  tipo_unidade: SEDE ou ANEXO na aplicação.
  A tabela anexo foi removida pela migração V3.
end note
legend bottom
  Asterisco (*) = NOT NULL | PK = chave primária | FK = chave estrangeira física
  Círculo = zero; barra = um; ramificação = muitos
endlegend
@enduml
```

## Leitura e limites

**Convenções:** `*` indica coluna obrigatória (NOT NULL, incluindo a chave primária); ausência de `*` indica coluna anulável. `PK` = chave primária; `FK` = chave estrangeira física; `unique`/`UNIQUE` = unicidade. No pé de galinha, círculo = zero, barra = um e ramificação = muitos. Toda FK referencia a coluna `id` da tabela na outra extremidade. Campos de auditoria que apenas copiam identificadores não são marcados como FK.

- A migração V3 removeu a tabela `anexo`. O vínculo agora é representado somente por `escola.escola_sede_id`, que referencia outra linha de `escola`.
- Uma escola pode ter zero ou uma sede; a mesma sede pode ser referenciada por várias escolas. As regras sobre os tipos SEDE/ANEXO são verificadas pela aplicação; a FK, isoladamente, não impõe esses tipos.
- `tipo_unidade` continua `varchar(255)`, sem enum nativo ou CHECK de valores definido nas migrações.
- `escola.aldeia_id` é UNIQUE e anulável: cada escola se vincula a no máximo uma aldeia, e uma aldeia pode ter no máximo uma escola nesse esquema.
- As cinco tabelas são auditadas. Suas versões estão no [ER de auditoria territorial](21-er-auditoria-territorio.md).

## Fontes no projeto

- [V1__esquema_inicial.sql](../../sigei-backend/src/main/resources/db/migration/V1__esquema_inicial.sql)
- [V3__unifica_vinculo_sede_anexo.sql](../../sigei-backend/src/main/resources/db/migration/V3__unifica_vinculo_sede_anexo.sql)
- [Escola.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/models/Escola.java)
- [Aldeia.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/models/Aldeia.java)
- [VinculoSedeService.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/service/VinculoSedeService.java)
