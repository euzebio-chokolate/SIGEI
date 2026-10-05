# 2 REFERENCIAL TEÓRICO

Este capítulo reúne os conceitos que fundamentam o SIGEI. Primeiro, apresenta o marco legal da educação escolar indígena no Brasil e a situação dessa modalidade no Acre. Em seguida, discute a gestão da informação educacional e o papel dos sistemas de informação na gestão pública. Por fim, trata do georreferenciamento, das aplicações web e das APIs REST, e apresenta trabalhos relacionados.

## 2.1 Educação escolar indígena no Brasil: marco legal

A Constituição Federal de 1988 marcou uma mudança na relação do Estado brasileiro com os povos indígenas. O artigo 231 reconhece aos indígenas sua organização social, costumes, línguas, crenças e tradições, além dos direitos originários sobre as terras que tradicionalmente ocupam. No campo educacional, o artigo 210, § 2º, estabelece que o ensino fundamental regular será ministrado em língua portuguesa, assegurada às comunidades indígenas também a utilização de suas línguas maternas e de processos próprios de aprendizagem (BRASIL, 1988).

A Lei nº 9.394/1996, Lei de Diretrizes e Bases da Educação Nacional (LDB), reafirmou essa garantia no artigo 32, § 3º, e dedicou os artigos 78 e 79 à educação escolar indígena. O artigo 78 prevê programas integrados de ensino e pesquisa para a oferta de educação escolar bilíngue e intercultural, com o objetivo de valorizar as memórias históricas, as identidades étnicas e as línguas e ciências dos povos indígenas, e de garantir o acesso aos conhecimentos da sociedade nacional e de outras sociedades. O artigo 79 trata do apoio técnico e financeiro da União aos sistemas de ensino para essa oferta, com programas planejados com a participação das comunidades indígenas (BRASIL, 1996).

A Resolução CNE/CEB nº 5, de 22 de junho de 2012, definiu as Diretrizes Curriculares Nacionais para a Educação Escolar Indígena na Educação Básica (BRASIL, 2012). O documento trata, entre outros aspectos, da organização das escolas indígenas, da formação de professores e da gestão dessas unidades [CONFIRMAR na Resolução os artigos que tratam da organização e da gestão das escolas indígenas, para citação direta].

Esse conjunto normativo caracteriza a escola indígena como uma unidade com especificidades de língua, calendário, currículo e organização, vinculada à comunidade em que se localiza [REFERÊNCIA NECESSÁRIA: pesquisar fonte acadêmica sobre as características da escola indígena específica, diferenciada, intercultural e bilíngue]. Para o poder público, essas especificidades também significam a necessidade de informações sobre cada escola: em que aldeia e terra indígena se encontra, que povo atende, quais etapas de ensino oferece e em que condições funciona.

## 2.2 Educação escolar indígena no Acre

No Acre, a educação escolar indígena é ofertada em escolas localizadas em aldeias situadas em terras indígenas de diferentes municípios [DADO A CONFIRMAR: número de escolas indígenas, povos e terras indígenas atendidos, com fonte oficial — SEE/AC, INEP ou FUNAI]. A rede estadual é responsável pela maior parte dessas escolas [CONFIRMAR: proporção entre rede estadual e rede municipal, com fonte].

As planilhas usadas na carga inicial do SIGEI indicam quais informações são acompanhadas sobre essas escolas. O quadro de infraestrutura registra, para cada escola, o município, a terra indígena, a aldeia, o povo, a situação de funcionamento, a existência de prédio, o ano de construção, o número de salas, as coordenadas geográficas, o tipo de acesso, a internet, a água, a energia e a presença de professor responsável, equipe gestora, merendeira, zeladora e barqueiro, além da necessidade de transporte. A planilha de matrículas traz os quantitativos por sexo e por etapa: educação infantil, anos do ensino fundamental, séries do ensino médio, Educação de Jovens e Adultos (EJA) e Atendimento Educacional Especializado (AEE). As escolas aparecem como sede ou como anexo de outra unidade [CONFIRMAR: origem oficial das planilhas, para citá-las como fonte].

Essas características influenciaram a modelagem do sistema, apresentada no Capítulo 3. A distinção entre escola-sede e anexo, por exemplo, levou ao relacionamento entre escolas; a variação anual das matrículas e da infraestrutura levou à organização dos dados por ano de referência.

[REFERÊNCIA NECESSÁRIA: pesquisar estudos sobre a educação escolar indígena no Acre, como trabalhos sobre a formação de professores indígenas no estado ou documentos da SEE/AC sobre a modalidade.]

