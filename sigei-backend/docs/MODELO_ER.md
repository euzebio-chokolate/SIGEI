# Modelo ER gerado das entidades JPA

Gerado por `scripts/gerar-modelo-er.ps1`. Nao editar manualmente este arquivo.

Modelo esperado a partir do codigo, sem consultar o banco. Tipos SQL simplificados; `double` representa `double precision`. PK = chave primaria, FK = chave estrangeira, UK = unicidade. As UKs de associacoes um-para-um sao inferidas do mapeamento JPA.

Cardinalidades: `||` exatamente um; `o|` zero ou um; `o{` zero ou muitos. Campos marcados NOT NULL possuem restricao explicita ou sao PK. Demais campos nao recebem garantia de nulabilidade neste diagrama.

```mermaid
erDiagram
    aldeia {
        bigint id PK "NOT NULL"
        varchar nome
        bigint terra_indigena_id FK "NOT NULL"
        bigint povo_id FK "NOT NULL"
    }
    ano_referencia {
        bigint id PK "NOT NULL"
        integer ano UK "NOT NULL"
    }
    equipe_escola {
        bigint id PK "NOT NULL"
        bigint registro_anual_id FK,UK
        bigint ano_referencia_id FK
        boolean tem_professor_responsavel
        boolean tem_equipe_gestora
        boolean tem_merendeira
        boolean tem_zeladora
        boolean tem_barqueiro
        boolean necessita_transporte
    }
    escola {
        bigint id PK "NOT NULL"
        varchar codigo
        varchar nome
        varchar tipo_unidade
        bigint escola_sede_id FK
        bigint aldeia_id FK
    }
    infraestrutura_escola {
        bigint id PK "NOT NULL"
        bigint registro_anual_id FK,UK
        integer ano_construcao
        integer num_salas
        double latitude
        double longitude
        varchar situacao
        varchar tipo_acesso
        boolean tem_predio
        varchar tipo_agua
        boolean tem_internet
        varchar tipo_energia
        boolean tem_registro_fotografico
        varchar foto_url
        varchar foto_public_id
    }
    lotacao_professor {
        bigint id PK "NOT NULL"
        bigint professor_id FK
        bigint ano_referencia_id FK "NOT NULL"
        bigint escola_id FK
        bigint municipio_id FK "NOT NULL"
        varchar cargo
        varchar segmento
        varchar numero_contrato "cifrado"
        date inicio_contrato
        varchar amparo_legal
        varchar tipo_contrato
    }
    matricula_nivel {
        bigint id PK "NOT NULL"
        bigint registro_id FK
        varchar nivel
        integer qtd_masculino
        integer qtd_feminino
        integer total
    }
    matricula_serie {
        bigint id PK "NOT NULL"
        bigint registro_id FK
        varchar denominacao
        varchar etapa_ensino
        integer qtd_masculino
        integer qtd_feminino
        integer total
    }
    municipio {
        bigint id PK "NOT NULL"
        varchar nome
    }
    povo {
        bigint id PK "NOT NULL"
        varchar nome
    }
    professor {
        bigint id PK "NOT NULL"
        varchar nome "NOT NULL"
        varchar cpf "cifrado"
        varchar cpf_hash
        varchar matricula "cifrado"
        varchar etnia
        varchar sexo
        varchar formacao
    }
    refresh_token {
        bigint id PK "NOT NULL"
        bigint usuario_id FK "NOT NULL"
        varchar token_hash UK "NOT NULL"
        timestamp criado_em "NOT NULL"
        timestamp expira_em "NOT NULL"
        boolean revogado "NOT NULL"
        timestamp revogado_em
    }
    registro_anual {
        bigint id PK "NOT NULL"
        bigint escola_id FK
        bigint ano_referencia_id FK
        varchar rede
        varchar local_funcionamento
        integer total_alunos
    }
    revisao {
        bigint id PK "NOT NULL"
        timestamp data_hora "NOT NULL"
        bigint usuario_id
        varchar usuario_email
        varchar usuario_nome
    }
    terra_indigena {
        bigint id PK "NOT NULL"
        varchar nome
        bigint municipio_id FK
    }
    usuario {
        bigint id PK "NOT NULL"
        varchar nome "NOT NULL"
        varchar email UK "NOT NULL"
        varchar senha "NOT NULL"
        varchar perfil "NOT NULL"
        boolean ativo "NOT NULL"
        boolean alterar_senha "NOT NULL"
    }
    terra_indigena ||..o{ aldeia : terra_indigena_id
    povo ||..o{ aldeia : povo_id
    registro_anual o|..o| equipe_escola : registro_anual_id
    ano_referencia o|..o{ equipe_escola : ano_referencia_id
    escola o|..o{ escola : escola_sede_id
    aldeia o|..o{ escola : aldeia_id
    registro_anual o|..o| infraestrutura_escola : registro_anual_id
    professor o|..o{ lotacao_professor : professor_id
    ano_referencia ||..o{ lotacao_professor : ano_referencia_id
    escola o|..o{ lotacao_professor : escola_id
    municipio ||..o{ lotacao_professor : municipio_id
    registro_anual o|..o{ matricula_nivel : registro_id
    registro_anual o|..o{ matricula_serie : registro_id
    usuario ||..o{ refresh_token : usuario_id
    escola o|..o{ registro_anual : escola_id
    ano_referencia o|..o{ registro_anual : ano_referencia_id
    municipio o|..o{ terra_indigena : municipio_id
```

## Unicidades compostas

- `professor`: UNIQUE (cpf_hash).
- `registro_anual`: UNIQUE (escola_id, ano_referencia_id).

Descricoes e regras de exclusao: [dicionario do banco](BANCO_DE_DADOS.md).
