# 4 SIGEI – SISTEMA DE GERENCIAMENTO ESCOLAR INDÍGENA

O SIGEI é uma aplicação web que centraliza as informações das escolas indígenas do Acre. O sistema reúne o cadastro territorial (municípios, terras indígenas, povos e aldeias), o cadastro das escolas e os dados que mudam a cada ano letivo (equipe escolar, infraestrutura e matrículas), mantendo um histórico por ano de referência.

O sistema se divide em duas áreas. A **área pública** é um mapa interativo, acessível sem *login*, que mostra a localização das escolas e permite consultá-las por nome, código e características. A **área administrativa** exige autenticação e reúne o painel de indicadores, os cadastros, o histórico escolar e o gerenciamento de usuários.

O público-alvo principal são os profissionais da SEE/AC que acompanham as escolas indígenas [CONFIRMAR: setor ou coordenação responsável]. Os usuários com perfil de administrador cadastram e atualizam os dados; os usuários com perfil de consulta apenas os visualizam. O mapa público pode ser consultado por qualquer pessoa.

Este capítulo descreve a versão do sistema registrada no repositório em 6 de outubro de 2026. A seção 4.1 apresenta as funcionalidades, a seção 4.2 descreve a arquitetura e a seção 4.3 apresenta os testes.

## 4.1 Funcionalidades

As funcionalidades são apresentadas na ordem em que o usuário as encontra: primeiro o mapa público, que é a página inicial, depois a autenticação e, em seguida, as telas da área administrativa, na ordem do menu lateral.

### 4.1.1 Mapa público interativo

O mapa público é a página inicial do SIGEI. Seu objetivo é permitir que qualquer pessoa visualize onde estão as escolas indígenas do Acre e consulte informações básicas de cada uma, sem necessidade de cadastro.

Ao abrir a página, o mapa é centralizado no estado do Acre, e cada escola com coordenadas válidas é representada por um marcador. A cor do marcador indica a situação da escola: verde para as escolas em funcionamento e vermelho para as que estão fora de funcionamento, isto é, paralisadas ou desativadas. Uma legenda no canto do mapa explica essas cores (Figura 7).

[INSERIR FIGURA 7 – Mapa público com os marcadores das escolas]

Figura 7 – Mapa público do SIGEI
Fonte: Elaborado pelo autor.

O usuário pode alternar entre quatro camadas de fundo: padrão (OpenStreetMap), satélite (Esri World Imagery), terreno (OpenTopoMap) e escuro (CARTO). A camada de satélite ajuda a identificar a localização das aldeias em relação a rios e áreas de floresta, enquanto a camada de terreno mostra o relevo.

A barra de busca localiza escolas pelo nome ou pelo código INEP. A busca desconsidera acentos e diferenças entre maiúsculas e minúsculas, e, ao selecionar um resultado, o mapa se aproxima da escola e abre suas informações. O painel de filtros permite restringir os marcadores por situação (funcionando, paralisada ou desativada), por tipo de acesso (terrestre, fluvial ou misto) e pelas opções "apenas com internet" e "apenas com prédio próprio" (Figura 8). As opções de situação e de acesso são as mesmas do formulário de escolas (seção 4.1.9), pois as duas telas usam uma única definição de opções.

[INSERIR FIGURA 8 – Painel de filtros e barra de busca do mapa público]

Figura 8 – Busca e filtros do mapa público
Fonte: Elaborado pelo autor.

Ao clicar em um marcador, abre-se uma janela (*popup*) com a fotografia da escola, quando houver, o nome, o código INEP, a situação, o tipo de acesso e indicadores de internet e de prédio próprio (Figura 9).

[INSERIR FIGURA 9 – Janela de informações de uma escola no mapa]

Figura 9 – Informações de uma escola no mapa público
Fonte: Elaborado pelo autor.

Em relação ao *back-end*, o mapa consome o *endpoint* `GET /infraestrutura/maisRecentes`. As consultas de infraestrutura são o único recurso de dados liberado sem autenticação. Como a infraestrutura é registrada a cada ano, uma escola pode ter vários registros. O *endpoint* devolve apenas um por escola, o do ano mais recente que tenha infraestrutura cadastrada, para que cada escola apareça com um único marcador e com seus dados atuais. Cada registro contém as coordenadas, a situação, o tipo de acesso, os indicadores de internet e prédio e a fotografia, além de um resumo da escola. Os filtros e a busca são aplicados no próprio navegador, sobre os dados já carregados, sem novas requisições à API.

### 4.1.2 Autenticação e troca obrigatória de senha

A autenticação controla o acesso à área administrativa. O acesso é feito pelo botão "Área administrativa", no cabeçalho do mapa, que leva à tela de *login*. Nela, o usuário informa o e-mail e a senha (Figura 10).

[INSERIR FIGURA 10 – Tela de login do SIGEI]

Figura 10 – Tela de autenticação do SIGEI
Fonte: Elaborado pelo autor.

Ao enviar os dados, o *front-end* chama `POST /auth/login`. O *back-end* localiza o usuário pelo e-mail, compara a senha informada com o *hash* armazenado e, por fim, verifica se o usuário está ativo. Se as credenciais forem válidas, a resposta grava os *cookies* de sessão no navegador e devolve apenas os dados de exibição do usuário: identificador, nome, e-mail, perfil e se ele precisa trocar a senha. O usuário é então direcionado ao painel de indicadores.

Quando o e-mail não está cadastrado ou a senha está incorreta, a resposta é a mesma: código 401 e a mensagem "E-mail ou senha inválidos". Assim, uma tentativa de acesso não revela quais e-mails existem no sistema. Pelo mesmo motivo, a comparação da senha é feita mesmo quando o e-mail não existe, de modo que o tempo de resposta também não diferencie os dois casos. A informação de que o usuário está desativado é dada apenas a quem informa a senha correta, com o código 403, e a tela de *login* exibe uma mensagem orientando a procurar o administrador.

