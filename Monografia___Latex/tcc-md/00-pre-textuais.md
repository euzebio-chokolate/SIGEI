# ELEMENTOS PRÉ-TEXTUAIS

## Capa

INSTITUTO FEDERAL DE EDUCAÇÃO, CIÊNCIA E TECNOLOGIA DO ACRE
CAMPUS RIO BRANCO
CURSO SUPERIOR DE TECNOLOGIA EM SISTEMAS PARA INTERNET

EUZÉBIO DO NASCIMENTO OLIVEIRA NETO

SIGEI: SISTEMA DE GERENCIAMENTO ESCOLAR INDÍGENA

Rio Branco – AC
2026

---

## Folha de rosto

EUZÉBIO DO NASCIMENTO OLIVEIRA NETO

SIGEI: SISTEMA DE GERENCIAMENTO ESCOLAR INDÍGENA

> Trabalho de Conclusão de Curso apresentado ao Curso Superior de Tecnologia em Sistemas para Internet do Instituto Federal de Educação, Ciência e Tecnologia do Acre – Campus Rio Branco, como requisito parcial para a obtenção do grau de Tecnólogo em Sistemas para Internet.
>
> Orientador: Prof. [TITULAÇÃO A CONFIRMAR: Me. ou Dr.] Marlon Amaro Coelho Teixeira

Rio Branco – AC
2026

[CONFIRMAR no modelo do IFAC (`abntspiifac.cls`) se a versão final exige ficha catalográfica e folha de aprovação. A folha de aprovação é preenchida após a defesa, com os nomes da banca.]

---

## Dedicatória (opcional)

[TEXTO DO AUTOR — opcional. Remova esta seção se não houver dedicatória.]

---

## Agradecimentos

[TEXTO DO AUTOR — os agradecimentos são pessoais e devem ser escritos por você. Sugestão de itens a considerar, sem obrigação: família; orientador, Prof. Marlon Amaro Coelho Teixeira; professores e coordenação do curso; profissionais da SEE/AC que contribuíram com o projeto, se houver; colegas que apoiaram o trabalho.]

---

## Epígrafe (opcional)

[TEXTO DO AUTOR — opcional. Remova esta seção se não houver epígrafe.]

---

## Resumo

As informações sobre as escolas indígenas do Acre, como localização, território, infraestrutura, equipe e matrículas, estavam organizadas em planilhas eletrônicas separadas, sem vínculo entre si e sem histórico estruturado por ano. Essa dispersão dificulta a consulta, a atualização e a comparação dos dados ao longo do tempo. Este trabalho apresenta o SIGEI – Sistema de Gerenciamento Escolar Indígena, uma aplicação web desenvolvida para centralizar essas informações e apoiar seu acompanhamento pela Secretaria de Estado de Educação do Acre. O sistema foi construído de forma incremental, com uma API REST em Java e Spring Boot, uma interface em Angular e um banco de dados PostgreSQL. O SIGEI oferece um mapa público com a localização georreferenciada das escolas, busca e filtros; uma área administrativa com controle de acesso por perfil; cadastros de municípios, terras indígenas, povos, aldeias e escolas; registro anual de equipe, infraestrutura e matrículas; consulta ao histórico de cada escola; abertura de novo ano letivo; um painel de indicadores consolidados; e uma trilha de auditoria que registra o autor e a data de cada alteração. O funcionamento do *back-end* foi verificado por 236 casos de teste automatizados e por testes manuais da API. Como resultado, o sistema reúne em uma única base dados antes dispersos, organizando-os por escola e por ano. Como trabalhos futuros, destacam-se a validação com os usuários da secretaria e a geração de relatórios exportáveis.

[AJUSTAR o número de testes se a execução com o JDK 21 mudar o resultado e a frase sobre validação conforme a seção 4.3.3.]

**Palavras-chave:** Educação escolar indígena. Sistemas de informação. Georreferenciamento. Aplicação web. API REST.

---

## Abstract

