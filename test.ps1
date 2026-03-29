$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
& (Join-Path $projectRoot "build.ps1")

$testSourceRoot = Join-Path $projectRoot "src\test\java"
$testOutputRoot = Join-Path $projectRoot "out\test"
$mainOutputRoot = Join-Path $projectRoot "out\main"

if (Test-Path $testOutputRoot) {
    Remove-Item -Recurse -Force $testOutputRoot
}

New-Item -ItemType Directory -Path $testOutputRoot -Force | Out-Null
$testSources = Get-ChildItem -Path $testSourceRoot -Recurse -Filter *.java | ForEach-Object FullName

if (-not $testSources) {
    throw "No test Java source files were found under $testSourceRoot"
}

& javac --release 26 -encoding UTF-8 -cp $mainOutputRoot -d $testOutputRoot $testSources
& java -ea -cp "$mainOutputRoot;$testOutputRoot" com.othello.test.OthelloTestRunner

