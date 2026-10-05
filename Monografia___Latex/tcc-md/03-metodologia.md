# 3 METODOLOGIA

Este capítulo descreve como o SIGEI foi desenvolvido. São apresentados o processo de desenvolvimento adotado, o levantamento e a modelagem do sistema, as tecnologias utilizadas e os procedimentos de validação.

Quanto à natureza, o trabalho é uma pesquisa aplicada, pois busca gerar conhecimento voltado à solução de um problema prático, que é a organização das informações das escolas indígenas do Acre (GIL, 2008). O resultado principal é um artefato de *software*, cujo desenvolvimento e cujo funcionamento são descritos neste capítulo e no Capítulo 4.

## 3.1 Processo de Desenvolvimento

O desenvolvimento seguiu o modelo de processo incremental. Pressman e Maxim (2016) descrevem esse modelo como a combinação de fluxos lineares aplicados em sequência, em que cada fluxo produz um incremento utilizável do *software*: o primeiro entrega o núcleo do produto, e os seguintes acrescentam funcionalidades. Essa organização permitiu construir o sistema em partes, testando cada uma antes de passar à seguinte.

O código do SIGEI foi versionado com Git e mantido em um repositório no GitHub desde o início do projeto. O histórico de *commits*, registrado entre abril e outubro de 2026, permite identificar os incrementos realizados. O Quadro 2 resume esses incrementos, com base nas datas e nas mensagens dos *commits*.

**Quadro 2 – Incrementos no desenvolvimento do SIGEI**

| Incremento | Período | Descrição |
| --- | --- | --- |
| 1 | abr. a jul. 2026 | Estrutura inicial do *back-end*: entidades, objetos de transferência de dados (DTOs), validação de entrada e conversores. Implementação das operações de cadastro, consulta, edição e exclusão de cada entidade (terra indígena, escola, aldeia, município, povo, equipe, infraestrutura, registro anual, matrículas e anexo), cada uma acompanhada de testes automatizados. |
| 2 | jul. a ago. 2026 | Testes manuais dos *endpoints* com a ferramenta Bruno. Início do *front-end*, com as primeiras páginas e formulários. O ano de referência passou a ser uma tabela própria. |
| 3 | ago. 2026 | Consultas por ano de referência e importação das planilhas de infraestrutura e de matrículas. |
| 4 | ago. 2026 | Integração do *front-end* com a API, documentação com Swagger, formulário de escolas com envio de fotografias, histórico anual e abertura de novo ano na interface. |
| 5 | ago. a set. 2026 | Autenticação com JWT, gerenciamento de usuários, perfis de acesso e troca obrigatória de senha. Mapa público, configurações, painel de indicadores, carga inicial automática e preparação para hospedagem. |
| 6 | set. a out. 2026 | Revisão dos testes, documentação dos *endpoints*, inclusão dos ciclos da EJA, consulta do histórico por escola, gravação do cadastro de escola em transação única, sessão em *cookies* com renovação e proteção CSRF, versionamento do banco com Flyway, documentação técnica e diagramas. |

Fonte: Elaborado pelo autor.

A ordem dos incrementos reflete uma decisão de desenvolvimento: primeiro foi construída a base de dados e as regras do *back-end*, com testes automatizados; depois, a interface; por último, a segurança, os indicadores e os ajustes de robustez. A construção da API antes da interface permitiu testá-la de forma isolada com a ferramenta Bruno, antes de existir qualquer tela.

[CONFIRMAR: se houve apresentação de incrementos à SEE/AC ou ao orientador com coleta de sugestões. Em caso afirmativo, descrever quando e quais ajustes resultaram disso. Em caso negativo, manter o texto como está.]

## 3.2 Levantamento e Modelagem do Sistema

### 3.2.1 Contexto da demanda e levantamento de requisitos