## 2.3 Gestão da informação educacional e o Censo Escolar

A gestão escolar envolve dimensões pedagógicas, administrativas, financeiras e de pessoal. Lück (2009) organiza essas dimensões em um conjunto de competências do gestor escolar, entre as quais o acompanhamento do funcionamento da escola e de seus resultados. Libâneo, Oliveira e Toschi (2012) discutem a organização dos sistemas de ensino e o papel das secretarias de educação na coordenação das escolas de sua rede. Nessa coordenação, a secretaria depende de informações confiáveis sobre cada unidade.

No Brasil, a principal fonte de dados sobre a educação básica é o Censo Escolar, levantamento anual coordenado pelo INEP com a colaboração das secretarias estaduais e municipais [REFERÊNCIA NECESSÁRIA: pesquisar fonte oficial do INEP sobre o Censo Escolar, seus objetivos e a periodicidade]. O Censo identifica cada escola por um código próprio, conhecido como código INEP, e registra matrículas, docentes e características da infraestrutura. O SIGEI adota esse código como identificador das escolas, o que facilita o cruzamento com outras bases [CONFIRMAR: se o campo "código" das planilhas corresponde ao código INEP em todos os registros].

O caráter anual das informações educacionais é um aspecto importante para a modelagem. A cada ano letivo, as matrículas mudam, a infraestrutura pode ser ampliada ou deteriorada e uma escola pode ser paralisada ou retomada. Sistemas que guardam apenas a situação atual perdem a possibilidade de comparar anos. Por isso, o SIGEI organiza os dados variáveis em registros anuais vinculados à escola, conforme descrito no Capítulo 4.

## 2.4 Sistemas de informação na gestão pública educacional

Stair e Reynolds (2015) distinguem dados de informação: dados são fatos isolados, enquanto a informação resulta da organização desses fatos de modo que tenham valor adicional para quem os utiliza. Laudon e Laudon (2014) definem sistema de informação como um conjunto de componentes inter-relacionados que coletam, processam, armazenam e distribuem informações para apoiar a tomada de decisão, a coordenação e o controle. O'Brien e Marakas (2013) destacam que os sistemas de informação apoiam as operações, a tomada de decisão e a vantagem estratégica das organizações.

Na administração pública, os sistemas de informação contribuem para a padronização dos registros, a redução de retrabalho e a disponibilidade de dados para o planejamento [REFERÊNCIA NECESSÁRIA: pesquisar fonte sobre sistemas de informação e governo digital na administração pública brasileira]. Na área educacional, um sistema que centraliza os cadastros permite que diferentes setores consultem a mesma informação e que as atualizações fiquem registradas em um único lugar.

Esses conceitos orientam três decisões do SIGEI: manter uma base única para os cadastros territoriais e escolares, separar os dados permanentes da escola dos dados que variam a cada ano e consolidar os dados em indicadores, como total de escolas, total de matrículas e percentual de escolas com internet.

## 2.5 Georreferenciamento e mapas na web

O geoprocessamento é a área que utiliza técnicas matemáticas e computacionais para tratar a informação geográfica (CÂMARA; DAVIS; MONTEIRO, 2001). Um Sistema de Informação Geográfica (SIG) integra dados espaciais, como pontos, linhas e polígonos, a atributos descritivos, permitindo consultar e analisar fenômenos de acordo com sua localização (LONGLEY *et al.*, 2013). O georreferenciamento consiste em associar a um objeto coordenadas em um sistema de referência conhecido, como latitude e longitude, de modo que ele possa ser posicionado em um mapa [REFERÊNCIA NECESSÁRIA: pesquisar fonte sobre georreferenciamento e sistemas de coordenadas geográficas].

A cartografia na web tornou possível consultar mapas interativos no navegador, com aproximação, deslocamento e seleção de elementos. Esses mapas costumam ser compostos por camadas: uma camada de fundo, formada por imagens divididas em blocos (*tiles*), e camadas de dados sobrepostas, como marcadores (MUEHLENHAUS, 2014) [CONFIRMAR: conteúdo do capítulo citado de Muehlenhaus (2014) antes de manter a citação]. Bibliotecas de código aberto, como o Leaflet, oferecem esses recursos para aplicações web (LEAFLET, [s.d.]).

No SIGEI, cada escola recebe latitude e longitude no registro de infraestrutura do ano. Essas coordenadas permitem posicionar a escola no mapa, e os atributos associados (situação, tipo de acesso, internet e prédio) podem ser usados como filtros. Assim, o mapa público do sistema funciona como uma forma simples de consulta geográfica, sem as funções de análise espacial de um SIG completo.

