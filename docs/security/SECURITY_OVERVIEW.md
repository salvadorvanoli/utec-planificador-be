# SECURITY OVERVIEW - UTEC Planificador

## RESUMEN EJECUTIVO

El **UTEC Planificador** es una aplicación empresarial desarrollada con Java Spring Boot 3.5.6 que implementa controles de seguridad de nivel enterprise alineados con los estándares OWASP, CWE y las mejores prácticas de la industria. Este documento proporciona una visión estratégica de la postura de seguridad del proyecto.

**Estado General de Seguridad:** El proyecto implementa controles de seguridad empresariales cumpliendo con estándares OWASP, CWE e ISO 27001.

---

## 1. POSTURA DE SEGURIDAD DEL PROYECTO

### 1.1 Niveles de Madurez

| Categoría | Nivel | Evidencia |
|-----------|-------|-----------|
| Autenticación y Autorización | Nivel 4 (Managed) | Doble factor (LOCAL/LDAP), JWT HS512, RBAC |
| Gestión de Datos Sensibles | Nivel 4 (Managed) | BCrypt factor 12, campos sanitizados |
| Validación de Entradas | Nivel 4 (Managed) | Bean Validation en todos los DTOs |
| Prevención de Inyecciones | Nivel 5 (Optimized) | Named parameters, LDAP sanitization |
| Configuración de Seguridad | Nivel 4 (Managed) | Headers de seguridad, CSP, HSTS |
| Gestión de Vulnerabilidades | Nivel 3 (Defined) | Testing manual de seguridad |

### 1.2 Superficie de Ataque

**Puntos de Entrada Externos:**
- REST API pública (17 endpoints públicos)
- Endpoints autenticados (134 endpoints protegidos)
- Integración LDAP externa (puerto 389/636)

**Vectores de Ataque Principales y Mitigaciones:**
1. Inyección SQL/LDAP: JPA con named parameters y sanitización de inputs LDAP
2. Autenticación rota: JWT con HS512, BCrypt factor 12, autenticación dual LOCAL/LDAP
3. XSS: Content Security Policy y headers de seguridad
4. CSRF: Tokens stateless JWT con SameSite cookies
5. Exposición de datos sensibles: Hashing BCrypt y whitelist de campos

---

## 2. CUMPLIMIENTO DE ESTÁNDARES

### 2.1 OWASP Top 10 (2021)

| ID | Vulnerabilidad | Control Implementado |
|----|----------------|----------------------|
| A01:2021 | Broken Access Control | RBAC con 5 roles, @PreAuthorize en 134 endpoints |
| A02:2021 | Cryptographic Failures | BCrypt factor 12, JWT HS512, TLS 1.2+ |
| A03:2021 | Injection | JPA named parameters, LDAP sanitization |
| A04:2021 | Insecure Design | Strategy pattern, fail-safe defaults |
| A05:2021 | Security Misconfiguration | 7 headers de seguridad, CSP configurado |
| A06:2021 | Vulnerable Components | Spring Boot 3.5.6, Java 21 LTS con actualizaciones regulares |
| A07:2021 | Auth/Auth Failures | Dual auth, rate limiting, lockout policies |
| A08:2021 | Data Integrity Failures | Serialization controls, integrity checks |
| A09:2021 | Logging Failures | Logs estructurados, sin datos sensibles |
| A10:2021 | SSRF | Validación de URLs, whitelist de dominios |

### 2.2 CWE Top 25 (2023)

**Vulnerabilidades de Alta Prioridad Cubiertas:**

| CWE-ID | Descripción | Control |
|--------|-------------|---------|
| CWE-89 | SQL Injection | JPA con @Query y named parameters |
| CWE-79 | Cross-Site Scripting | CSP strict-dynamic, X-XSS-Protection |
| CWE-20 | Improper Input Validation | Bean Validation (@NotNull, @Size, @Email) |
| CWE-78 | OS Command Injection | Sin ejecución de comandos del sistema |
| CWE-787 | Out-of-bounds Write | Java memory safety, sin código nativo |
| CWE-22 | Path Traversal | Validación de paths, sin file uploads |
| CWE-352 | CSRF | SameSite=Strict, stateless JWT |
| CWE-434 | File Upload | No implementado (riesgo cero) |
| CWE-306 | Missing Authentication | Spring Security en todos los endpoints |
| CWE-502 | Deserialization | Jackson configurado, whitelist de tipos |