Todo usuário novo, inclusive o administrador criado na primeira execução do sistema, é cadastrado com a indicação de troca obrigatória de senha. O mesmo ocorre quando um administrador redefine a senha de outro usuário. Nesse caso, após o *login*, o usuário é levado à tela de alteração de senha, onde informa a senha atual, a nova senha, com no mínimo 8 caracteres, e a confirmação (Figura 11). Enquanto a troca não é feita, o *back-end* não concede ao usuário as permissões do seu perfil, apenas a permissão de trocar a senha.

[INSERIR FIGURA 11 – Tela de alteração de senha]

Figura 11 – Tela de alteração de senha
Fonte: Elaborado pelo autor.

A troca é processada por `PUT /auth/alterar-senha`, que verifica a senha atual, impede a reutilização da mesma senha, grava a nova senha cifrada e encerra todas as sessões do usuário, exigindo um novo *login*. A mesma tela fica disponível no cabeçalho da área administrativa para que o usuário altere sua senha quando quiser.

A sessão é renovada automaticamente: quando o *token* de acesso, válido por 15 minutos, expira, o *front-end* solicita um novo par de *tokens* a `POST /auth/refresh` e repete a requisição que havia falhado, sem que o usuário perceba. O botão "Sair" chama `POST /auth/logout`, que revoga a sessão no servidor. Esses mecanismos são detalhados na seção 4.2.5.

### 4.1.3 Tela inicial: painel de indicadores

Após o *login*, o usuário é levado ao painel de indicadores, chamado na interface de "Painel gerencial". Seu objetivo é oferecer uma visão consolidada das escolas indígenas em um ano de referência, sem que o usuário precise consultar os cadastros individualmente.

No topo, o painel exibe quatro totais: escolas, matrículas, municípios e terras indígenas (Figura 12). Um campo permite escolher o ano de referência; quando nenhum ano é escolhido, o sistema usa o ano mais recente cadastrado.

[INSERIR FIGURA 12 – Painel de indicadores: totais e seleção do ano]

Figura 12 – Painel de indicadores do SIGEI
Fonte: Elaborado pelo autor.

Abaixo dos totais, o painel apresenta: a evolução anual do número de matrículas e de escolas com registro, em gráfico de barras; o percentual de escolas com internet, com energia e com prédio próprio; a distribuição das matrículas por nível; a quantidade de escolas por município; e a situação das escolas conforme informada na infraestrutura (Figura 13). Ao passar o cursor sobre as barras, o valor exato é exibido.

[INSERIR FIGURA 13 – Gráficos do painel: evolução anual, infraestrutura, matrículas por nível, escolas por município e situação]

Figura 13 – Indicadores detalhados do painel
Fonte: Elaborado pelo autor.

Todos os dados vêm de uma única chamada a `GET /dashboard`, com o ano como parâmetro opcional. No *back-end*, o `DashboardService` resolve o ano e executa consultas de agregação definidas no `DashboardRepository`. As séries de evolução abrangem todos os anos cadastrados, enquanto os demais indicadores se referem apenas ao ano selecionado. Os gráficos são desenhados com elementos HTML, cuja altura ou largura é proporcional ao valor.

### 4.1.4 Municípios

A tela de municípios permite consultar os municípios do Acre e, para cada um, a quantidade de terras indígenas, aldeias e escolas vinculadas (Figura 14). A lista tem busca e paginação.

[INSERIR FIGURA 14 – Tela de municípios com estatísticas]

Figura 14 – Consulta de municípios
Fonte: Elaborado pelo autor.

Os municípios não são cadastrados pela interface. Na primeira execução, o *back-end* cadastra automaticamente os 22 municípios do Acre, comparando os nomes de forma normalizada para não criar duplicatas. Essa decisão evita erros de digitação em um cadastro que muda raramente. A tela consome `GET /municipio/estatisticas`, que retorna os municípios com as contagens já calculadas.

### 4.1.5 Terras indígenas

A tela de terras indígenas lista as terras cadastradas com o município, a quantidade de aldeias e a quantidade de escolas de cada uma (Figura 15). O usuário pode buscar pelo nome e navegar entre páginas.

[INSERIR FIGURA 15 – Listagem de terras indígenas]

Figura 15 – Listagem de terras indígenas
Fonte: Elaborado pelo autor.

O administrador pode cadastrar uma nova terra indígena, informando o nome e o município (Figura 16), e também editar ou excluir as existentes. A exclusão exige confirmação em uma janela de diálogo. Para o usuário de consulta, os botões de cadastro, edição e exclusão não são exibidos.

[INSERIR FIGURA 16 – Formulário de cadastro de terra indígena]

Figura 16 – Cadastro de terra indígena
Fonte: Elaborado pelo autor.

A listagem usa `GET /terra/estatisticas`; o formulário usa `POST /terra/salvar` e `PUT /terra/editar`; a exclusão, `DELETE /terra/excluir/{id}`. Se a terra indígena tiver aldeias vinculadas, a chave estrangeira do banco de dados impede a exclusão, e a violação de integridade é convertida pela API na resposta 409 (conflito) [CONFIRMAR: verificar esse retorno em teste manual antes da entrega].

### 4.1.6 Povos indígenas

A tela de povos indígenas lista os povos cadastrados e a quantidade de aldeias vinculadas a cada um. Uma janela permite ver quais são essas aldeias (Figura 17). O administrador pode cadastrar, editar e excluir povos, informando apenas o nome.

[INSERIR FIGURA 17 – Listagem de povos indígenas com as aldeias vinculadas]

Figura 17 – Listagem de povos indígenas
Fonte: Elaborado pelo autor.

No *back-end*, a tela utiliza o recurso `/povo`, com as operações de cadastro, consulta, edição e exclusão.

### 4.1.7 Aldeias

A tela de aldeias lista as aldeias com a terra indígena e o povo de cada uma (Figura 18). No formulário, o administrador informa o nome da aldeia e seleciona a terra indígena e o povo, ambos obrigatórios (Figura 19).

[INSERIR FIGURA 18 – Listagem de aldeias]

Figura 18 – Listagem de aldeias
Fonte: Elaborado pelo autor.

[INSERIR FIGURA 19 – Formulário de cadastro de aldeia]

Figura 19 – Cadastro de aldeia
Fonte: Elaborado pelo autor.

A aldeia é o elo entre o território e a escola: cada escola é vinculada a uma aldeia, e por meio dela o sistema identifica a terra indígena, o povo e o município da escola. O recurso correspondente na API é `/aldeia`.

