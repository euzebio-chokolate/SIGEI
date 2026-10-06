# Banco de dados — SIGEI

Documentação das 13 entidades persistidas da API, com dicionário de dados e diagrama de relacionamentos. Fonte: [entidades JPA](../src/main/java/ac/gov/br/sigei_backend/models) e [configuração da aplicação](../src/main/resources/application.properties).

Este documento descreve o modelo definido no código, sem consultar uma instância do banco. Nomes e tipos SQL abaixo são os esperados para PostgreSQL com a configuração atual. O esquema é criado e evoluído pelas migrações do Flyway em [`src/main/resources/db/migration`](../src/main/resources/db/migration), e o Hibernate apenas o valida (`ddl-auto=validate`). Bancos criados antes do Flyway pelo antigo `ddl-auto=update` podem conter colunas ou objetos antigos que não constam nas entidades. Este documento não substitui as migrações nem é uma auditoria do banco implantado.

## Convenções

- Tabelas e colunas seguem `snake_case`; por exemplo, `RegistroAnual` → `registro_anual` e `totalAlunos` → `total_alunos`. Não há estratégia de nomes personalizada na configuração atual.
- Todas as tabelas possuem `id`: chave primária `bigint`, não nula, gerada por `GenerationType.IDENTITY`. Esse campo está implícito em cada tabela do dicionário abaixo.
- `Long` → `bigint`, `Integer` → `integer`, `Double` → `double precision`, `Boolean`/`boolean` → `boolean`, `String` → `varchar(255)` na ausência de configuração específica.
- **PK**: chave primária; **FK**: chave estrangeira; **UK**: unicidade.
- “Aceita nulo” descreve o mapeamento de persistência, não as validações do DTO ou do service. Nos booleanos primitivos de equipe, não há `nullable=false` explícito; a coluna física deve ser conferida no DDL, e o objeto Java não representa `null`.
- Coleções com `mappedBy` são o lado inverso da associação: não criam coluna de lista ou tabela intermediária.

## Diagrama de relacionamentos

O [modelo ER completo gerado automaticamente](MODELO_ER.md) inclui todas as colunas e é atualizado com `scripts/gerar-modelo-er.ps1`. O diagrama abaixo é uma visão resumida mantida manualmente.

O diagrama usa Mermaid, com apenas as chaves para manter a leitura simples. Os demais campos estão no dicionário. `||` significa exatamente um, `o|` zero ou um e `o{` zero ou muitos. As cardinalidades refletem a nulabilidade e os relacionamentos JPA atuais, inclusive vínculos opcionais.

```mermaid
erDiagram
    municipio o|..o{ terra_indigena : municipio_id
    terra_indigena ||..o{ aldeia : terra_indigena_id
    povo ||..o{ aldeia : povo_id
    aldeia o|..o| escola : aldeia_id
    escola o|..o{ escola : escola_sede_id
    escola o|..o{ registro_anual : escola_id
    ano_referencia o|..o{ registro_anual : ano_referencia_id
    registro_anual o|..o| equipe_escola : registro_anual_id
    ano_referencia o|..o{ equipe_escola : ano_referencia_id
    registro_anual o|..o| infraestrutura_escola : registro_anual_id
    registro_anual o|..o{ matricula_nivel : registro_id
    registro_anual o|..o{ matricula_serie : registro_id

    municipio {
        bigint id PK
    }
    terra_indigena {
        bigint id PK
        bigint municipio_id FK
    }
    povo {
        bigint id PK
    }
    aldeia {
        bigint id PK
        bigint terra_indigena_id FK
        bigint povo_id FK
    }
    escola {
        bigint id PK
        bigint aldeia_id FK,UK
        bigint escola_sede_id FK
    }
    ano_referencia {
        bigint id PK
        integer ano UK
    }
    registro_anual {
        bigint id PK
        bigint escola_id FK
        bigint ano_referencia_id FK
    }
    equipe_escola {
        bigint id PK
        bigint registro_anual_id FK,UK
        bigint ano_referencia_id FK
    }
    infraestrutura_escola {
        bigint id PK
        bigint registro_anual_id FK,UK
    }
    matricula_nivel {
        bigint id PK
        bigint registro_id FK
    }
    matricula_serie {
        bigint id PK
        bigint registro_id FK
    }
    usuario {
        bigint id PK
        varchar email UK
    }
    refresh_token {
        bigint id PK
        bigint usuario_id FK
        varchar token_hash UK
    }
    usuario ||--o{ refresh_token : "possui"
```

