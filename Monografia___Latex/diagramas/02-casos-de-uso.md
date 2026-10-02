# Casos de uso e perfis de acesso

Funcionalidades existentes, com distinção entre consulta pública, consulta autenticada e manutenção.

```plantuml
@startuml 02_casos_de_uso
title Casos de uso e perfis de acesso
skinparam backgroundColor white
skinparam shadowing false
skinparam defaultFontName Arial
skinparam defaultFontSize 12
skinparam ArrowColor #334155
skinparam packageStyle rectangle
skinparam classAttributeIconSize 0
skinparam monochrome true
left to right direction
actor Visitante as V
actor "Usuário autenticado\nUSUARIO" as U
actor "Administrador\nADMIN" as A
U --|> V
A --|> U
rectangle SIGEI {
  usecase "Consultar mapa público\n(busca, filtros, camadas e fotos)" as M
  usecase "Entrar no sistema" as L
  usecase "Alterar própria senha\ne encerrar sessão" as S
  usecase "Consultar cadastros\nterritoriais e escolares" as Q
  usecase "Consultar histórico\npor escola e período" as H
  usecase "Consultar indicadores\npor ano" as D
  usecase "Consultar usuários" as QU
  usecase "Salvar preferências locais" as P
  usecase "Manter terras, povos\ne aldeias" as T
  usecase "Cadastrar e editar escola\ncom seus dados anuais" as E
  usecase "Excluir escola" as EX
  usecase "Manter usuários" as GU
  usecase "Abrir novo ano letivo" as N
  usecase "Importar planilhas\n<<via API>>" as I
  usecase "Manter municípios e\nvínculos da tabela anexo\n<<via API>>" as API
}
V --> M
V --> L
U --> S
U --> Q
U --> H
U --> D
U --> QU
U --> P
A --> T
A --> E
A --> EX
A --> GU
A --> N
A --> I
A --> API
note bottom of E
  Inclui registro anual, equipe, infraestrutura,
  matrículas por nível/série e foto opcional.
end note
@enduml
```

## Leitura e limites

A autenticação é precondição das funções restritas. A herança dos atores representa capacidades acumuladas. O perfil temporário `ROLE_TROCAR_SENHA` precisa trocar a senha antes de acessar as consultas restritas. A interface de municípios é de consulta; a API oferece manutenção. Preferências são gravadas no navegador. Não há exportação de relatórios em arquivo implementada.

## Fontes no projeto

- [SecurityConfig.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/config/security/SecurityConfig.java)
- [admin.routes.ts](../../sigei-frontend/src/app/features/admin/admin.routes.ts)
- [configuracoes.component.ts](../../sigei-frontend/src/app/features/admin/configuracoes/pages/configuracoes/configuracoes.component.ts)
- [ImportacaoExcelController.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/controllers/importacao/ImportacaoExcelController.java)