Os requisitos são normalmente classificados em funcionais e não funcionais. Os requisitos funcionais descrevem os serviços que o sistema deve fornecer e como deve reagir às entradas; os não funcionais tratam de restrições e propriedades do sistema como um todo, como segurança, desempenho e confiabilidade (SOMMERVILLE, 2019).

A demanda do SIGEI parte da necessidade de organizar as informações das escolas indígenas acompanhadas pela SEE/AC [CONFIRMAR: como a demanda chegou ao autor — por exemplo, vínculo profissional, estágio ou contato com a coordenação de educação escolar indígena da secretaria]. O levantamento de requisitos baseou-se nas seguintes fontes:

a) análise das duas planilhas eletrônicas usadas como base de dados inicial, que indicaram quais informações eram registradas sobre cada escola (território, situação, infraestrutura, equipe e matrículas por etapa);

b) [CONFIRMAR: reuniões ou conversas com profissionais da SEE/AC — informar datas aproximadas, formato (presencial ou on-line) e os temas tratados. Se não ocorreram, remover este item];

c) estudo da legislação sobre educação escolar indígena, apresentada no Capítulo 2.

A análise das planilhas levou a duas constatações que orientaram a modelagem. A primeira é que parte das informações é estável (a escola, sua aldeia, sua terra indígena e seu município), enquanto outra parte muda a cada ano (matrículas, infraestrutura, equipe). A segunda é que as escolas se relacionam entre si, pois algumas funcionam como anexos de uma escola-sede.

### 3.2.2 Requisitos funcionais

O Quadro 3 apresenta os requisitos funcionais do SIGEI. Todos estão implementados na versão descrita neste trabalho.

**Quadro 3 – Requisitos funcionais**

| Requisito | Descrição |
| --- | --- |
| RF01 | O sistema deve permitir que o usuário se autentique com e-mail e senha. |
| RF02 | O sistema deve manter a sessão do usuário, renovando-a automaticamente enquanto válida, e permitir seu encerramento. |
| RF03 | O sistema deve exigir a troca da senha no primeiro acesso e permitir que o usuário altere a própria senha. |
| RF04 | O sistema deve permitir ao administrador cadastrar, editar, consultar e excluir usuários, definindo o perfil de acesso e se o usuário está ativo. |
| RF05 | O sistema deve permitir consultar os municípios do Acre, com a quantidade de terras indígenas, aldeias e escolas de cada um. |
| RF06 | O sistema deve permitir cadastrar, editar, consultar e excluir terras indígenas, associadas a um município. |
| RF07 | O sistema deve permitir cadastrar, editar, consultar e excluir povos indígenas. |
| RF08 | O sistema deve permitir cadastrar, editar, consultar e excluir aldeias, associadas a uma terra indígena e a um povo. |
| RF09 | O sistema deve permitir cadastrar escolas com código INEP, nome, tipo de unidade (sede ou anexo), aldeia e, para anexos, escola-sede. |
| RF10 | O sistema deve permitir registrar, para cada escola e ano de referência, a rede, o local de funcionamento e o total de alunos. |
| RF11 | O sistema deve permitir registrar a equipe escolar de cada ano. |
| RF12 | O sistema deve permitir registrar a infraestrutura de cada ano, incluindo coordenadas geográficas e fotografia da escola. |
| RF13 | O sistema deve permitir registrar as matrículas de cada ano por nível e por série ou etapa, separadas por sexo. |
| RF14 | O sistema deve permitir editar os dados de uma escola e de um ano específico, inclusive incluir dados de anos anteriores. |
| RF15 | O sistema deve permitir excluir escolas, mediante confirmação. |
| RF16 | O sistema deve permitir consultar o histórico das escolas por ano de referência e por escola, com período opcional. |
| RF17 | O sistema deve permitir abrir um novo ano letivo, criando para cada escola um registro baseado no ano anterior. |
| RF18 | O sistema deve exibir, sem necessidade de autenticação, um mapa interativo com a localização das escolas e sua situação de funcionamento. |
| RF19 | O sistema deve permitir buscar escolas no mapa por nome ou código INEP e filtrá-las por situação, tipo de acesso, internet e prédio próprio. |
| RF20 | O sistema deve apresentar um painel de indicadores por ano de referência. |
| RF21 | O sistema deve permitir importar as planilhas de infraestrutura e de matrículas por meio da API. |
| RF22 | O sistema deve cadastrar automaticamente, na primeira execução, os municípios do Acre, os dados das planilhas iniciais e um usuário administrador. |
| RF23 | O sistema deve permitir que o usuário salve, no navegador, preferências de exibição do mapa. |