As UKs de `aldeia_id` e `registro_anual_id` representam a unicidade esperada dos mapeamentos `@OneToOne`. A unicidade composta de `registro_anual` está detalhada adiante. `usuario` não possui FK para as tabelas do domínio; apenas `refresh_token` referencia `usuario`.

## Dicionário de dados

Todas as tabelas abaixo também possuem a coluna `id` descrita nas convenções.

### `municipio`

Entidade [Municipio.java](../src/main/java/ac/gov/br/sigei_backend/models/Municipio.java). Cadastro de municípios; o inicializador inclui os municípios ausentes do Acre.

| Coluna | Tipo SQL esperado | Aceita nulo | Descrição |
| --- | --- | --- | --- |
| `nome` | `varchar(255)` | Sim | Nome do município. |

### `terra_indigena`

Entidade [TerraIndigena.java](../src/main/java/ac/gov/br/sigei_backend/models/TerraIndigena.java). Cadastro de terras indígenas, vinculadas opcionalmente a um município no mapeamento.

| Coluna | Tipo SQL esperado | Aceita nulo | Descrição |
| --- | --- | --- | --- |
| `nome` | `varchar(255)` | Sim | Nome da terra indígena. |
| `municipio_id` | `bigint` | Sim | FK → `municipio.id`; muitas terras podem referenciar o mesmo município. |

### `povo`

Entidade [Povo.java](../src/main/java/ac/gov/br/sigei_backend/models/Povo.java). Cadastro de povos indígenas.

| Coluna | Tipo SQL esperado | Aceita nulo | Descrição |
| --- | --- | --- | --- |
| `nome` | `varchar(255)` | Sim | Nome do povo. |

`Povo.aldeias` é uma coleção inversa; o vínculo é armazenado em `aldeia.povo_id`.

### `aldeia`

Entidade [Aldeia.java](../src/main/java/ac/gov/br/sigei_backend/models/Aldeia.java). Aldeia vinculada obrigatoriamente a uma terra indígena e um povo.

| Coluna | Tipo SQL esperado | Aceita nulo | Descrição |
| --- | --- | --- | --- |
| `nome` | `varchar(255)` | Sim | Nome da aldeia. |
| `terra_indigena_id` | `bigint` | Não | FK → `terra_indigena.id`. |
| `povo_id` | `bigint` | Não | FK → `povo.id`. |

### `escola`

Entidade [Escola.java](../src/main/java/ac/gov/br/sigei_backend/models/Escola.java). Identidade da unidade escolar, separada dos dados que variam por ano.

| Coluna | Tipo SQL esperado | Aceita nulo | Descrição |
| --- | --- | --- | --- |
| `codigo` | `varchar(255)` | Sim | Código da escola; sem UK declarada na entidade. |
| `nome` | `varchar(255)` | Sim | Nome da escola. |
| `tipo_unidade` | `varchar(255)` | Sim | `SEDE` ou `ANEXO`; o campo é `String`, não enum persistido. Os cadastros pela API validam e gravam o valor em maiúsculas. |
| `escola_sede_id` | `bigint` | Sim | FK → `escola.id`; autorrelacionamento muitos-para-um. É a única representação do vínculo entre anexo e escola-sede. |
| `aldeia_id` | `bigint` | Sim | FK → `aldeia.id`; associação `@OneToOne`, com unicidade esperada. |

O modelo atual permite no máximo uma escola por aldeia por causa de `@OneToOne`; não documentar esse vínculo como muitos-para-um sem alterar o mapeamento. `Escola.registros` é a coleção inversa de `registro_anual.escola_id`.

