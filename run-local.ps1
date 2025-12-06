#!/usr/bin/env pwsh

# ============================================
# UTEC Planificador - Local Development Runner
# ============================================
# This script loads environment variables from .env
# and runs the Spring Boot application locally.
#
# PREREQUISITES:
# 1. PostgreSQL running on localhost:5432
# 2. Database 'planificador_db' created
# 3. .env file properly configured
#
# USAGE:
#   PowerShell:    .\run-local.ps1
#   Linux/macOS:   pwsh ./run-local.ps1
#
# This will:
# - Load all variables from .env
# - Activate the 'dev' Spring profile
# - Start the application with bootRun
# ============================================

# Set error action preference to stop on errors
$ErrorActionPreference = "Stop"

# Function to load .env file
function Load-DotEnv {
    param (
        [string]$envFilePath = ".env"
    )

    if (-not (Test-Path $envFilePath)) {
        Write-Host "Error: .env file not found" -ForegroundColor Red
        Write-Host "Please copy .env.example to .env and configure it" -ForegroundColor Yellow
        exit 1
    }

    Write-Host "Loading environment variables from .env..." -ForegroundColor Cyan

    # Read the .env file
    Get-Content $envFilePath | ForEach-Object {
        $line = $_.Trim()
        
        # Skip empty lines and comments
        if ($line -eq "" -or $line.StartsWith("#")) {
            return
        }

        # Parse KEY=VALUE format
        if ($line -match '^([^=]+)=(.*)$') {
            $key = $matches[1].Trim()
            $value = $matches[2].Trim()
            
            # Remove surrounding quotes if present
            $value = $value -replace '^["'']|["'']$', ''
            
            # Set environment variable for current process
            [System.Environment]::SetEnvironmentVariable($key, $value, [System.EnvironmentVariableTarget]::Process)
            
            Write-Verbose "Loaded: $key"
        }
    }

    Write-Host "Environment variables loaded" -ForegroundColor Green
}

# Main execution
try {
    Write-Host ""
    Write-Host "=====================================" -ForegroundColor Magenta
    Write-Host "  UTEC Planificador - Local Dev     " -ForegroundColor Magenta
    Write-Host "=====================================" -ForegroundColor Magenta
    Write-Host ""

    # Load environment variables
    Load-DotEnv

    Write-Host ""
    Write-Host "Starting Spring Boot application with 'dev' profile..." -ForegroundColor Cyan
    Write-Host ""

    # Determine the correct gradlew executable based on OS
    # Use PSVersionTable.Platform for cross-version compatibility
    if ($PSVersionTable.PSVersion.Major -ge 6) {
        # PowerShell Core 6+ has $IsWindows automatic variable (read-only)
        $isWindowsOS = $IsWindows
    } else {
        # Windows PowerShell 5.1 and earlier (only runs on Windows)
        $isWindowsOS = $true
    }

    $gradlewCmd = if ($isWindowsOS) {
        ".\gradlew.bat"
    } else {
        "./gradlew"
    }

    # Make gradlew executable on Unix-like systems (already done by git)
    # No need for chmod as gradlew comes with execute permissions from repo

    # Run the application with dev profile
    & $gradlewCmd bootRun --args='--spring.profiles.active=dev'

    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "Application exited with error code: $LASTEXITCODE" -ForegroundColor Red
        exit $LASTEXITCODE
    }
}
catch {
    Write-Host ""
    Write-Host "An error occurred: $_" -ForegroundColor Red
    exit 1
}