### 4.1.8 Gerenciamento de escolas

A tela de escolas é o ponto de partida para consultar e manter as escolas. No topo, três indicadores mostram o número de escolas cadastradas, de aldeias atendidas e de terras indígenas. Abaixo, uma tabela lista as escolas com tipo de unidade, aldeia, terra indígena, município e povo, com busca por qualquer desses campos e paginação (Figura 20).

[INSERIR FIGURA 20 – Listagem de escolas]

Figura 20 – Listagem de escolas
Fonte: Elaborado pelo autor.

Pela listagem, o administrador pode iniciar o cadastro de uma escola, editar uma escola existente, excluí-la ou abrir um novo ano letivo (seção 4.1.11). A exclusão pede confirmação, informando o nome da escola (Figura 21). Como os registros anuais pertencem à escola, a exclusão remove também todo o histórico da unidade.

[INSERIR FIGURA 21 – Janela de confirmação de exclusão de escola]

Figura 21 – Confirmação de exclusão de escola
Fonte: Elaborado pelo autor.

A listagem utiliza `GET /escola`, que retorna as escolas com os dados da aldeia, da terra indígena, do município e do povo. A exclusão utiliza `DELETE /escola/excluir/{id}`.

### 4.1.9 Cadastro de escolas em etapas

O cadastro de uma escola reúne muitas informações: dados de identificação, dados do ano, equipe, infraestrutura e matrículas. Para não apresentar tudo em uma única tela, o formulário foi dividido em sete etapas, cada uma com sua validação. O usuário só avança quando a etapa atual está correta e pode voltar às anteriores a qualquer momento.

**Etapa 1 – Dados da escola.** O usuário informa o código INEP, o nome, o tipo de unidade (sede ou anexo) e a aldeia (Figura 22). A lista de aldeias mostra apenas as que ainda não têm escola vinculada, pois cada aldeia pode ter no máximo uma escola. Quando o tipo é "anexo", aparece um campo obrigatório para escolher a escola-sede à qual o anexo está vinculado; a lista mostra apenas escolas do tipo sede.

[INSERIR FIGURA 22 – Etapa 1 do cadastro: dados da escola e vínculo com a escola-sede]

Figura 22 – Cadastro de escola: dados da escola
Fonte: Elaborado pelo autor.

**Etapa 2 – Registro anual.** O usuário escolhe o ano de referência, a rede (estadual ou municipal), o local de funcionamento (rural ou urbano) e, opcionalmente, o total de alunos (Figura 23). Se o ano desejado não estiver na lista, é possível incluir um ano anterior diretamente nessa etapa.

[INSERIR FIGURA 23 – Etapa 2 do cadastro: registro anual]

Figura 23 – Cadastro de escola: registro anual
Fonte: Elaborado pelo autor.

**Etapa 3 – Equipe escolar.** São seis perguntas de resposta "sim" ou "não": se a escola possui professor responsável, equipe gestora, merendeira, zeladora e barqueiro, e se necessita de transporte (Figura 24).

[INSERIR FIGURA 24 – Etapa 3 do cadastro: equipe escolar]

Figura 24 – Cadastro de escola: equipe escolar
Fonte: Elaborado pelo autor.

**Etapa 4 – Infraestrutura e fotografia.** O usuário informa o ano de construção, o número de salas, a latitude e a longitude, a situação (funcionando, paralisada ou desativada), o tipo de acesso (terrestre, fluvial ou misto), o abastecimento de água (rede pública, poço, cacimba, rio, igarapé, vertente, rio/vertente, outro ou não possui), a fonte de energia (rede pública, gerador, solar, outra ou não possui), se há internet e prédio próprio e se há registro fotográfico (Figura 25). Quando essa última opção é marcada, é obrigatório enviar uma foto, que pode ser substituída ou removida na edição.

As opções de situação, acesso, água e energia foram definidas a partir dos valores encontrados na planilha de infraestrutura e são as mesmas em todo o sistema: no formulário, no filtro do mapa, na API e na importação. A API recusa, com o código 400, valores que não pertençam a essas opções.

[INSERIR FIGURA 25 – Etapa 4 do cadastro: infraestrutura e envio de fotografia]

Figura 25 – Cadastro de escola: infraestrutura
Fonte: Elaborado pelo autor.

**Etapas 5 e 6 – Matrículas.** A etapa 5 registra as matrículas por nível, separadas em masculino e feminino: ensino infantil, ensino fundamental anos iniciais, ensino fundamental anos finais, ensino médio, EJA e AEE (Figura 26). A etapa 6 registra as matrículas por série ou etapa: do 1º ao 9º ano do ensino fundamental, da 1ª à 3ª série do ensino médio e os ciclos 1 e 2 da EJA (Figura 27).

[INSERIR FIGURA 26 – Etapa 5 do cadastro: matrículas por nível]

Figura 26 – Cadastro de escola: matrículas por nível
Fonte: Elaborado pelo autor.

[INSERIR FIGURA 27 – Etapa 6 do cadastro: matrículas por série]

Figura 27 – Cadastro de escola: matrículas por série
Fonte: Elaborado pelo autor.

**Etapa 7 – Revisão.** Antes de salvar, o sistema apresenta um resumo de todas as etapas para conferência (Figura 28). Ao confirmar, os dados são gravados, e uma mensagem de sucesso é exibida.

[INSERIR FIGURA 28 – Etapa 7 do cadastro: revisão dos dados]

Figura 28 – Cadastro de escola: revisão
Fonte: Elaborado pelo autor.

Em relação ao *back-end*, o formulário envia uma única requisição, do tipo *multipart*, com os dados em JSON e a foto opcional: `POST /escola/salvarCompleto` no cadastro e `PUT /escola/editarCompleto` na edição. O `CadastroEscolaService` grava, em uma única transação, a escola, o registro do ano, a equipe, a infraestrutura e as matrículas. Se o registro daquele ano já existir, ele é atualizado; caso contrário, é criado, e os registros dos outros anos não são alterados. A foto é enviada ao Cloudinary somente depois que os dados foram validados pelo banco; se a transação for desfeita após o envio, a imagem é removida, para não deixar arquivos sem vínculo.