Information about indigenous schools in the state of Acre, Brazil, such as location, territory, infrastructure, staff and enrollment, was kept in separate spreadsheets, with no links between them and no structured yearly history. This dispersion makes it harder to query, update and compare data over time. This work presents SIGEI (Indigenous School Management System), a web application developed to centralize this information and support its monitoring by the Acre State Department of Education. The system was built incrementally, with a REST API in Java and Spring Boot, an Angular user interface and a PostgreSQL database. SIGEI provides a public map with the georeferenced location of schools, search and filters; an administrative area with role-based access control; registration of municipalities, indigenous lands, peoples, villages and schools; yearly records of staff, infrastructure and enrollment; consultation of each school's history; opening of a new school year; a dashboard with consolidated indicators; and an audit trail that records the author and date of each change. The back-end was verified by 236 automated test cases and by manual API tests. As a result, the system brings previously scattered data into a single database, organized by school and by year. Future work includes validation with the department's users and exportable reports.

[ADJUST the number of tests if the run with JDK 21 changes the result, and the sentence on validation according to section 4.3.3.]

**Keywords:** Indigenous school education. Information systems. Georeferencing. Web application. REST API.

---

## Lista de figuras

Figura 1 – Diagrama de casos de uso do SIGEI
Figura 2 – Diagrama de classes do território e da escola
Figura 3 – Diagrama de classes do registro anual
Figura 4 – Modelo entidade-relacionamento: território e escola
Figura 5 – Modelo entidade-relacionamento: dados anuais
Figura 6 – Modelo entidade-relacionamento: usuários e sessões
Figura 7 – Mapa público do SIGEI
Figura 8 – Busca e filtros do mapa público
Figura 9 – Informações de uma escola no mapa público
Figura 10 – Tela de autenticação do SIGEI
Figura 11 – Tela de alteração de senha
Figura 12 – Painel de indicadores do SIGEI
Figura 13 – Indicadores detalhados do painel
Figura 14 – Consulta de municípios
Figura 15 – Listagem de terras indígenas
Figura 16 – Cadastro de terra indígena
Figura 17 – Listagem de povos indígenas
Figura 18 – Listagem de aldeias
Figura 19 – Cadastro de aldeia
Figura 20 – Listagem de escolas
Figura 21 – Confirmação de exclusão de escola
Figura 22 – Cadastro de escola: dados da escola
Figura 23 – Cadastro de escola: registro anual
Figura 24 – Cadastro de escola: equipe escolar
Figura 25 – Cadastro de escola: infraestrutura
Figura 26 – Cadastro de escola: matrículas por nível
Figura 27 – Cadastro de escola: matrículas por série
Figura 28 – Cadastro de escola: revisão
Figura 29 – Sequência do cadastro completo de escola
Figura 30 – Histórico escolar por ano de referência
Figura 31 – Histórico escolar por escola
Figura 32 – Detalhe de um registro anual
Figura 33 – Abertura de novo ano letivo
Figura 34 – Fluxo da abertura de novo ano letivo
Figura 35 – Listagem de usuários
Figura 36 – Cadastro de usuário
Figura 37 – Configurações do SIGEI
Figura 38 – Resultado da importação de planilha
Figura 39 – Arquitetura geral do SIGEI
Figura 40 – Componentes do *front-end*
Figura 41 – Componentes do *back-end*
Figura 42 – Documentação da API no Swagger
Figura 43 – Sequência de autenticação no SIGEI
Figura 44 – Implantação do SIGEI com Docker
Figura 45 – Execução dos testes automatizados do *back-end*
Figura 46 – Teste manual do *login* com o Bruno

[Os números de página são gerados pelo LaTeX.]

---

## Lista de quadros

Quadro 1 – Comparação entre trabalhos relacionados e o SIGEI
Quadro 2 – Incrementos no desenvolvimento do SIGEI
Quadro 3 – Requisitos funcionais
Quadro 4 – Requisitos não funcionais
Quadro 5 – Atores do SIGEI
Quadro 6 – Processo de validação
Quadro 7 – Regras de autorização da API
Quadro 8 – Testes automatizados do *back-end*
Quadro 9 – Roteiro de testes das funcionalidades

---

## Lista de abreviaturas e siglas