As regras do vínculo com a sede ficam em [VinculoSedeService.java](../src/main/java/ac/gov/br/sigei_backend/service/VinculoSedeService.java) e valem para os cadastros simples e completo: um `ANEXO` exige uma sede do tipo `SEDE`, diferente de si mesmo; uma `SEDE` não tem sede; e uma escola com anexos vinculados não pode passar a `ANEXO`. Com isso, o vínculo tem um único nível e não forma ciclos. Essas regras não são constraints do banco. A importação de Excel grava o vínculo diretamente: o anexo recebe a escola `SEDE` de mesmo código.

Até a migração `V3__unifica_vinculo_sede_anexo.sql`, havia também a tabela `anexo` (`escola_id`, `escola_anexo_id`), uma segunda representação do mesmo vínculo, sem sincronização com `escola_sede_id`. A V3 copiou para `escola_sede_id` os vínculos válidos e não ambíguos dessa tabela, descartou os demais e removeu a tabela.

### `ano_referencia`

Entidade [AnoReferencia.java](../src/main/java/ac/gov/br/sigei_backend/models/AnoReferencia.java). Anos usados no histórico escolar.

| Coluna | Tipo SQL esperado | Aceita nulo | Descrição |
| --- | --- | --- | --- |
| `ano` | `integer` | Não | Ano civil, como `2025`; UK. Não confundir com `id`. |

### `registro_anual`

Entidade [RegistroAnual.java](../src/main/java/ac/gov/br/sigei_backend/models/RegistroAnual.java). Centro dos dados históricos de uma escola em um ano.

| Coluna | Tipo SQL esperado | Aceita nulo | Descrição |
| --- | --- | --- | --- |
| `escola_id` | `bigint` | Sim | FK → `escola.id`. |
| `ano_referencia_id` | `bigint` | Sim | FK → `ano_referencia.id`. |
| `rede` | `varchar(255)` | Sim | Rede de ensino. |
| `local_funcionamento` | `varchar(255)` | Sim | Local de funcionamento da unidade naquele ano. |
| `total_alunos` | `integer` | Sim | Total de alunos armazenado no registro. |

UK composta: `(escola_id, ano_referencia_id)`. O par identifica um único registro quando os dois valores estão preenchidos. Os atributos Java `equipe`, `infraestrutura`, `matriculasNivel` e `matriculasSerie` são associações inversas, sem colunas correspondentes nesta tabela.

### `equipe_escola`

Entidade [EquipeEscola.java](../src/main/java/ac/gov/br/sigei_backend/models/EquipeEscola.java). Disponibilidade de profissionais e necessidade de transporte.

| Coluna | Tipo SQL esperado | Aceita nulo | Descrição |
| --- | --- | --- | --- |
| `registro_anual_id` | `bigint` | Sim | FK → `registro_anual.id`; `@OneToOne`, unicidade esperada. |
| `ano_referencia_id` | `bigint` | Sim | FK → `ano_referencia.id`; vínculo adicional muitos-para-um. |
| `tem_professor_responsavel` | `boolean` | Ver nota | Disponibilidade de professor responsável. |
| `tem_equipe_gestora` | `boolean` | Ver nota | Disponibilidade de equipe gestora. |
| `tem_merendeira` | `boolean` | Ver nota | Disponibilidade de merendeira. |
| `tem_zeladora` | `boolean` | Ver nota | Disponibilidade de zeladora. |
| `tem_barqueiro` | `boolean` | Ver nota | Disponibilidade de barqueiro. |
| `necessita_transporte` | `boolean` | Ver nota | Necessidade de transporte. |

Nota: os seis indicadores são `boolean` primitivos, inicializados em `false` pelo Java, sem `@Column(nullable=false)` ou `@ColumnDefault` explícitos. Não confundir esse valor inicial com um default SQL declarado.

O ano também pode ser obtido por `registro_anual.ano_referencia_id`. Não há restrição JPA declarada que obrigue esse ano e `equipe_escola.ano_referencia_id` a coincidirem.

