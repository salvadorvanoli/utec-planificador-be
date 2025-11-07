#!/bin/bash

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
#   ./run-local.sh
#
# This will:
# - Load all variables from .env
# - Activate the 'dev' Spring profile
# - Start the application with bootRun
# ============================================

# Check if .env exists
if [ ! -f .env ]; then
    echo "❌ Error: .env file not found"
    echo "Please copy .env.example to .env and configure it"
    exit 1
fi

echo "Loading environment variables from .env..."

# Load .env file
# set -a: automatically export all variables
# source .env: load the file
# set +a: stop auto-exporting
set -a
source .env
set +a

echo "Environment variables loaded"
echo "Starting Spring Boot application with 'dev' profile..."
echo ""

# Run the application with dev profile
./gradlew bootRun --args='--spring.profiles.active=dev'