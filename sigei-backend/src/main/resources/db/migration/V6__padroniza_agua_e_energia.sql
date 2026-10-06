-- Padroniza infraestrutura_escola.tipo_agua e tipo_energia com as opções do sistema
-- (enums TipoAgua e TipoEnergia, as mesmas do formulário de escolas). A comparação
-- ignora acentos, maiúsculas e separadores ("POÇO" → POCO, "RIO/VERTENTE" →
-- RIO_VERTENTE, "NÃO" → NAO). Na energia, "REDE ELETRICA" vira REDE_PUBLICA e "SIM"
-- (tem energia, fonte não informada) vira OUTRO. Os demais valores viram nulo (não
-- informado), como o "DESATIVADA" das escolas paralisadas na carga de 2025.
--
-- Alterações por SQL não passam pelo Envers: as linhas ajustadas são registradas em
-- uma revisão do sistema, para manter a trilha de auditoria completa.

create temporary table infraestrutura_ajustada on commit drop as
with normalizada as (
    select id,
           regexp_replace(upper(translate(trim(tipo_agua),
                   'áàâãäéèêëíìîïóòôõöúùûüçÁÀÂÃÄÉÈÊËÍÌÎÏÓÒÔÕÖÚÙÛÜÇ',
                   'aaaaaeeeeiiiiooooouuuucAAAAAEEEEIIIIOOOOOUUUUC')), '[[:space:]/]+', '_', 'g') as agua,
           regexp_replace(upper(translate(trim(tipo_energia),
                   'áàâãäéèêëíìîïóòôõöúùûüçÁÀÂÃÄÉÈÊËÍÌÎÏÓÒÔÕÖÚÙÛÜÇ',
                   'aaaaaeeeeiiiiooooouuuucAAAAAEEEEIIIIOOOOOUUUUC')), '[[:space:]/]+', '_', 'g') as energia
    from infraestrutura_escola
)
select id,
       case when agua in ('REDE_PUBLICA', 'POCO', 'CACIMBA', 'RIO', 'IGARAPE', 'VERTENTE',
                          'RIO_VERTENTE', 'OUTRO', 'NAO')
            then agua end as tipo_agua,
       case when energia = 'REDE_ELETRICA' then 'REDE_PUBLICA'
            when energia = 'SIM' then 'OUTRO'
            when energia in ('REDE_PUBLICA', 'GERADOR', 'SOLAR', 'OUTRO', 'NAO') then energia
       end as tipo_energia
from normalizada;

delete from infraestrutura_ajustada a
using infraestrutura_escola i
where a.id = i.id
  and a.tipo_agua is not distinct from i.tipo_agua
  and a.tipo_energia is not distinct from i.tipo_energia;

update infraestrutura_escola i
set tipo_agua = a.tipo_agua,
    tipo_energia = a.tipo_energia
from infraestrutura_ajustada a
where a.id = i.id;

insert into revisao (data_hora)
select now() where exists (select 1 from infraestrutura_ajustada);

insert into infraestrutura_escola_aud (id, rev, revtype, ano_construcao, latitude, longitude, num_salas,
                                       tem_internet, tem_predio, tem_registro_fotografico,
                                       registro_anual_id, foto_public_id, foto_url, situacao,
                                       tipo_acesso, tipo_agua, tipo_energia)
select id, (select max(id) from revisao), 1, ano_construcao, latitude, longitude, num_salas,
       tem_internet, tem_predio, tem_registro_fotografico, registro_anual_id, foto_public_id,
       foto_url, situacao, tipo_acesso, tipo_agua, tipo_energia
from infraestrutura_escola
where id in (select id from infraestrutura_ajustada);
