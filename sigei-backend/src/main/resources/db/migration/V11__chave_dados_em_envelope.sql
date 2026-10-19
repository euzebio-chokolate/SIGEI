-- Cifra em envelope dos dados sensíveis (CPF, matrícula e número do contrato).
-- A chave de dados, que cifra os valores, fica aqui cifrada pela chave mestra
-- (SIGEI_CHAVE_DADOS_SENSIVEIS) e, opcionalmente, pela chave pública de
-- recuperação. Trocar a chave mestra passa a recifrar só este registro.
--
-- A tabela nasce vazia: na primeira inicialização, a aplicação grava o registro
-- usando como chave de dados a chave mestra atual, que é a que já cifra os dados
-- existentes; por isso nenhum valor precisa ser recifrado. Não grave nesta
-- tabela por SQL direto. Não é auditada.

create table chave_dados (
    id smallint not null,
    cifrada_mestra varchar(255) not null,
    cifrada_recuperacao varchar(1024),
    impressao_recuperacao varchar(64),
    criada_em timestamp(6) with time zone not null,
    atualizada_em timestamp(6) with time zone not null,
    primary key (id),
    constraint ck_chave_dados_registro_unico check (id = 1)
);