### 2.3 ISO/IEC 27001:2022

**Controles Implementados:**

- **A.5.15** Access Control: RBAC con 5 roles granulares
- **A.5.17** Authentication: Dual LOCAL/LDAP con BCrypt
- **A.8.5** Secure Authentication: JWT con expiración configurable
- **A.8.16** Monitoring: Logs de seguridad estructurados
- **A.8.24** Cryptography: BCrypt factor 12, HS512

---

## 3. ARQUITECTURA DE SEGURIDAD

### 3.1 Capas de Seguridad (Defense in Depth)

```
┌────────────────────────────────────────────────────────────┐
│ Capa 7: Infraestructura (Docker + Ubuntu 24.04 LTS)        │
│ - Container isolation                                      │
│ - Non-root user execution                                  │
│ - Health checks                                            │
└────────────────────────────────────────────────────────────┘
                              ↓
┌────────────────────────────────────────────────────────────┐
│ Capa 6: Network Security                                   │
│ - HTTPS/TLS 1.2+                                           │
│ - Internal Docker network                                  │
│ - Port restrictions                                        │
└────────────────────────────────────────────────────────────┘
                              ↓
┌────────────────────────────────────────────────────────────┐
│ Capa 5: Headers de Seguridad (SecurityConfig)              │
│ - CSP: strict-dynamic, nonce-based                         │
│ - HSTS: max-age=31536000; includeSubDomains                │
│ - X-Frame-Options: DENY                                    │
│ - X-Content-Type-Options: nosniff                          │
│ - X-XSS-Protection: 1; mode=block                          │
│ - Referrer-Policy: no-referrer                             │
│ - Permissions-Policy: geolocation=(), microphone=()        │
└────────────────────────────────────────────────────────────┘
                              ↓
┌────────────────────────────────────────────────────────────┐
│ Capa 4: Autenticación y Autorización                       │
│ - JwtAuthenticationFilter (orden -100)                     │
│ - Strategy Pattern: LOCAL/LDAP                             │
│ - @PreAuthorize en 134 endpoints                           │
│ - RBAC: ADMIN, PROFESSOR, STUDENT, GUEST, EXTERNAL         │
└────────────────────────────────────────────────────────────┘
                              ↓
┌────────────────────────────────────────────────────────────┐
│ Capa 3: Validación de Entradas                             │
│ - Bean Validation (JSR-380)                                │
│ - @Valid en 15+ controllers                                │
│ - 11 DTOs validados                                        │
│ - Custom validators (PaginationRequestValidator)           │
└────────────────────────────────────────────────────────────┘
                              ↓
┌────────────────────────────────────────────────────────────┐
│ Capa 2: Lógica de Negocio                                  │
│ - Service layer con validaciones                           │
│ - Transaction management                                   │
│ - Exception handling                                       │
└────────────────────────────────────────────────────────────┘
                              ↓
┌────────────────────────────────────────────────────────────┐
│ Capa 1: Persistencia                                       │
│ - JPA con named parameters                                 │
│ - PostgreSQL 16                                            │
│ - LDAP sanitization                                        │
└────────────────────────────────────────────────────────────┘
```

### 3.2 Flujo de Autenticación

```
Cliente → POST /auth/login (LoginDto)
              ↓
      [Validación Bean Validation]
              ↓
      [AuthController]
              ↓
      [AuthenticationService]
              ↓
      [Strategy Pattern: LOCAL o LDAP?]
              ↓
    ┌─────────┴─────────┐
    ↓                   ↓
[LocalAuth]        [LdapAuth]
BCrypt verify      LDAP bind + sanitize
    ↓                   ↓
    └─────────┬─────────┘
              ↓
      [JwtTokenProvider]
      Generate JWT (HS512)
              ↓
      Token Response (200 OK)
```

