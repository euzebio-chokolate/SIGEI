# Referências a pesquisar

Esta lista reúne os assuntos marcados com [REFERÊNCIA NECESSÁRIA] ou [DADO A CONFIRMAR] nos capítulos. Nenhuma referência foi inventada. Para cada item, encontre uma fonte verificável, registre-a no `refs.bib` e substitua o marcador no texto.

As sugestões de onde procurar indicam tipos de fonte, não obras específicas. Confirme a existência e o conteúdo de qualquer fonte antes de citá-la.

## Prioridade alta (dados factuais na introdução e no Capítulo 2)

| # | Assunto | Onde aparece | Onde procurar |
| --- | --- | --- | --- |
| 1 | Número de povos indígenas e de terras indígenas no Acre | Cap. 1 (1º e 2º parágrafos) e seção 2.2 | Censo Demográfico 2022 do IBGE (dados sobre a população indígena); FUNAI (terras indígenas por unidade da federação); Instituto Socioambiental (*Terras Indígenas no Brasil*). |
| 2 | Número de escolas indígenas no Acre e distribuição por rede | Seção 2.2 | Sinopse estatística ou microdados do Censo Escolar (INEP); documentos da SEE/AC. O pré-projeto cita "149 escolas e 19 anexos" sem fonte; não use esse número sem confirmá-lo. |
| 3 | Condições de acesso às aldeias e escolas indígenas no Acre (fluvial, terrestre) | Cap. 1 (2º parágrafo) | Documentos da SEE/AC, relatórios da FUNAI, estudos acadêmicos sobre a educação escolar indígena no Acre. |
| 4 | Denominação oficial vigente da secretaria (SEE/AC) | Cap. 1, siglas e todo o texto | Portal do Governo do Acre; legislação estadual de estrutura administrativa. O `tcc.tex` usa "Secretaria de Estado de Educação, Cultura e Esportes". |
| 5 | Estudos sobre a educação escolar indígena no Acre | Seção 2.2 | Teses e dissertações (BDTD), periódicos da UFAC, publicações da Comissão Pró-Índio do Acre sobre a formação de professores indígenas. |
| 6 | Censo Escolar: definição, periodicidade, responsabilidade do INEP e código da escola | Seção 2.3 | Portal do INEP (página do Censo Escolar e glossário). |
| 7 | Origem das planilhas usadas na carga inicial | Cap. 1, seções 2.2 e 3.2.1 | Informação a ser fornecida pelo autor: setor de origem, data e autorização de uso. Se forem documentos oficiais, cite-os como fonte. |

## Prioridade média (fundamentação teórica)

