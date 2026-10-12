# Publicação da demonstração — 12/10/2026

Site: <https://sigei-coral.vercel.app>. API: <https://sigei-x4nv.onrender.com>.

Foram reutilizados os recursos existentes, sem cadastrar cartão ou contratar recursos pagos:

| Serviço | Recurso | Plano confirmado |
| --- | --- | --- |
| Vercel | Projeto `sigei`, domínio `sigei-coral.vercel.app` | Hobby |
| Render | Serviço `SIGEI`, região Oregon | Free |
| Neon | Projeto `sigei-db`, PostgreSQL 17, AWS US West 2 | Free |

## Configuração dos agentes

As instruções foram obtidas diretamente de <https://vercel.com/get-started.md> e <https://neon.com/.well-known/agent-skills/neon/SKILL.md>.

- As CLIs Vercel 63.1.2 e Neon 8.3.6 já estavam instaladas e autenticadas; foram reutilizadas.
- Instaladas as skills `neon` e `neon-postgres`, para Codex, no escopo do usuário: `~/.agents/skills/`. Nenhuma skill anterior foi substituída.
- Instalado e habilitado o plugin oficial `vercel@openai-curated`, no escopo do usuário, incluindo o comando de status. As extensões serão carregadas ao iniciar uma nova sessão do Codex.
- Adicionada a entrada `Neon` em `~/.codex/config.toml`, apontando para `https://mcp.neon.tech/mcp?projectId=square-fog-94276549`. O instalador criou uma chave limitada ao projeto, mantida somente na configuração do usuário. A conexão autenticada foi verificada com `initialize`, `tools/list` e uma consulta de contagem com `run_sql`.
- Adicionada a entrada `vercel` no mesmo arquivo, apontando para `https://mcp.vercel.com`, autenticada por OAuth. Verificadas as chamadas de leitura `search_vercel_documentation` e `list_teams` em uma sessão separada. A segunda chamada teve sucesso e retornou uma lista vazia de equipes.
- Mantidas as demais opções de modelo, projetos, interface e plugins. O arquivo de configuração permanece com permissão `600`.
- Criado o contexto local `.neon`, apontando para a branch `production` do banco existente, com `--no-env-pull --no-config`. O `.env` do backend foi preservado. Adicionada a exclusão `.neon` ao Git.

## Frontend e Vercel

- Criado `sigei-frontend/vercel.json`: Angular, instalação por `npm ci`, build de produção e saída em `dist/sigei-frontend/browser`.
- Adicionado o encaminhamento `/api/:path*` para a API Render, removendo o prefixo `/api` no destino. Isso mantém a sessão no domínio da Vercel, compatível com os cookies existentes.
- Adicionados cabeçalhos que impedem cache das respostas da API e fallback para `index.html` nas rotas da aplicação.
- Vinculada a pasta ao projeto Vercel existente. A CLI criou `.vercel/project.json` e `.env.local` com um token OIDC temporário; ambos estão ignorados pelo Git. O `.env.local` recebeu permissão `600`.
- Consolidado `.gitignore` do frontend para ignorar `.vercel/`, `.env` e `.env.*`, mantendo `.env.example` permitido.
- Publicado somente o frontend a partir de uma cópia temporária em `/tmp`. O pacote foi conferido antes do envio: 163 arquivos, sem `.env`, backend, planilhas ou monografia. Mantidos o domínio e a configuração Node.js 24 existentes.

## Backend e Render

Os deploys anteriores falhavam porque `SIGEI_CHAVE_DADOS_SENSIVEIS` estava ausente. Antes de gerar a chave, foram verificadas somente contagens e versões: 183 escolas, dois usuários existentes, nenhuma linha em `professor` ou `professor_aud`, e sete migrações Flyway aplicadas com sucesso. Nenhum dado cifrado existente precisava de uma chave anterior.

Foram acrescentadas ou atualizadas as seguintes variáveis no serviço existente:

| Variável | Configuração |
| --- | --- |
| `SIGEI_CHAVE_DADOS_SENSIVEIS` | Nova chave aleatória de 32 bytes, em Base64; valor privado |
| `JAVA_OPTS` | `-Xms64m -Xmx256m -XX:+UseSerialGC -Dfile.encoding=UTF-8` |
| `DATABASE_POOL_SIZE` / `DATABASE_POOL_MIN_IDLE` | `3` / `0` |
| `SERVER_TOMCAT_THREADS_MAX` / `SERVER_TOMCAT_THREADS_MIN_SPARE` | `20` / `2` |
| `SERVER_COMPRESSION_ENABLED` | `true` |
| `SERVER_COMPRESSION_MIME_TYPES` / `SERVER_COMPRESSION_MIN_RESPONSE_SIZE` | `application/json` / `1KB` |
| `SIGEI_DADOS_INICIAIS_ENABLED` | `false`, preservando os dados existentes e evitando importação automática de professores |
| `SIGEI_COOKIE_SECURE` | `true` |
| `CORS_ALLOWED_ORIGINS` | Mantidas as origens anteriores e acrescentados os dois domínios de produção da Vercel |
| `SPRING_FLYWAY_URL` / `SPRING_FLYWAY_USER` / `SPRING_FLYWAY_PASSWORD` | Conexão direta para migrações, com as credenciais existentes; o pool da aplicação permanece na conexão pooled |
| `SIGEI_ADMIN_EMAIL` / `SIGEI_ADMIN_NAME` / `SIGEI_ADMIN_PASSWORD` | Conta própria para demonstração; senha privada |