Um cuidado foi necessário para garantir esse comportamento. Por padrão, o Spring desfaz uma transação apenas quando ocorre uma exceção não verificada, e o envio da foto pode lançar `IOException`, que é uma exceção verificada. Sem tratamento, uma falha de comunicação com o Cloudinary confirmaria a transação, gravando a escola sem a foto, e uma nova tentativa do usuário criaria uma escola duplicada. Por isso, os métodos de gravação declaram `@Transactional(rollbackFor = IOException.class)`. Os testes automatizados verificam que uma falha em uma etapa posterior da gravação ou no envio da foto, inclusive por `IOException`, desfaz todo o cadastro ou toda a edição, sem deixá-los pela metade. A Figura 29 apresenta essa sequência.

[INSERIR FIGURA 29 – Diagrama de sequência do cadastro completo de escola (exportar de `diagramas/14-sequencia-cadastro-completo.md`)]

Figura 29 – Sequência do cadastro completo de escola
Fonte: Elaborado pelo autor.

### 4.1.10 Histórico escolar

A tela de histórico escolar permite consultar os dados registrados ao longo dos anos. Ela tem dois modos de consulta.

No modo **por ano**, o usuário escolhe um ano de referência e vê a lista de escolas com registro naquele ano, com rede, local de funcionamento e total de alunos (Figura 30). A lista tem busca por escola, código ou rede, e paginação.

[INSERIR FIGURA 30 – Histórico escolar: consulta por ano]

Figura 30 – Histórico escolar por ano de referência
Fonte: Elaborado pelo autor.

No modo **por escola**, o usuário escolhe uma escola e, opcionalmente, um período (ano inicial e ano final). O sistema mostra todos os anos registrados daquela escola, em ordem cronológica, com rede, funcionamento, total de alunos e número de salas (Figura 31). Nesse modo, o administrador pode editar os dados de um ano específico ou cadastrar dados de um ano anterior; em ambos os casos, o formulário de cadastro é aberto diretamente na etapa do registro anual, preservando os demais anos.

[INSERIR FIGURA 31 – Histórico escolar: evolução de uma escola ao longo dos anos]

Figura 31 – Histórico escolar por escola
Fonte: Elaborado pelo autor.

Em qualquer dos modos, é possível abrir o detalhe de um registro, que mostra em uma janela os dados da escola, da equipe, da infraestrutura e das matrículas daquele ano (Figura 32).

[INSERIR FIGURA 32 – Janela de detalhe de um registro anual]

Figura 32 – Detalhe de um registro anual
Fonte: Elaborado pelo autor.

O modo por ano utiliza `GET /registroAnual/buscarPorAno?ano=`, e o modo por escola, `GET /registroAnual/historico?escolaId=&anoInicial=&anoFinal=`. Os limites do período são opcionais e inclusivos, e anos sem registro não aparecem na resposta.

O histórico escolar mostra o estado dos dados em cada ano. Quem fez cada alteração e quais eram os valores anteriores ficam registrados na trilha de auditoria, descrita na seção 4.2.6, que por enquanto é consultada apenas pela API.

### 4.1.11 Abertura de novo ano letivo

A abertura de novo ano prepara o sistema para o registro de um novo ano letivo. Sem essa funcionalidade, o administrador teria de cadastrar o novo ano escola por escola, repetindo dados que, em grande parte, não mudam de um ano para o outro.

O botão "Iniciar Novo Ano", na tela de escolas, abre uma janela de confirmação (Figura 33). Ao confirmar, o sistema cria o ano seguinte ao maior ano cadastrado e, para cada escola que tenha algum registro, cria um registro no novo ano copiando do registro mais recente a rede, o local de funcionamento, o total de alunos, a equipe, a infraestrutura (inclusive o endereço da fotografia) e as matrículas por nível e por série, com seus totais. A partir daí, o administrador edita apenas o que mudou. Como o painel de indicadores mostra, por padrão, o ano mais recente, a cópia dos totais evita que os indicadores do novo ano apareçam zerados logo após a abertura.

[INSERIR FIGURA 33 – Janela de confirmação da abertura de novo ano letivo]

Figura 33 – Abertura de novo ano letivo
Fonte: Elaborado pelo autor.

A operação é executada por `POST /atualizacao-anual/iniciar`, no `AtualizacaoAnualService`, em uma única transação. Se o ano seguinte já existir, a operação é recusada. Escolas sem nenhum registro anterior são ignoradas. A Figura 34 apresenta o fluxo dessa operação.

[INSERIR FIGURA 34 – Diagrama de atividades da abertura de novo ano (exportar de `diagramas/16-atividade-abertura-ano.md`)]

Figura 34 – Fluxo da abertura de novo ano letivo
Fonte: Elaborado pelo autor.

A fotografia é compartilhada entre os anos: o novo registro recebe o endereço da imagem, mas não o seu identificador no Cloudinary, que permanece apenas no registro que fez o envio. Com isso, a remoção da foto em um ano não apaga a imagem exibida nos outros. Um teste automatizado verifica a cópia de cada campo.

### 4.1.12 Gerenciamento de usuários

A tela de usuários lista as pessoas com acesso ao sistema, com nome, e-mail, perfil e situação (ativo ou inativo), e permite buscar por nome, e-mail ou perfil (Figura 35).

[INSERIR FIGURA 35 – Listagem de usuários]

Figura 35 – Listagem de usuários
Fonte: Elaborado pelo autor.

O administrador pode cadastrar um usuário informando nome, e-mail, senha inicial, perfil (administrador ou usuário) e se ele está ativo (Figura 36). Na edição, a senha pode ficar em branco para manter a atual. Quando uma senha é definida pelo administrador, o usuário precisa trocá-la no próximo acesso, como descrito na seção 4.1.2. Um usuário inativo não consegue entrar no sistema, e suas sessões deixam de ser renovadas.

[INSERIR FIGURA 36 – Formulário de cadastro de usuário]

Figura 36 – Cadastro de usuário
Fonte: Elaborado pelo autor.

A tela utiliza o recurso `/usuario`. A API nunca devolve a senha, nem mesmo cifrada, nas respostas.