Fonte: Elaborado pelo autor.

### 3.2.3 Requisitos não funcionais

O Quadro 4 apresenta os requisitos não funcionais. Foram incluídos apenas aqueles cujo atendimento pode ser verificado na implementação.

**Quadro 4 – Requisitos não funcionais**

| Requisito | Descrição |
| --- | --- |
| RNF01 | O sistema deve ser acessado por navegador web, sem instalação de *software* no computador do usuário. |
| RNF02 | A interface deve se adaptar a telas de diferentes tamanhos, incluindo dispositivos móveis. |
| RNF03 | O acesso às funções administrativas deve ser restrito a usuários autenticados, de acordo com o perfil de cada um. |
| RNF04 | As senhas devem ser armazenadas de forma cifrada, e os *tokens* de sessão não devem ficar acessíveis a *scripts* da página. |
| RNF05 | As requisições que alteram dados devem ser protegidas contra falsificação de requisições entre sites (CSRF). |
| RNF06 | O banco de dados deve garantir a integridade referencial e impedir registros duplicados de uma mesma escola em um mesmo ano. |
| RNF07 | O cadastro de uma escola com seus dados anuais deve ser gravado em uma única transação. |
| RNF08 | O esquema do banco de dados deve ser versionado por migrações. |
| RNF09 | A API deve ter documentação interativa dos seus *endpoints*. |
| RNF10 | O sistema deve poder ser executado em contêineres. |

Fonte: Elaborado pelo autor.

### 3.2.4 Atores e casos de uso

Foram identificados três atores. O Quadro 5 apresenta cada um e suas permissões, definidas na configuração de segurança da API e nas rotas do *front-end*.

**Quadro 5 – Atores do SIGEI**

| Ator | Descrição | Permissões |
| --- | --- | --- |
| Visitante | Qualquer pessoa que acessa o sistema sem autenticação. | Consultar o mapa público e acessar a tela de *login*. |
| Usuário | Usuário autenticado com perfil `USUARIO`. | Tudo o que o visitante faz, além de consultar cadastros, histórico, indicadores e usuários, alterar a própria senha e salvar preferências. |
| Administrador | Usuário autenticado com perfil `ADMIN`. | Tudo o que o usuário faz, além de cadastrar, editar e excluir dados, gerenciar usuários, abrir novo ano letivo e importar planilhas. |

Fonte: Elaborado pelo autor.

A Figura 1 apresenta o diagrama de casos de uso. A herança entre os atores indica que cada perfil acumula as permissões do anterior.

[INSERIR FIGURA 1 – Diagrama de casos de uso do SIGEI (exportar de `diagramas/02-casos-de-uso.md`)]

Figura 1 – Diagrama de casos de uso do SIGEI
Fonte: Elaborado pelo autor.

### 3.2.5 Diagrama de classes

As classes do domínio foram divididas em dois grupos. O primeiro reúne o território e a escola: município, terra indígena, povo, aldeia, escola e anexo (Figura 2). O segundo reúne os dados anuais: ano de referência, registro anual, equipe, infraestrutura e matrículas (Figura 3). A divisão foi feita para manter os diagramas legíveis.

[INSERIR FIGURA 2 – Diagrama de classes do território e da escola (exportar de `diagramas/06-classes-territorio.md`)]

Figura 2 – Diagrama de classes do território e da escola
Fonte: Elaborado pelo autor.

