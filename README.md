# SIGEI — Sistema de Gerenciamento Escolar Indígena

Plataforma web que centraliza as informações das escolas indígenas do Estado
do Acre: localização, povos, aldeias, terras indígenas, matrículas, equipes
escolares, professores e infraestrutura, com histórico por ano.

O sistema tem duas partes:

- um **mapa público**, sem login, com a localização georreferenciada das escolas;
- uma **área administrativa**, com login, para cadastros, histórico anual e indicadores.

Projeto desenvolvido como Trabalho de Conclusão de Curso no IFAC.

## Funcionalidades

**Mapa público**
- Marcadores das escolas no mapa do Acre, com cor conforme a situação (em funcionamento ou paralisada).
- Busca por nome ou código INEP.
- Filtros por situação, tipo de acesso, internet e prédio.
- Quatro camadas de fundo: padrão, satélite, terreno e escuro.
- Janela com foto e dados resumidos de cada escola.

**Área administrativa**
- Dashboard com totais, evolução histórica de matrículas e escolas, matrículas por nível, infraestrutura e escolas por município.
- Cadastro de terras indígenas, aldeias e povos. Os municípios do Acre são carregados automaticamente.
- Cadastro de escolas em sete etapas: dados da escola, registro anual, equipe, infraestrutura com foto, matrículas por nível, matrículas por série e revisão.
- Distinção entre escola-sede e anexo.
- Histórico anual por ano ou por escola, incluindo o cadastro retroativo de anos anteriores.
- Abertura de um novo ano letivo, copiando o último registro de cada escola.
- Importação de planilhas de escolas e infraestrutura, matrículas e quadro de professores pela tela, para qualquer ano. As colunas são reconhecidas pelo cabeçalho, em qualquer ordem e aba, e a tela oferece um modelo para preencher.
- Quadro de professores: cadastro dos professores, lotação anual em escolas ou no núcleo de educação do município, vagas em aberto. CPF, matrícula e número do contrato são gravados com criptografia e exibidos apenas a administradores.
- Gestão de usuários com os perfis `ADMIN` (leitura e escrita) e `USUARIO` (somente leitura).
- Troca de senha obrigatória no primeiro acesso.
- Relatórios exportáveis em PDF e planilha (indicadores consolidados, escolas e infraestrutura, matrículas por etapa, professores e lotações), filtrados por ano e município. O painel mostra também o quadro de professores do ano.

## Arquitetura

```text
Navegador ──► Frontend (Angular + Leaflet, servido por Nginx)
                 │  HTTP/JSON + JWT
                 ▼
             Backend (Spring Boot, API REST)
                 │  JPA/Hibernate          │  upload de fotos
                 ▼                         ▼
             PostgreSQL                Cloudinary
```

| Camada | Tecnologias |
| --- | --- |
| Frontend | Angular 21 (standalone, sem Zone.js), TypeScript 5.9, Tailwind CSS 4, Leaflet 1.9 |
| Backend | Java 21, Spring Boot 4.0.5, Spring Data JPA/Hibernate, Spring Security + JWT, Bean Validation, MapStruct, Apache POI, Springdoc OpenAPI |
| Banco de dados | PostgreSQL (produção), H2 (testes) |
| Imagens | Cloudinary |
| Implantação | Docker e Docker Compose; Nginx no frontend |

## Estrutura do repositório

```text
SIGEI/
├── sigei-backend/        # API REST (Spring Boot)
│   ├── src/main/java/ac/gov/br/sigei_backend/
│   │   ├── config/       # segurança, inicializadores, Cloudinary
│   │   ├── controllers/  # rotas HTTP
│   │   ├── dto/          # contratos de entrada e saída
│   │   ├── mappers/      # conversões MapStruct
│   │   ├── models/       # entidades JPA
│   │   ├── repository/   # acesso a dados
│   │   └── service/      # regras de negócio
│   ├── docs/             # banco de dados, modelo ER, histórico anual
│   └── planilhas/        # planilhas de referência para importar pela tela
└── sigei-frontend/       # aplicação Angular
    ├── src/app/
    │   ├── core/         # autenticação
    │   ├── domains/      # serviços HTTP e modelos por domínio
    │   ├── features/     # telas (mapa público, login, área administrativa)
    │   └── shared/       # componentes reutilizáveis
    └── docs/             # documentação técnica do frontend
```

