# 1 INTRODUÇÃO

A educação escolar indígena no Brasil tem fundamento na Constituição Federal de 1988, que reconhece aos povos indígenas sua organização social, costumes, línguas, crenças e tradições e assegura às comunidades indígenas, no ensino fundamental, a utilização de suas línguas maternas e de processos próprios de aprendizagem (BRASIL, 1988). A Lei de Diretrizes e Bases da Educação Nacional retomou esse princípio e atribuiu aos sistemas de ensino a oferta de educação escolar bilíngue e intercultural aos povos indígenas (BRASIL, 1996). Desse modo, a escola indígena constitui uma modalidade com especificidades próprias e exige do poder público instrumentos de gestão compatíveis com essas especificidades.

O Estado do Acre abriga diferentes povos indígenas, distribuídos em terras indígenas localizadas em vários municípios [DADO A CONFIRMAR: número de povos e de terras indígenas no Acre, com fonte oficial — ver `referencias-a-pesquisar.md`]. Muitas aldeias ficam em áreas de acesso difícil, alcançadas por via terrestre, fluvial ou combinando ambas [REFERÊNCIA NECESSÁRIA: pesquisar fonte sobre as condições de acesso às aldeias e escolas indígenas no Acre]. A oferta de educação escolar nesses territórios é coordenada pela Secretaria de Estado de Educação do Acre (SEE/AC) [CONFIRMAR: denominação oficial vigente da secretaria], responsável por acompanhar unidades escolares que se diferenciam quanto à localização, à infraestrutura, à equipe disponível e ao número de estudantes atendidos.

Acompanhar essas escolas depende de informações que mudam a cada ano letivo, como as matrículas por etapa de ensino, a situação de funcionamento da unidade, as condições do prédio e o acesso à água, à energia e à internet. Durante a concepção deste trabalho, observou-se que parte dessas informações estava organizada em planilhas eletrônicas independentes, como um quadro geral sobre a construção e a infraestrutura das escolas e uma planilha com dados de matrículas das escolas indígenas referentes a 2025. Essas planilhas foram usadas como fonte da carga inicial do sistema desenvolvido [CONFIRMAR: origem das planilhas e autorização de uso dos dados]. Informações mantidas em arquivos separados, sem vínculo entre si e sem registro histórico organizado por ano, tornam mais trabalhosas a consulta, a atualização e o cruzamento de dados, por exemplo, para saber quantos estudantes uma escola atendia em anos anteriores ou quais unidades de um município não têm acesso à internet [CONFIRMAR com a SEE/AC: dificuldades efetivamente relatadas pela equipe responsável].

Laudon e Laudon (2014) descrevem os sistemas de informação como componentes inter-relacionados que coletam, processam, armazenam e distribuem informações para apoiar a tomada de decisão, a coordenação e o controle em uma organização. No caso das escolas indígenas, a localização geográfica de cada unidade também é relevante, pois a distância e o tipo de acesso interferem no planejamento de visitas, transporte e manutenção. A representação de dados em mapas permite relacionar atributos descritivos à posição no território (CÂMARA; DAVIS; MONTEIRO, 2001).

Nesse contexto, foi desenvolvido o SIGEI – Sistema de Gerenciamento Escolar Indígena, uma aplicação web que reúne em uma única base os dados territoriais (municípios, terras indígenas, povos e aldeias), os dados cadastrais das escolas e os dados anuais de cada unidade (equipe, infraestrutura e matrículas). O sistema oferece uma área administrativa com controle de acesso e um mapa público com a localização das escolas.

Diante desse cenário, este trabalho busca responder à seguinte questão: **como centralizar, organizar por ano e disponibilizar de forma georreferenciada as informações das escolas indígenas do Acre, de modo a apoiar a consulta e o acompanhamento dessas unidades pela SEE/AC?**

## 1.1 Objetivos

### 1.1.1 Objetivo geral

Desenvolver uma aplicação web para centralizar o cadastro e a consulta das informações das escolas indígenas do Acre, com histórico anual, mapa interativo e indicadores consolidados, como apoio à gestão realizada pela SEE/AC.

### 1.1.2 Objetivos específicos

a) modelar as informações territoriais, cadastrais e anuais das escolas indígenas a partir dos dados disponíveis em planilhas;

b) desenvolver uma API REST para cadastro, consulta e consolidação dessas informações, com persistência em banco de dados relacional;

c) desenvolver uma interface web para o gerenciamento dos cadastros, incluindo o registro dos dados anuais de cada escola e a consulta ao seu histórico;

d) disponibilizar um mapa interativo de acesso público com a localização das escolas e filtros de consulta;

e) apresentar indicadores consolidados por ano de referência;

f) implementar autenticação e controle de acesso por perfil de usuário;

g) verificar o funcionamento do sistema por meio de testes automatizados e de testes manuais da API.

## 1.2 Justificativa

A justificativa deste trabalho tem três aspectos.

O primeiro é o direito à educação escolar diferenciada. A legislação brasileira reconhece às comunidades indígenas o uso de suas línguas e de processos próprios de aprendizagem (BRASIL, 1988; BRASIL, 1996), e as Diretrizes Curriculares Nacionais para a Educação Escolar Indígena tratam da organização dessas escolas na educação básica (BRASIL, 2012). Para que esse direito seja acompanhado pelo poder público, é necessário saber onde as escolas estão, em que condições funcionam e quantos estudantes atendem.

O segundo é a gestão da informação. Quando os dados ficam distribuídos em arquivos isolados, cada consulta depende de busca manual e de conferência entre fontes. Um sistema com base única, identificação das escolas pelo código do Instituto Nacional de Estudos e Pesquisas Educacionais Anísio Teixeira (INEP) e registro organizado por ano reduz o retrabalho e permite comparar a situação de uma escola ao longo do tempo. Stair e Reynolds (2015) destacam que o valor da informação depende de ela estar organizada de forma útil para quem decide.

O terceiro é a dimensão territorial. A localização das escolas em mapa, associada à situação de funcionamento, ao tipo de acesso e à infraestrutura, permite visualizar a distribuição das unidades no estado, o que não se obtém facilmente em tabelas (LONGLEY *et al.*, 2013).

Do ponto de vista acadêmico, o trabalho aplica conteúdos do Curso Superior de Tecnologia em Sistemas para Internet, como desenvolvimento de aplicações web, construção de APIs, modelagem de banco de dados, segurança de aplicações e testes de *software*, a um problema concreto da administração pública educacional.

Além desta introdução, o trabalho está organizado em mais quatro capítulos. O Capítulo 2 apresenta o referencial teórico sobre educação escolar indígena, gestão da informação educacional, georreferenciamento e aplicações web. O Capítulo 3 descreve a metodologia: o processo de desenvolvimento, o levantamento e a modelagem do sistema, as tecnologias utilizadas e a estratégia de validação. O Capítulo 4 apresenta o SIGEI, suas funcionalidades, sua arquitetura e os testes realizados. O Capítulo 5 traz as conclusões, as limitações e as propostas de trabalhos futuros.
