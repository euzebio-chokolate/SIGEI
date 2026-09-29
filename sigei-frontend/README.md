# SIGEI Frontend

Interface web do Sistema de Gerenciamento Escolar Indígena, construída com Angular 21.

Arquitetura, rotas, autenticação e funcionamento das telas estão descritos em
[docs/FRONTEND.md](docs/FRONTEND.md).

## Desenvolvimento

Requisitos: Node.js 22 e npm.

```bash
npm ci
npm start
```

O frontend abre em `http://localhost:4200` e, no ambiente de desenvolvimento,
consulta a API em `http://localhost:8080`.

## Build de produção

```bash
npm ci
npm run build
```

Os arquivos são gerados em `dist/sigei-frontend/browser`. Em produção, o front
usa `/api` na mesma origem. O servidor web deve encaminhar esse prefixo para o
backend e devolver `index.html` para rotas que não correspondam a arquivos.

## Execução com Docker

Com o backend disponível na porta local 8080:

```bash
cp .env.example .env
docker compose up --build -d
```

A aplicação estará em `http://localhost:4200`. O container inclui:

- Nginx com fallback para as rotas do Angular;
- proxy reverso de `/api` para o backend;
- cache longo somente para assets versionados;
- compressão, headers de segurança e healthcheck em `/healthz`.

Para acompanhar ou encerrar:

```bash
docker compose logs -f frontend
docker compose down
```

## Hospedagem

O caminho recomendado é publicar o `Dockerfile`. Configure `API_UPSTREAM` com a
URL interna ou pública do backend, sem barra no final, por exemplo:

```text
API_UPSTREAM=https://api.exemplo.gov.br
```

O navegador acessará apenas `https://seu-front/api/...`; a URL configurada fica
no Nginx. Isso evita recompilar o Angular ao trocar o endereço da API e elimina
problemas de CORS quando frontend e proxy usam a mesma origem.

No backend, mantenha `CORS_ALLOWED_ORIGINS` com a URL HTTPS pública do frontend
caso a API também seja acessada diretamente. Nunca publique segredos, `.env` ou
credenciais administrativas no repositório.

Em plataformas sem Docker, publique o conteúdo de
`dist/sigei-frontend/browser`, configure o fallback SPA para `/index.html` e um
proxy `/api` equivalente ao arquivo `nginx/default.conf.template`.