### 4.1.13 Configurações

A tela de configurações permite que cada usuário personalize o comportamento do mapa público no seu navegador: a camada de fundo inicial, a abertura das informações da escola ao passar o cursor sobre o marcador e a exibição das fotografias nas janelas do mapa (Figura 37). As preferências são gravadas no armazenamento local do navegador e não são compartilhadas entre usuários ou dispositivos.

[INSERIR FIGURA 37 – Tela de configurações]

Figura 37 – Configurações do SIGEI
Fonte: Elaborado pelo autor.

[ATENÇÃO — trecho que depende de correção no sistema: a tela também exibe opções de identificação institucional, página inicial, menu compacto, redução de animações, confirmação de exclusões e lembrete de ano letivo, que são salvas mas ainda não produzem efeito. Ao capturar a figura, decida se essas opções serão removidas, implementadas ou citadas como em desenvolvimento.]

### 4.1.14 Carga inicial e importação de planilhas

Para que o sistema começasse a ser usado com dados reais, foram implementados a carga inicial automática e dois *endpoints* de importação de planilhas. Essas funções não têm tela própria.

Na primeira execução com o banco vazio, o sistema cadastra os municípios do Acre, importa a planilha de infraestrutura e, em seguida, a planilha de matrículas, ambas armazenadas junto à aplicação. Também cria o usuário administrador inicial, com troca de senha obrigatória.

A importação também pode ser feita por um administrador, enviando os arquivos para `POST /importacoes/infraestrutura` e `POST /importacoes/matriculas`. A importação de infraestrutura lê cada linha da planilha e cria, quando ainda não existem, o município, a terra indígena, o povo, a aldeia e a escola, além do registro anual, da infraestrutura e da equipe. A importação de matrículas localiza a escola e o registro anual correspondentes e grava as matrículas por nível e por série. As coordenadas são convertidas para o formato decimal quando necessário.

Os valores de situação, acesso, água e energia são convertidos para as opções do sistema, sem diferenciar acentos e maiúsculas: "POÇO" é gravado como poço, e "REDE ELETRICA", como rede pública. Na energia, as escolas marcadas apenas com "SIM" passam a ter a fonte "outra", pois a planilha indica que há energia, mas não informa a fonte. Valores que não correspondem a nenhuma opção são gravados como não informados. É o caso das sete escolas paralisadas da planilha, que trazem "DESATIVADA" nas colunas de acesso, água e energia; esses campos precisam ser preenchidos na próxima edição de cada escola.

Cada importação devolve um resumo com o número de linhas lidas, os registros criados ou atualizados, as linhas ignoradas e a lista de erros encontrados (Figura 38). Uma resposta de sucesso, portanto, não significa que todas as linhas foram importadas; é preciso conferir os contadores e os erros.

[INSERIR FIGURA 38 – Resultado de uma importação de planilha, exibido no Swagger ou no Bruno]

Figura 38 – Resultado da importação de planilha
Fonte: Elaborado pelo autor.

A importação foi construída para o formato das duas planilhas disponíveis, com abas e colunas fixas e o ano de 2025. Não se trata de um importador genérico.

## 4.2 Arquitetura do Sistema

O SIGEI segue a arquitetura cliente-servidor, com o *front-end* e o *back-end* desenvolvidos e executados de forma independente. O *front-end* é uma aplicação de página única executada no navegador; o *back-end* é uma API REST que concentra as regras de negócio e o acesso ao banco de dados. As fotografias ficam em um serviço externo de imagens, e os mapas de fundo vêm de servidores de *tiles* externos. A Figura 39 apresenta essa organização.

[INSERIR FIGURA 39 – Arquitetura geral do SIGEI (exportar de `diagramas/03-arquitetura-containers.md`)]

Figura 39 – Arquitetura geral do SIGEI
Fonte: Elaborado pelo autor.

### 4.2.1 *Front-end*

O *front-end* foi desenvolvido em Angular e organizado em quatro grupos de pastas, cada um com uma responsabilidade (Figura 40):

a) **`core`:** autenticação, com o serviço de sessão, o interceptador de requisições e as guardas de rota;

b) **`domains`:** serviços de acesso à API e modelos de dados de cada domínio (escolas, territórios e usuários);

c) **`features`:** telas do sistema, divididas em mapa público, autenticação e área administrativa;

d) **`shared`:** componentes reutilizados em várias telas, como as janelas de confirmação e de sucesso.

[INSERIR FIGURA 40 – Organização dos componentes do *front-end* (exportar de `diagramas/05-componentes-frontend.md`)]

Figura 40 – Componentes do *front-end*
Fonte: Elaborado pelo autor.

As rotas da área administrativa ficam sob o caminho `/admin` e são carregadas sob demanda. As guardas de rota impedem o acesso à área administrativa sem *login*, exigem a troca de senha quando ela é obrigatória e bloqueiam os formulários de cadastro e edição para usuários sem perfil de administrador. Essas guardas melhoram a experiência do usuário, mas a decisão de autorização é sempre do *back-end*.

Cada tela obtém seus dados por meio dos serviços da pasta `domains`, que encapsulam as chamadas HTTP e definem, em interfaces TypeScript, o formato dos dados enviados e recebidos.

### 4.2.2 *Back-end*

O *back-end* foi desenvolvido em Java com Spring Boot e organizado em camadas, cada uma em um pacote (Figura 41):

a) **`controllers`:** recebem as requisições HTTP, validam os dados de entrada e definem o código de resposta;

b) **`service`:** concentram as regras de negócio e delimitam as transações;

c) **`repository`:** interfaces do Spring Data JPA para acesso ao banco, incluindo as consultas de agregação do painel;

d) **`models`:** entidades JPA que representam as tabelas;

e) **`dto`:** objetos de transferência de dados, separados em requisição e resposta;

f) **`mappers`:** interfaces do MapStruct que convertem entidades em DTOs e vice-versa;

g) **`config`:** configuração de segurança, inicializadores da carga inicial, cliente do Cloudinary e registro do autor das alterações na trilha de auditoria;

h) **`exceptions`:** tratamento centralizado de erros, que converte exceções em respostas HTTP.

