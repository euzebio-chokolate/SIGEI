# Sequência de gravação do cadastro escolar

Criação ou edição do cadastro completo, com transação no banco e foto opcional.

```plantuml
@startuml 14_sequencia_cadastro_completo
title Sequência de gravação do cadastro escolar
skinparam backgroundColor white
skinparam shadowing false
skinparam defaultFontName Arial
skinparam defaultFontSize 12
skinparam ArrowColor #334155
skinparam packageStyle rectangle
skinparam classAttributeIconSize 0
skinparam monochrome true
autonumber
actor Administrador as U
participant "EscolaFormComponent /\nEscolaService Angular" as F
participant "EscolaController\nSecurity + Bean Validation" as C
participant CadastroEscolaService as S
database "PostgreSQL\nvia repositories / JPA" as DB
participant CloudinaryService as CL
participant Cloudinary as EXT
U -> F : confirmar revisão das sete etapas
F -> C : POST /api/escola/salvarCompleto\nou PUT /api/escola/editarCompleto\nmultipart: dados JSON + foto opcional
C -> S : salvar(dto, foto) ou editar(dto, foto)
group Transação Spring / banco de dados
  S -> DB : resolver aldeia e sede opcional
  S -> DB : criar ou atualizar escola
  S -> DB : resolver ano e localizar registro da escola/ano
  S -> S : criar ou atualizar registro, equipe,\ninfraestrutura e matrículas
  S -> DB : saveAndFlush(registro)
  opt foto fornecida
    S -> CL : uploadImagem(foto)
    CL -> EXT : enviar imagem
    EXT --> CL : secure_url e public_id
    CL --> S : resultado
    S -> S : registrar compensação se rollback\ne atribuir URL / publicId
  end
  S -> DB : commit (caminho de sucesso)
end
S --> C : CadastroEscolaResponseDTO
C --> F : resposta de sucesso
F --> U : modal de sucesso; ao fechar,\nretorno à lista ou ao histórico
note over S,EXT
  Cloudinary não participa da transação SQL.
  Se ocorrer rollback após upload, tenta excluir a nova imagem;
  falha nessa compensação é registrada em log.
end note
@enduml
```

## Leitura e limites

A figura mostra o caminho de sucesso e a compensação prevista. Cloudinary não configurado lança exceção de execução e a API responde 503. `@Transactional` não declara `rollbackFor = IOException.class`; portanto, não se afirma rollback para toda falha possível de I/O. Na edição, matrículas ausentes do payload são preservadas; os demais anos não são alterados.

## Fontes no projeto

- [escola.service.ts](../../sigei-frontend/src/app/domains/escolas/data-access/escola.service.ts)
- [EscolaController.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/controllers/EscolaController.java)
- [CadastroEscolaService.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/service/CadastroEscolaService.java)
- [CloudinaryService.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/service/armazenamento/CloudinaryService.java)
