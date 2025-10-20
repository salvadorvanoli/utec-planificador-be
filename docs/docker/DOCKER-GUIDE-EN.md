# Docker Implementation - UTEC Planner Backend

**Stack:** Spring Boot 3.5.6 + PostgreSQL 16 + Docker  
**Version:** 1.0.0

---

## Configuration Files

### **Dockerfile**
Multi-stage build that optimizes the final image.

**Stage 1 - Builder:**
- Base: `gradle:8.5-jdk21-alpine`
- Downloads dependencies (cached)
- Compiles source code
- Generates executable JAR

**Stage 2 - Runtime:**
- Base: `eclipse-temurin:21-jre-alpine`
- Non-root user (`spring`)
- Configurable JVM options via `JAVA_OPTS`
- Final image: ~200MB

### **.dockerignore**
Excludes unnecessary files from build context:
- Git, IDEs, build artifacts
- Documentation, tests
- Environment variables (`.env`)
- Reduces context from ~500MB to ~50MB

### **docker-compose.yml**
Base configuration shared across environments.

**Services:**
- **db:** PostgreSQL 16-alpine with health check
- **app:** Spring Boot with 18 configurable variables

**Features:**
- Network isolation (`app-network`)
- Persistent volume for PostgreSQL
- Health checks for dependency management
- 18 environment variables with defaults

### **docker-compose.override.yml**
Automatic overrides for development.

**Changes:**
- Exposes DB port (5432)
- Remote debugging enabled (port 5005)
- SQL queries visible (`JPA_SHOW_SQL=true`)
- Locally mounted logs
- DevTools enabled

### **docker-compose.prod.yml**
Production configuration.

**Key differences:**
- DB NOT exposed
- Resource limits (CPU/Memory)
- Configured restart policy
- Logging with rotation
- JPA DDL in `validate` mode
- SQL logs disabled

### **.env**
Environment variables for local development.  
**[!] DO NOT commit** (in `.gitignore`)

### **.env.example**
Documented variable template.  
**[OK] Commit** as reference.

---

## Environment Variables (18 total)

| Category | Variables | Examples |
|----------|-----------|----------|
| **Project** | 1 | `COMPOSE_PROJECT_NAME` |
| **Database** | 3 | `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` |
| **Application** | 2 | `SPRING_PROFILES_ACTIVE`, `SERVER_PORT` |
| **JPA** | 2 | `JPA_DDL_AUTO`, `HIBERNATE_BATCH_SIZE` |
| **Hikari Pool** | 5 | `HIKARI_MAX_POOL_SIZE`, timeouts |
| **CORS** | 1 | `CORS_ALLOWED_ORIGINS` |
| **Health Checks** | 8 | Intervals, timeouts, retries |
| **JVM** | 1 | `JAVA_OPTS` |

---

## Essential Commands

### **Development**

```bash
# First time
cp .env.example .env
docker-compose up -d

# After code changes
docker-compose up -d --build

# View logs
docker-compose logs -f app

# Restart
docker-compose restart app

# Stop
docker-compose down
```

### **Production**

```bash
# Build
docker-compose -f docker-compose.yml -f docker-compose.prod.yml build

# Deploy
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# Monitor
docker-compose -f docker-compose.yml -f docker-compose.prod.yml logs -f app
docker stats
```

### **Validation**

```bash
# Check syntax
docker-compose config --quiet

# Service status
docker-compose ps

# Health check
curl http://localhost:8080/api/actuator/health
```

---

## Architecture

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

## Workflow

### **Scenario 1: Java code changes**
```bash
docker-compose up -d --build
```

### **Scenario 2: .env changes**
```bash
docker-compose restart app
```

### **Scenario 3: docker-compose.yml changes**
```bash
docker-compose down
docker-compose up -d
```

---

## Maintenance

### **Disk Space Management**

Each `docker-compose up -d --build` creates a **new image** without removing the old one. Over time, this accumulates **dangling images** (orphaned builds).

**Check disk usage:**
```bash
# View Docker disk usage
docker system df

# List dangling images
docker images -f "dangling=true"
```

**Clean up regularly:**
```bash
# Remove dangling images only (RECOMMENDED)
docker image prune

# Remove all unused images (more aggressive)
docker image prune -a

# Full system cleanup (CAUTION: removes everything unused)
docker system prune -a --volumes
```

**Best practice:** Run `docker image prune` weekly during development to free space from old builds.

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Port in use | Change `APP_PORT` in `.env` |
| App won't start | Check `docker-compose logs app` |
| DB won't connect | Verify credentials in `.env` |
| Slow build | Normal first time (~5 min) |
| Data lost | Don't use `down -v` |
| Disk space full | Run `docker image prune` |

---

## Endpoints

| URL | Description | Environment |
|-----|-------------|-------------|
| `localhost:8080/api/actuator/health` | Health check | Dev/Prod |
| `localhost:8080/api/actuator/info` | App info | Dev/Prod |
| `localhost:8080/api/actuator/metrics` | Metrics | Dev |
| `localhost:5432` | PostgreSQL | Dev only |
| `localhost:5005` | Remote debug | Dev only |

---

## Features

- Multi-stage build (~200MB final image)
- Non-root user (security)
- Automatic health checks
- 18 configurable variables
- Dev/prod separation
- Resource limits (prod)
- Network isolation
- Persistent volumes

---

**Documentation:** October 19, 2025  
**Status:** Production-ready