### `infraestrutura_escola`

Entidade [InfraestruturaEscola.java](../src/main/java/ac/gov/br/sigei_backend/models/InfraestruturaEscola.java). Situação física, serviços, localização e fotografia do registro anual.

| Coluna | Tipo SQL esperado | Aceita nulo | Descrição |
| --- | --- | --- | --- |
| `registro_anual_id` | `bigint` | Sim | FK → `registro_anual.id`; `@OneToOne`, unicidade esperada. |
| `ano_construcao` | `integer` | Sim | Ano de construção. |
| `num_salas` | `integer` | Sim | Número de salas. |
| `latitude` | `double precision` | Sim | Latitude. |
| `longitude` | `double precision` | Sim | Longitude. |
| `situacao` | `varchar(255)` | Sim | `FUNCIONANDO`, `PARALISADA` ou `DESATIVADA` (enum `SituacaoEscola`); nulo = não informada. A restrição é aplicada pela aplicação, não por constraint do banco. |
| `tipo_acesso` | `varchar(255)` | Sim | `TERRESTRE`, `FLUVIAL` ou `MISTO` (enum `TipoAcesso`); nulo = não informado. A restrição é aplicada pela aplicação, não por constraint do banco. |
| `tem_predio` | `boolean` | Sim | Existência de prédio. |
| `tipo_agua` | `varchar(255)` | Sim | Forma de abastecimento de água. |
| `tem_internet` | `boolean` | Sim | Disponibilidade de internet. |
| `tipo_energia` | `varchar(255)` | Sim | Fonte/tipo de energia. |
| `tem_registro_fotografico` | `boolean` | Sim | Existência de registro fotográfico. |
| `foto_url` | `varchar(255)` | Sim | URL da imagem armazenada externamente. |
| `foto_public_id` | `varchar(255)` | Sim | Identificador da imagem no Cloudinary. |

O arquivo de imagem não é persistido como BLOB. Os indicadores `Boolean` permitem distinguir `null` de `false`.

### `matricula_nivel`

Entidade [MatriculaNivel.java](../src/main/java/ac/gov/br/sigei_backend/models/MatriculaNivel.java). Quantidades de matrículas por nível de ensino.

| Coluna | Tipo SQL esperado | Aceita nulo | Descrição |
| --- | --- | --- | --- |
| `registro_id` | `bigint` | Sim | FK → `registro_anual.id`. |
| `nivel` | `varchar(255)` | Sim | Nível de ensino. |
| `qtd_masculino` | `integer` | Sim | Quantidade masculina. |
| `qtd_feminino` | `integer` | Sim | Quantidade feminina. |
| `total` | `integer` | Sim | Total armazenado. |

### `matricula_serie`

Entidade [MatriculaSerie.java](../src/main/java/ac/gov/br/sigei_backend/models/MatriculaSerie.java). Quantidades de matrículas por série/etapa.

| Coluna | Tipo SQL esperado | Aceita nulo | Descrição |
| --- | --- | --- | --- |
| `registro_id` | `bigint` | Sim | FK → `registro_anual.id`. |
| `denominacao` | `varchar(255)` | Sim | Denominação da série. |
| `etapa_ensino` | `varchar(255)` | Sim | Etapa de ensino. |
| `qtd_masculino` | `integer` | Sim | Quantidade masculina. |
| `qtd_feminino` | `integer` | Sim | Quantidade feminina. |
| `total` | `integer` | Sim | Total armazenado. |

Nas duas tabelas de matrícula, não há UK composta por registro e nível/série declarada nas entidades. Os totais são campos persistidos comuns, sem expressão SQL gerada ou restrição declarada que imponha a soma das quantidades. Também não há `CHECK` de valores não negativos declarado nesses campos.

### `usuario`

Entidade [Usuario.java](../src/main/java/ac/gov/br/sigei_backend/models/Usuario.java). Contas de acesso à API, sem relacionamento JPA com escolas ou municípios.

