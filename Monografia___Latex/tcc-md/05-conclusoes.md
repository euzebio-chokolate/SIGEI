# 5 CONCLUSÕES E TRABALHOS FUTUROS

Este trabalho partiu do problema da organização das informações das escolas indígenas do Acre, que estavam distribuídas em planilhas separadas e sem um histórico organizado por ano. Para enfrentá-lo, foi desenvolvido o SIGEI, uma aplicação web que reúne em uma única base os dados territoriais, os dados cadastrais das escolas e os dados anuais de equipe, infraestrutura e matrículas.

O objetivo geral, de desenvolver uma aplicação web para centralizar o cadastro e a consulta dessas informações, com histórico anual, mapa interativo e indicadores consolidados, foi alcançado na forma de um sistema funcional, composto por uma API REST em Spring Boot, uma interface em Angular e um banco de dados PostgreSQL.

Em relação aos objetivos específicos:

a) a modelagem das informações, feita a partir das planilhas disponíveis, separou os dados estáveis da escola dos dados que variam a cada ano, organizando estes últimos em registros anuais vinculados à escola e ao ano de referência;

b) a API REST disponibiliza 72 operações para cadastro, consulta, consolidação e importação, com documentação interativa e esquema de banco versionado;

c) a interface web permite manter os cadastros territoriais e as escolas, com um formulário em sete etapas que é gravado em uma única transação, além da consulta ao histórico por ano e por escola, do cadastro de anos anteriores e da abertura de um novo ano letivo;

d) o mapa público apresenta a localização das escolas, com busca, filtros, camadas de fundo e informações resumidas de cada unidade;

e) o painel apresenta indicadores consolidados por ano, como totais, evolução histórica, matrículas por nível, infraestrutura e distribuição por município;

f) a autenticação e o controle de acesso foram implementados com dois perfis, troca obrigatória de senha, sessão em *cookies* protegidos e renovação automática;

g) o funcionamento do *back-end* foi verificado por 217 casos de teste automatizados e por testes manuais da API.

[AJUSTAR o item g) após a nova execução dos testes e o preenchimento do roteiro de testes das funcionalidades (Quadro 9).]

A principal contribuição do SIGEI é reunir em um único sistema informações antes distribuídas em arquivos isolados, vinculando cada escola ao seu território e mantendo seus dados organizados por ano. Com isso, consultas que antes exigiam cruzar planilhas manualmente, como a evolução das matrículas de uma escola ou as escolas sem internet de um município, passam a ser feitas no próprio sistema. O mapa público também amplia o acesso à informação sobre a localização das escolas indígenas no estado.

[CONFIRMAR antes de manter: se a SEE/AC avaliou ou passou a usar o sistema. Em caso afirmativo, citar aqui o resultado dessa avaliação. Em caso negativo, manter o parágrafo acima como descrição do potencial do sistema, sem afirmar ganhos que não foram medidos.]

## Limitações

A versão descrita neste trabalho tem as seguintes limitações:

a) o sistema não gera relatórios exportáveis, em formatos como PDF ou planilha; a consolidação dos dados está restrita ao painel de indicadores;

b) a importação de planilhas funciona apenas pela API, sem tela, e está presa ao formato das duas planilhas disponíveis e ao ano de 2025;

c) o histórico guarda um registro por escola e por ano, mas não registra quem alterou cada dado nem quando, ou seja, não há trilha de auditoria;

d) o *front-end* ainda não tem testes automatizados;

e) o sistema não passou por validação formal com os usuários da SEE/AC [AJUSTAR conforme a seção 4.3.3];

f) parte das preferências da tela de configurações é salva, mas ainda não produz efeito;

g) o vínculo entre escola-sede e anexo tem duas representações no banco de dados, que não são sincronizadas;

h) o esquema permite no máximo uma escola por aldeia, regra que precisa ser confirmada com a secretaria.

[ATUALIZAR esta lista após as correções que o autor fará no sistema. Itens que podem deixar de ser limitações após as correções: marcadores repetidos no mapa para escolas com mais de um ano registrado; cópia incompleta de campos na abertura de novo ano; desfazimento da transação em caso de `IOException`; respostas de erro do *login* (senha incorreta ou usuário inativo podem resultar em erro 500); divergência entre as opções de situação e de acesso do formulário de escolas e as do filtro do mapa.]

## Trabalhos futuros

Como continuidade deste trabalho, sugerem-se:

a) **validação com a SEE/AC:** apresentar o sistema à equipe responsável pelas escolas indígenas, avaliar se as informações registradas correspondem às acompanhadas pela secretaria e medir a usabilidade da interface com instrumentos apropriados;

b) **relatórios exportáveis:** gerar relatórios em PDF e em planilha a partir dos filtros do histórico e do painel, para uso em documentos oficiais;

c) **importação genérica:** permitir importar planilhas de qualquer ano por uma tela, com pré-visualização dos dados, indicação de erros por linha e confirmação antes da gravação;

d) **auditoria:** registrar o autor e a data de cada alteração, permitindo saber quem modificou um dado e qual era o valor anterior;

e) **integração com dados oficiais:** relacionar os cadastros a bases públicas, como os microdados do Censo Escolar, pelo código INEP;

f) **análise geográfica:** incluir os limites das terras indígenas e dos municípios no mapa e permitir consultas por região;

g) **testes do *front-end*:** criar testes de componentes e testes de ponta a ponta das principais telas;

h) **uso em campo:** permitir a coleta de dados em locais sem conexão, com sincronização posterior, considerando as condições de acesso às aldeias.
