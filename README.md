# PlanificadorUTEC - Backend
Proyecto "Planificador UTEC" - API REST con Spring Boot

## Inicio Rápido

### Opción 1: Docker (Recomendado)

```powershell
# Iniciar backend (PostgreSQL + API)
.\scripts\start.ps1

# Ver estado
.\scripts\status.ps1

# Ver logs
.\scripts\logs.ps1

# Detener
.\scripts\stop.ps1
```

**URLs disponibles:**
- API: http://localhost:8080/api
- Health: http://localhost:8080/api/actuator/health
- Swagger: http://localhost:8080/api/swagger-ui.html

### Opción 2: Desarrollo Local

**Requisitos:**
- PostgreSQL 16 corriendo en localhost:5432
- Java 21 JDK
- Base de datos `planificador_db` creada

```powershell
# Cargar variables de entorno y ejecutar
.\run-local.ps1
```

## Requisitos

- **Docker Desktop** (para opción 1)
- **Java 21 JDK** (para opción 2)
- **PostgreSQL 16** (para opción 2)
- **PowerShell** (incluido en Windows, instalable en Linux/macOS)

## Configuración

1. Copia `.env.example` a `.env`
2. Edita `.env` con tus configuraciones
3. Ejecuta `.\start.ps1`

## Testing

El proyecto cuenta con una suite completa de tests automatizados:

### Ejecutar Tests
```bash
# Ejecutar todos los tests
./gradlew test

# Ejecutar con reporte de cobertura
./gradlew test jacocoTestReport

# Ver reportes
# Tests: build/reports/tests/test/index.html
# Cobertura: build/reports/jacoco/test/html/index.html
```

### Documentación de Tests
- **Guía completa**: [docs/test/TESTING.md](docs/test/TESTING.md)

### Cobertura
- **Objetivo mínimo**: 60%
- **Objetivo recomendado**: 80%
- **Tests implementados**: 47+ tests unitarios e integración

### CI/CD
Los tests se ejecutan automáticamente en cada push y pull request mediante GitHub Actions.
Ver: [.github/workflows/backend-ci.yml](.github/workflows/backend-ci.yml)

