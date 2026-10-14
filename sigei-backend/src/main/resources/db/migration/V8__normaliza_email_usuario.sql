-- Normaliza usuario.email (sem espaços nas pontas e em minúsculas), como a aplicação
-- passa a gravar e a buscar (utils/EmailUtils). Sem isso, um usuário cadastrado como
-- "Joao@x.com" não conseguiria entrar, porque o login busca o e-mail normalizado.
--
-- E-mails que, normalizados, coincidem com o de outro usuário não são alterados (a
-- restrição unique impediria a migração e a subida da aplicação); um aviso informa
-- os IDs, para que um administrador resolva a duplicidade.
--
-- Alterações por SQL não passam pelo Envers: as linhas ajustadas são registradas em
-- uma revisão do sistema, para manter a trilha de auditoria completa.

create temporary table usuario_ajustado on commit drop as
select id, lower(trim(email)) as email
from usuario
where email <> lower(trim(email))
  and lower(trim(email)) in (
      select lower(trim(email)) from usuario group by lower(trim(email)) having count(*) = 1
  );

do $$
declare
    duplicados text;
begin
    select string_agg(id::text, ', ' order by id) into duplicados
    from usuario
    where lower(trim(email)) in (
        select lower(trim(email)) from usuario group by lower(trim(email)) having count(*) > 1
    );
    if duplicados is not null then
        raise warning 'Usuários com e-mail repetido, ignorando maiúsculas (não normalizados): %', duplicados;
    end if;
end
$$;

update usuario u
set email = a.email
from usuario_ajustado a
where a.id = u.id;

insert into revisao (data_hora)
select now() where exists (select 1 from usuario_ajustado);

insert into usuario_aud (id, rev, revtype, alterar_senha, ativo, email, nome, perfil)
select id, (select max(id) from revisao), 1, alterar_senha, ativo, email, nome, perfil
from usuario
where id in (select id from usuario_ajustado);
