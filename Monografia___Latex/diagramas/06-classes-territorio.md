# Classes do domínio territorial e escolar

Atributos escalares e associações persistentes. Métodos de acesso foram omitidos para legibilidade.

```plantuml
@startuml 06_classes_territorio
title Classes do domínio territorial e escolar
skinparam backgroundColor white
skinparam shadowing false
skinparam defaultFontName Arial
skinparam defaultFontSize 12
skinparam ArrowColor #334155
skinparam packageStyle rectangle
skinparam classAttributeIconSize 0
skinparam monochrome true
class Municipio {
  id : Long
  nome : String
}
class TerraIndigena {
  id : Long
  nome : String
}
class Povo {
  id : Long
  nome : String
}
class Aldeia {
  id : Long
  nome : String
}
class Escola {
  id : Long
  codigo : String
  nome : String
  tipoUnidade : String
}
class Anexo {
  id : Long
}
Municipio "0..1" -- "0..*" TerraIndigena
TerraIndigena "1" -- "0..*" Aldeia
Povo "1" -- "0..*" Aldeia
Aldeia "0..1" -- "0..1" Escola
Escola "0..1 sede" <-- "0..* unidades" Escola : escolaSede
Escola "0..1" <-- "0..*" Anexo : escola
Escola "0..1" <-- "0..*" Anexo : escolaAnexo
note bottom of Escola
  tipoUnidade é String (valores SEDE/ANEXO na aplicação).
  aldeia_id é UNIQUE no banco.
end note
note bottom of Anexo
  Tabela de vínculos independente do campo escolaSede.
  UNIQUE(escola_id, escola_anexo_id).
end note
@enduml
```

## Leitura e limites

Multiplicidades seguem a nulabilidade e unicidade persistidas. A entrada do cadastro completo exige aldeia, embora a coluna permita nulo. O cadastro completo usa `escolaSede`; a entidade `Anexo` também existe, com API própria. Não se presume sincronização automática entre as duas representações.

## Fontes no projeto

- [Escola.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/models/Escola.java)
- [Aldeia.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/models/Aldeia.java)
- [TerraIndigena.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/models/TerraIndigena.java)
- [Anexo.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/models/Anexo.java)
