-- Escola.aldeia passa de @OneToOne para @ManyToOne: uma aldeia pode ter mais de
-- uma escola. Na planilha de 2025, a aldeia Belo Monte (Feijó) tem duas
-- escolas-sede, e a segunda não era importada por causa da coluna única.
--
-- O nome da restrição depende de quem criou o esquema (V1 ou o Hibernate, nos
-- bancos anteriores ao Flyway), por isso ela é procurada pela coluna.
do $$
declare
    restricao text;
begin
    for restricao in
        select c.conname
        from pg_constraint c
        join pg_attribute a on a.attrelid = c.conrelid and a.attnum = any (c.conkey)
        where c.conrelid = 'escola'::regclass
          and c.contype = 'u'
          and array_length(c.conkey, 1) = 1
          and a.attname = 'aldeia_id'
    loop
        execute format('alter table escola drop constraint %I', restricao);
    end loop;
end $$;

-- A restrição única servia também de índice para a chave estrangeira
create index if not exists idx_escola_aldeia on escola (aldeia_id);