## 2.6 Aplicações web e APIs REST

Aplicações web são sistemas acessados pelo navegador, sem instalação no computador do usuário. Em uma arquitetura cliente-servidor, a interface executa no navegador e solicita dados a um servidor, que aplica as regras de negócio e acessa o banco de dados [REFERÊNCIA NECESSÁRIA: pesquisar fonte sobre arquitetura cliente-servidor em aplicações web]. Nas aplicações de página única (*Single Page Application*, SPA), o navegador carrega a aplicação uma vez e passa a trocar apenas dados com o servidor, atualizando a tela sem recarregar a página inteira [REFERÊNCIA NECESSÁRIA: pesquisar fonte sobre SPA, por exemplo a documentação oficial do Angular].

O estilo arquitetural REST (*Representational State Transfer*) foi descrito por Fielding (2000) como um conjunto de restrições para sistemas distribuídos em rede, entre elas a separação entre cliente e servidor, a comunicação sem estado, o uso de cache, a interface uniforme e o sistema em camadas. Em uma API REST, os dados são organizados em recursos identificados por URIs e manipulados por métodos HTTP, como `GET`, `POST`, `PUT` e `DELETE` (RICHARDSON; AMUNDSEN, 2013). As representações costumam ser trocadas no formato JSON (*JavaScript Object Notation*), um formato textual leve para dados estruturados (BRAY, 2017).

A comunicação sem estado (*stateless*) exige que cada requisição traga as informações necessárias para ser atendida, inclusive a identificação do usuário. Uma forma comum de fazer isso é o JSON Web Token (JWT), um formato compacto para transmitir declarações (*claims*) entre duas partes, assinado digitalmente para garantir sua integridade (JONES; BRADLEY; SAKIMURA, 2015). Quando o *token* é guardado em um *cookie*, o navegador o envia automaticamente, o que exige proteção contra a falsificação de requisições entre sites (*Cross-Site Request Forgery*, CSRF) [REFERÊNCIA NECESSÁRIA: pesquisar fonte sobre CSRF e cookies HttpOnly, por exemplo a documentação da OWASP].

O SIGEI adota esse modelo: uma SPA desenvolvida em Angular consome uma API REST desenvolvida em Spring Boot, que troca dados em JSON e identifica o usuário por JWT. Os detalhes dessa arquitetura estão no Capítulo 4.

## 2.7 Trabalhos relacionados

[SEÇÃO A COMPLETAR após a pesquisa bibliográfica. Nenhum trabalho relacionado consta no repositório do projeto, e esta seção não deve ser escrita com sistemas ou publicações cuja existência e cujas funcionalidades não tenham sido verificadas.]

Sugere-se pesquisar e comparar ao SIGEI ao menos três tipos de trabalho:

a) ferramentas oficiais de consulta a dados de escolas, como as disponibilizadas pelo INEP a partir do Censo Escolar [REFERÊNCIA NECESSÁRIA: identificar a ferramenta, o endereço eletrônico e a data de acesso];

b) sistemas de gestão escolar adotados por secretarias de educação [REFERÊNCIA NECESSÁRIA: identificar exemplos documentados];

c) trabalhos acadêmicos sobre sistemas de informação ou mapeamento aplicados à educação escolar indígena ou à educação no campo [REFERÊNCIA NECESSÁRIA: pesquisar em repositórios como o Portal de Periódicos da CAPES, a SciELO, a Biblioteca Digital Brasileira de Teses e Dissertações e os anais de eventos da Sociedade Brasileira de Computação].

Para cada trabalho encontrado, sugere-se registrar no Quadro 1 o objetivo, o público, as funcionalidades e a diferença em relação ao SIGEI. Os critérios propostos para a comparação são: foco nas escolas indígenas, organização dos dados por ano, mapa interativo, indicadores consolidados e controle de acesso.

**Quadro 1 – Comparação entre trabalhos relacionados e o SIGEI**

| Trabalho | Objetivo | Mapa | Histórico anual | Foco em escolas indígenas | Diferença em relação ao SIGEI |
| --- | --- | --- | --- | --- | --- |
| [A PREENCHER] | | | | | |
| [A PREENCHER] | | | | | |
| SIGEI | Centralizar dados das escolas indígenas do Acre | Sim | Sim | Sim | — |

Fonte: Elaborado pelo autor.
