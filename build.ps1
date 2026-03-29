$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
$sourceRoot = Join-Path $projectRoot "src\main\java"
$outputRoot = Join-Path $projectRoot "out\main"

if (Test-Path $outputRoot) {
    Remove-Item -Recurse -Force $outputRoot
}

New-Item -ItemType Directory -Path $outputRoot -Force | Out-Null
$sources = Get-ChildItem -Path $sourceRoot -Recurse -Filter *.java | ForEach-Object FullName

if (-not $sources) {
    throw "No Java source files were found under $sourceRoot"
}

& javac --release 26 -encoding UTF-8 -d $outputRoot $sources
Write-Host "Compiled main sources to $outputRoot"