| Sigla | Significado |
| --- | --- |
| AEE | Atendimento Educacional Especializado |
| API | *Application Programming Interface* |
| CNE/CEB | Conselho Nacional de Educação / Câmara de Educação Básica |
| CSRF | *Cross-Site Request Forgery* |
| CSS | *Cascading Style Sheets* |
| DTO | *Data Transfer Object* |
| EJA | Educação de Jovens e Adultos |
| HTML | *HyperText Markup Language* |
| HTTP | *HyperText Transfer Protocol* |
| IFAC | Instituto Federal de Educação, Ciência e Tecnologia do Acre |
| INEP | Instituto Nacional de Estudos e Pesquisas Educacionais Anísio Teixeira |
| JPA | *Jakarta Persistence API* |
| JSON | *JavaScript Object Notation* |
| JVM | *Java Virtual Machine* |
| JWT | *JSON Web Token* |
| LDB | Lei de Diretrizes e Bases da Educação Nacional |
| REST | *Representational State Transfer* |
| RF | Requisito Funcional |
| RNF | Requisito Não Funcional |
| SEE/AC | Secretaria de Estado de Educação do Acre [CONFIRMAR denominação] |
| SIG | Sistema de Informação Geográfica |
| SIGEI | Sistema de Gerenciamento Escolar Indígena |
| SPA | *Single Page Application* |
| SQL | *Structured Query Language* |
| URI | *Uniform Resource Identifier* |

[JPA: o projeto usa os pacotes `jakarta.persistence`; a sigla passou a significar *Jakarta Persistence API*. O `tcc.tex` atual usa *Java Persistence API*, nome anterior da especificação. Escolha um dos dois e mantenha-o em todo o texto.]

---

## Sumário

1 INTRODUÇÃO
1.1 Objetivos
1.1.1 Objetivo geral
1.1.2 Objetivos específicos
1.2 Justificativa
2 REFERENCIAL TEÓRICO
2.1 Educação escolar indígena no Brasil: marco legal
2.2 Educação escolar indígena no Acre
2.3 Gestão da informação educacional e o Censo Escolar
2.4 Sistemas de informação na gestão pública educacional
2.5 Georreferenciamento e mapas na web
2.6 Aplicações web e APIs REST
2.7 Trabalhos relacionados
3 METODOLOGIA
3.1 Processo de Desenvolvimento
3.2 Levantamento e Modelagem do Sistema
3.2.1 Contexto da demanda e levantamento de requisitos
3.2.2 Requisitos funcionais
3.2.3 Requisitos não funcionais
3.2.4 Atores e casos de uso
3.2.5 Diagrama de classes
3.2.6 Modelo do banco de dados
3.3 Tecnologias
3.3.1 *Back-end*: Java, Spring Boot, Spring Data JPA e Hibernate
3.3.2 Segurança: Spring Security e JWT
3.3.3 Banco de dados: PostgreSQL e Flyway
3.3.4 *Front-end*: Angular, TypeScript e Tailwind CSS
3.3.5 Mapas: Leaflet
3.3.6 Ferramentas de apoio
3.4 Validação
4 SIGEI – SISTEMA DE GERENCIAMENTO ESCOLAR INDÍGENA
4.1 Funcionalidades
4.1.1 Mapa público interativo
4.1.2 Autenticação e troca obrigatória de senha
4.1.3 Tela inicial: painel de indicadores
4.1.4 Municípios
4.1.5 Terras indígenas
4.1.6 Povos indígenas
4.1.7 Aldeias
4.1.8 Gerenciamento de escolas
4.1.9 Cadastro de escolas em etapas
4.1.10 Histórico escolar
4.1.11 Abertura de novo ano letivo
4.1.12 Gerenciamento de usuários
4.1.13 Configurações
4.1.14 Carga inicial e importação de planilhas
4.2 Arquitetura do Sistema
4.2.1 *Front-end*
4.2.2 *Back-end*
4.2.3 Banco de dados
4.2.4 Comunicação entre *front-end* e *back-end*
4.2.5 Segurança e controle de acesso
4.2.6 Trilha de auditoria
4.2.7 Implantação
4.3 Testes
4.3.1 Testes da API
4.3.2 Testes das funcionalidades
4.3.3 Validação do Sistema
5 CONCLUSÕES E TRABALHOS FUTUROS
REFERÊNCIAS