[INSERIR FIGURA 3 – Diagrama de classes do registro anual (exportar de `diagramas/07-classes-registro-anual.md`)]

Figura 3 – Diagrama de classes do registro anual
Fonte: Elaborado pelo autor.

A classe central do primeiro grupo é `Escola`, ligada a uma `Aldeia`, que pertence a uma `TerraIndigena` e é habitada por um `Povo`. A terra indígena está associada a um `Municipio`. No segundo grupo, a classe central é `RegistroAnual`, que liga uma escola a um `AnoReferencia` e reúne os dados daquele ano. As relações e suas cardinalidades são detalhadas na seção 4.2.3.

### 3.2.6 Modelo do banco de dados

O modelo relacional foi derivado das classes de domínio e é criado por migrações versionadas. Ele contém 14 tabelas, apresentadas em três figuras: as tabelas territoriais e de escola (Figura 4), as tabelas de dados anuais (Figura 5) e as tabelas de usuários e sessões (Figura 6).

[INSERIR FIGURA 4 – Modelo entidade-relacionamento: território e escola (exportar de `diagramas/08-er-territorio.md`)]

Figura 4 – Modelo entidade-relacionamento: território e escola
Fonte: Elaborado pelo autor.

[INSERIR FIGURA 5 – Modelo entidade-relacionamento: dados anuais (exportar de `diagramas/09-er-dados-anuais.md`)]

Figura 5 – Modelo entidade-relacionamento: dados anuais
Fonte: Elaborado pelo autor.

[INSERIR FIGURA 6 – Modelo entidade-relacionamento: usuários e sessões (exportar de `diagramas/10-er-autenticacao.md`)]

Figura 6 – Modelo entidade-relacionamento: usuários e sessões
Fonte: Elaborado pelo autor.

Algumas restrições do banco merecem destaque. O par formado pela escola e pelo ano de referência é único na tabela `registro_anual`, o que impede dois registros da mesma escola no mesmo ano. O valor do ano é único na tabela `ano_referencia`. Cada registro anual tem no máximo uma equipe e uma infraestrutura, garantido por restrição de unicidade na chave estrangeira. O e-mail do usuário também é único.

## 3.3 Tecnologias

Esta seção apresenta as tecnologias utilizadas e o papel de cada uma no SIGEI. As versões indicadas são as declaradas nos arquivos de configuração do projeto (`pom.xml` e `package.json`).

### 3.3.1 *Back-end*: Java, Spring Boot, Spring Data JPA e Hibernate

O *back-end* foi desenvolvido na linguagem Java, versão 21. O Java é uma linguagem orientada a objetos, com tipagem estática, executada sobre a máquina virtual Java (JVM) [REFERÊNCIA NECESSÁRIA: pesquisar fonte oficial sobre a linguagem Java, como a documentação da Oracle ou do OpenJDK].

Sobre o Java foi utilizado o Spring Boot, versão 4.0.5. O Spring Boot simplifica a criação de aplicações com o *framework* Spring, oferecendo configuração automática e um servidor web embutido (WALLS, 2019). No SIGEI, o módulo Spring MVC recebe as requisições HTTP e as encaminha aos controladores, que estão anotados com `@RestController`. O Bean Validation verifica os dados de entrada com anotações como `@NotBlank` e `@NotNull`, e o Spring Boot Actuator fornece um *endpoint* de verificação de funcionamento (`/actuator/health`).

O acesso ao banco de dados utiliza o Spring Data JPA, que gera a implementação dos repositórios a partir de interfaces, e o Hibernate, que realiza o mapeamento objeto-relacional entre as classes Java e as tabelas do banco (BAUER; KING; GREGORY, 2015). As entidades do SIGEI são classes anotadas com `@Entity`, e os relacionamentos são declarados com anotações como `@ManyToOne` e `@OneToMany`. No SIGEI, o Hibernate não cria o esquema do banco; ele apenas verifica, na inicialização, se as tabelas correspondem às entidades.

