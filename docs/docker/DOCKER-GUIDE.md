# Implementación Docker - Backend UTEC Planificador

**Stack:** Spring Boot 3.5.6 + PostgreSQL 16 + Docker  
**Versión:** 1.0.0

---

## Archivos de Configuración

### **Dockerfile**
Multi-stage build que optimiza la imagen final.

**Stage 1 - Builder:**
- Base: `gradle:8.5-jdk21-jammy` (Ubuntu 22.04)
- Descarga dependencias (cacheadas)
- Compila código fuente
- Genera JAR ejecutable

**Stage 2 - Runtime:**
- Base: `ubuntu:24.04` (Ubuntu 24.04 LTS)
- OpenJDK 21 JRE Headless
- Usuario no-root (`spring`)
- JVM options configurables via `JAVA_OPTS`
- Imagen final: ~250MB

### **.dockerignore**
Excluye archivos innecesarios del contexto de build:
- Git, IDEs, build artifacts
- Documentación, tests
- Variables de entorno (`.env`)
- Reduce contexto de ~500MB a ~50MB

### **docker-compose.yml**
Configuración base compartida entre ambientes.

**Servicios:**
- **db:** PostgreSQL 16-alpine con health check
- **app:** Spring Boot con 18 variables configurables

**Características:**
- Network isolation (`app-network`)
- Volume persistente para PostgreSQL
- Health checks para dependency management
- 18 variables de entorno con defaults

### **docker-compose.override.yml**
Overrides automáticos para desarrollo.

**Cambios:**
- Expone puerto DB (5432)
- Debug remoto habilitado (puerto 5005)
- SQL queries visibles (`JPA_SHOW_SQL=true`)
- Logs montados localmente
- DevTools habilitado

### **docker-compose.prod.yml**
Configuración para producción.

**Diferencias clave:**
- DB NO expuesto
- Resource limits (CPU/Memory)
- Restart policy configurado
- Logging con rotación
- JPA DDL en modo `validate`
- SQL logs deshabilitados

### **.env**
Variables de entorno para desarrollo local.  
**[!] NO commitear** (está en `.gitignore`)

### **.env.example**
Template documentado de variables.  
**[OK] Commitear** como referencia.

---

## Variables de Entorno (18 total)

| Categoría | Variables | Ejemplos |
|-----------|-----------|----------|
| **Project** | 1 | `COMPOSE_PROJECT_NAME` |
| **Database** | 3 | `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` |
| **Application** | 2 | `SPRING_PROFILES_ACTIVE`, `SERVER_PORT` |
| **JPA** | 2 | `JPA_DDL_AUTO`, `HIBERNATE_BATCH_SIZE` |
| **Hikari Pool** | 5 | `HIKARI_MAX_POOL_SIZE`, timeouts |
| **CORS** | 1 | `CORS_ALLOWED_ORIGINS` |
| **Health Checks** | 8 | Intervals, timeouts, retries |
| **JVM** | 1 | `JAVA_OPTS` |

---

## Comandos Esenciales

### **Desarrollo**

```bash
# Primera vez
cp .env.example .env
docker-compose up -d

# Después de cambios en código
docker-compose up -d --build

# Ver logs
docker-compose logs -f app

# Reiniciar
docker-compose restart app

# Detener
docker-compose down
```

### **Producción**

```bash
# Build
docker-compose -f docker-compose.yml -f docker-compose.prod.yml build

# Deploy
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# Monitorear
docker-compose -f docker-compose.yml -f docker-compose.prod.yml logs -f app
docker stats
```

### **Validación**

```bash
# Verificar sintaxis
docker-compose config --quiet

# Estado de servicios
docker-compose ps

# Health check
curl http://localhost:8080/api/actuator/health
```

---

## Arquitectura

```
┌──────────────────────────────┐
│   Docker Compose             │
├──────────────────────────────┤
│  ┌──────────┐  ┌──────────┐ │
│  │PostgreSQL│◄─┤Spring Boot│ │
│  │  :5432   │  │   :8080  │ │
│  └──────────┘  └──────────┘ │
│       │             │        │
│  postgres_data    logs       │
│                              │
│    app-network (bridge)      │
└──────────────────────────────┘
```

---

## Flujo de Trabajo

### **Escenario 1: Cambios en código Java**
```bash
docker-compose up -d --build
```

### **Escenario 2: Cambios en .env**
```bash
docker-compose restart app
```

### **Escenario 3: Cambios en docker-compose.yml**
```bash
docker-compose down
docker-compose up -d
```

---

## Mantenimiento

### **Gestión de Espacio en Disco**

Cada `docker-compose up -d --build` crea una **nueva imagen** sin eliminar la anterior. Con el tiempo, esto acumula **imágenes huérfanas** (builds antiguos).

**Verificar uso de disco:**
```bash
# Ver uso de disco de Docker
docker system df

# Listar imágenes huérfanas
docker images -f "dangling=true"
```

**Limpiar periódicamente:**
```bash
# Eliminar solo imágenes huérfanas (RECOMENDADO)
docker image prune

# Eliminar todas las imágenes no usadas (más agresivo)
docker image prune -a

# Limpieza completa del sistema (PRECAUCIÓN: elimina todo lo no usado)
docker system prune -a --volumes
```

**Buena práctica:** Ejecutar `docker image prune` semanalmente durante desarrollo para liberar espacio de builds antiguos.

---

## Troubleshooting

| Problema | Solución |
|----------|----------|
| Puerto en uso | Cambiar `APP_PORT` en `.env` |
| App no inicia | Revisar `docker-compose logs app` |
| DB no conecta | Verificar credenciales en `.env` |
| Build lento | Normal primera vez (~5 min) |
| Datos perdidos | No usar `down -v` |
| Disco lleno | Ejecutar `docker image prune` |

---

## Endpoints

| URL | Descripción | Ambiente |
|-----|-------------|----------|
| `localhost:8080/api/actuator/health` | Health check | Dev/Prod |
| `localhost:8080/api/actuator/info` | App info | Dev/Prod |
| `localhost:8080/api/actuator/metrics` | Métricas | Dev |
| `localhost:5432` | PostgreSQL | Solo Dev |
| `localhost:5005` | Debug remoto | Solo Dev |

---

## Características

- Multi-stage build (~200MB imagen final)
- Non-root user (seguridad)
- Health checks automáticos
- 18 variables configurables
- Separación dev/prod
- Resource limits (prod)
- Network isolation
- Persistent volumes

---

**Documentación:** 19 de octubre de 2025  
**Status:** Production-ready
