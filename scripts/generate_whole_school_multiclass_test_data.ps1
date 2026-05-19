$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$primary = Join-Path $root 'docs\product\primary-school-homework\整校多班测试数据.sql'
$mirror = Join-Path $root 'docs\product\primary-school-homework\whole-school-multi-class-test-data.sql'

if (-not (Test-Path $primary)) { throw "Missing SQL file: $primary" }
if (-not (Test-Path $mirror)) { throw "Missing SQL file: $mirror" }

Write-Output 'Whole-school SQL seed data is ready.'
Write-Output ("Primary: {0}" -f $primary)
Write-Output ("Mirror : {0}" -f $mirror)
