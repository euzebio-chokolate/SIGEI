-- Padroniza infraestrutura_escola.situacao e tipo_acesso com as opções do sistema
-- (enums SituacaoEscola e TipoAcesso, as mesmas do formulário e do filtro do mapa):
-- valores válidos ficam em maiúsculas e sem espaços nas pontas; os demais viram nulo
-- (não informado). Na carga de 2025, as escolas paralisadas tinham "DESATIVADA" na
-- coluna de acesso, que não é um tipo de acesso.
--
-- Alterações por SQL não passam pelo Envers: as linhas ajustadas são registradas em
-- uma revisão do sistema, para manter a trilha de auditoria completa.

create temporary table infraestrutura_ajustada on commit drop as
select id,
       case when upper(trim(situacao)) in ('FUNCIONANDO', 'PARALISADA', 'DESATIVADA')
            then upper(trim(situacao)) end as situacao,
       case when upper(trim(tipo_acesso)) in ('TERRESTRE', 'FLUVIAL', 'MISTO')
            then upper(trim(tipo_acesso)) end as tipo_acesso
from infraestrutura_escola;

delete from infraestrutura_ajustada a
using infraestrutura_escola i
where a.id = i.id
  and a.situacao is not distinct from i.situacao
  and a.tipo_acesso is not distinct from i.tipo_acesso;

update infraestrutura_escola i
set situacao = a.situacao,
    tipo_acesso = a.tipo_acesso
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