| Coluna | Tipo SQL esperado | Aceita nulo | Descrição |
| --- | --- | --- | --- |
| `nome` | `varchar(255)` | Não | Nome do usuário. |
| `email` | `varchar(255)` | Não | E-mail de autenticação; UK. |
| `senha` | `varchar(255)` | Não | Hash BCrypt; não é senha em texto puro. |
| `perfil` | Representação textual de enum | Não | `@Enumerated(EnumType.STRING)`: `ADMIN` ou `USUARIO`; tipo/constraint SQL final depende do Hibernate/dialeto. |
| `ativo` | `boolean` | Não | Inicializado em `true` no Java, sem default SQL explícito. |
| `alterar_senha` | `boolean` | Não | Inicializado em `true` no Java e com `@ColumnDefault("true")`. |

Não há tabela separada para os perfis no modelo atual.

### `refresh_token`

Entidade [RefreshToken.java](../src/main/java/ac/gov/br/sigei_backend/models/RefreshToken.java). Sessões renováveis emitidas no login e em `POST /auth/refresh`. O token JWT de acesso não é persistido.

| Coluna | Tipo SQL esperado | Aceita nulo | Descrição |
| --- | --- | --- | --- |
| `usuario_id` | `bigint` | Não | FK → `usuario.id`, com `ON DELETE CASCADE` (`@OnDelete`). |
| `token_hash` | `varchar(64)` | Não | Hash SHA-256 (hexadecimal) do token entregue ao cliente; UK. O valor original não é gravado. |
| `criado_em` | `timestamp` | Não | Momento da emissão. |
| `expira_em` | `timestamp` | Não | Fim da validade (`jwt.refresh-expiration`). |
| `revogado` | `boolean` | Não | `true` após uso (rotação), logout, troca de senha ou detecção de reutilização. |
| `revogado_em` | `timestamp` | Sim | Momento da revogação; distingue concorrência entre abas (até 30 s) de reutilização suspeita. |

Tokens revogados são mantidos para detectar reutilização; os expirados de um usuário são apagados quando ele recebe um novo token.

## Trilha de auditoria

Criada pela migração `V4__trilha_auditoria.sql` e mantida pelo Hibernate Envers. Todas as entidades acima, exceto `refresh_token`, têm `@Audited`. Cada transação que cria, altera ou exclui uma entidade auditada gera uma linha em `revisao`. Os valores da versão vão para a tabela `<entidade>_aud` correspondente. Essas tabelas não aparecem no diagrama.

### `revisao`

Entidade [Revisao.java](../src/main/java/ac/gov/br/sigei_backend/models/Revisao.java); o autor é preenchido por [RevisaoListener.java](../src/main/java/ac/gov/br/sigei_backend/config/auditoria/RevisaoListener.java).

| Coluna | Tipo SQL esperado | Aceita nulo | Descrição |
| --- | --- | --- | --- |
| `id` | `bigint` identity | Não | PK; número da revisão, crescente. |
| `data_hora` | `timestamp with time zone` | Não | Momento da transação. |
| `usuario_id` | `bigint` | Sim | ID do usuário autenticado, sem FK, para que a trilha resista à exclusão do usuário. Nulo indica o sistema (inicializadores, carga de planilhas na subida, migração). |
| `usuario_email`, `usuario_nome` | `varchar(255)` | Sim | Cópia do e-mail e do nome no momento da alteração. |

### Tabelas `<entidade>_aud`

`municipio_aud`, `terra_indigena_aud`, `povo_aud`, `aldeia_aud`, `escola_aud`, `ano_referencia_aud`, `registro_anual_aud`, `equipe_escola_aud`, `infraestrutura_escola_aud`, `matricula_nivel_aud`, `matricula_serie_aud` e `usuario_aud`. Cada uma repete as colunas da tabela de origem, sem constraints, e acrescenta:

| Coluna | Tipo SQL esperado | Aceita nulo | Descrição |
| --- | --- | --- | --- |
| `rev` | `bigint` | Não | FK → `revisao.id`; PK composta `(id, rev)`. |
| `revtype` | `smallint` | Sim | `0` criação, `1` alteração, `2` exclusão. |

