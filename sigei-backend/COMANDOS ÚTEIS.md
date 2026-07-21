# Comandos Úteis - Terminal

## 1. Gerenciamento do Projeto

* **Limpar a pasta de build (pasta target):**
  *Remove os artefatos antigos e mappers gerados anteriormente. Essencial para resolver erros de compilação ou conflitos de Beans.*
  ```powershell
  ./mvnw clean
  ```

* **Compilar e buildar o projeto:**
  *Gera o arquivo `.jar` final executando todos os testes unitários.*
  ```powershell
  ./mvnw clean package
  ```

* **Compilar pulando os testes (Mais rápido):**
  *Ideal para o dia a dia quando você quer apenas testar visualmente se o código compila sem perder tempo com a esteira de testes.*
  ```powershell
  ./mvnw clean package -DskipTests
  ```

* **Rodar a aplicação diretamente pelo terminal:**
  ```powershell
  ./mvnw spring-boot:run
  ```

---

## 2. Ambiente Local (Docker & Docker Compose)

* **Subir os serviços em segundo plano:**
  *Inicia o banco de dados e libera o terminal para você continuar trabalhando.*
  ```powershell
  docker compose up -d
  ```

* **Subir acompanhando os logs no terminal:**
  ```powershell
  docker compose up
  ```

* **Parar os serviços:**
  ```powershell
  docker compose down
  ```

* **Parar os serviços limpando os dados salvos (Resetar Banco):**
  *Apaga os volumes criados. Útil se quiser resetar o banco de dados local para o estado zero.*
  ```powershell
  docker compose down --volumes
  ```

* **Ver status dos contêineres do projeto:**
  ```powershell
  docker compose ps
  ```

* **Visualizar logs de um serviço específico:**
  ```powershell
  docker compose logs -f <nome_do_servico_no_yml>
  ```
