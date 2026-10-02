# Alterações sugeridas para a monografia

Este arquivo reúne os trechos do `desenvolvimento.tex` que ficaram desatualizados após três melhorias no sistema:

1. **Cadastro de escola em transação única:** o formulário de escolas passou a enviar uma única requisição, e o *backend* grava escola, registro anual, equipe, infraestrutura, matrículas e fotografia em uma só transação.
2. **Renovação automática da sessão:** o *login* passou a emitir também um *refresh token*. Com ele, o *frontend* renova o *token* de acesso automaticamente, sem exigir nova autenticação.
3. **Tokens em cookies protegidos:** os *tokens* deixaram de ser guardados no `localStorage`. Agora ficam em *cookies* `HttpOnly` e `SameSite=Strict`, inacessíveis a *scripts* da página, e as requisições que alteram dados exigem um *token* CSRF. Para isso, o *frontend* passou a acessar a API pela mesma origem, por meio do *proxy* `/api`.

Cada item indica a seção, a linha aproximada no `desenvolvimento.tex`, o texto atual e o texto novo, já em LaTeX. Os números de linha valem para a versão atual do arquivo e mudam à medida que as alterações forem aplicadas; prefira localizar os trechos pelo texto.

---

## 1. Seção "Segurança e Controle de Acesso": fluxo de autenticação

**Onde:** lista `alineas` logo após "O fluxo ocorre da seguinte forma:" (linhas 109–115).

**Texto atual:**

```latex
\begin{alineas}
  \item o usuário informa e-mail e senha na tela de \textit{login};
  \item o \textit{backend} verifica se o usuário está ativo e compara a senha com o valor armazenado, cifrado com o algoritmo BCrypt;
  \item em caso de sucesso, é emitido um \textit{token} assinado com validade padrão de 24 horas, contendo a identificação e o perfil do usuário;
  \item o \textit{frontend} armazena o \textit{token} e o envia no cabeçalho \texttt{Authorization} de cada requisição por meio de um interceptador HTTP;
  \item a cada requisição, um filtro do \textit{backend} valida o \textit{token} e consulta novamente o cadastro do usuário, de modo que a desativação de uma conta tem efeito imediato.
\end{alineas}
```

**Substituir por:**

```latex
\begin{alineas}
  \item o usuário informa e-mail e senha na tela de \textit{login};
  \item o \textit{backend} verifica se o usuário está ativo e compara a senha com o valor armazenado, cifrado com o algoritmo BCrypt;
  \item em caso de sucesso, são emitidos dois \textit{tokens}: um \textit{token} de acesso JWT assinado, com validade padrão de 15 minutos, contendo a identificação e o perfil do usuário, e um \textit{token} de renovação (\textit{refresh token}), com validade padrão de 7 dias;
  \item os dois \textit{tokens} são entregues ao navegador em \textit{cookies} marcados como \texttt{HttpOnly}, que não podem ser lidos por \textit{scripts} da página, e o navegador os envia automaticamente em cada requisição à API;
  \item a cada requisição, um filtro do \textit{backend} valida o \textit{token} de acesso e consulta novamente o cadastro do usuário, de modo que a desativação de uma conta tem efeito imediato;
  \item quando o \textit{token} de acesso expira e o \textit{backend} responde com o código 401, um interceptador HTTP do \textit{frontend} solicita a renovação da sessão, o \textit{backend} grava novos \textit{cookies} e a requisição é repetida, sem exigir nova autenticação do usuário.
\end{alineas}
```

**Inserir logo após a lista**, antes de "Foram definidos dois perfis de acesso...":

```latex
O \textit{refresh token} é um valor aleatório opaco, e o banco de dados armazena apenas o seu resumo criptográfico (SHA-256), nunca o valor original. Cada \textit{refresh token} pode ser utilizado uma única vez: a cada renovação, o \textit{token} apresentado é revogado e outro é emitido, técnica conhecida como rotação. Caso um \textit{token} já utilizado seja reapresentado, o que indica um possível vazamento, todas as sessões do usuário são encerradas; reapresentações ocorridas nos 30 segundos seguintes à renovação são apenas recusadas, pois correspondem a abas abertas renovando a sessão ao mesmo tempo. As sessões também são revogadas quando o usuário encerra a sessão (\textit{logout}), altera a própria senha ou tem a conta desativada. Esse arranjo permite manter o \textit{token} de acesso com validade curta, o que reduz o impacto de um eventual vazamento, sem obrigar o usuário a se autenticar novamente a cada expiração.

Como os \textit{tokens} ficam em \textit{cookies} \texttt{HttpOnly}, um \textit{script} malicioso injetado na página não consegue lê-los nem enviá-los a terceiros. Os \textit{cookies} também são marcados como \texttt{SameSite=Strict}, de modo que o navegador só os envia em requisições originadas no próprio sistema, e como \texttt{Secure}, de modo que só trafegam por conexões HTTPS. Por isso, o \textit{frontend} acessa a API pela mesma origem, por meio de um \textit{proxy} reverso no servidor Nginx. Como os \textit{cookies} são enviados automaticamente pelo navegador, foi adotada também a proteção contra falsificação de requisições entre sites (CSRF) no padrão \textit{double submit cookie}: o \textit{backend} grava um valor aleatório em um \textit{cookie} legível pela aplicação, e toda requisição que altera dados deve repeti-lo em um cabeçalho HTTP. Uma página de outro domínio não consegue ler esse valor e, portanto, não consegue forjar a requisição. O Angular realiza esse envio automaticamente.
```