- `usuario_aud` não guarda `senha` (`@NotAudited`).
- Exclusões guardam os últimos valores (`store_data_at_delete`). Assim, o histórico mostra o que foi removido, inclusive por cascata.
- Mudanças em coleções não geram revisão do lado pai (`revision_on_collection_change=false`); cada filho registra a sua própria.
- A V4 criou uma revisão inicial atribuída ao sistema, com o estado de todos os dados existentes. Autoria e datas anteriores à migração não são conhecidas.
- Os índices em `registro_anual_aud.escola_id`, `equipe_escola_aud.registro_anual_id`, `infraestrutura_escola_aud.registro_anual_id`, `matricula_nivel_aud.registro_id` e `matricula_serie_aud.registro_id` atendem ao histórico por escola.

As tabelas de auditoria só crescem: nenhuma rotina as limpa. Alterações feitas por SQL direto, fora do Hibernate, não são registradas.

## Cascatas e exclusões

| Associação no Java | Configuração | Efeito no ciclo de vida JPA |
| --- | --- | --- |
| `Escola.registros` | `CascadeType.ALL`, `orphanRemoval=true` | Operações são propagadas aos registros anuais; remover um registro da coleção pode excluí-lo. |
| `RegistroAnual.equipe` | `CascadeType.ALL`, `orphanRemoval=true` | Equipe acompanha o ciclo de vida do registro; desvinculá-la pode removê-la. |
| `RegistroAnual.infraestrutura` | `CascadeType.ALL`, `orphanRemoval=true` | Infraestrutura acompanha o ciclo de vida do registro. |
| `RegistroAnual.matriculasNivel` | `CascadeType.ALL`, `orphanRemoval=true` | Propagação às matrículas por nível e exclusão de órfãos. |
| `RegistroAnual.matriculasSerie` | `CascadeType.ALL`, `orphanRemoval=true` | Propagação às matrículas por série e exclusão de órfãos. |

As demais associações não declaram cascata de remoção. Por exemplo, excluir um povo não implica excluir suas aldeias automaticamente. Referências existentes podem impedir exclusões.

Essas cascatas são do ORM: não equivalem a `ON DELETE CASCADE` no PostgreSQL. A única exceção é `refresh_token.usuario_id`, que declara `@OnDelete(CASCADE)`: excluir um usuário remove seus tokens no próprio banco. Exclusões por SQL direto ou operações em lote podem ter comportamento diferente das operações de entidade. A remoção de uma linha de infraestrutura também não descreve, por si só, a remoção do arquivo no Cloudinary.

## Como manter esta documentação

Ao alterar uma entidade, revise sua tabela, campos, nulabilidade, FK, UK, cardinalidade e cascatas neste documento. Em entidades auditadas, a migração também precisa alterar a tabela `<entidade>_aud`. A inclusão de um campo em um DTO não implica automaticamente uma nova coluna.

Para conferir uma instância real, use o catálogo do banco ou a ferramenta de banco da IDE. Exemplo de consultas somente de leitura no PostgreSQL, substituindo `public` caso outro schema seja usado:

```sql
-- Colunas, tipos físicos, nulabilidade, defaults e identidade.
SELECT table_name, column_name, data_type, udt_name,
       character_maximum_length, is_nullable, column_default, is_identity
FROM information_schema.columns
WHERE table_schema = 'public'
ORDER BY table_name, ordinal_position;

-- Chaves e restrições efetivamente presentes, inclusive ações das FKs.
SELECT t.relname AS tabela, c.conname AS restricao,
       c.contype AS tipo, pg_get_constraintdef(c.oid) AS definicao
FROM pg_constraint c
JOIN pg_class t ON t.oid = c.conrelid
JOIN pg_namespace n ON n.oid = t.relnamespace
WHERE n.nspname = 'public'
ORDER BY t.relname, c.conname;
```

Essas consultas não foram executadas para produzir este documento. Não foram adicionadas migrações, alteradas entidades ou criados endpoints de documentação.