[INSERIR FIGURA 41 – Camadas e componentes do *back-end* (exportar de `diagramas/04-componentes-backend.md`)]

Figura 41 – Componentes do *back-end*
Fonte: Elaborado pelo autor.

O fluxo de uma requisição é o seguinte: o filtro de segurança identifica o usuário; o controlador valida o DTO de entrada; o serviço busca as entidades relacionadas pelos identificadores recebidos, aplica as regras de negócio e chama o repositório; o repositório executa a operação no banco; por fim, o *mapper* converte a entidade resultante em um DTO de resposta, que é devolvido em JSON. As entidades nunca são expostas diretamente pela API.

Os erros também seguem um padrão. Dados de entrada inválidos resultam em resposta 400, com a lista de campos e mensagens, assim como as violações de regras de negócio, como um anexo sem escola-sede ou um valor fora das opções de infraestrutura; registros inexistentes, em 404; violações de integridade do banco, como excluir uma terra indígena com aldeias vinculadas, em 409; e a ausência de configuração do serviço de imagens, em 503. No *login*, credenciais inválidas resultam em 401, e usuário desativado, em 403. As exceções são convertidas nessas respostas por um tratador centralizado.

A API possui 16 controladores, que expõem 70 operações. Todas estão documentadas com anotações OpenAPI, e a documentação interativa pode ser acessada pelo Swagger (Figura 42).

[INSERIR FIGURA 42 – Documentação interativa da API no Swagger]

Figura 42 – Documentação da API no Swagger
Fonte: Elaborado pelo autor.

### 4.2.3 Banco de dados

O banco de dados é o PostgreSQL, com 13 tabelas de domínio e de autenticação, além das tabelas da trilha de auditoria (seção 4.2.6), e seu esquema é versionado por migrações do Flyway. As relações apresentadas a seguir foram verificadas nos mapeamentos JPA e nas migrações.

**Território.** Um município pode ter várias terras indígenas, e cada terra indígena está associada a um município. Uma terra indígena pode ter várias aldeias, e cada aldeia pertence a uma terra indígena e é habitada por um povo; um povo pode estar em várias aldeias.

**Escola.** Cada escola está vinculada a uma aldeia, e cada aldeia pode ter no máximo uma escola, regra garantida por restrição de unicidade no banco. Uma escola pode ser anexo de outra escola, chamada escola-sede, e uma escola-sede pode ter vários anexos. Esse vínculo é representado por uma referência da própria tabela de escolas à escola-sede. O *back-end* garante as regras do vínculo: um anexo deve indicar uma escola-sede, que precisa ser do tipo sede e diferente do próprio anexo; uma escola-sede não tem sede; e uma escola com anexos não pode passar a ser anexo. Com isso, o vínculo tem um único nível e não forma ciclos.

Versões anteriores do sistema tinham também uma tabela `anexo`, que registrava o mesmo vínculo em paralelo, sem sincronização com a referência na tabela de escolas e sem uso pelo formulário. Para eliminar essa duplicidade, uma migração copiou para a tabela de escolas os vínculos válidos e removeu a tabela `anexo`.

**Dados anuais.** Uma escola pode ter vários registros anuais, um por ano de referência; a combinação de escola e ano é única. Cada registro anual tem uma equipe escolar e uma infraestrutura, e pode ter várias matrículas por nível e várias matrículas por série. Esses dados pertencem ao registro: quando um registro é excluído, eles também são excluídos, e o mesmo vale para os registros de uma escola excluída.

**Usuários e sessões.** Cada usuário pode ter vários *tokens* de renovação, um por sessão. Do *token*, o banco guarda apenas o *hash*, nunca o valor original. Quando um usuário é excluído, seus *tokens* são excluídos automaticamente.

As coordenadas geográficas ficam na infraestrutura, e não na escola. Com isso, a localização faz parte do registro de cada ano.

### 4.2.4 Comunicação entre *front-end* e *back-end*

A comunicação ocorre por requisições HTTP, com dados em JSON. O cadastro completo de escola é a exceção: usa o formato *multipart*, para enviar a foto junto com os dados.

O *front-end* acessa a API pela mesma origem da aplicação, usando o prefixo `/api`. Em produção, o servidor web Nginx entrega os arquivos do Angular e encaminha as requisições com esse prefixo à API, removendo o prefixo. Em desenvolvimento, o servidor do Angular faz o mesmo encaminhamento. Esse arranjo é necessário porque os *cookies* de sessão só são enviados em requisições à mesma origem.

O caminho de uma requisição é o seguinte: Angular → requisição HTTP para `/api/...` → Nginx → filtro de segurança → controlador → serviço → repositório → PostgreSQL. A resposta percorre o caminho inverso: o repositório devolve a entidade, o serviço a converte em DTO, o controlador define o código HTTP e o Angular recebe o JSON e atualiza a tela. A Figura 43 apresenta esse fluxo para o *login*.

[INSERIR FIGURA 43 – Diagrama de sequência do login (exportar de `diagramas/11-sequencia-login.md`)]

Figura 43 – Sequência de autenticação no SIGEI
Fonte: Elaborado pelo autor.

### 4.2.5 Segurança e controle de acesso

A API não guarda estado de sessão em memória. Cada requisição é autenticada por um JWT assinado, que contém o e-mail, o identificador, o nome e o perfil do usuário. A cada requisição, o filtro de segurança valida o *token* e consulta o usuário no banco de dados; assim, a desativação de um usuário ou a mudança de perfil têm efeito imediato.

Os *tokens* são guardados em *cookies* `HttpOnly`, que *scripts* da página não conseguem ler, e `SameSite=Strict`, que o navegador não envia em requisições originadas de outros sites. O *token* de renovação é rotacionado a cada uso: o *token* usado é revogado e um novo é emitido. Se um *token* já revogado for apresentado novamente após um curto intervalo, o sistema interpreta isso como possível reutilização indevida e encerra todas as sessões do usuário. As requisições que alteram dados exigem ainda um *token* CSRF, enviado em um *cookie* legível e devolvido pelo Angular em um cabeçalho, o que impede que outro site faça alterações em nome do usuário. O Quadro 7 resume as regras de autorização.

**Quadro 7 – Regras de autorização da API**

