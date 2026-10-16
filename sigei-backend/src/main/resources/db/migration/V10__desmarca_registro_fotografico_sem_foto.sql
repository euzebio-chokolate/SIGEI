-- Desmarca infraestrutura_escola.tem_registro_fotografico quando não há foto anexada.
-- A importação gravava nesse campo a coluna RELATÓRIO FOTOGRÁFICO da planilha, mas no
-- sistema ele indica uma foto anexada: o formulário exige a foto quando o campo está
-- marcado, e as escolas importadas com SIM não podiam ser editadas. A importação deixou
-- de ler essa coluna; esta migração corrige os registros já gravados.
--
-- Alterações por SQL não passam pelo Envers: as linhas ajustadas são registradas em
-- uma revisão do sistema, para manter a trilha de auditoria completa.

create temporary table infraestrutura_sem_foto on commit drop as
select id
from infraestrutura_escola
where tem_registro_fotografico is true
  and (foto_url is null or trim(foto_url) = '');

update infraestrutura_escola
set tem_registro_fotografico = false
where id in (select id from infraestrutura_sem_foto);

insert into revisao (data_hora)
select now() where exists (select 1 from infraestrutura_sem_foto);

insert into infraestrutura_escola_aud (id, rev, revtype, ano_construcao, latitude, longitude, num_salas,
                                       tem_internet, tem_predio, tem_registro_fotografico,
                                       registro_anual_id, foto_public_id, foto_url, situacao,
                                       tipo_acesso, tipo_agua, tipo_energia)
select id, (select max(id) from revisao), 1, ano_construcao, latitude, longitude, num_salas,
       tem_internet, tem_predio, tem_registro_fotografico, registro_anual_id, foto_public_id,
       foto_url, situacao, tipo_acesso, tipo_agua, tipo_energia
from infraestrutura_escola
where id in (select id from infraestrutura_sem_foto);
