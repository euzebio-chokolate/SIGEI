# Gera documentacao a partir do subconjunto de JPA utilizado neste projeto.
# Nao acessa o banco nem inicia a aplicacao.
[CmdletBinding()]
param([switch]$Check)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$modelsPath = Join-Path $projectRoot 'src/main/java/ac/gov/br/sigei_backend/models'
$outputPath = Join-Path $projectRoot 'docs/MODELO_ER.md'

function ConvertTo-SnakeCase([string]$Name) {
    return [regex]::Replace($Name, '([a-z0-9])([A-Z])', '$1_$2').ToLowerInvariant()
}

$entities = @()
foreach ($file in (Get-ChildItem $modelsPath -Filter '*.java' | Sort-Object Name)) {
    $source = Get-Content -LiteralPath $file.FullName -Raw -Encoding UTF8
    if ($source -notmatch '@Entity\b') { continue }
    if ($source -match '@(Embedded|EmbeddedId|IdClass|Inheritance|SecondaryTable|ManyToMany|ElementCollection|Access|AttributeOverride|JoinTable|JoinColumns)\b') {
        throw "Mapeamento ainda nao suportado em $($file.Name). Atualize o gerador."
    }
    $class = [regex]::Match($source, 'public\s+class\s+(\w+)')
    if (-not $class.Success) { throw "Classe nao reconhecida: $($file.Name)" }
    $name = $class.Groups[1].Value
    $table = ConvertTo-SnakeCase $name
    $tableMatch = [regex]::Match($source, '@Table\(\s*name\s*=\s*"([^"]+)"')
    if ($tableMatch.Success) { $table = $tableMatch.Groups[1].Value }
    $body = $source.Substring($source.IndexOf('{', $class.Index) + 1)
    $fields = [regex]::Matches($body, '(?ms)(?<annotations>(?:\s*@\w+(?:\([^;]*?\))?\s*)*)\s*private\s+(?<type>\w+(?:<[^>]+>)?)\s+(?<name>\w+)\s*(?:=[^;]*)?;')
    $entities += [pscustomobject]@{ Name = $name; Table = $table; Fields = $fields; Source = $source }
}
if ($entities.Count -eq 0) { throw 'Nenhuma entidade encontrada.' }

$tables = @{}
foreach ($entity in $entities) { $tables[$entity.Name] = $entity.Table }
$types = @{ Long = 'bigint'; Integer = 'integer'; Double = 'double'; Boolean = 'boolean'; String = 'varchar'; LocalDate = 'date'; Instant = 'timestamp' }
$lines = [System.Collections.Generic.List[string]]::new()
$relations = [System.Collections.Generic.List[string]]::new()
$constraints = [System.Collections.Generic.List[string]]::new()
$lines.Add('# Modelo ER gerado das entidades JPA')
$lines.Add('')
$lines.Add('Gerado por `scripts/gerar-modelo-er.ps1`. Nao editar manualmente este arquivo.')
$lines.Add('')
$lines.Add('Modelo esperado a partir do codigo, sem consultar o banco. Tipos SQL simplificados; `double` representa `double precision`. PK = chave primaria, FK = chave estrangeira, UK = unicidade. As UKs de associacoes um-para-um sao inferidas do mapeamento JPA.')
$lines.Add('')
$lines.Add('Cardinalidades: `||` exatamente um; `o|` zero ou um; `o{` zero ou muitos. Campos marcados NOT NULL possuem restricao explicita ou sao PK. Demais campos nao recebem garantia de nulabilidade neste diagrama.')
$lines.Add('')
$lines.Add('```mermaid')
$lines.Add('erDiagram')
foreach ($entity in $entities) {
    $lines.Add("    $($entity.Table) {")
    foreach ($field in $entity.Fields) {
        $annotations = $field.Groups['annotations'].Value
        $type = $field.Groups['type'].Value
        $name = $field.Groups['name'].Value
        if ($annotations -match '@Transient\b|mappedBy\s*=') { continue }
        if ($type -match '<') { throw "Colecao sem mappedBy nao suportada: $($entity.Name).$name" }
        $column = ConvertTo-SnakeCase $name
        $columnMatch = [regex]::Match($annotations, '@(?:JoinColumn|Column)\(\s*name\s*=\s*"([^"]+)"')
        if ($columnMatch.Success) { $column = $columnMatch.Groups[1].Value }
        $keys = @()
        if ($annotations -match '@Id\b') { $keys += 'PK' }
        if ($tables.ContainsKey($type)) {
            if ($annotations -notmatch '@(ManyToOne|OneToOne)\b') { throw "Associacao desconhecida: $name" }
            if (-not $columnMatch.Success) { $column = "${column}_id" }
            $sqlType = 'bigint'
            $keys += 'FK'
            $parent = 'o|'
            if ($annotations -match '(?:nullable|optional)\s*=\s*false') { $parent = '||' }
            $children = 'o{'
            if ($annotations -match '@OneToOne\b|unique\s*=\s*true') { $children = 'o|'; $keys += 'UK' }
            $relations.Add("    $($tables[$type]) $parent..$children $($entity.Table) : $column")
        } elseif ($types.ContainsKey($type)) {
            $sqlType = $types[$type]
        } elseif ($annotations -match '@Enumerated\(EnumType.STRING\)') {
            $sqlType = 'varchar'
        } else {
            throw "Tipo nao suportado: $($entity.Name).$name ($type)"
        }
        if ($annotations -match 'unique\s*=\s*true' -and $keys -notcontains 'UK') { $keys += 'UK' }
        $suffix = ''
        if ($keys.Count -gt 0) { $suffix = ' ' + ($keys -join ',') }
        $notes = @()
        if ($annotations -match 'nullable\s*=\s*false|@Id\b') { $notes += 'NOT NULL' }
        # @Convert nos campos sensiveis: o conversor grava o valor cifrado
        if ($annotations -match '@Convert\b') { $notes += 'cifrado' }
        if ($notes.Count -gt 0) { $suffix += ' "' + ($notes -join ', ') + '"' }
        $lines.Add("        $sqlType $column$suffix")
    }
    $lines.Add('    }')
    foreach ($constraint in [regex]::Matches($entity.Source, '@UniqueConstraint\((?:\s*name\s*=\s*"[^"]*"\s*,)?\s*columnNames\s*=\s*\{([^}]+)\}')) {
        $columns = [regex]::Matches($constraint.Groups[1].Value, '"([^"]+)"') | ForEach-Object { $_.Groups[1].Value }
        $constraints.Add('- `' + $entity.Table + '`: UNIQUE (' + ($columns -join ', ') + ').')
    }
}
foreach ($relation in $relations) { $lines.Add($relation) }
$lines.Add('```')
$lines.Add('')
$lines.Add('## Unicidades compostas')
$lines.Add('')
foreach ($constraint in $constraints) { $lines.Add($constraint) }
$lines.Add('')
$lines.Add('Descricoes e regras de exclusao: [dicionario do banco](BANCO_DE_DADOS.md).')
$content = ($lines -join "`n") + "`n"
if ($Check) {
    if (-not (Test-Path $outputPath) -or ([IO.File]::ReadAllText($outputPath).Replace("`r`n", "`n") -cne $content)) {
        throw 'Modelo ER desatualizado. Execute scripts/gerar-modelo-er.ps1.'
    }
    Write-Output 'Modelo ER atualizado.'
} else {
    [IO.File]::WriteAllText($outputPath, $content, [Text.UTF8Encoding]::new($false))
    Write-Output "Modelo ER gerado: $($entities.Count) tabelas, $($relations.Count) relacionamentos."
}