| Requisição | Acesso |
| --- | --- |
| *Login*, renovação e encerramento de sessão | Público |
| Verificação de funcionamento (`/actuator/health`) e documentação (Swagger) | Público |
| Consultas de infraestrutura (`GET /infraestrutura` e derivadas), usadas pelo mapa | Público |
| Alteração da própria senha | Qualquer usuário autenticado |
| Trilha de auditoria de uma escola (`GET /escola/{id}/historico`) | Perfil `ADMIN` |
| Demais consultas (`GET`) | Perfis `ADMIN` e `USUARIO` |
| Cadastro, edição, exclusão, abertura de ano e importação | Perfil `ADMIN` |

Fonte: Elaborado pelo autor.

Uma requisição sem autenticação a uma rota protegida recebe a resposta 401; uma requisição de usuário autenticado sem permissão recebe 403. Ao receber 401, o *front-end* tenta renovar a sessão uma vez e, se não conseguir, leva o usuário à tela de *login*.

### 4.2.6 Trilha de auditoria

O histórico anual descrito na seção 4.1.10 mostra os dados de cada escola em cada ano, mas não indica quem os registrou nem como eram antes de uma correção. Para isso, o SIGEI mantém uma trilha de auditoria, implementada com o Hibernate Envers, módulo do Hibernate que registra as versões das entidades [REFERÊNCIA NECESSÁRIA: pesquisar a documentação oficial do Hibernate Envers].

Cada transação que cria, altera ou exclui dados gera uma revisão, com a data, a hora e o autor da alteração. O autor é obtido do usuário autenticado na requisição. O identificador, o e-mail e o nome do usuário são copiados para a revisão, sem chave estrangeira, para que a trilha se mantenha mesmo que o usuário seja renomeado ou excluído. As alterações feitas sem usuário autenticado, como a carga inicial, são atribuídas ao sistema. Os valores de cada versão ficam em tabelas de auditoria, uma para cada tabela de domínio e para a de usuários. A senha não é registrada. As exclusões guardam os últimos valores do registro, de modo que também é possível saber o que foi removido.

A trilha de uma escola pode ser consultada por `GET /escola/{id}/historico`, restrito a administradores. A resposta reúne as alterações da escola, de seus registros anuais e, em cada registro, da equipe, da infraestrutura e das matrículas, da mais recente para a mais antiga. Cada item informa o autor, a data e a hora, a operação (criação, alteração ou exclusão), o ano a que o dado pertence e somente os campos que mudaram, com o valor anterior e o novo. Uma escola excluída mantém seu histórico.

A migração que criou a trilha registrou, em uma revisão atribuída ao sistema, o estado de todos os dados existentes naquele momento. Assim, a primeira alteração de um dado antigo já mostra o valor anterior, embora a autoria e as datas de antes da migração não sejam conhecidas. As alterações feitas diretamente por SQL não passam pelo Envers; por isso, as migrações que corrigem dados registram seus ajustes na trilha em uma revisão própria.

### 4.2.7 Implantação

O sistema pode ser executado em contêineres Docker. O *back-end* tem um arquivo de composição com dois serviços, a API e o PostgreSQL, com um volume para preservar os dados. O *front-end* tem outro arquivo, com o Nginx servindo a aplicação compilada. O Nginx também define cabeçalhos de segurança, como a política de segurança de conteúdo (*Content Security Policy*). A Figura 44 apresenta essa topologia.

[INSERIR FIGURA 44 – Diagrama de implantação com Docker (exportar de `diagramas/20-implantacao-docker.md`)]

Figura 44 – Implantação do SIGEI com Docker
Fonte: Elaborado pelo autor.

[CONFIRMAR: se o sistema foi implantado em algum servidor (da SEE/AC, do IFAC ou de hospedagem externa). Se sim, descrever o ambiente; se não, informar que a execução foi feita em ambiente local e de rede local.]

## 4.3 Testes

Os testes do SIGEI foram feitos de duas formas: testes automatizados no *back-end*, escritos junto com o código, e testes manuais da API, feitos durante o desenvolvimento com a ferramenta Bruno e com o Swagger. O *front-end* tem a ferramenta de testes Vitest configurada, mas ainda não possui testes automatizados.

### 4.3.1 Testes da API

**Testes automatizados.** O *back-end* possui 34 classes de teste, que somam 236 casos de teste, organizados conforme o Quadro 8.

**Quadro 8 – Testes automatizados do *back-end***

| Tipo | Classes | Casos | O que verificam |
| --- | --- | --- | --- |
| Testes unitários | 11 | 103 | Regras de negócio de cada entidade, com os repositórios simulados pelo Mockito: cadastro, edição, exclusão, busca e rejeição de dados inválidos ou duplicados, incluindo as regras do vínculo entre escola-sede e anexo. Também a validação e a conversão das opções de situação, acesso, água e energia. |
| Testes de repositório | 13 | 72 | Consultas ao banco em memória H2, incluindo as agregações do painel de indicadores e a seleção da infraestrutura mais recente de cada escola para o mapa. |
| Testes de integração | 7 | 52 | Cadastro completo de escola em transação única, inclusive o desfazimento quando uma etapa ou o envio da foto falha; abertura de novo ano, com a cópia de cada campo; trilha de auditoria (autor, data, campos alterados e exclusões); ciclo de vida das sessões (renovação, rotação, reutilização, expiração e revogação); consulta do histórico com filtros de período; e, por meio de requisições HTTP, a gravação dos *cookies*, a exigência do *token* CSRF, as respostas de erro do *login*, o encerramento da sessão e a restrição da trilha de auditoria aos administradores. |
| Outros | 3 | 9 | Carregamento do contexto da aplicação, cadastro inicial dos municípios e aplicação das migrações em um PostgreSQL real, inclusive a conversão dos dados existentes. |
| **Total** | **34** | **236** | |

Fonte: Elaborado pelo autor.

A suíte foi executada em 6 de outubro de 2026 com o comando `mvnw clean test`. Foram executados 236 casos, sem falhas, sem erros e sem casos ignorados. Como o Docker estava disponível, os testes de migração foram executados em um PostgreSQL temporário. A Figura 45 apresenta o resultado.