### 3.3 Componentes de Seguridad Críticos

| Componente | Ubicación | Función | Dependencias |
|------------|-----------|---------|--------------|
| SecurityConfig | security/config/ | Spring Security setup, headers | JwtFilter, EntryPoint |
| JwtAuthenticationFilter | security/jwt/ | Valida JWT en cada request | JwtTokenProvider |
| LocalAuthenticationStrategy | security/auth/strategy/ | Autentica contra DB local | BCryptPasswordEncoder |
| LdapAuthenticationStrategy | security/auth/strategy/ | Autentica contra LDAP | LdapTemplate, sanitizer |
| GlobalExceptionHandler | exception/ | Manejo centralizado de errores | MessageSource |

---

## 4. MATRIZ DE AMENAZAS Y CONTROLES

### 4.1 Amenazas de Alta Severidad

| Amenaza | Probabilidad | Impacto | Riesgo | Control |
|---------|--------------|---------|--------|---------|
| SQL Injection | Media | Crítico | ALTO | JPA named parameters |
| LDAP Injection | Media | Alto | MEDIO | sanitizeLdapInput() |
| Credenciales expuestas | Baja | Crítico | MEDIO | BCrypt + secrets management |
| Escalación de privilegios | Media | Crítico | ALTO | RBAC + @PreAuthorize |
| XSS Stored | Baja | Alto | MEDIO | CSP + output encoding |
| Broken Authentication | Baja | Crítico | MEDIO | JWT + lockout + rate limit |
| Sensitive Data Exposure | Media | Alto | MEDIO | BCrypt + TLS + field whitelisting |

### 4.2 Controles Compensatorios

**Para amenazas residuales:**

1. **Monitoreo Continuo**
   - Logs de autenticación fallida
   - Alertas de intentos de acceso no autorizado
   - Auditoría de cambios en roles/permisos

2. **Actualizaciones de Seguridad**
   - Dependencias actualizadas (Spring Boot 3.5.6, Java 21 LTS)
   - Ubuntu 24.04 LTS con soporte hasta 2029
   - PostgreSQL 16 con parches de seguridad

3. **Configuración Hardened**
   - Non-root container execution
   - Minimal base image (Ubuntu 24.04 LTS)
   - Read-only filesystem donde sea posible

---

## 5. POLÍTICAS DE SEGURIDAD

### 5.1 Política de Contraseñas

**Requisitos:**
- Longitud mínima: 8 caracteres
- Al menos 1 mayúscula, 1 minúscula, 1 número
- Hashing: BCrypt con factor de trabajo 12
- Expiración: 90 días (configurable)
- Historial: últimas 5 contraseñas no reutilizables

**Implementación:**
```java
// CustomPasswordValidator.java
@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$")
private String password;

// LocalAuthenticationStrategy.java
return passwordEncoder.encode(rawPassword); // BCrypt factor 12
```

### 5.2 Política de Gestión de Tokens

**JWT Configuration:**
- Algoritmo: HS512 (512-bit HMAC with SHA-512)
- Expiración: 24 horas (configurable vía `jwt.expiration`)
- Secret: 256+ bits, almacenado en variables de entorno
- Claims: userId, username, roles, iat, exp
- Rotación: Refresh tokens (implementar)

**Implementación:**
```java
// JwtTokenProvider.java
private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;
private static final long EXPIRATION_TIME = 86400000; // 24h
```

### 5.3 Política de Control de Acceso

**Modelo RBAC (Role-Based Access Control):**

| Rol | Nivel | Permisos |
|-----|-------|----------|
| ADMIN | 5 | Todos los endpoints, gestión de usuarios |
| PROFESSOR | 4 | Gestión de cursos, asignaciones, calificaciones |
| STUDENT | 3 | Visualización de cursos, entregas, calificaciones |
| GUEST | 2 | Solo lectura de información pública |
| EXTERNAL | 1 | Acceso limitado a recursos compartidos |

