-- Unifica o vínculo entre escola-sede e anexo em escola.escola_sede_id.
-- A tabela anexo era uma segunda representação do mesmo vínculo, sem sincronização
-- com escola_sede_id; ela é removida.
--
-- Antes da remoção, os vínculos da tabela anexo são copiados para escola_sede_id
-- quando são válidos e não ambíguos: o anexo é do tipo ANEXO, ainda não tem sede,
-- aparece em um único vínculo e aponta para outra escola do tipo SEDE. Vínculos
-- fora dessas condições são descartados.

update escola e
set escola_sede_id = a.escola_id
from anexo a
join escola sede on sede.id = a.escola_id
where a.escola_anexo_id = e.id
  and e.escola_sede_id is null
  and upper(e.tipo_unidade) = 'ANEXO'
  and upper(sede.tipo_unidade) = 'SEDE'
  and sede.id <> e.id
  and (select count(*) from anexo a2 where a2.escola_anexo_id = e.id) = 1;

drop table anexo;