As credenciais existentes do banco, JWT e Cloudinary foram preservadas. O backup privado `backups/hospedagem-20261012.json` guarda as variáveis anteriores e a nova chave de criptografia, com permissão `600`. A pasta `backups/` está excluída do Git e do contexto Docker. Guarde esse backup: a chave é necessária para ler dados sensíveis que forem cadastrados posteriormente.

As senhas configuradas anteriormente no ambiente não autenticaram nas contas existentes. Foi criada a conta `demo@sigei.com` pelo inicializador da própria aplicação; os usuários anteriores foram preservados. A senha inicial da nova conta foi trocada pelo fluxo normal da interface. As credenciais estão somente em `backups/acesso-demonstracao.json` e `backups/ACESSO_DEMONSTRACAO.txt`, com permissão `600`, fora do Git. A variável de criação do administrador no Render foi sincronizada com a senha final para as próximas inicializações.

O serviço Render passou a usar a branch `deploy/demo-gratuita-20261012`. A `main` não foi modificada. O arquivo `render.yaml` agora corresponde ao nome `SIGEI` e à região Oregon, com os limites de recursos usados na publicação.

## Código e documentação

- `InfraestruturaEscolaRepository`: a consulta do mapa retorna os campos do DTO diretamente, evitando carregar o grafo de entidades de cada escola.
- `InfraestruturaEscolaResponseDTO`: construtor para a projeção da consulta.
- `InfraestruturaEscolaService`: devolve a projeção sem remontar entidades.
- `InfraestruturaEscolaRepositoryTest`: regressão que verifica o ano mais recente, os campos retornados, uma consulta SQL e nenhuma entidade carregada.
- `.dockerignore` e `.gitignore` do backend: exclusões de arquivos de ambiente e backups.
- `README.md`, `CLAUDE.md` e `docs/HOSPEDAGEM_GRATUITA.md`: instruções de publicação, fluxo da consulta e resultados da validação.
- `docs/HOSPEDAGEM_SEM_CARTAO.md`: alternativa de demonstração pelo computador local, já preparada anteriormente.

## Verificação e operação

A compilação Angular de produção e os 272 testes do backend passaram. O deploy Render foi marcado como `live`, o deploy Vercel como `Ready`, e `/api/actuator/health` retornou `UP`. O mapa público carregou 178 marcadores, com camadas externas, sem erros JavaScript. A resposta JSON foi comprimida em gzip e os cabeçalhos de cache da API foram conferidos.

Os fluxos administrativos foram verificados em Chromium temporário: login, troca da senha inicial, recarga de rota interna, escolas, municípios, terras indígenas, aldeias, povos, professores, histórico escolar, usuários, auditoria, renovação de sessão e logout. Sem o cabeçalho CSRF, a renovação foi rejeitada com HTTP 401; com o token correto, respondeu HTTP 200. Os cookies de sessão têm HttpOnly, Secure e SameSite=Strict. Não ocorreram erros JavaScript ou respostas 5xx durante a navegação.

O painel carregado mostrou 183 escolas, 6.021 matrículas, 12 municípios e 32 terras indígenas, na referência 2025. Cinco requisições com a API ativa deram medianas de 412 ms para o mapa e 503 ms para o dashboard. São amostras pequenas, sem garantia de latência. Upload de fotos e edição de registros reais não foram exercitados; as credenciais Cloudinary foram preservadas.

O relatório sem credenciais está em `docs/deploy/verificacao-publicada-20261012.json`. As capturas estão em `/tmp/sigei-publico.png` e `/tmp/sigei-dashboard-carregado.png`. Nenhuma ferramenta de teste foi adicionada às dependências do projeto; Playwright e Chromium ficaram em `/tmp`.

Render Free pode suspender a API após inatividade. O início observado nesta publicação levou cerca de três minutos. Abra o sistema de três a cinco minutos antes da apresentação e confirme o mapa e o painel carregados. O desempenho com a API ativa não elimina essa espera ao reativar o serviço. Veja <https://render.com/docs/free>.

Para futuras publicações, use os arquivos de configuração desta branch ou incorpore a alteração na `main` antes de publicar por Git. A ligação original da Vercel com `main` foi preservada; uma publicação automática de código que não contenha o `vercel.json` pode perder o proxy. As variáveis privadas permanecem nos painéis dos provedores.