> Se a sigla CSRF (*Cross-Site Request Forgery*) for usada, acrescente-a à lista de siglas do `tcc.tex`, ao lado de JWT.

> Se houver referência bibliográfica sobre JWT/OAuth 2.0 no `refs.bib` (por exemplo, a RFC 6749 ou a RFC 6819), vale citá-la ao final do parágrafo.

---

## 2. Quadro "Regras de autorização da API" (`qua:permissoes`)

**Onde:** primeira linha do corpo da tabela (linha 136).

**Texto atual:**

```latex
\textit{Login}, verificação de saúde e documentação da API & Público \\[3pt]
```

**Substituir por:**

```latex
\textit{Login}, renovação e encerramento de sessão, verificação de saúde e documentação da API & Público \\[3pt]
```

---

## 3. Quadro "Recursos da API REST do SIGEI" (`qua:endpoints`)

**Onde:** linhas das rotas `/escola` (linha 170) e `/auth` (linha 180).

**Texto atual:**

```latex
\texttt{/escola}             & Cadastro e consulta; detalhe da escola com o registro anual mais recente. \\[3pt]
```

**Substituir por:**

```latex
\texttt{/escola}             & Cadastro e consulta; detalhe da escola com o registro anual mais recente; cadastro e edição completos da escola e do registro anual em uma única transação. \\[3pt]
```

**Texto atual:**

```latex
\texttt{/auth}               & \textit{Login} e alteração de senha. \\
```

**Substituir por:**

```latex
\texttt{/auth}               & \textit{Login}, renovação e encerramento de sessão e alteração de senha. \\
```

---

## 4. Subseção "Cadastro de Escolas"

**Onde:** depois do parágrafo "Os totais de matrículas são calculados automaticamente pela interface [...] quando carrega os dados já cadastrados da escola." (linha 244), antes do comentário `% TODO: inserir captura de tela`.

**Inserir:**

```latex
Ao final da revisão, todos os dados das sete etapas são enviados ao \textit{backend} em uma única requisição. O serviço correspondente grava a escola, o registro anual, a equipe, a infraestrutura, as matrículas e a fotografia em uma só transação no banco de dados: se qualquer etapa falhar, nenhuma alteração é mantida, o que impede a existência de cadastros incompletos. Como o serviço de armazenamento de imagens não participa da transação, a fotografia é enviada somente depois que os dados são validados pelo banco; se a transação for desfeita após o envio, a imagem é removida por meio de uma operação de compensação. Na edição, o registro do ano selecionado é atualizado ou, se ainda não existir, criado, sem alterar os registros dos demais anos.
```

---

## 5. Quadro de atendimento aos requisitos funcionais (`qua:atendimentorf`), opcional

**Onde:** linha do RF01 (linha 291).

**Texto atual:**

```latex
RF01 & Cadastrar escola          & Atendido & Cadastro em sete etapas, com dados anuais, infraestrutura, fotografia e matrículas. \\[3pt]
```

**Substituir por:**

```latex
RF01 & Cadastrar escola          & Atendido & Cadastro em sete etapas, com dados anuais, infraestrutura, fotografia e matrículas, gravado em uma única transação. \\[3pt]
```

---

## 6. Parágrafo dos requisitos não funcionais, opcional

**Onde:** parágrafo "Quanto aos requisitos não funcionais..." (linha 304).

**Trecho atual:**

```latex
O controle de acesso é garantido pela autenticação JWT e pelos perfis descritos na seção anterior (RNF03). A integridade dos dados é assegurada por chaves estrangeiras, restrições de unicidade e validações com \textit{Bean Validation} na camada de aplicação (RNF06).
```

**Substituir por:**

```latex
O controle de acesso é garantido pela autenticação JWT, com renovação de sessão por \textit{refresh tokens} rotativos, \textit{tokens} armazenados em \textit{cookies} \texttt{HttpOnly} e proteção contra CSRF, e pelos perfis descritos na seção anterior (RNF03). A integridade dos dados é assegurada por chaves estrangeiras, restrições de unicidade, validações com \textit{Bean Validation} na camada de aplicação e pela gravação do cadastro de escolas em uma única transação (RNF06).
```

