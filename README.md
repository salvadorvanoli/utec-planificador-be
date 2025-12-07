<div align="center">

# UTEC Planificador - Backend

### Sistema de Planificación Académica con IA

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21%20LTS-orange?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![Tests](https://img.shields.io/badge/Tests-89%20passing-success?logo=junit5&logoColor=white)](./docs/test/TESTING.md)
[![Security](https://img.shields.io/badge/Security-OWASP%20Compliant-red?logo=owasp&logoColor=white)](./docs/security/SECURITY_OVERVIEW.md)

**API REST robusta y escalable para la gestión integral de planificación docente en UTEC**

[Características](#-características-principales) •
[Inicio Rápido](#-inicio-rápido) •
[Arquitectura](#-arquitectura) •
[Documentación](#-documentación) •
[Testing](#-testing)

</div>

---

## Tabla de Contenidos

- [Descripción](#-descripción)
- [Características Principales](#-características-principales)
- [Stack Tecnológico](#-stack-tecnológico)
- [Arquitectura](#-arquitectura)
- [Inicio Rápido](#-inicio-rápido)
- [Configuración](#-configuración)
- [API Endpoints](#-api-endpoints)
- [Seguridad](#-seguridad)
- [Testing](#-testing)
- [Base de Datos](#-base-de-datos)
- [Documentación](#-documentación)
- [Contribuir](#-contribuir)

---

## Descripción

**UTEC Planificador Backend** es una API REST empresarial construida con Spring Boot que proporciona servicios de backend para el sistema de planificación académica de la Universidad Tecnológica del Uruguay (UTEC). El sistema gestiona cursos, docentes, planificaciones semanales, contenido programático y se integra con un agente de IA para asesoramiento académico inteligente.

### ¿Qué resuelve?

- **Gestión de Cursos**: Administración completa del ciclo de vida de cursos académicos
- **Planificación Docente**: Planificaciones semanales con actividades, bibliografía y recursos
- **Control de Acceso Multinivel**: Sistema RBAC con 5 roles (Teacher, Coordinator, Analyst, Education Manager, Administrator)
- **Autenticación Dual**: Soporte para autenticación local (BCrypt) y LDAP corporativo
- **Integración IA**: Comunicación con agente conversacional para asesoramiento pedagógico
- **Multi-Campus**: Soporte para múltiples sedes (Montevideo, ITR Centro-Sur, ITR Norte, etc.)
- **Auditoría Completa**: Registro de modificaciones con trazabilidad total

---

## Características Principales

### Seguridad Empresarial

- **Autenticación Dual**: LOCAL (BCrypt factor 12) / LDAP (Active Directory)
- **Autorización RBAC**: 5 roles con permisos granulares
- **JWT Tokens**: HS512 con expiración configurable
- **OWASP Top 10**: Cumplimiento completo 2021
- **Headers de Seguridad**: CSP, HSTS, X-Frame-Options, etc.
- **Validación de Entradas**: Bean Validation en todos los DTOs
- **Prevención de Inyecciones**: Named parameters en JPA, sanitización LDAP

### Gestión Académica

- **Cursos**: CRUD completo con filtros avanzados, paginación y búsqueda
- **Planificación Semanal**: Gestión por semana, fecha o período académico
- **Contenido Programático**: Objetivos, competencias, metodologías, evaluación
- **Actividades**: Presenciales/virtuales con duración, recursos y observaciones
- **Bibliografía**: Referencias bibliográficas con soporte físico/digital
- **ODS y DUA**: Objetivos de Desarrollo Sostenible y Diseño Universal para el Aprendizaje

### Integración IA

- **Chat Conversacional**: Endpoint proxy al agente FastAPI
- **Generación de Informes**: Reportes automáticos sobre cursos
- **Sugerencias Inteligentes**: Recomendaciones pedagógicas basadas en GPT-4o-mini
- **Contexto de Curso**: El agente tiene acceso completo a los datos del curso

### Arquitectura Moderna

- **Clean Architecture**: Separación en capas (Controller → Service → Repository → Entity)
- **DTOs**: Request/Response separados para mayor control
- **Strategy Pattern**: Autenticación pluggable (LOCAL/LDAP)
- **Repository Pattern**: Abstracción de acceso a datos con Spring Data JPA
- **Exception Handling**: Manejo global con `@ControllerAdvice`
- **Logging Estructurado**: Logs con contexto y niveles configurables

### Observabilidad

- **Spring Boot Actuator**: Endpoints de health, metrics, info
- **Health Checks**: Database, LDAP, diskspace
- **Logs**: Archivo rotativo con niveles configurables por paquete
- **Metrics**: Métricas de JVM, HTTP requests, database connections

---

## Stack Tecnológico

### Backend Core

| Tecnología | Versión | Propósito |
|-----------|---------|-----------|
| **Java** | 21 LTS | Lenguaje de programación |
| **Spring Boot** | 3.5.6 | Framework principal |
| **Spring Data JPA** | 3.5.6 | ORM y acceso a datos |
| **Spring Security** | 6.x | Autenticación y autorización |
| **Spring LDAP** | - | Integración con Active Directory |
| **Hibernate** | 6.x | Provider JPA |
| **Gradle** | 8.11.1 | Build tool y gestión de dependencias |

### Base de Datos

| Tecnología | Versión | Propósito |
|-----------|---------|-----------|
| **PostgreSQL** | 16 | Base de datos principal (producción) |
| **H2** | - | Base de datos en memoria (testing) |
| **HikariCP** | - | Connection pool |

### Seguridad

| Tecnología | Versión | Propósito |
|-----------|---------|-----------|
| **JWT** | jjwt 0.12.6 | JSON Web Tokens (HS512) |
| **BCrypt** | - | Hashing de contraseñas (factor 12) |
| **Spring Security LDAP** | - | Autenticación LDAP |
| **UnboundID LDAP SDK** | - | Cliente LDAP |

### Documentación

| Tecnología | Versión | Propósito |
|-----------|---------|-----------|
| **SpringDoc OpenAPI** | 2.7.0 | Generación automática de OpenAPI 3.0 |
| **Swagger UI** | - | Interfaz interactiva de API |

### Testing

| Tecnología | Versión | Propósito |
|-----------|---------|-----------|
| **JUnit 5** | - | Framework de testing |
| **Mockito** | - | Mocking para unit tests |
| **Spring Boot Test** | - | Testing de integración |
| **MockMvc** | - | Testing de controllers |
| **JaCoCo** | 0.8.11 | Cobertura de código |

### DevOps

| Tecnología | Versión | Propósito |
|-----------|---------|-----------|
| **Docker** | - | Containerización |
| **Docker Compose** | - | Orquestación multi-contenedor |
| **GitHub Actions** | - | CI/CD |
| **PowerShell** | - | Scripts de automatización |

### Utilidades

| Tecnología | Versión | Propósito |
|-----------|---------|-----------|
| **Lombok** | - | Reducción de boilerplate |
| **Jackson** | - | Serialización JSON |
| **Spring Boot DevTools** | - | Hot reload en desarrollo |
| **Spring Boot Actuator** | - | Monitoring y métricas |

---

## Arquitectura

### Arquitectura de Capas

```
┌─────────────────────────────────────────────────────────────────┐
│                         REST API Layer                          │
│  Controllers (@RestController) - Endpoints HTTP + Swagger Docs  │
└──────────────────────────┬──────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────┐
│                       Security Layer                            │
│  JWT Filter → Spring Security → RBAC (@PreAuthorize)            │
└──────────────────────────┬──────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────┐
│                       Service Layer                             │
│  Business Logic (@Service) - Strategy Pattern - Transactions    │
└──────────────────────────┬──────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────┐
│                      Repository Layer                           │
│  Data Access (@Repository) - Spring Data JPA - JPQL/Native SQL  │
└──────────────────────────┬──────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────┐
│                       Entity Layer                              │
│  JPA Entities (@Entity) - Hibernate Mappings - Relationships    │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                  ┌────────▼────────┐
                  │   PostgreSQL    │
                  │   Database      │
                  └─────────────────┘
```

### Estructura del Proyecto

El proyecto sigue una arquitectura en capas estándar de Spring Boot:

```
src/main/java/edu/utec/planificador/
├── config/              # Configuración (Security, CORS, LDAP, OpenAPI)
├── controller/          # REST Controllers con endpoints HTTP
├── dto/                 # Data Transfer Objects (Request/Response)
├── entity/              # Entidades JPA (User, Course, WeeklyPlanning, etc.)
├── enumeration/         # Enumeraciones del dominio (Role, Shift, ActivityType, etc.)
├── exception/           # Excepciones personalizadas y manejo global
├── repository/          # Repositorios Spring Data JPA
├── security/            # Componentes de seguridad (JWT, UserDetails, Filters)
├── service/             # Lógica de negocio e interfaces
│   └── impl/            # Implementaciones de servicios
└── util/                # Utilidades y helpers

src/main/resources/
├── application.yml          # Configuración principal
├── application-dev.yml      # Configuración de desarrollo
├── application-prod.yml     # Configuración de producción
└── logback-spring.xml       # Configuración de logging

src/test/
├── controller/          # Tests de integración de controladores
├── service/             # Tests unitarios de servicios
└── resources/           # Configuración de tests
```

### Modelo de Dominio (Entidades Principales)

```
User
└── Position[] (abstract)
    ├── Teacher
    ├── Coordinator
    ├── Analyst
    ├── EducationManager
    └── Administrator

Course
├── WeeklyPlanning[]
│   └── ProgrammaticContent[]
│       └── Activity[]
├── OfficeHours[]
├── Modification[]
└── CurricularUnit
    └── Term
        └── Program
            └── Campus
                └── RegionalTechnologicalInstitute
```

---

## Inicio Rápido

### Prerrequisitos

- **Docker Desktop** 4.x+ (recomendado) o
- **Java JDK** 21 LTS + **PostgreSQL** 16 (desarrollo local)
- **Git** (para clonar el repositorio)

### Opción 1: Docker (Recomendado)

**Clonar y configurar:**

```bash
# Clonar repositorio
git clone https://github.com/salvadorvanoli/utec-planificador-be.git
cd utec-planificador-be

# Configurar variables de entorno
cp .env.example .env
nano .env  # Editar JWT_SECRET y otras variables
```

**Iniciar servicios:**

```powershell
# Windows (PowerShell)
.\scripts\start.ps1

# Linux/macOS (bash)
./scripts/start.ps1  # Requiere PowerShell instalado
```

**Verificar que todo funciona:**

```bash
# Ver estado de los contenedores
.\scripts\status.ps1

# Ver logs en tiempo real
.\scripts\logs.ps1

# Probar API
curl http://localhost:8080/api/v1/auth/health
```

**URLs disponibles:**

- **API Base**: http://localhost:8080/api/v1
- **Swagger UI**: http://localhost:8080/api/v1/swagger-ui.html
- **API Docs (JSON)**: http://localhost:8080/api/v1/api-docs
- **Health Check**: http://localhost:8080/api/v1/actuator/health

**Detener servicios:**

```powershell
.\scripts\stop.ps1
```

### Opción 2: Desarrollo Local (sin Docker)

**Prerrequisitos adicionales:**

1. PostgreSQL 16 corriendo en `localhost:5432`
2. Base de datos creada: `planificador_db`
3. Usuario: `postgres` con contraseña configurada

**Configurar y ejecutar:**

```bash
# Configurar variables de entorno
cp .env.example .env
nano .env  # Configurar DATABASE_URL, DATABASE_USERNAME, DATABASE_PASSWORD

# Ejecutar con script (carga .env automáticamente)
.\run-local.ps1

# O con Gradle directamente
./gradlew bootRun --args='--spring.profiles.active=dev'

# O compilar JAR y ejecutar
./gradlew clean bootJar
java -jar build/libs/utec-planificador-docente-backend-0.0.1-SNAPSHOT.jar
```

### Primer Login

El sistema incluye un **DataSeeder** que carga datos iniciales en modo desarrollo:

```json
{
  "email": "juan.perez@utec.edu.uy",
  "password": "password123"
}
```

**Realizar login:**

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"juan.perez@utec.edu.uy","password":"password123"}'
```

Respuesta:

```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "email": "juan.perez@utec.edu.uy",
    "fullName": "Juan Pérez",
    "role": "TEACHER"
  }
}
```

---

## Configuración

### Variables de Entorno

El proyecto utiliza variables de entorno para configuración. Ver `.env.example` para la lista completa.

**Principales variables:**

```env
# Docker Compose
COMPOSE_PROJECT_NAME=utec-planificador

# Database
POSTGRES_DB=planificador_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=<tu-password-seguro>
DATABASE_URL=jdbc:postgresql://db:5432/planificador_db

# JWT
JWT_SECRET=<tu-secreto-largo-y-aleatorio-minimo-64-caracteres>
JWT_EXPIRATION=86400000  # 24 horas
JWT_ISSUER=UTEC-Planificador

# Authentication
AUTH_DEFAULT_PROVIDER=LOCAL  # LOCAL | LDAP

# AI Agent
AI_AGENT_BASE_URL=http://host.docker.internal:8000

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:4200

# Logging
LOG_LEVEL=INFO
LOG_SECURITY_LEVEL=DEBUG
```

### Perfiles de Spring Boot

| Perfil | Descripción | Uso |
|--------|-------------|-----|
| `dev` | Desarrollo local con DataSeeder | `--spring.profiles.active=dev` |
| `prod` | Producción con optimizaciones | `--spring.profiles.active=prod` |
| `test` | Testing con H2 in-memory | Activado automáticamente en tests |

### Configuración LDAP (Opcional)

Para habilitar autenticación LDAP:

```env
LDAP_ENABLED=true
LDAP_URL=ldap://ldap.utec.edu.uy:389
LDAP_BASE=dc=utec,dc=edu,dc=uy
LDAP_USER_DN_PATTERN=uid={0},ou=people
LDAP_MANAGER_DN=cn=admin,dc=utec,dc=edu,dc=uy
LDAP_MANAGER_PASSWORD=<ldap-admin-password>
```

---

## API Endpoints

### Autenticación (`/auth`)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/login` | Login con email/password | Público |
| GET | `/me` | Obtener usuario actual | JWT |
| GET | `/status` | Estado de autenticación | Público |
| GET | `/health` | Health check | Público |

### Usuarios (`/users`)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/` | Listar usuarios (paginado) | ADMIN, EDUCATION_MANAGER |
| GET | `/teachers` | Listar docentes | ALL |
| GET | `/positions` | Listar cargos de un docente | TEACHER, COORDINATOR+ |
| POST | `/positions` | Asignar cargo a docente | COORDINATOR+ |
| DELETE | `/positions/{id}` | Eliminar cargo | COORDINATOR+ |

### Cursos (`/courses`)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/` | Listar cursos (filtros, paginación) | ALL |
| GET | `/{id}` | Obtener curso por ID | ALL |
| GET | `/latest` | Últimos cursos creados | ALL |
| GET | `/campus/{id}/my-courses` | Mis cursos en sede | TEACHER+ |
| POST | `/` | Crear curso | COORDINATOR+ |
| PUT | `/{id}` | Actualizar curso | TEACHER+ (owner) |
| DELETE | `/{id}` | Eliminar curso | COORDINATOR+ |
| GET | `/{id}/statistics` | Estadísticas del curso | TEACHER+ |
| GET | `/{id}/pdf-data` | Datos para generar PDF | TEACHER+ |

### Planificación Semanal (`/weekly-plannings`)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/{id}` | Obtener planificación por ID | ALL |
| GET | `/course/{courseId}` | Planificaciones de un curso | ALL |
| GET | `/course/{courseId}/week/{week}` | Planificación de semana específica | ALL |
| GET | `/course/{courseId}/date` | Planificación en fecha (query param) | ALL |
| POST | `/` | Crear planificación | TEACHER+ (owner) |
| PUT | `/{id}` | Actualizar planificación | TEACHER+ (owner) |
| DELETE | `/{id}` | Eliminar planificación | TEACHER+ (owner) |

### Contenido Programático (`/programmatic-contents`)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/course/{courseId}` | Contenido de un curso | ALL |
| POST | `/` | Crear contenido | TEACHER+ (owner) |
| PUT | `/{id}` | Actualizar contenido | TEACHER+ (owner) |
| DELETE | `/{id}` | Eliminar contenido | TEACHER+ (owner) |

### Actividades (`/activities`)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/{id}` | Obtener actividad | ALL |
| POST | `/` | Crear actividad | TEACHER+ (owner) |
| PUT | `/{id}` | Actualizar actividad | TEACHER+ (owner) |
| DELETE | `/{id}` | Eliminar actividad | TEACHER+ (owner) |

### AI Agent (`/ai`)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| POST | `/chat` | Enviar mensaje al agente | TEACHER+ |
| GET | `/suggestions/{courseId}` | Obtener sugerencias | TEACHER+ |
| POST | `/report/{courseId}` | Generar informe | TEACHER+ |
| DELETE | `/sessions/{sessionId}` | Limpiar sesión de chat | TEACHER+ |

### Enumeraciones (`/enums`)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/roles` | Lista de roles | ALL |
| GET | `/shifts` | Lista de turnos | ALL |
| GET | `/grading-systems` | Sistemas de calificación | ALL |
| GET | `/activity-types` | Tipos de actividad | ALL |
| GET | `/sdgs` | Objetivos Desarrollo Sostenible | ALL |
| GET | `/udl-principles` | Principios DUA | ALL |
| ... | ... | (20+ enums) | ALL |

### Campus (`/campuses`)

| Método | Endpoint | Descripción | Roles |
|--------|----------|-------------|-------|
| GET | `/` | Listar sedes | ALL |
| POST | `/` | Crear sede | ADMIN |
| PUT | `/{id}` | Actualizar sede | ADMIN |
| DELETE | `/{id}` | Eliminar sede | ADMIN |

**Ver documentación completa en Swagger:** http://localhost:8080/api/v1/swagger-ui.html

---

## Seguridad

El proyecto implementa seguridad de nivel empresarial cumpliendo con **OWASP Top 10 2021**.

### Características de Seguridad

#### Autenticación y Autorización

- **Autenticación Dual**: LOCAL (BCrypt) + LDAP (Active Directory)
- **JWT HS512**: Tokens firmados con clave de 512 bits
- **RBAC**: 5 roles con permisos granulares
- **@PreAuthorize**: Control de acceso a nivel de método (134 endpoints protegidos)
- **Password Hashing**: BCrypt con factor de trabajo 12

#### Roles del Sistema

```
ADMINISTRATOR (máximo privilegio)
    ↓
EDUCATION_MANAGER (gestor educativo)
    ↓
COORDINATOR (coordinador de carrera)
    ↓
ANALYST (analista académico)
    ↓
TEACHER (docente)
```

**Matriz de permisos:** Ver [docs/security/ROLES_AND_PERMISSIONS.md](./docs/security/ROLES_AND_PERMISSIONS.md)

#### Protecciones Implementadas

| Vulnerabilidad | Mitigación |
|----------------|------------|
| **SQL Injection** | JPA con named parameters, sin SQL dinámico |
| **XSS** | Content Security Policy, X-XSS-Protection headers |
| **CSRF** | SameSite cookies, JWT stateless |
| **Broken Authentication** | BCrypt factor 12, JWT expiración, rate limiting |
| **Sensitive Data Exposure** | Passwords hasheados, no logging de secretos |
| **Broken Access Control** | RBAC en 134 endpoints, validación de ownership |
| **Security Misconfiguration** | 7 headers de seguridad, HSTS, CSP |
| **LDAP Injection** | Sanitización de inputs, prepared statements |
| **Deserialization** | Jackson whitelist, validación de tipos |
| **Insufficient Logging** | Logs estructurados, auditoría en Modification |

#### Headers de Seguridad

```http
Strict-Transport-Security: max-age=31536000; includeSubDomains
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline'
Referrer-Policy: strict-origin-when-cross-origin
Permissions-Policy: geolocation=(), microphone=(), camera=()
```

### Testing de Seguridad

El proyecto incluye 89 tests que validan la seguridad:

```bash
# Ejecutar tests de seguridad
./gradlew test --tests "*Security*"
./gradlew test --tests "*Auth*"

# Ver reporte de cobertura
./gradlew jacocoTestReport
# Abrir: build/reports/jacoco/test/html/index.html
```

**Documentación completa:** [docs/security/SECURITY_OVERVIEW.md](./docs/security/SECURITY_OVERVIEW.md)

---

## Testing

### Suite de Tests

- **89 tests** en total
- **39 integration tests** (controllers con MockMvc)
- **50 unit tests** (services, utilities)
- **H2 in-memory** para tests (no requiere PostgreSQL)

### Ejecutar Tests

```bash
# Todos los tests
./gradlew test

# Con reporte de cobertura
./gradlew test jacocoTestReport

# Tests de un paquete específico
./gradlew test --tests "edu.utec.planificador.controller.*"
./gradlew test --tests "edu.utec.planificador.service.*"

# Tests de una clase
./gradlew test --tests "CourseControllerIntegrationTest"

# Con más detalle
./gradlew test --info
```

### Reportes

Después de ejecutar los tests, se generan reportes HTML:

- **Tests**: `build/reports/tests/test/index.html`
- **Cobertura**: `build/reports/jacoco/test/html/index.html`

### Cobertura de Código

| Capa | Cobertura | Objetivo |
|------|-----------|----------|
| Controllers | 85%+ | 80% |
| Services | 90%+ | 80% |
| Repositories | 100% | 90% |
| Utilities | 95%+ | 80% |
| **Global** | **89%** | **80%** |

### CI/CD

Los tests se ejecutan automáticamente en cada push y pull request mediante **GitHub Actions**.

**Ver workflow:** `.github/workflows/backend-ci.yml`

**Documentación completa:** [docs/test/TESTING.md](./docs/test/TESTING.md)

---

## Base de Datos

### PostgreSQL 16

El proyecto utiliza PostgreSQL como base de datos principal.

#### Modelo de Datos

**Entidades principales:**

- **User** (herencia JOINED con Teacher, Coordinator, Analyst, EducationManager, Administrator)
- **Course** (cursos académicos)
- **WeeklyPlanning** (planificación semanal)
- **Activity** (actividades de planificación)
- **ProgrammaticContent** (contenido programático)
- **CurricularUnit** (unidad curricular)
- **Term** (semestre)
- **Program** (carrera)
- **Campus** (sede)
- **RegionalTechnologicalInstitute** (ITR)
- **OfficeHours** (horarios de oficina)
- **Modification** (auditoría)
- **Position** (cargos docentes)

#### Schema Management

El proyecto usa **JPA DDL Auto** con estrategia `update`:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # Crea/actualiza schema automáticamente
```

Para migraciones manuales, ver `/scripts/migrations/`.

#### Data Seeder

En modo desarrollo (`dev` profile), se cargan datos iniciales:

- 1 Usuario docente
- 1 Programa académico
- 1 Unidad curricular
- 1 Curso de ejemplo
- Sedes de UTEC

**Documentación:** [docs/SEEDER-GUIDE.md](./docs/SEEDER-GUIDE.md)

#### Backups

```bash
# Backup automático (Docker)
docker exec utec-planificador-db pg_dump -U postgres planificador_db > backup.sql

# Restore
docker exec -i utec-planificador-db psql -U postgres planificador_db < backup.sql
```

#### Conexión Directa

```bash
# Conectar con psql (contenedor)
docker exec -it utec-planificador-db psql -U postgres -d planificador_db

# Conectar con cliente externo
psql -h localhost -p 5432 -U postgres -d planificador_db
```

**Credenciales por defecto:**

- Host: `localhost`
- Port: `5432`
- Database: `planificador_db`
- User: `postgres`
- Password: Ver `.env`

---

## Documentación

### Documentación Disponible

| Documento | Descripción |
|-----------|-------------|
| [SECURITY_OVERVIEW.md](./docs/security/SECURITY_OVERVIEW.md) | Visión completa de seguridad (OWASP, CWE, ISO 27001) |
| [AUTHENTICATION_IMPLEMENTATION.md](./docs/security/AUTHENTICATION_IMPLEMENTATION.md) | Implementación de autenticación LOCAL/LDAP |
| [ROLES_AND_PERMISSIONS.md](./docs/security/ROLES_AND_PERMISSIONS.md) | Matriz de roles y permisos |
| [ACCESS_CONTROL_IMPLEMENTATION.md](./docs/security/ACCESS_CONTROL_IMPLEMENTATION.md) | Control de acceso con @PreAuthorize |
| [SECURITY_TESTING.md](./docs/security/SECURITY_TESTING.md) | Tests de seguridad |
| [TESTING.md](./docs/test/TESTING.md) | Estrategia y guía de testing |
| [SEEDER-GUIDE.md](./docs/SEEDER-GUIDE.md) | Guía del DataSeeder |
| [UBUNTU-SERVER-DEPLOYMENT.md](./docs/UBUNTU-SERVER-DEPLOYMENT.md) | Deployment en Ubuntu Server |

### API Documentation

**Swagger UI (Interactivo):**

http://localhost:8080/api/v1/swagger-ui.html

**OpenAPI JSON:**

http://localhost:8080/api/v1/api-docs

### Javadoc

```bash
# Generar Javadoc
./gradlew javadoc

# Abrir: build/docs/javadoc/index.html
```

---

## Contribuir

### Guía de Contribución

1. **Fork** el repositorio
2. **Crea** una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. **Commit** tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. **Push** a la rama (`git push origin feature/AmazingFeature`)
5. **Abre** un Pull Request

### Convenciones de Código

- **Java Style Guide**: Google Java Style Guide
- **Naming**:
  - Controllers: `*Controller.java`
  - Services: `*Service.java` (interfaz), `*ServiceImpl.java` (impl)
  - Repositories: `*Repository.java`
  - DTOs: `*Request.java`, `*Response.java`
  - Entities: Sustantivos en singular (`User.java`, `Course.java`)
- **Testing**: Todo nuevo feature debe incluir tests
- **Documentation**: Swagger annotations en controllers

### Comandos de Verificación

```bash
# Ejecutar tests
./gradlew test

# Build completo
./gradlew clean build
```

---

## Licencia

Este proyecto es parte del Proyecto Final de la carrera **Analista en Tecnologías de la Información** de la **Universidad Tecnológica del Uruguay (UTEC)**.

**Desarrollado por:** Salvador Vanoli

**Institución:** Universidad Tecnológica del Uruguay (UTEC)

**Año:** 2025

---

## Soporte y Contacto

### Resolución de Problemas

La documentación completa se encuentra en el directorio `/docs` del proyecto. Para problemas específicos, se recomienda revisar:

- Documentación de seguridad en `/docs/security`
- Guías de testing en `/docs/test`
- Documentación de deployment en `/docs`

### Preguntas Frecuentes

**P: ¿Cómo cambio el puerto del backend?**

R: Edita `SERVER_PORT` en `.env` y reinicia con `.\scripts\stop.ps1` y `.\scripts\start.ps1`.

**P: ¿Cómo regenero el schema de la base de datos?**

R: Detén los servicios, elimina el volumen (`docker volume rm utec-planificador-postgres-data`) y reinicia.

**P: ¿Cómo conecto el backend con el agente de IA?**

R: Asegúrate de que `AI_AGENT_BASE_URL` en `.env` apunte correctamente al contenedor (ej: `http://host.docker.internal:8000`).

**P: ¿Cómo habilito LDAP?**

R: Configura las variables `LDAP_*` en `.env` y cambia `AUTH_DEFAULT_PROVIDER=LDAP`.

---

<div align="center">

**Si este proyecto te fue útil, considera darle una estrella**

Desarrollado en UTEC

[Volver arriba](#utec-planificador---backend)

</div>

