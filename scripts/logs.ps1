#!/usr/bin/env pwsh
# =============================================================================
# SCRIPT: scripts/logs.ps1
# =============================================================================
# Muestra los logs del backend
# =============================================================================

param(
    [Parameter(Position=0)]
    [ValidateSet('all', 'app', 'db')]
    [string]$Service = 'all',
    
    [Parameter()]
    [switch]$Follow,
    
    [Parameter()]
    [int]$Tail = 100
)

$ErrorActionPreference = "Stop"

# Cambiar al directorio raíz del proyecto
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Logs del Backend                       " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$args = @('logs')
if ($Follow) { $args += '--follow' }
$args += '--tail', $Tail

switch ($Service) {
    'app' { 
        Write-Host "Mostrando logs de: Spring Boot API" -ForegroundColor Yellow
        $args += 'app'
    }
    'db' { 
        Write-Host "Mostrando logs de: PostgreSQL" -ForegroundColor Yellow
        $args += 'db'
    }
    'all' { 
        Write-Host "Mostrando logs de: Todos los servicios" -ForegroundColor Yellow
    }
}

Write-Host ""

& docker-compose @args

Write-Host ""
Write-Host "Uso:" -ForegroundColor Cyan
Write-Host "  .\scripts\logs.ps1 [all|app|db] [-Follow] [-Tail <número>]" -ForegroundColor White
Write-Host ""
Write-Host "Ejemplos:" -ForegroundColor Cyan
Write-Host "  .\scripts\logs.ps1 app          # Últimos 100 logs de la API" -ForegroundColor Gray
Write-Host "  .\scripts\logs.ps1 app -Follow  # Seguir logs de la API en tiempo real" -ForegroundColor Gray
Write-Host "  .\scripts\logs.ps1 db -Tail 50  # Últimos 50 logs de la base de datos" -ForegroundColor Gray
Write-Host ""
