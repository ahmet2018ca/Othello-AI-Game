param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$AppArgs
)

$ErrorActionPreference = "Stop"

$projectRoot = $PSScriptRoot
& (Join-Path $projectRoot "build.ps1")

$mainOutput = Join-Path $projectRoot "out\main"
& java -cp $mainOutput com.othello.app.OthelloApplication @AppArgs