Para converter entidades em objetos de transferência de dados, foi utilizado o MapStruct, versão 1.6.3, que gera o código de conversão durante a compilação. Assim, a API nunca expõe diretamente as entidades JPA. A leitura das planilhas Excel é feita com a biblioteca Apache POI, versão 5.4.1.

### 3.3.2 Segurança: Spring Security e JWT

O controle de acesso utiliza o Spring Security, que intercepta as requisições por uma cadeia de filtros e aplica as regras de autorização [REFERÊNCIA NECESSÁRIA: pesquisar a documentação oficial do Spring Security]. As senhas são armazenadas com o algoritmo BCrypt, que gera um *hash* com custo computacional ajustável e não permite recuperar a senha original [REFERÊNCIA NECESSÁRIA: pesquisar fonte sobre o algoritmo BCrypt].

A identificação do usuário em cada requisição usa *tokens* no padrão JWT (JONES; BRADLEY; SAKIMURA, 2015), gerados com a biblioteca JJWT, versão 0.12.6. O *token* de acesso tem validade de 15 minutos e é acompanhado de um *token* de renovação com validade de 7 dias. Ambos são enviados ao navegador em *cookies* com os atributos `HttpOnly` e `SameSite=Strict`, que impedem sua leitura por *scripts* da página e seu envio em requisições originadas de outros sites. O funcionamento desse mecanismo é detalhado na seção 4.2.5.

### 3.3.3 Banco de dados: PostgreSQL e Flyway

O banco de dados utilizado é o PostgreSQL, um sistema gerenciador de banco de dados relacional de código aberto (OBE; HSU, 2017). O PostgreSQL armazena todos os dados do SIGEI, com chaves estrangeiras e restrições de unicidade que garantem a integridade das relações. Na configuração com contêineres, é utilizada a versão 17.

O esquema do banco é criado e alterado pelo Flyway, uma ferramenta de migração de banco de dados. Cada alteração é registrada em um arquivo SQL numerado, como `V1__esquema_inicial.sql` e `V2__refresh_token.sql`, e o Flyway aplica, na inicialização, as migrações ainda não executadas [REFERÊNCIA NECESSÁRIA: pesquisar a documentação oficial do Flyway]. Isso permite reproduzir o mesmo esquema em qualquer ambiente e acompanhar sua evolução no controle de versão.

Nos testes automatizados, é utilizado o H2, um banco de dados em memória, que dispensa a instalação de um servidor PostgreSQL para executar os testes.

### 3.3.4 *Front-end*: Angular, TypeScript e Tailwind CSS

O *front-end* foi desenvolvido com o Angular, versão 21, um *framework* para aplicações web de página única (ANGULAR, [s.d.]). A aplicação é formada por componentes independentes (*standalone*), cada um com seu modelo HTML, sua lógica em TypeScript e seu estilo. As telas são carregadas sob demanda, o que reduz o volume de código baixado no primeiro acesso. As chamadas à API são feitas pelo serviço `HttpClient`, que trabalha com a biblioteca RxJS para tratar respostas assíncronas.

A linguagem utilizada é o TypeScript, versão 5.9, que acrescenta tipagem estática ao JavaScript (FAIN; MOISEEV, 2017). No SIGEI, os contratos de dados da API foram descritos como interfaces TypeScript, o que permite detectar, durante a compilação, o uso de campos inexistentes.

A estilização utiliza o Tailwind CSS, versão 4, um *framework* de CSS baseado em classes utilitárias aplicadas diretamente no HTML [REFERÊNCIA NECESSÁRIA: pesquisar a documentação oficial do Tailwind CSS]. As classes com prefixos de tamanho de tela, como `md:` e `lg:`, foram usadas para adaptar o *layout* a telas menores. Os ícones vêm da biblioteca Font Awesome. Os gráficos do painel de indicadores foram feitos com elementos HTML estilizados, sem biblioteca de gráficos.

### 3.3.5 Mapas: Leaflet