| # | Assunto | Onde aparece | Onde procurar |
| --- | --- | --- | --- |
| 8 | Características da escola indígena específica, diferenciada, intercultural e bilíngue | Seção 2.1 | Referencial Curricular Nacional para as Escolas Indígenas (MEC, 1998); Resolução CNE/CEB nº 3/1999; Parecer CNE/CEB nº 13/2012. O `refs.bib` já tem Grupioni (2006), que pode servir se tratar do tema; confira. |
| 9 | Artigos da Resolução CNE/CEB nº 5/2012 sobre organização e gestão da escola indígena | Seção 2.1 | Texto integral da Resolução no portal do MEC. |
| 10 | Sistemas de informação e governo digital na administração pública brasileira | Seção 2.4 | Artigos da *Revista do Serviço Público* (ENAP) e da *Revista de Administração Pública* (FGV); Estratégia de Governo Digital do governo federal. |
| 11 | Georreferenciamento e sistemas de coordenadas | Seção 2.5 | Livros de cartografia e geodésia; normas do IBGE sobre o sistema geodésico de referência (SIRGAS 2000). |
| 12 | Arquitetura cliente-servidor em aplicações web | Seção 2.6 | Livros de sistemas distribuídos ou de engenharia de *software*, como Pressman e Maxim (2016), já no `refs.bib`; confira o capítulo. |
| 13 | Aplicações de página única (SPA) | Seção 2.6 | Documentação oficial do Angular (https://angular.dev); Freeman (2019), já no `refs.bib`. |
| 14 | CSRF e *cookies* `HttpOnly`/`SameSite` | Seção 2.6 | OWASP (*Cross-Site Request Forgery Prevention Cheat Sheet*); MDN Web Docs; RFC 6265 (*HTTP State Management Mechanism*). |
| 15 | Trabalhos relacionados | Seção 2.7 | Portal de Periódicos da CAPES, SciELO, BDTD, anais da SBC (por exemplo, WIE e SBSI), repositório de TCCs do IFAC; ferramentas do INEP para consulta de escolas. Procure ao menos três trabalhos e preencha o Quadro 1. |

## Prioridade baixa (tecnologias, se o orientador exigir)

| # | Assunto | Onde aparece | Onde procurar |
| --- | --- | --- | --- |
| 16 | Linguagem Java | Seção 3.3.1 | Documentação da Oracle ou do OpenJDK; *The Java Language Specification*. |
| 17 | Spring Security | Seção 3.3.2 | Documentação oficial (https://docs.spring.io/spring-security/reference/). |
| 18 | BCrypt | Seção 3.3.2 | Artigo original de Provos e Mazières (1999), "A Future-Adaptable Password Scheme", USENIX; confira os dados bibliográficos. |
| 19 | Flyway | Seção 3.3.3 | Documentação oficial do Flyway (Redgate). |
| 20 | Tailwind CSS | Seção 3.3.4 | Documentação oficial (https://tailwindcss.com/docs). |
| 21 | Serviços de mapas de fundo (OpenStreetMap, Esri, OpenTopoMap, CARTO) | Seção 3.3.5 | Páginas oficiais e termos de uso de cada serviço. A atribuição dos mapas também deve aparecer nas legendas das figuras do mapa. |
| 22 | Cloudinary, Docker, Swagger/OpenAPI, JUnit, Mockito, Bruno, Maven | Seção 3.3.6 | Documentação oficial de cada ferramenta. |
| 23 | Hibernate Envers | Seções 3.3.1 e 4.2.6 | Documentação oficial do Hibernate ORM, capítulo *Envers* (https://hibernate.org/orm/documentation/). |

## Referências do `refs.bib` que precisam de conferência

| Chave | Problema | Ação |
| --- | --- | --- |
| `angular2023` | URL antiga (`angular.io`) | Atualizar para `angular.dev` e a data de acesso. |
| `postgresql2023` | Título "PostgreSQL 18 documentation" com ano 2023 | Corrigir a versão ou o ano e a data de acesso. |
| `pivotal2023` | Autor "Pivotal Software" com editora VMware | Conferir o autor institucional atual do Spring. |
| `secretariaacre2022` | Plano Estadual de Educação 2015–2025 com ano 2022 | Conferir o documento, o ano e se ele trata da educação escolar indígena. |
| `brasil2004` | Decreto nº 5.051/2004 | Verificar a vigência: o decreto foi consolidado pelo Decreto nº 10.088/2019. Se citar a Convenção 169 da OIT, use a norma vigente. |
| `munduruku2019`, `paladino2012` | Dados bibliográficos não conferidos | Conferir ano, editora e evento no exemplar antes de usar. Os capítulos atuais não os citam. |
| `wazlawick2013` | Ano e edição a conferir | Os capítulos atuais não citam esta obra. |
| `walls2019`, `sommerville2019`, `stair2015`, `fain2017` | Ano ou edição a conferir | Conferir no exemplar. |

As novas referências usadas nos capítulos (`BRAY, 2017` – RFC 8259; `JONES; BRADLEY; SAKIMURA, 2015` – RFC 7519) não estão no `refs.bib` e devem ser acrescentadas.
