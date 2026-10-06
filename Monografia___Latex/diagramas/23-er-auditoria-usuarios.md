# Modelo relacional - auditoria de usuários e autoria

Dados versionados de usuários e identificação de quem realizou cada revisão. O autor da revisão e o usuário que sofreu a alteração podem ser pessoas diferentes.

Atualizado em **06/10/2026**, considerando o estado resultante das migrações **V1 a V6** e os mapeamentos atuais das entidades.

```plantuml
@startuml 23_er_auditoria_usuarios
title Modelo relacional - auditoria de usuários e autoria
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
entity usuario_aud {
  {field} * id : bigint <<PK>>
  {field} * rev : bigint <<PK, FK>>
  {field} alterar_senha : boolean
  {field} ativo : boolean
  {field} revtype : smallint
  {field} email : varchar(255)
  {field} nome : varchar(255)
  {field} perfil : varchar(255)
  --
  {field} PK (id, rev)
  {field} CHECK (perfil in ('ADMIN','USUARIO'))
}
revisao ||--o{ usuario_aud : rev
note right of revisao
  usuario_id / email / nome: autor da operação.
  São cópias, sem vínculo FK com o cadastro atual.
  Autor nulo: sistema.
end note
note right of usuario_aud
  id: usuário que foi alterado.
  rev: revisão da alteração (FK).
  revtype: 0 criação, 1 alteração, 2 exclusão.
  Não contém senha.
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
- `usuario_aud.id` identifica o usuário alterado; `revisao.usuario_id` identifica o autor da transação. Não devem ser confundidos.
- `usuario_aud` guarda nome, e-mail, perfil, ativo e alterar_senha, mas **não guarda senha**. `perfil` mantém o CHECK ADMIN/USUARIO, embora a coluna auditada seja anulável.
- Não existe FK de `usuario_aud.id` para `usuario.id`, nem FK de autoria para `usuario`. Alterar ou excluir o cadastro atual não exige alterar as cópias históricas.
- Os tokens de renovação não são auditados.

## Fontes no projeto

- [V4__trilha_auditoria.sql](../../sigei-backend/src/main/resources/db/migration/V4__trilha_auditoria.sql)
- [Revisao.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/models/Revisao.java)
- [RevisaoListener.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/config/auditoria/RevisaoListener.java)
- [application.properties](../../sigei-backend/src/main/resources/application.properties)
- [Usuario.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/models/Usuario.java)
- [RefreshToken.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/models/RefreshToken.java)