O mapa interativo utiliza o Leaflet, versão 1.9.4, uma biblioteca JavaScript de código aberto para mapas interativos (LEAFLET, [s.d.]). O Leaflet desenha o mapa a partir de camadas de *tiles* e permite adicionar marcadores, janelas de informação (*popups*) e controles de aproximação. No SIGEI, ele é usado diretamente nos componentes Angular, sem bibliotecas intermediárias. O mapa oferece quatro camadas de fundo, fornecidas por serviços externos: OpenStreetMap, Esri World Imagery, OpenTopoMap e CARTO [REFERÊNCIA NECESSÁRIA: pesquisar as páginas oficiais desses serviços e seus termos de uso].

### 3.3.6 Ferramentas de apoio

Além das tecnologias principais, foram utilizadas as seguintes ferramentas:

a) **Cloudinary:** serviço externo de armazenamento de imagens, onde ficam as fotografias das escolas; o banco guarda apenas o endereço e o identificador de cada imagem;

b) **Docker e Docker Compose:** empacotam a API, o banco de dados e o *front-end* em contêineres, e o servidor web Nginx entrega a aplicação Angular e encaminha as chamadas à API;

c) **Git e GitHub:** controle de versão do código;

d) **Springdoc OpenAPI (Swagger):** gera a documentação interativa da API a partir das anotações dos controladores;

e) **JUnit 5 e Mockito:** testes automatizados do *back-end*, e Testcontainers, para testar as migrações em um PostgreSQL real;

f) **Bruno:** cliente HTTP utilizado para testar manualmente os *endpoints* da API;

g) **Maven:** gerenciamento de dependências e compilação do *back-end*, e npm, para o *front-end*.

[REFERÊNCIA NECESSÁRIA: pesquisar a documentação oficial de cada ferramenta citada, caso o orientador exija referência para elas.]

## 3.4 Validação

A validação do SIGEI foi planejada em quatro procedimentos, apresentados no Quadro 6.

**Quadro 6 – Processo de validação**

| Procedimento | Descrição | Situação |
| --- | --- | --- |
| Testes automatizados do *back-end* | Testes de serviços com objetos simulados (Mockito), testes de repositórios com banco em memória (H2), testes das rotas HTTP de autenticação e de histórico e teste das migrações em PostgreSQL. | Realizado; resultados na seção 4.3.1. |
| Testes manuais da API | Envio de requisições aos *endpoints* com a ferramenta Bruno e pela interface do Swagger, verificando códigos de resposta e conteúdo retornado. | Realizado durante o desenvolvimento; evidências na seção 4.3.1. |
| Testes das funcionalidades | Execução, na interface, de roteiros que cobrem os requisitos funcionais: *login*, cadastros, edição, exclusão, filtros, histórico, abertura de ano e controle de perfis. | [A REALIZAR ou CONFIRMAR: registrar a execução e o resultado de cada roteiro na seção 4.3.2]. |
| Conferência dos dados importados | Comparação entre os contadores retornados pela importação, os registros gravados e as planilhas de origem. | [A REALIZAR ou CONFIRMAR]. |
| Validação com a demandante | Apresentação do sistema à equipe da SEE/AC para avaliar se atende às necessidades de consulta e cadastro. | [CONFIRMAR: se ocorreu. Se não ocorreu, apresentar como etapa prevista e mencioná-la nos trabalhos futuros]. |

Fonte: Elaborado pelo autor.

Os critérios considerados são:

a) **correção:** as operações produzem o resultado esperado e respeitam as regras de negócio, como a unicidade do registro anual por escola e ano;

b) **segurança:** cada perfil acessa apenas as funções permitidas, e requisições sem autenticação ou sem permissão são recusadas;

c) **integridade dos dados:** o cadastro completo de uma escola é gravado por inteiro ou não é gravado;

d) **adequação:** as informações registradas correspondem às acompanhadas pela secretaria, critério que depende da validação com a demandante.
