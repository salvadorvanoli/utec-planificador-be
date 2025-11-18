# PlanificadorUTEC
Proyecto "Planificador UTEC"

## 🧪 Testing

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

