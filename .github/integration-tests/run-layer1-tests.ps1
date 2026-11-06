$ErrorActionPreference = "Stop"
$projectRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
Push-Location $projectRoot
try {
    & .\mvnw.cmd verify "-Dgroups=Layer1"
    $testExit = $LASTEXITCODE
    if ($testExit -eq 0) {
        Write-Host "✅ Layer 1 integration tests PASSED"
    }
    else {
        Write-Host "❌ Layer 1 integration tests FAILED"
    }
    exit $testExit
}
finally {
    Pop-Location
}