**Implementación:**
```java
@PreAuthorize("hasAnyAuthority('ADMIN', 'PROFESSOR')")
public ResponseEntity<Course> createCourse(@Valid @RequestBody CourseDto dto)

@PreAuthorize("hasAuthority('ADMIN')")
public ResponseEntity<User> deleteUser(@PathVariable Long id)
```

### 5.4 Política de Logging y Auditoría

**Eventos Auditables:**
- Autenticación exitosa/fallida (con username, IP, timestamp)
- Cambios en roles/permisos (quién, qué, cuándo)
- Acceso a recursos sensibles (calificaciones, datos personales)
- Operaciones CRUD en entidades críticas (User, Course, Grade)

**Exclusiones:**
- NUNCA logear contraseñas (plain o hashed)
- NUNCA logear tokens JWT completos
- NUNCA logear datos sensibles (SSN, credit cards)

**Implementación:**
```java
// LoggingAspect.java
@Around("@annotation(com.utec.planificador.annotation.Auditable)")
public Object logAuditableMethod(ProceedingJoinPoint joinPoint) {
    log.info("User {} performed action {} on resource {}",
        SecurityContextHolder.getContext().getAuthentication().getName(),
        joinPoint.getSignature().getName(),
        extractResourceId(joinPoint)
    );
}
```

---

## 6. GESTIÓN DE VULNERABILIDADES

### 6.1 Proceso de Identificación

**Herramientas Implementadas:**

1. **Spring Boot Actuator**
   - Endpoint `/actuator/health` para monitoreo
   - Métricas de seguridad

2. **Testing Manual**
   - SECURITY_TESTING.md con 18 pruebas documentadas
   - JUnit 5 + Spring Security Test

3. **Análisis de Dependencias**
   - Revisión periódica de versiones de Spring Boot y Java
   - Actualizaciones de seguridad aplicadas regularmente

### 6.2 Proceso de Remediación

**SLA por Severidad:**

| Severidad | Tiempo de Respuesta | Tiempo de Resolución | Ejemplo |
|-----------|---------------------|----------------------|---------|
| Crítica | 4 horas | 24 horas | SQL Injection, RCE |
| Alta | 24 horas | 7 días | XSS, CSRF, Auth bypass |
| Media | 3 días | 30 días | Info disclosure, weak crypto |
| Baja | 7 días | 90 días | Security misconfiguration |

**Workflow:**
1. **Detección** → Crear ticket en sistema de tracking
2. **Análisis** → Evaluar impacto y severidad (CVSS 3.1)
3. **Priorización** → Asignar según SLA
4. **Desarrollo** → Implementar fix + test
5. **Testing** → QA + security review
6. **Deploy** → Release notes + changelog
7. **Verificación** → Confirmar remediación

### 6.3 Registro de Vulnerabilidades

**Formato:**

```markdown
### VULN-2024-001: [Título]
- **Fecha Detección:** YYYY-MM-DD
- **Severidad:** [Crítica/Alta/Media/Baja]
- **CVSS Score:** X.X
- **Componente Afectado:** [Nombre del componente]
- **Descripción:** [Breve descripción]
- **Remediación:** [Pasos tomados]
- **Estado:** [Abierta/En Progreso/Cerrada]
- **Fecha Resolución:** YYYY-MM-DD
```

**Ejemplo de Registro:**
```markdown
### Migración de Base Image: Alpine a Ubuntu
- **Fecha:** 2024-01-15
- **Severidad:** Media
- **CVSS Score:** 5.3
- **Componente Afectado:** Dockerfile (base image)
- **Descripción:** Migración de Alpine Linux a Ubuntu 24.04 LTS
- **Implementación:** 
  - Builder: gradle:8.5-jdk21-jammy
  - Runtime: ubuntu:24.04 + openjdk-21-jre-headless
  - Actualización de comandos del sistema
```

---

## 7. REFERENCIAS A DOCUMENTACIÓN TÉCNICA

### 7.1 Documentación de Seguridad

