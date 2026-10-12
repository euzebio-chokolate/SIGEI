# Demonstracao publica do SIGEI sem custo

> Este guia é para demonstração que tolere espera no primeiro acesso.
> O Render pode exigir cartão para verificar algumas contas. Se isso acontecer,
> não prossiga com esse provedor quando o requisito for não cadastrar cartão.
> Veja também [as alternativas sem cartão](HOSPEDAGEM_SEM_CARTAO.md).

Arquitetura para o TCC: Angular na Vercel Hobby, API Docker no Render Free e
PostgreSQL no Neon Free. O computador do desenvolvedor pode ficar desligado.
Use os subdominios gratuitos dos provedores; nao e necessario comprar dominio.

Escolha explicitamente os planos Hobby/Free, sem iniciar planos pagos.
A [equipe do Render informa que pode solicitar cartão na verificação](https://community.render.com/t/the-deployement-of-a-web-service-fails/36005).
O [Neon permite cadastro gratuito sem cartão](https://neon.com/blog/making-pricing-more-predictable).
Em [02/10/2026, o armazenamento gratuito do Neon passou para 1 GB por projeto](https://neon.com/blog/neon-free-plan-1-gb-per-project);
há também cotas de computação, e o plano não oferece execução ilimitada.

Os arquivos deste repositorio preparam a implantacao, mas nao criam contas,
publicam servicos nem transferem o banco local automaticamente.

O Git local tem sua raiz em `SIGEI`, com `sigei-backend` e `sigei-frontend`
no mesmo repositorio. As instrucoes abaixo usam essa estrutura.

## 1. Criar o banco no Neon

Crie um projeto no plano Free. Para ficar proximo da API configurada no
`render.yaml`, use AWS US West (Oregon), a região do projeto SIGEI existente.
Escolha PostgreSQL 17, que e a versao usada pelo Compose local.

No painel de conexao, obtenha host, banco, usuario e senha. Use inicialmente a
conexao direta (sem `-pooler`): a API ja limita seu pool a tres conexoes, e o
mesmo endereco sera usado pelo Flyway nas migracoes.

Separe as credenciais da URL ao configurar o Render:

```text
DATABASE_URL=jdbc:postgresql://HOST-DO-NEON:5432/neondb?sslmode=require
DATABASE_USERNAME=USUARIO-DO-NEON
DATABASE_PASSWORD=SENHA-DO-NEON
```

Substitua tambem `neondb` se escolheu outro nome. Nao coloque uma URL
`postgresql://usuario:senha@...` diretamente em `DATABASE_URL`: o Java espera JDBC.
Mantenha as credenciais nos paineis dos provedores, fora do Git e do frontend.

## 2. Publicar a API no Render

Envie as alteracoes para o repositorio SIGEI no GitHub. No Render,
crie um **Blueprint** a partir desse repositorio e informe o caminho
`sigei-backend/render.yaml` em **Blueprint Path**. O arquivo ja define
`rootDir: sigei-backend`, para o build encontrar o Dockerfile e o Maven.
Se voce publicar o backend em um repositorio separado, com `pom.xml` na raiz,
remova `rootDir` e use `render.yaml` como caminho do Blueprint.
Confira que o unico servico
proposto usa **Free** e nao adicione um Render Postgres: o banco sera o Neon.

Preencha as variaveis solicitadas:

| Variavel | Valor |
| --- | --- |
| `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` | Dados da etapa anterior. |
| `SIGEI_ADMIN_EMAIL` | E-mail do administrador da demonstracao. |
| `SIGEI_ADMIN_PASSWORD` | Senha inicial escolhida por voce. |
| `SIGEI_CHAVE_DADOS_SENSIVEIS` | Chave mestra em Base64, com pelo menos 32 bytes aleatorios. |
| `CORS_ALLOWED_ORIGINS` | URL HTTPS do frontend, sem barra final. Se ainda nao existir, use temporariamente `http://localhost:4200` e atualize na etapa 3. |

O Render gera `JWT_SECRET`. Para um banco novo, voce pode gerar a chave mestra
no PowerShell e copia-la diretamente para o painel:

```powershell
$chaveDemo = New-Object byte[] 32
$geradorDemo = [System.Security.Cryptography.RandomNumberGenerator]::Create()
try { $geradorDemo.GetBytes($chaveDemo) } finally { $geradorDemo.Dispose() }
[Convert]::ToBase64String($chaveDemo)
```

Guarde a chave. Se restaurar um banco local que ja tenha dados cifrados, use a
mesma chave desse banco; uma chave nova nao consegue ler os valores existentes.

O Blueprint limita o heap Java a 256 MB para deixar espaco para a memoria nativa
dentro dos 512 MB do Render Free. A API foi validada localmente nesse limite,
com CPU limitada a 0,1 e PostgreSQL descartável. A validação no endereço
publicado, com Neon e proxy Vercel, ainda precisa ser feita. Nao aumente o heap
para 512 MB, pois a JVM consome memoria adicional fora dele.

O mesmo arquivo limita os workers HTTP a 20 (com dois iniciais) e habilita
compressão de JSON a partir de 1 KB. O mapa usa uma projeção direta para evitar
carregar o grafo de entidades JPA e executar consultas adicionais por escola.
Manter o Neon e a API na mesma região reduz a latência das consultas do dashboard.

O Render fornece `PORT`; a aplicacao ja le essa variavel. Aguarde o health check
`https://ENDERECO-REAL-DA-API/actuator/health` retornar `{"status":"UP"}`.

## 3. Publicar o Angular na Vercel

Copie `docs/deploy/vercel.json.example` deste repositorio para **`vercel.json`
na pasta sigei-frontend**, ao lado de `package.json`.

No arquivo copiado, substitua apenas
`https://SUBSTITUIR-PELA-API.example.invalid` pela URL HTTPS real da API no Render.
Mantenha `/:path*` no final. O endereco de exemplo nao funciona e nao deve ser
publicado como configuracao definitiva.

O proxy transforma `/api/escola` em `/escola` na API. Mantenha o frontend usando
`environment.apiUrl = '/api'`; nao troque por uma chamada direta ao dominio do
Render. Isso preserva a origem dos cookies de sessao e do token CSRF.
As respostas da API ficam fora do cache compartilhado. A outra regra permite
abrir diretamente rotas do Angular, como `/admin`, sem retornar 404.

Envie o arquivo ao GitHub e importe o repositorio SIGEI na Vercel Hobby.
Configure **Root Directory** como `sigei-frontend` e selecione Node.js 22.x.
Se o frontend estiver em um repositorio separado, use a raiz desse repositorio.
O exemplo configura:

- Instalacao: `npm ci`.
- Build: `npm run build -- --configuration production`.
- Saida: `dist/sigei-frontend/browser`.

A Vercel compila o Angular; o Dockerfile/Nginx do frontend nao e usado nessa
implantacao. Use o dominio de producao do projeto, e nao um endereco temporario
de preview, como link da demonstracao.

Copie esse dominio HTTPS para `CORS_ALLOWED_ORIGINS` no Render e aplique a
alteracao. Mantenha `SIGEI_COOKIE_SECURE=true`.

## 4. Preparar os dados e as fotos

`SIGEI_DADOS_INICIAIS_ENABLED=false` evita carregar todas as planilhas durante a
subida da instancia pequena. Em banco novo, Flyway cria as tabelas, e os
inicializadores cadastram municipios e administrador; escolas, anos e
professores da demonstracao precisam ser cadastrados ou importados separadamente.

Prepare essa carga antes da apresentacao. Para uma carga grande, uma opcao e
executar a API localmente com um ambiente separado apontando para o banco de
demonstracao no Neon, usando a mesma chave mestra. Confirme o banco de destino
antes de importar. Nao substitua inadvertidamente as configuracoes do banco local.

Para fotos, adicione no painel do Render `CLOUDINARY_CLOUD_NAME`,
`CLOUDINARY_API_KEY` e `CLOUDINARY_API_SECRET`. Sem elas, cadastros sem foto
funcionam, mas o envio de fotos retorna 503. O cadastro completo com foto e
desfeito quando o upload falha.

## 5. Conferir a demonstracao

- Abrir o link da Vercel em janela anonima e verificar o mapa.
- Entrar como administrador, trocar a senha inicial e salvar um cadastro.
- Conferir que `/api/...` recebe JSON, e nao o HTML do Angular.
- Atualizar diretamente uma rota `/admin/...` e conferir a navegacao.
- Conferir dashboard, historico e renovacao da sessao apos o token expirar.
- Desligar a aplicacao local e acessar novamente pelo celular.

O Render Free suspende a API depois de 15 minutos sem trafego e informa cerca
de um minuto para reativar. A primeira chamada pelo proxy pode expirar nesse
intervalo: abra o health check da API, aguarde `UP` e depois acesse o frontend.
Faca isso antes da apresentacao. O Neon tambem pode suspender o compute ocioso.
O endereco continua publico; isso nao significa resposta imediata 24 horas.

Para a apresentação, abra o health check e percorra mapa, login e dashboard
de três a cinco minutos antes de começar. A partida inicial da aplicação Java
levou aproximadamente 161 segundos na simulação local de 0,1 CPU; o tempo real
do provedor pode variar. Teste também um cadastro e uma foto, caso
esse fluxo faça parte da demonstração. Isso reduz a chance de encontrar a
reativação do serviço durante a fala, mas não garante tempo de resposta.

Mantenha todos os servicos nos planos gratuitos e acompanhe suas cotas.
O historico de auditoria tambem ocupa espaco no banco.

## 6. Validação local de usabilidade e recursos

Executada em 12/10/2026, com a imagem Docker do projeto, limite de 512 MB e
0,1 CPU para a API, PostgreSQL 17 local e 100 escolas fictícias com dois anos.
Não usou o banco real nem publicou dados. As medições não incluem a internet,
o Neon, o proxy Vercel, imagens Cloudinary ou camadas externas do mapa.

| Verificação | Resultado |
| --- | --- |
| Suíte Java, incluindo migrações PostgreSQL | 272 testes; zero falhas, erros ou testes ignorados. |
| Build Angular de produção | Passou; 354,92 kB iniciais, transferência estimada de 92,09 kB, mais módulos sob demanda. Aviso CommonJS do Leaflet. |
| Consulta do mapa | Uma consulta SQL, sem carregar entidades; teste de regressão adicionado. |
| Mapa antes da otimização, seis requisições | Mediana de 650 ms. |
| Mapa após a otimização, seis requisições | Mediana de 204 ms; amostras entre 97 e 299 ms. |
| Oito requisições ao mapa, quatro simultâneas | Todas concluídas; entre 167 e 786 ms. |
| Compressão JSON do mapa | 36.377 bytes reduzidos a 1.511 bytes com gzip. |
| Pico de memória do contêiner após as mudanças | 341,6 MiB, dentro dos 512 MiB configurados. |
| Login, troca de senha, dashboard, cadastro de ano com CSRF, refresh e logout | Passaram na API com limite de recursos. |

Essa é uma verificação pequena para demonstração, não uma garantia de latência
ou capacidade em produção. Os cookies foram testados por HTTP em loopback com
`SIGEI_COOKIE_SECURE=false` somente no ambiente descartável; a configuração
do Render mantém `true`, e o fluxo HTTPS deve ser conferido após a publicação.
O upload de fotos depende das credenciais Cloudinary e não foi exercitado.

A publicação exige acesso às contas Neon, Render e Vercel e a definição da URL
real da API no proxy do frontend. Preparar os arquivos não cria esses serviços.

## Referencias

- [Render: limites gratuitos](https://render.com/docs/free)
- [Render: planos de computacao](https://render.com/docs/compute-plans)
- [Render: Blueprint YAML](https://render.com/docs/blueprint-spec)
- [Neon: plano gratuito](https://neon.com/blog/how-to-make-the-most-of-neons-free-plan)
- [Vercel: proxy com rewrites](https://vercel.com/docs/routing/rewrites)
- [Vercel: plano Hobby](https://vercel.com/docs/plans/hobby)
