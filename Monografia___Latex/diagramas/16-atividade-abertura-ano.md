# Atividade de abertura de novo ano

Regra implementada por AtualizacaoAnualService, incluindo o comportamento de cópia.

```plantuml
@startuml 16_atividade_abertura_ano
title Atividade de abertura de novo ano
skinparam backgroundColor white
skinparam shadowing false
skinparam defaultFontName Arial
skinparam defaultFontSize 12
skinparam ArrowColor #334155
skinparam packageStyle rectangle
skinparam classAttributeIconSize 0
skinparam monochrome true
start
:ADMIN solicita POST /atualizacao-anual/iniciar;
:Iniciar transação;
:Consultar maior ano de referência;
if (Existe ano?) then (não)
  :Rejeitar: nenhum ano cadastrado;
  stop
endif
:Calcular novoAno = maiorAno + 1;
if (Novo ano já existe?) then (sim)
  :Rejeitar duplicidade;
  stop
endif
:Persistir novo AnoReferencia;
:Listar escolas;
while (Há próxima escola?) is (sim)
  :Consultar registro mais recente da escola;
  if (Possui registro anterior?) then (sim)
    :Criar registro no novo ano;
    :Copiar rede e local de funcionamento;
    :Copiar equipe, se existente;
    :Copiar infraestrutura, se existente;
    :Copiar matrículas por nível e por série;
    :Associar novos objetos ao novo registro;
  else (não)
    :Ignorar escola sem histórico;
  endif
endwhile (não)
:Confirmar transação;
:Retornar confirmação;
stop
@enduml
```

## Leitura e limites

A origem é o registro mais recente **de cada escola**, mesmo que não pertença ao maior ano global. O código copia os quantitativos das matrículas. Não copia `RegistroAnual.totalAlunos`, `InfraestruturaEscola.fotoPublicId` nem atribui `EquipeEscola.anoReferencia`; copia `fotoUrl`. Esses detalhes descrevem a implementação existente e não uma proposta de regra de negócio.

## Fontes no projeto

- [AtualizacaoAnualService.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/service/AtualizacaoAnualService.java)
- [AtualizacaoAnualController.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/controllers/AtualizacaoAnualController.java)
- [atualizacao-anual.service.ts](../../sigei-frontend/src/app/domains/escolas/data-access/atualizacao-anual.service.ts)
