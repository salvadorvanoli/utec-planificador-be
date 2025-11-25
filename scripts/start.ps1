#!/usr/bin/env pwsh
# =============================================================================
# SCRIPT: scripts/start.ps1
# =============================================================================
# Inicia el backend del Planificador Docente UTEC
# Incluye: PostgreSQL + Spring Boot API
# =============================================================================

$ErrorActionPreference = "Stop"

# Cambiar al directorio raíz del proyecto
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Iniciando Backend - Planificador UTEC " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar Docker
try {
    docker --version | Out-Null
    Write-Host "[OK] Docker disponible" -ForegroundColor Green
}
catch {
    Write-Host "[ERROR] Docker no está instalado o no está en el PATH" -ForegroundColor Red
    Write-Host "Instala Docker desde: https://docs.docker.com/get-docker/" -ForegroundColor Yellow
    exit 1
}

# Verificar .env
if (-not (Test-Path ".env")) {
    Write-Host "[ADVERTENCIA] Archivo .env no encontrado" -ForegroundColor Yellow
    
    if (Test-Path ".env.example") {
        Write-Host "[INFO] Creando .env desde .env.example..." -ForegroundColor Yellow
        Copy-Item ".env.example" ".env"
        Write-Host "[OK] Archivo .env creado" -ForegroundColor Green
        Write-Host "[ACCION REQUERIDA] Edita el archivo .env con tus configuraciones" -ForegroundColor Yellow
        Write-Host "Ejecuta: code .env" -ForegroundColor Cyan
        Write-Host ""
    }
    else {
        Write-Host "[ERROR] Tampoco existe .env.example" -ForegroundColor Red
        exit 1
    }
}
else {
    Write-Host "[OK] Archivo .env encontrado" -ForegroundColor Green
}

Write-Host ""
Write-Host "[PASO 1/3] Iniciando servicios..." -ForegroundColor Blue

docker-compose up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "[ERROR] Falló el inicio de los servicios" -ForegroundColor Red
    exit 1
}

Write-Host "[OK] Contenedores iniciados" -ForegroundColor Green
Write-Host ""

Write-Host "[PASO 2/3] Esperando que la base de datos esté lista..." -ForegroundColor Blue

$maxAttempts = 30
$attempt = 0
$dbReady = $false

while ($attempt -lt $maxAttempts) {
    $attempt++
    
    try {
        $result = docker-compose exec -T db pg_isready -U postgres 2>&1
        if ($LASTEXITCODE -eq 0) {
            $dbReady = $true
            break
        }
    }
    catch {
        # Ignorar errores
    }
    
    Write-Host "  Intento $attempt/$maxAttempts..." -ForegroundColor Gray
    Start-Sleep -Seconds 1
}

if ($dbReady) {
    Write-Host "[OK] Base de datos lista" -ForegroundColor Green
}
else {
    Write-Host "[ADVERTENCIA] Base de datos tardó más de lo esperado" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "[PASO 3/3] Esperando que la API esté lista..." -ForegroundColor Blue

$maxAttempts = 60
$attempt = 0
$apiReady = $false

while ($attempt -lt $maxAttempts) {
    $attempt++
    
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/api/actuator/health" -Method Get -TimeoutSec 2 -UseBasicParsing -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            $apiReady = $true
            break
        }
    }
    catch {
        # Ignorar errores de conexión
    }
    
    if ($attempt -eq 1 -or $attempt % 5 -eq 0) {
        Write-Host "  Intento $attempt/$maxAttempts..." -ForegroundColor Gray
    }
    Start-Sleep -Seconds 2
}

Write-Host ""

if ($apiReady) {
    Write-Host "========================================" -ForegroundColor Green
    Write-Host " BACKEND INICIADO CORRECTAMENTE        " -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "Servicios disponibles:" -ForegroundColor Cyan
    Write-Host "  API:          http://localhost:8080/api" -ForegroundColor White
    Write-Host "  Health:       http://localhost:8080/api/actuator/health" -ForegroundColor White
    Write-Host "  Swagger:      http://localhost:8080/api/swagger-ui.html" -ForegroundColor White
    Write-Host "  Database:     localhost:5432 (interno)" -ForegroundColor White
    Write-Host ""
    Write-Host "Comandos útiles:" -ForegroundColor Cyan
    Write-Host "  Ver logs:     .\scripts\logs.ps1" -ForegroundColor White
    Write-Host "  Ver estado:   .\scripts\status.ps1" -ForegroundColor White
    Write-Host "  Detener:      .\scripts\stop.ps1" -ForegroundColor White
    Write-Host ""
}
else {
    Write-Host "========================================" -ForegroundColor Yellow
    Write-Host " BACKEND INICIADO CON ADVERTENCIAS     " -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "La API está tardando en iniciar. Esto puede ser normal." -ForegroundColor Yellow
    Write-Host "Verifica el estado con: .\scripts\logs.ps1 app" -ForegroundColor Cyan
    Write-Host ""
}