## Como executar

### Com Docker (recomendado)

Pré-requisito: Docker com Compose.

1. Suba o backend e o banco:

   ```bash
   cd sigei-backend
   cp .env.example .env    # troque todas as senhas e segredos
   docker compose up --build -d
   ```

   A API fica em `http://localhost:8080` (health check em `/actuator/health`).
   Na primeira execução, a aplicação cadastra os municípios do Acre e o
   administrador inicial. Os dados de escolas, matrículas e professores são
   importados pela tela de importações (`/admin/importacoes`); as planilhas da
   pasta `planilhas/` servem de referência.

2. Suba o frontend:

   ```bash
   cd sigei-frontend
   cp .env.example .env
   docker compose up --build -d
   ```

   A aplicação fica em `http://localhost:4200`.

### Sem Docker

Pré-requisitos: JDK 21, Node.js 22, npm e um PostgreSQL acessível.

```bash
# Backend: configure DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD, JWT_SECRET
# e SIGEI_CHAVE_DADOS_SENSIVEIS
# (no ambiente ou em sigei-backend/.env)
cd sigei-backend
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run

# Frontend, em outro terminal
cd sigei-frontend
npm ci
npm start
```

> O proxy de desenvolvimento do frontend (`proxy.conf.json`) encaminha `/api`
> para a porta **8081**. Rode o backend com `PORT=8081` ou ajuste o proxy
> para 8080.

### Primeiro acesso

O administrador inicial é criado a partir de `SIGEI_ADMIN_EMAIL` e
`SIGEI_ADMIN_PASSWORD`. Se nenhuma senha for configurada, o backend gera uma
temporária e a registra no log. No primeiro login o sistema exige a troca da
senha.

## Testes

```bash
cd sigei-backend
./mvnw clean test             # 27 classes de teste (services e repositories), H2 em memória
```

O frontend tem o Vitest configurado (`npm test`), mas ainda não possui testes.

## Documentação

| Assunto | Documento |
| --- | --- |
| Arquitetura, contratos HTTP, segurança e configuração do backend | [sigei-backend/CLAUDE.md](sigei-backend/CLAUDE.md) |
| Execução, Docker e hospedagem do backend | [sigei-backend/README.md](sigei-backend/README.md) |
| Dicionário de dados e relacionamentos | [sigei-backend/docs/BANCO_DE_DADOS.md](sigei-backend/docs/BANCO_DE_DADOS.md) |
| Modelo ER gerado das entidades | [sigei-backend/docs/MODELO_ER.md](sigei-backend/docs/MODELO_ER.md) |
| Histórico anual das escolas | [sigei-backend/docs/HISTORICO_ESCOLAS.md](sigei-backend/docs/HISTORICO_ESCOLAS.md) |
| Arquitetura, rotas e telas do frontend | [sigei-frontend/docs/FRONTEND.md](sigei-frontend/docs/FRONTEND.md) |
| Execução, Docker e hospedagem do frontend | [sigei-frontend/README.md](sigei-frontend/README.md) |
| API interativa | Swagger em `http://localhost:8080/swagger-ui.html` |

## Limitações conhecidas

- A importação reconhece colunas pelo título do cabeçalho (com sinônimos), mas cada tipo tem seu conjunto de colunas; planilhas com outra estrutura, como uma linha por etapa em vez de uma por escola, não são aceitas.
- Não há renovação de token: após 24 horas é preciso entrar de novo.
- O cadastro de uma escola é gravado em várias requisições, não em uma transação única.
- Divergências entre frontend e backend estão listadas em [sigei-frontend/docs/FRONTEND.md](sigei-frontend/docs/FRONTEND.md#divergências-conhecidas).

## Autor

**Euzébio do Nascimento Oliveira Neto**
Instituto Federal do Acre — IFAC, Campus Rio Branco.
Curso Superior de Tecnologia em Análise e Desenvolvimento de Sistemas.

Orientação: **Prof. Me. Marlon Amaro Coelho Teixeira**.
