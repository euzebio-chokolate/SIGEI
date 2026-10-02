# Classes do histórico escolar anual

O registro anual reúne dados de equipe, infraestrutura e matrículas de uma escola em um ano.

```plantuml
@startuml 07_classes_registro_anual
title Classes do histórico escolar anual
skinparam backgroundColor white
skinparam shadowing false
skinparam defaultFontName Arial
skinparam defaultFontSize 12
skinparam ArrowColor #334155
skinparam packageStyle rectangle
skinparam classAttributeIconSize 0
skinparam monochrome true
class AnoReferencia {
  id : Long
  ano : Integer
}
class RegistroAnual {
  id : Long
  rede : String
  localFuncionamento : String
  totalAlunos : Integer
}
class EquipeEscola {
  id : Long
  temProfessorResponsavel : boolean
  temEquipeGestora : boolean
  temMerendeira : boolean
  temZeladora : boolean
  temBarqueiro : boolean
  necessitaTransporte : boolean
}
class InfraestruturaEscola {
  id : Long
  anoConstrucao : Integer
  numSalas : Integer
  latitude : Double
  longitude : Double
  situacao : String
  tipoAcesso : String
  temPredio : Boolean
  tipoAgua : String
  temInternet : Boolean
  tipoEnergia : String
  temRegistroFotografico : Boolean
  fotoUrl : String
  fotoPublicId : String
}
class MatriculaNivel {
  id : Long
  nivel : String
  qtdMasculino : Integer
  qtdFeminino : Integer
  total : Integer
}
class MatriculaSerie {
  id : Long
  denominacao : String
  etapaEnsino : String
  qtdMasculino : Integer
  qtdFeminino : Integer
  total : Integer
}
class Escola {
  id : Long
  nome : String
}
Escola "0..1" *-- "0..*" RegistroAnual
AnoReferencia "0..1" -- "0..*" RegistroAnual
RegistroAnual "0..1" *-- "0..1" EquipeEscola
RegistroAnual "0..1" *-- "0..1" InfraestruturaEscola
RegistroAnual "0..1" *-- "0..*" MatriculaNivel
RegistroAnual "0..1" *-- "0..*" MatriculaSerie
AnoReferencia "0..1" -- "0..*" EquipeEscola
note bottom of RegistroAnual
  UNIQUE(escola_id, ano_referencia_id).
  Composição indica cascade ALL e orphanRemoval.
  As FKs do esquema permitem nulo; os serviços
  vinculam os objetos nos fluxos de cadastro.
end note
@enduml
```

## Fontes no projeto

- [RegistroAnual.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/models/RegistroAnual.java)
- [EquipeEscola.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/models/EquipeEscola.java)
- [InfraestruturaEscola.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/models/InfraestruturaEscola.java)
- [MatriculaNivel.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/models/MatriculaNivel.java)
- [MatriculaSerie.java](../../sigei-backend/src/main/java/ac/gov/br/sigei_backend/models/MatriculaSerie.java)
