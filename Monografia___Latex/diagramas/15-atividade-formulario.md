# Atividade de cadastro e edição de escola

Fluxo do formulário em sete etapas, incluindo seleção de ano na edição.

```plantuml
@startuml 15_atividade_formulario
title Atividade de cadastro e edição de escola
skinparam backgroundColor white
skinparam shadowing false
skinparam defaultFontName Arial
skinparam defaultFontSize 12
skinparam ArrowColor #334155
skinparam packageStyle rectangle
skinparam classAttributeIconSize 0
skinparam monochrome true
start
:ADMIN acessa formulário;
if (Edição?) then (sim)
  :Carregar escola e dados do ano solicitado;
else (não)
  :Preparar novo cadastro;
endif
repeat
  :1. Identificar escola, aldeia e sede opcional;
  :2. Selecionar ano, rede e local de funcionamento;
  if (Ano ainda não cadastrado?) then (sim)
    :Criar ano de referência pela API;
  endif
  if (Escola existente?) then (sim)
    :Buscar registro do ano selecionado;
    if (Registro encontrado?) then (sim)
      :Preencher dados para edição;
    else (404)
      :Preparar registro para esse ano;
    endif
  endif
  :3. Informar equipe e transporte;
  :4. Informar infraestrutura, coordenadas e foto;
  :5. Informar matrículas por nível;
  :6. Informar matrículas por série;
  :7. Revisar dados;
  note right
    As etapas permitem voltar.
    Validações impedem avanço ou envio inválido.
    Falha de consulta diferente de 404 impede salvar.
  end note
repeat while (Precisa corrigir?) is (sim) not (não)
:Enviar cadastro completo em uma requisição;
if (API confirmou?) then (sim)
  :Exibir modal de sucesso e retornar\nà lista ou ao histórico ao fechar;
else (não)
  :Exibir erro e manter formulário para correção;
endif
stop
@enduml
```

## Leitura e limites

O fluxo é uma síntese da interação, não uma enumeração de todos os eventos da tela. Criar um `AnoReferencia` pelo formulário é uma operação separada e não equivale à abertura global do próximo ano.

## Fontes no projeto

- [escola-form.component.ts](../../sigei-frontend/src/app/features/admin/escolas/pages/escola-form/escola-form.component.ts)
- [escola-form.component.html](../../sigei-frontend/src/app/features/admin/escolas/pages/escola-form/escola-form.component.html)
