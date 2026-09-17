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
    anexo {
        bigint id PK "NOT NULL"
        bigint escola_id FK
        bigint escola_anexo_id FK
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
        bigint aldeia_id FK,UK
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
    registro_anual {
        bigint id PK "NOT NULL"
        bigint escola_id FK
        bigint ano_referencia_id FK
        varchar rede
        varchar local_funcionamento
        integer total_alunos
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
    escola o|..o{ anexo : escola_id
    escola o|..o{ anexo : escola_anexo_id
    registro_anual o|..o| equipe_escola : registro_anual_id
    ano_referencia o|..o{ equipe_escola : ano_referencia_id
    escola o|..o{ escola : escola_sede_id
    aldeia o|..o| escola : aldeia_id
    registro_anual o|..o| infraestrutura_escola : registro_anual_id
    registro_anual o|..o{ matricula_nivel : registro_id
    registro_anual o|..o{ matricula_serie : registro_id
    escola o|..o{ registro_anual : escola_id
    ano_referencia o|..o{ registro_anual : ano_referencia_id
    municipio o|..o{ terra_indigena : municipio_id
```

## Unicidades compostas

- `anexo`: UNIQUE (escola_id, escola_anexo_id).
- `registro_anual`: UNIQUE (escola_id, ano_referencia_id).

Descricoes e regras de exclusao: [dicionario do banco](BANCO_DE_DADOS.md).