---

## 7. Seção "Testes"

**Onde:** primeiro parágrafo da seção (linha 308).

O repositório tem agora **31 classes de teste**. Antes destas alterações eram 28, e o texto atual cita 27; vale conferir esse número antes de publicar. Foram acrescentadas `CadastroEscolaServiceTest`, `RefreshTokenFluxoTest` e `AuthSessaoHttpTest`.

**Texto atual:**

```latex
A verificação do \textit{backend} foi realizada com testes automatizados escritos com JUnit 5 e Mockito, organizados em 27 classes de teste que espelham os pacotes da aplicação \cite{sommerville2019}. Os testes de serviço isolam as regras de negócio por meio de objetos simulados (\textit{mocks}) dos repositórios. Os testes de repositório utilizam a anotação \texttt{@DataJpaTest} com um banco de dados H2 em memória, verificando os mapeamentos, as consultas personalizadas e as agregações do painel de indicadores. Também foram testados o inicializador de municípios e o histórico anual das escolas.
```

**Substituir por:**

```latex
A verificação do \textit{backend} foi realizada com testes automatizados escritos com JUnit 5 e Mockito, organizados em 31 classes de teste que espelham os pacotes da aplicação \cite{sommerville2019}. Os testes de serviço isolam as regras de negócio por meio de objetos simulados (\textit{mocks}) dos repositórios. Os testes de repositório utilizam a anotação \texttt{@DataJpaTest} com um banco de dados H2 em memória, verificando os mapeamentos, as consultas personalizadas e as agregações do painel de indicadores. Também foram testados o inicializador de municípios e o histórico anual das escolas. O cadastro completo de escolas e a renovação de sessão foram verificados com transações reais, confirmando que uma falha intermediária desfaz todo o cadastro e que \textit{refresh tokens} utilizados, expirados ou revogados são rejeitados. As rotas de sessão foram testadas também com a configuração de segurança completa da aplicação, verificando os atributos dos \textit{cookies} e a recusa de requisições sem o \textit{token} CSRF.
```

---

## 8. Seção "Limitações e Trabalhos Futuros"

**Onde:** lista `alineas` da seção (linhas 316–323).

**Remover estes dois itens**, pois as limitações foram resolvidas:

```latex
  \item o cadastro de uma escola é gravado por uma sequência de requisições à API, e não por uma única transação, de modo que uma falha intermediária pode deixar o cadastro incompleto até nova gravação;
  \item não há renovação automática do \textit{token} de acesso; após o período de validade, o usuário precisa se autenticar novamente;
```

> A limitação sobre o armazenamento dos *tokens* no `localStorage`, sugerida em versão anterior deste arquivo, também foi resolvida (cookies `HttpOnly` com proteção CSRF) e não deve ser incluída.

**Lista resultante:**

```latex
\begin{alineas}
  \item a exportação de relatórios em formatos como PDF ou planilha ainda não foi implementada;
  \item o módulo de importação foi construído para o leiaute das planilhas disponibilizadas pela SEE/AC, com colunas e ano de referência fixos, e precisa ser generalizado para cargas de anos futuros;
  \item o \textit{frontend} não possui testes automatizados, e não foram realizadas avaliações formais de desempenho e de usabilidade com os gestores da SEE/AC;
  \item o esquema do banco de dados é mantido pelo Hibernate, e a adoção de uma ferramenta de versionamento de esquema, como Flyway, é recomendável antes do uso em produção.
\end{alineas}
```

---

## 9. Seção do modelo de dados

**Onde:** primeiro parágrafo da seção (linha 80). Foi criada a entidade `RefreshToken`, então o total passa de treze para catorze entidades.

**Texto atual:**

```latex
O modelo de dados implementado é composto por treze entidades persistidas pelo Hibernate \cite{bauer2015}.
```

**Substituir por:**

```latex
O modelo de dados implementado é composto por catorze entidades persistidas pelo Hibernate \cite{bauer2015}.
```

**Trecho atual (final do mesmo parágrafo):**

```latex
A entidade \texttt{Usuario} armazena as credenciais e o perfil de acesso.
```

**Substituir por:**

```latex
A entidade \texttt{Usuario} armazena as credenciais e o perfil de acesso, e a entidade \texttt{RefreshToken} registra as sessões renováveis de cada usuário, guardando apenas o resumo criptográfico do \textit{token}, sua validade e se foi revogado.
```

> Se o diagrama entidade-relacionamento (imagem) for atualizado, inclua a tabela `refresh_token`, ligada a `usuario` por um relacionamento um-para-muitos.

---

## Conferência

- O `tcc.tex` não menciona as duas limitações; apenas a lista de siglas cita JWT, o que continua correto.
- Recompile o documento para conferir a quebra das tabelas `longtable` depois de alongar as linhas dos quadros.
