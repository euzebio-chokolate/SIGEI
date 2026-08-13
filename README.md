# SIGEI

Sistema de Gerenciamento Escolar Indígena desenvolvido para centralizar, organizar e disponibilizar informações das escolas indígenas do Estado do Acre.

O sistema reúne dados de localização, povos, aldeias, terras indígenas, matrículas, equipes escolares e infraestrutura. Também oferece um **mapa público interativo** para consulta das escolas e uma **área administrativa** para gerenciamento e geração de relatórios.

## Estado atual

O projeto está em desenvolvimento.

- **Back-end:** API REST em fase final de implementação, com entidades, DTOs, mapeadores, serviços, repositórios e controladores.
- **Front-end:** desenvolvimento iniciado pelo mapa público, com layout responsivo e componentes independentes.
- **Banco de dados:** modelo relacional estruturado no PostgreSQL.
- **Área administrativa:** prevista para as próximas etapas do desenvolvimento.

## Funcionalidades

- Visualização georreferenciada das escolas indígenas em mapa público.
- Consulta de informações por escola.
- Filtros por município, terra indígena, aldeia, povo e ano de referência.
- Cadastro e atualização de escolas.
- Gerenciamento de municípios, terras indígenas, aldeias e povos.
- Registro anual de matrículas, equipe escolar e infraestrutura.
- Consulta ao histórico anual de cada escola.
- Identificação de escolas-sede e unidades anexas.
- Geração de relatórios administrativos.
- Registro fotográfico das escolas.

## Stack

- **Back-end:** Java, Spring Boot, Spring Data JPA, Hibernate, MapStruct e Jakarta Bean Validation.
- **Front-end:** Angular standalone, TypeScript, Tailwind CSS e Leaflet.
- **Banco de dados:** PostgreSQL.
- **Arquitetura:** API REST com separação em controllers, services, repositories, DTOs e mappers.
- **Build:** Maven no back-end e Angular CLI no front-end.

## Estrutura do repositório

```text
sigei/
├── sigei-backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/ac/gov/br/sigei_backend/
│   │   │   │   ├── controllers/
│   │   │   │   ├── dtos/
│   │   │   │   ├── exceptions/
│   │   │   │   ├── mappers/
│   │   │   │   ├── models/
│   │   │   │   ├── repository/
│   │   │   │   └── services/
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   └── pom.xml
├── sigei-frontend/
│   ├── public/
│   ├── src/
│   │   ├── app/
│   │   │   ├── core/
│   │   │   ├── features/
│   │   │   │   ├── mapa/
│   │   │   │   └── administrativo/
│   │   │   └── shared/
│   │   ├── styles.css
│   │   └── main.ts
│   ├── angular.json
│   ├── package.json
│   └── tailwind.config.js
└── README.md
```

> A estrutura pode variar conforme a organização local adotada para as pastas do back-end e do front-end.

## Pré-requisitos

Antes de executar o projeto, instale:

- Java 17 ou superior.
- Maven 3.9 ou superior.
- Node.js 20 ou superior.
- npm 10 ou superior.
- PostgreSQL.
- Angular CLI.

## Configuração do banco de dados

Crie um banco chamado `sigei` no PostgreSQL:

```sql
CREATE DATABASE sigei;
```

Configure a conexão em `sigei-backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/sigei
spring.datasource.username=postgres
spring.datasource.password=SUA_SENHA

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Não envie senhas reais para o repositório. Em produção, utilize variáveis de ambiente ou um gerenciador de segredos.

## Como executar

### Back-end

Entre na pasta do back-end:

```bash
cd sigei-backend
```

Execute a aplicação:

```bash
./mvnw spring-boot:run
```

No Windows:

```powershell
mvnw.cmd spring-boot:run
```

A API ficará disponível, por padrão, em:

```text
http://localhost:8080
```

### Front-end

Em outro terminal, entre na pasta do front-end:

```bash
cd sigei-frontend
```

Instale as dependências:

```bash
npm install
```

Inicie o servidor de desenvolvimento:

```bash
npm start
```

Caso o script `start` não esteja configurado, utilize:

```bash
ng serve
```

O front-end ficará disponível em:

```text
http://localhost:4200
```

## Mapa público

O mapa público é a página inicial de consulta do SIGEI. Ele utiliza o Leaflet para exibir as escolas a partir de suas coordenadas geográficas.

Principais componentes:

- `MapaHome`: organiza a página pública.
- `MapaView`: renderiza o mapa e os marcadores.
- `MapaFiltro`: disponibiliza os filtros de consulta.
- `MapaPopup`: apresenta os dados resumidos da escola selecionada.

## Modelo de dados

As principais entidades do sistema são:

- `Municipio`
- `TerraIndigena`
- `Aldeia`
- `Povo`
- `Escola`
- `AnoReferencia`
- `RegistroAnual`
- `EquipeEscola`
- `InfraestruturaEscola`
- `MatriculaNivel`
- `MatriculaSerie`
- `Anexo`

Cada escola pertence a uma aldeia, que está vinculada a uma terra indígena e a um município. Os dados que mudam ao longo do tempo são armazenados por ano de referência, preservando o histórico da unidade escolar.

## Testes

Para executar os testes do back-end:

```bash
cd sigei-backend
./mvnw test
```

Para executar os testes do front-end:

```bash
cd sigei-frontend
npm test
```

## Próximas etapas

- Concluir a integração entre o mapa público e a API.
- Implementar autenticação e controle de acesso.
- Desenvolver a área administrativa.
- Implementar relatórios e exportação de dados.
- Finalizar o processo de atualização anual das escolas.
- Ampliar a cobertura de testes automatizados.

## Autor

**Euzébio do Nascimento Oliveira Neto**  
Instituto Federal do Acre — IFAC, Campus Rio Branco.

Projeto desenvolvido como Trabalho de Conclusão de Curso, sob orientação do **Prof. Me. Marlon Amaro Coelho Teixeira**.