[INSERIR FIGURA 45 – Resultado da execução dos testes automatizados no terminal]

Figura 45 – Execução dos testes automatizados do *back-end*
Fonte: Elaborado pelo autor.

[ATENÇÃO: a execução de 06/10/2026 usou o JDK 24 da máquina, com a opção `-Dmaven.compiler.proc=full` exigida pelo MapStruct nessa versão. Antes da entrega, execute novamente a suíte com o JDK 21 do projeto e com o Docker ativo, e atualize este parágrafo e a Figura 45 se o resultado mudar.]

**Testes manuais.** Durante o desenvolvimento, os *endpoints* foram testados manualmente com o Bruno, um cliente HTTP de código aberto. A coleção do projeto tem 51 requisições, organizadas por recurso, que cobrem o *login*, os cadastros, as consultas por ano e por escola, o envio de foto, a abertura de novo ano e as importações. Para cada requisição, foram observados o código de resposta e o conteúdo retornado. Sugere-se apresentar como evidência as seguintes capturas:

a) *login* com credenciais válidas, mostrando a resposta 200 com os dados do usuário e os *cookies* gravados (Figura 46);

b) tentativa de alteração sem autenticação, com resposta 401, e com usuário de perfil `USUARIO`, com resposta 403;

c) cadastro de uma aldeia ou terra indígena com resposta 201, e com dados inválidos, com resposta 400 e a lista de campos;

d) consulta do histórico de uma escola por período, com resposta 200;

e) exclusão de uma terra indígena com aldeias vinculadas, com resposta esperada 409.

[INSERIR FIGURA 46 – Requisição de login no Bruno e resposta da API]

Figura 46 – Teste manual do *login* com o Bruno
Fonte: Elaborado pelo autor.

[ATENÇÃO: a coleção do Bruno foi criada antes da mudança da sessão para *cookies* com proteção CSRF. Antes de capturar as figuras, atualize as requisições para enviar o cabeçalho `X-XSRF-TOKEN` ou use o Swagger, que já está configurado para isso. Não use nas capturas a senha real do administrador.]

### 4.3.2 Testes das funcionalidades

Os testes das funcionalidades verificam, pela interface, se cada requisito funcional é atendido. O Quadro 9 apresenta o roteiro proposto. A coluna de resultado deve ser preenchida a partir da execução real de cada caso.

**Quadro 9 – Roteiro de testes das funcionalidades**

| Caso | Requisitos | Procedimento | Resultado esperado | Resultado obtido |
| --- | --- | --- | --- | --- |
| CT01 | RF01, RF03 | Entrar com o administrador inicial | Redirecionamento à troca de senha; após a troca, novo *login* e acesso ao painel | [A PREENCHER] |
| CT02 | RF01 | Entrar com senha incorreta e, depois, com um e-mail não cadastrado | Acesso negado, com a mesma mensagem "E-mail ou senha inválidos" nos dois casos | [A PREENCHER] |
| CT03 | RF04 | Cadastrar usuário com perfil `USUARIO` e entrar com ele | Telas de consulta acessíveis; botões de cadastro, edição e exclusão ocultos | [A PREENCHER] |
| CT04 | RF06 a RF08 | Cadastrar terra indígena, povo e aldeia | Registros exibidos nas listagens | [A PREENCHER] |
| CT05 | RF09 a RF13 | Cadastrar escola nas sete etapas, com foto | Escola na listagem, marcador no mapa e foto na janela | [A PREENCHER] |
| CT06 | RF14 | Editar o ano de uma escola pelo histórico | Somente o ano escolhido é alterado | [A PREENCHER] |
| CT07 | RF14 | Cadastrar um ano anterior para uma escola | Novo ano no histórico da escola | [A PREENCHER] |
| CT08 | RF15 | Excluir uma escola | Escola removida da listagem e do mapa | [A PREENCHER] |
| CT09 | RF16 | Consultar o histórico por ano e por escola com período | Somente registros do ano ou do período | [A PREENCHER] |
| CT10 | RF17 | Abrir novo ano letivo | Novo ano criado com registros copiados; painel do novo ano com os mesmos totais do ano anterior | [A PREENCHER] |
| CT11 | RF18, RF19 | Buscar escola pelo código INEP e aplicar filtros no mapa | Mapa centralizado na escola; marcadores filtrados | [A PREENCHER] |
| CT12 | RF20 | Alterar o ano no painel | Indicadores atualizados para o ano escolhido | [A PREENCHER] |
| CT13 | RF23 | Alterar a camada inicial nas configurações | Mapa abre com a camada escolhida | [A PREENCHER] |
| CT14 | RF02 | Manter a sessão aberta por mais de 15 minutos e continuar usando | Sessão renovada sem novo *login* | [A PREENCHER] |
| CT15 | RF01, RF04 | Desativar um usuário e entrar com ele usando a senha correta | Acesso negado, com a mensagem de usuário desativado | [A PREENCHER] |

Fonte: Elaborado pelo autor.

[ATENÇÃO: este quadro é um roteiro. Não preencha a coluna "Resultado obtido" sem executar o caso. Se algum caso falhar, registre o resultado real e trate a falha nas limitações ou corrija o sistema antes.]

### 4.3.3 Validação do Sistema

[CONFIRMAR: se o SIGEI foi apresentado à SEE/AC ou a outros usuários potenciais. Escolha uma das versões abaixo e apague a outra.]

**Versão A – se houve validação:** descrever quem participou (cargo ou setor, sem expor nomes se não houver autorização), quando e como ocorreu (demonstração, uso assistido ou questionário), quais funcionalidades foram avaliadas, as observações recebidas e os ajustes feitos em seguida. Não acrescentar resultados que não tenham sido registrados.

**Versão B – se não houve validação:** Até a conclusão deste trabalho, o SIGEI não foi submetido à validação formal com a equipe da SEE/AC. A verificação do sistema baseou-se nos testes automatizados, nos testes manuais da API e nos testes das funcionalidades descritos nas seções anteriores. A validação com os futuros usuários, avaliando se o sistema atende às necessidades de consulta e cadastro da secretaria e se a interface é adequada ao uso cotidiano, fica como etapa necessária antes da adoção do sistema e é retomada nos trabalhos futuros.