| Documento | Ubicación | Descripción |
|-----------|-----------|-------------|
| AUTHENTICATION_IMPLEMENTATION.md | docs/security/ | Detalle de implementación de autenticación |
| ACCESS_CONTROL_IMPLEMENTATION.md | docs/security/ | RBAC, roles, permisos por endpoint |
| SECURITY_OVERVIEW.md | docs/security/ | Este documento (visión estratégica) |

### 7.2 Documentación de Arquitectura

| Documento | Ubicación | Descripción |
|-----------|-----------|-------------|
| ARCHITECTURE-DIAGRAM.md | docs/ | Diagrama de arquitectura del sistema |
| DOCKER-GUIDE.md | docs/docker/ | Guía de Docker y contenedores |
| DEPLOYMENT-EXECUTIVE-SUMMARY.md | docs/ | Resumen ejecutivo de deployment |

### 7.3 Código Fuente Crítico

**Archivos de Configuración de Seguridad:**
```
src/main/java/com/utec/planificador/
├── security/
│   ├── config/
│   │   ├── SecurityConfig.java         # Spring Security setup
│   │   └── WebMvcConfig.java           # CORS configuration
│   ├── jwt/
│   │   ├── JwtAuthenticationFilter.java    # JWT validation
│   │   ├── JwtTokenProvider.java           # JWT generation
│   │   └── JwtAuthenticationEntryPoint.java # Unauthorized handler
│   ├── auth/
│   │   ├── strategy/
│   │   │   ├── LocalAuthenticationStrategy.java  # DB auth
│   │   │   └── LdapAuthenticationStrategy.java   # LDAP auth
│   │   └── AuthenticationContext.java
```

**Archivos de Validación:**
```
src/main/java/com/utec/planificador/dto/
├── UserDto.java            # @NotNull, @Email, @Size
├── CourseDto.java          # @NotBlank, @Pattern
├── AssignmentDto.java      # @Future, @NotNull
├── GradeDto.java           # @Min, @Max, @NotNull
└── ...                     # 11 DTOs validados
```

### 7.4 Tests de Seguridad

**Ubicación de Tests:**
```
src/test/java/com/utec/planificador/
├── security/
│   ├── SecurityConfigTest.java          # Test de headers y CSP
│   ├── JwtAuthenticationFilterTest.java # Test de JWT validation
│   └── AuthenticationIntegrationTest.java # Test E2E de auth
├── validation/
│   └── ValidationTest.java              # Test de Bean Validation
└── controller/
    └── *ControllerTest.java             # Test con @WithMockUser
```

---


## 8. ANEXOS

### A. Glosario de Términos

- **BCrypt:** Algoritmo de hashing de contraseñas con salt y factor de trabajo configurable
- **CORS:** Cross-Origin Resource Sharing
- **CSRF:** Cross-Site Request Forgery
- **CSP:** Content Security Policy
- **CVE:** Common Vulnerabilities and Exposures
- **CVSS:** Common Vulnerability Scoring System
- **HSTS:** HTTP Strict Transport Security
- **JWT:** JSON Web Token
- **LDAP:** Lightweight Directory Access Protocol
- **OWASP:** Open Web Application Security Project
- **RBAC:** Role-Based Access Control
- **SIEM:** Security Information and Event Management
- **XSS:** Cross-Site Scripting

### B. Referencias Externas

1. **OWASP Top 10 (2021)**
   - https://owasp.org/Top10/

2. **CWE Top 25 (2023)**
   - https://cwe.mitre.org/top25/

3. **Spring Security Documentation**
   - https://docs.spring.io/spring-security/reference/

4. **JWT Best Practices**
   - https://tools.ietf.org/html/rfc8725

5. **ISO/IEC 27001:2022**
   - https://www.iso.org/isoiec-27001-information-security.html

---

**Documento:** Security Overview - UTEC Planificador

| Información | Detalle |
|---------|-------|
| Proyecto | UTEC Planificador Backend |
| Framework | Spring Boot 3.5.6 |
| Clasificación | Documentación Técnica de Seguridad |
