# Atividade de importação das planilhas

Visão dos dois importadores e da carga automática inicial.

```plantuml
@startuml 19_atividade_importacao
title Atividade de importação das planilhas
skinparam backgroundColor white
skinparam shadowing false
skinparam defaultFontName Arial
skinparam defaultFontSize 12
skinparam ArrowColor #334155
skinparam packageStyle rectangle
skinparam classAttributeIconSize 0
skinparam monochrome true
start
if (Carga automática ao iniciar?) then (sim)
  if (Habilitada e sem registros anuais?) then (sim)
    :Validar disponibilidade das duas planilhas;
    :Importar infraestrutura;
    :Importar matrículas;
    :Registrar resultados no log;
  else (não)
    :Ignorar carga automática;
  endif
else (requisição ADMIN)
  :Receber arquivo multipart em /importacoes;
  if (Endpoint /infraestrutura?) then (sim)
    :Importar infraestrutura;
  else (/matriculas)
    :Importar matrículas;
  endif
  :Retornar contadores e erros;
endif
stop

note right
  Subfluxo infraestrutura:
  - validar arquivo e aba Planilha4;
  - obter/criar ano 2025;
  - processar sedes antes dos anexos;
  - localizar município e manter território/escola;
  - manter registro, equipe e infraestrutura;
  - registrar erro por linha e continuar.

  Subfluxo matrículas:
  - validar arquivo e aba Censo_2025;
  - ignorar cabeçalhos, vazios e linhas sem código;
  - localizar escola e registro de 2025;
  - substituir matrículas do registro;
  - atualizar total de alunos;
  - registrar erro por linha e continuar.
end note
@enduml
```

## Leitura e limites

Layouts, nomes de abas e ano 2025 estão fixados nos importadores. Não há tela de importação no Angular. A infraestrutura não tem transação global no serviço; matrículas possui `@Transactional`, mas captura exceções por linha. Isso não garante isolamento ou rollback independente por linha, nem atomicidade entre as duas planilhas da carga inicial.

## Fontes no projeto

- [DadosIniciaisInitializer.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/config/initializer/DadosIniciaisInitializer.java)
- [ImportacaoExcelService.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/service/importacao/ImportacaoExcelService.java)
- [ImportacaoMatriculasService.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/service/importacao/ImportacaoMatriculasService.java)
- [ImportacaoExcelController.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/controllers/importacao/ImportacaoExcelController.java)
