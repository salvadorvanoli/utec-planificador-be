#!/usr/bin/env pwsh
# =============================================================================
# SCRIPT: scripts/status.ps1
# =============================================================================
# Muestra el estado del backend
# =============================================================================

$ErrorActionPreference = "Stop"

# Cambiar al directorio raíz del proyecto
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Estado del Backend                     " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "[Contenedores]" -ForegroundColor Blue
docker-compose ps

Write-Host ""
Write-Host "[Health Checks]" -ForegroundColor Blue

# Check database
Write-Host "  PostgreSQL..." -NoNewline
try {
    $dbCheck = docker-compose exec -T db pg_isready -U postgres 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host " OK" -ForegroundColor Green
    }
    else {
        Write-Host " ERROR" -ForegroundColor Red
    }
}
catch {
    Write-Host " NO DISPONIBLE" -ForegroundColor Red
}

# Check API
Write-Host "  Spring Boot API..." -NoNewline
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api/actuator/health" -Method Get -TimeoutSec 3 -UseBasicParsing -ErrorAction Stop
    if ($response.StatusCode -eq 200) {
        Write-Host " OK" -ForegroundColor Green
    }
    else {
        Write-Host " WARN (Status: $($response.StatusCode))" -ForegroundColor Yellow
    }
}
catch {
    Write-Host " NO RESPONDE" -ForegroundColor Red
}

Write-Host ""
Write-Host "[Endpoints]" -ForegroundColor Blue
Write-Host "  API:          http://localhost:8080/api" -ForegroundColor White
Write-Host "  Health:       http://localhost:8080/api/actuator/health" -ForegroundColor White
Write-Host "  Swagger:      http://localhost:8080/api/swagger-ui.html" -ForegroundColor White
Write-Host ""
