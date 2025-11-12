# Implementación de Autenticación JWT y LDAP

## Tabla de Contenidos

1. [Visión General](#visión-general)
2. [Arquitectura](#arquitectura)
3. [Componentes Principales](#componentes-principales)
4. [Flujos de Autenticación](#flujos-de-autenticación)
5. [Seguridad y Validaciones](#seguridad-y-validaciones)
6. [Configuración](#configuración)
7. [Endpoints](#endpoints)
8. [Manejo de Errores](#manejo-de-errores)

---

## Visión General

El sistema implementa un mecanismo de autenticación dual que soporta dos proveedores:

- **LOCAL**: Autenticación tradicional con credenciales almacenadas en base de datos (BCrypt)
- **LDAP**: Autenticación contra servidor LDAP institucional de UTEC

La autenticación utiliza **JSON Web Tokens (JWT)** como mecanismo de sesión stateless, permitiendo escalabilidad horizontal y desacoplamiento entre frontend y backend.

### Características Principales

- Autenticación basada en JWT (stateless)
- Soporte para múltiples proveedores de autenticación (Strategy Pattern)
- Validación de proveedor de autenticación (defense-in-depth)
- Internacionalización de mensajes de error (i18n)
- Rate limiting y protección contra fuerza bruta
- Logging exhaustivo para auditoría
- Manejo de errores consistente y seguro

---

## Arquitectura

### Patrón Strategy para Autenticación

El sistema utiliza el patrón Strategy para permitir múltiples métodos de autenticación sin acoplamiento:

```
AuthenticationService
        |
        |--- selecciona estrategia
        |
        v
AuthenticationStrategy (interfaz)
        |
        |--- implements
        |
        +--- LocalAuthenticationStrategy (BCrypt)
        |
        +--- LdapAuthenticationStrategy (LDAP Server)
```

### Flujo General

```
Cliente
   |
   | POST /auth/login
   |
   v
AuthController
   |
   | LoginRequest
   |
   v
AuthenticationService
   |
   | selecciona estrategia según AuthProvider
   |
   v
AuthenticationStrategy
   |
   | autentica y retorna User
   |
   v
JwtTokenProvider
   |
   | genera JWT token
   |
   v
AuthResponse (con token)
```

---

## Componentes Principales

### 1. AuthenticationService

**Ubicación**: `service/AuthenticationService.java`

Servicio principal que coordina el proceso de autenticación.

**Responsabilidades**:
- Seleccionar la estrategia de autenticación apropiada
- Generar tokens JWT
- Validar intentos de login (rate limiting)
- Obtener información del usuario actual

**Métodos principales**:

```java
AuthResponse login(LoginRequest request)
UserResponse getCurrentUser()
void logout()
```

### 2. AuthenticationStrategy

**Ubicación**: `service/AuthenticationStrategy.java`

Interfaz que define el contrato para las estrategias de autenticación.

**Métodos**:

```java
User authenticate(String email, String password)
boolean supports(String providerName)
```

### 3. LocalAuthenticationStrategy

**Ubicación**: `service/impl/LocalAuthenticationStrategy.java`

Implementación para autenticación con credenciales locales.

**Flujo de validación**:

1. **Buscar usuario por email**
   - Si no existe: `InvalidCredentialsException` (mensaje genérico por seguridad)

2. **Validar AuthProvider**
   - Verifica que el usuario esté configurado para LOCAL
   - Si no: `InvalidCredentialsException` con mensaje específico del provider correcto

3. **Validar cuenta habilitada**
   - Verifica que `user.isEnabled() == true`
   - Si no: `InvalidCredentialsException` con mensaje de cuenta deshabilitada

4. **Verificar contraseña**
   - Usa `PasswordEncoder.matches()` para comparar con hash BCrypt
   - Si no coincide: `InvalidCredentialsException` (mensaje genérico por seguridad)

**Código ejemplo**:

```java
@Override
public User authenticate(String email, String password) {
    log.debug("Attempting LOCAL authentication for user: {}", email);

    // Step 1: Find user (fail-fast if not found)
    User user = userRepository.findByUtecEmail(email)
        .orElseThrow(() -> {
            log.warn("LOCAL authentication failed for {}: user not found", email);
            return new InvalidCredentialsException(
                messageSource.getMessage("auth.error.invalid-credentials", 
                    null, LocaleContextHolder.getLocale())
            );
        });

    // Step 2: Validate auth provider (defense-in-depth)
    if (user.getAuthProvider() != AuthProvider.LOCAL) {
        log.warn("LOCAL authentication failed for {}: incorrect provider", email);
        throw new InvalidCredentialsException(
            messageSource.getMessage("auth.error.incorrect-auth-provider",
                new Object[]{user.getAuthProvider().getDisplayName()},
                LocaleContextHolder.getLocale())
        );
    }

    // Step 3: Validate account is enabled
    if (!user.isEnabled()) {
        log.warn("LOCAL authentication failed for {}: account disabled", email);
        throw new InvalidCredentialsException(
            messageSource.getMessage("auth.error.account-disabled", 
                null, LocaleContextHolder.getLocale())
        );
    }

    // Step 4: Verify password
    if (!passwordEncoder.matches(password, user.getPassword())) {
        log.warn("LOCAL authentication failed for {}: invalid password", email);
        throw new InvalidCredentialsException(
            messageSource.getMessage("auth.error.invalid-credentials", 
                null, LocaleContextHolder.getLocale())
        );
    }

    log.info("User authenticated successfully via LOCAL: {}", email);
    return user;
}
```

### 4. LdapAuthenticationStrategy

**Ubicación**: `service/impl/LdapAuthenticationStrategy.java`

Implementación para autenticación contra servidor LDAP.

**Flujo de validación**:

1. **Verificar configuración LDAP**
   - Valida que `LdapTemplate` esté disponible
   - Si no: `InvalidCredentialsException` con mensaje de LDAP no habilitado

2. **Autenticar con LDAP**
   - Sanitiza el email para prevenir LDAP injection
   - Extrae username del email (parte antes de @)
   - Construye filtro LDAP: `uid={username}`
   - Ejecuta autenticación con `ldapTemplate.authenticate()`
   - Si falla: `InvalidCredentialsException` (mensaje genérico por seguridad)

3. **Buscar usuario en BD local**
   - LDAP solo valida credenciales, pero necesitamos datos locales para RBAC
   - Si no existe: `InvalidCredentialsException` (mensaje genérico por seguridad)

4. **Validar AuthProvider**
   - Verifica que el usuario esté configurado para LDAP
   - Si no: `InvalidCredentialsException` con mensaje específico del provider correcto

5. **Validar cuenta habilitada**
   - Verifica que `user.isEnabled() == true`
   - Si no: `InvalidCredentialsException` con mensaje de cuenta deshabilitada

**Código ejemplo**:

```java
@Override
public User authenticate(String email, String password) {
    log.debug("Attempting LDAP authentication for user: {}", email);

    // Step 1: Verify LDAP is configured
    if (ldapTemplate == null) {
        log.error("LDAP template is not configured");
        throw new InvalidCredentialsException(
            messageSource.getMessage("auth.error.ldap-not-enabled",
                null, LocaleContextHolder.getLocale())
        );
    }

    try {
        // Step 2: Authenticate with LDAP server
        boolean authenticated = authenticateWithLdap(email, password);

        if (!authenticated) {
            log.warn("LDAP authentication failed for {}: invalid LDAP credentials", email);
            throw new InvalidCredentialsException(
                messageSource.getMessage("auth.error.invalid-credentials",
                    null, LocaleContextHolder.getLocale())
            );
        }

        // Step 3: Find user in local database (required for authorization)
        User user = findAndValidateLdapUser(email);

        // Step 4: Validate account is enabled
        if (!user.isEnabled()) {
            log.warn("LDAP authentication failed for {}: account disabled", email);
            throw new InvalidCredentialsException(
                messageSource.getMessage("auth.error.account-disabled",
                    null, LocaleContextHolder.getLocale())
            );
        }

        log.info("User authenticated successfully via LDAP: {}", email);
        return user;

    } catch (InvalidCredentialsException e) {
        throw e;
    } catch (Exception e) {
        log.error("LDAP authentication error for user {}: {}", email, e.getMessage());
        throw new InvalidCredentialsException(
            messageSource.getMessage("auth.error.ldap-error",
                new Object[]{e.getMessage()},
                LocaleContextHolder.getLocale()), 
            e
        );
    }
}
```

**Sanitización LDAP** (prevención de LDAP injection):

```java
private String sanitizeLdapInput(String input) {
    if (input == null) {
        return "";
    }
    
    return input
        .replace("\\", "\\\\")  // Must be first
        .replace("*", "\\*")
        .replace("(", "\\(")
        .replace(")", "\\)")
        .replace("\0", "\\00")
        .replace("/", "\\/");
}
```

### 5. JwtTokenProvider

**Ubicación**: `security/JwtTokenProvider.java`

Componente responsable de la generación y validación de tokens JWT.

**Responsabilidades**:
- Generar tokens JWT con claims personalizados
- Validar tokens
- Extraer información del token (email, roles, etc.)
- Manejar expiración y refresh

**Configuración del token**:

```properties
# application.yml
security:
  jwt:
    secret: ${JWT_SECRET:your-secret-key-here}
    expiration: 86400000  # 24 horas en milisegundos
```

**Claims incluidos en el token**:
- `sub`: Email del usuario (subject)
- `roles`: Lista de roles del usuario
- `iat`: Timestamp de emisión
- `exp`: Timestamp de expiración

### 6. JwtAuthenticationFilter

**Ubicación**: `security/JwtAuthenticationFilter.java`

Filtro de Spring Security que intercepta todas las peticiones y valida el token JWT.

**Flujo**:

1. Extrae token del header `Authorization: Bearer {token}`
2. Valida el token usando `JwtTokenProvider`
3. Si es válido, carga los detalles del usuario
4. Establece el contexto de seguridad (`SecurityContextHolder`)
5. Continúa con la cadena de filtros

### 7. SecurityConfig

**Ubicación**: `security/SecurityConfig.java`

Configuración central de Spring Security.

**Endpoints públicos** (sin autenticación):
- `POST /auth/login`
- `POST /auth/logout`

**Endpoints protegidos**:
- `GET /auth/me` - Requiere autenticación
- `GET /auth/status` - Requiere autenticación
- Todos los demás endpoints - Requiere autenticación

**Configuración CORS**:
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    // ...
}
```

---

## Flujos de Autenticación

### Flujo 1: Login con Autenticación LOCAL

```
1. Cliente envía POST /auth/login
   {
     "email": "usuario@utec.edu.uy",
     "password": "password123",
     "authProvider": "LOCAL"
   }

2. AuthController recibe LoginRequest

3. AuthenticationService.login():
   - Valida rate limiting (máximo intentos por IP/usuario)
   - Selecciona LocalAuthenticationStrategy

4. LocalAuthenticationStrategy.authenticate():
   - Busca usuario en BD
   - Valida que authProvider sea LOCAL (defense-in-depth)
   - Valida que cuenta esté habilitada
   - Verifica contraseña con BCrypt

5. Si autenticación exitosa:
   - JwtTokenProvider genera token JWT
   - Se retorna AuthResponse con token
   
6. Cliente recibe respuesta:
   {
     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "type": "Bearer",
     "email": "usuario@utec.edu.uy",
     "roles": ["TEACHER"]
   }

7. Cliente almacena token (localStorage/sessionStorage)

8. En peticiones subsecuentes:
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Flujo 2: Login con Autenticación LDAP

```
1. Cliente envía POST /auth/login
   {
     "email": "usuario@utec.edu.uy",
     "password": "ldapPassword",
     "authProvider": "LDAP"
   }

2. AuthController recibe LoginRequest

3. AuthenticationService.login():
   - Valida rate limiting
   - Selecciona LdapAuthenticationStrategy

4. LdapAuthenticationStrategy.authenticate():
   - Verifica que LDAP esté configurado
   - Sanitiza input para prevenir LDAP injection
   - Autentica contra servidor LDAP de UTEC
   - Si LDAP exitoso:
     * Busca usuario en BD local (necesario para RBAC)
     * Valida que authProvider sea LDAP (defense-in-depth)
     * Valida que cuenta esté habilitada

5. Si autenticación exitosa:
   - JwtTokenProvider genera token JWT
   - Se retorna AuthResponse con token

6. Cliente procede igual que en flujo LOCAL
```

### Flujo 3: Validación de Token en Peticiones Subsecuentes

```
1. Cliente envía petición con token:
   GET /api/courses
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

2. JwtAuthenticationFilter intercepta:
   - Extrae token del header
   - Valida firma del token
   - Verifica que no haya expirado
   - Extrae email del token

3. CustomUserDetailsService:
   - Carga detalles completos del usuario desde BD
   - Incluye roles y permisos

4. SecurityContextHolder:
   - Establece Authentication en contexto
   - Permite acceso a @PreAuthorize y validaciones de roles

5. Continúa con el controlador solicitado

6. Si token inválido/expirado:
   - JwtAuthenticationEntryPoint maneja el error
   - Retorna 401 Unauthorized
```

---

## Seguridad y Validaciones

### Defense-in-Depth

El sistema implementa múltiples capas de validación:

**Capa 1 - LoginRequest (DTO)**:
```java
@NotBlank(message = "{validation.email.required}")
@Email(message = "{validation.email.format}")
@Size(max = 100, message = "{validation.email.size}")
private String email;

@NotBlank(message = "{validation.password.required}")
@Size(min = 8, max = 128, message = "{validation.password.size}")
private String password;

@NotNull
private AuthProvider authProvider;
```

**Capa 2 - Validación en AuthenticationService**:
- Rate limiting por IP y usuario
- Registro de intentos fallidos
- Bloqueo temporal tras múltiples fallos

**Capa 3 - Validación en Strategy**:
- Verificación de que el usuario existe
- Validación del AuthProvider configurado
- Verificación de cuenta habilitada
- Validación de credenciales

**Capa 4 - Spring Security**:
- Validación de token JWT
- Verificación de roles y permisos
- CORS configuration
- CSRF protection (deshabilitado por ser API stateless)

### Mensajes de Error Seguros

Los mensajes de error están diseñados para no revelar información sensible:

**MAL** (revela si usuario existe):
```
"Usuario no encontrado"
"Contraseña incorrecta"
```

**BIEN** (mensaje genérico):
```
"Credenciales inválidas"
```

**Excepción**: Mensajes específicos cuando ayudan al usuario sin comprometer seguridad:
```
"Este usuario debe autenticarse usando: LDAP"
"La cuenta de usuario está deshabilitada"
```

### Prevención de LDAP Injection

La estrategia LDAP sanitiza todos los inputs:

```java
private String sanitizeLdapInput(String input) {
    // Escapa caracteres especiales de LDAP
    return input
        .replace("\\", "\\\\")  // Debe ser primero
        .replace("*", "\\*")    // Wildcard
        .replace("(", "\\(")    // Paréntesis de filtros
        .replace(")", "\\)")
        .replace("\0", "\\00")  // Null byte
        .replace("/", "\\/");   // Separador de ruta
}
```

### Rate Limiting

Protección contra ataques de fuerza bruta:

```java
// En AuthenticationServiceImpl
private final Map<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();

private static final int MAX_ATTEMPTS = 5;
private static final long LOCK_TIME_MS = 15 * 60 * 1000; // 15 minutos

private void checkRateLimiting(String identifier) {
    LoginAttempt attempt = loginAttempts.computeIfAbsent(
        identifier, 
        k -> new LoginAttempt()
    );
    
    if (attempt.isLocked()) {
        throw new InvalidCredentialsException(
            messageSource.getMessage("auth.error.account-locked",
                new Object[]{attempt.getRemainingLockTimeMinutes()},
                LocaleContextHolder.getLocale())
        );
    }
    
    if (attempt.getAttempts() >= MAX_ATTEMPTS) {
        attempt.lock();
        throw new InvalidCredentialsException(
            messageSource.getMessage("auth.error.too-many-attempts",
                new Object[]{15}, 
                LocaleContextHolder.getLocale())
        );
    }
}
```

---

## Configuración

### Variables de Entorno

```properties
# JWT Configuration
JWT_SECRET=your-super-secret-key-change-in-production
JWT_EXPIRATION=86400000

# LDAP Configuration (opcional)
LDAP_ENABLED=true
LDAP_URL=ldap://ldap.utec.edu.uy:389
LDAP_BASE_DN=dc=utec,dc=edu,dc=uy
LDAP_USERNAME=cn=admin,dc=utec,dc=edu,dc=uy
LDAP_PASSWORD=admin-password
```

### application.yml

```yaml
security:
  jwt:
    secret: ${JWT_SECRET}
    expiration: ${JWT_EXPIRATION:86400000}
  
  ldap:
    enabled: ${LDAP_ENABLED:false}
    url: ${LDAP_URL:ldap://localhost:389}
    base: ${LDAP_BASE_DN:dc=utec,dc=edu,dc=uy}
    username: ${LDAP_USERNAME:}
    password: ${LDAP_PASSWORD:}

spring:
  ldap:
    embedded:
      base-dn: dc=utec,dc=edu,dc=uy
      credential:
        username: cn=admin
        password: admin
      ldif: classpath:test-ldap.ldif
      port: 8389
      validation:
        enabled: false
```

### Configuración de LDAP (Condicional)

```java
@Configuration
@ConditionalOnProperty(name = "security.ldap.enabled", havingValue = "true")
public class LdapConfig {
    
    @Bean
    public LdapContextSource contextSource() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapUrl);
        contextSource.setBase(ldapBase);
        contextSource.setUserDn(ldapUsername);
        contextSource.setPassword(ldapPassword);
        return contextSource;
    }
    
    @Bean
    public LdapTemplate ldapTemplate(LdapContextSource contextSource) {
        return new LdapTemplate(contextSource);
    }
}
```

---

## Endpoints

### POST /auth/login

Autenticar usuario y obtener token JWT.

**Request**:
```json
{
  "email": "usuario@utec.edu.uy",
  "password": "password123",
  "authProvider": "LOCAL"
}
```

**Response 200 OK**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "email": "usuario@utec.edu.uy",
  "roles": ["TEACHER", "COORDINATOR"]
}
```

**Errores**:
- `401 Unauthorized`: Credenciales inválidas
- `403 Forbidden`: Cuenta deshabilitada o bloqueada
- `429 Too Many Requests`: Demasiados intentos fallidos

### GET /auth/me

Obtener información del usuario autenticado.

**Request**:
```
GET /auth/me
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response 200 OK**:
```json
{
  "id": 1,
  "email": "usuario@utec.edu.uy",
  "name": "Juan",
  "lastName": "Pérez",
  "roles": ["TEACHER"],
  "authProvider": "LOCAL",
  "enabled": true
}
```

**Errores**:
- `401 Unauthorized`: Token inválido o expirado

### POST /auth/logout

Cerrar sesión del usuario (invalidar token del lado del cliente).

**Request**:
```
POST /auth/logout
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response 200 OK**:
```json
{
  "message": "Logout exitoso"
}
```

### GET /auth/status

Verificar estado de autenticación (health check).

**Request**:
```
GET /auth/status
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response 200 OK**:
```json
{
  "authenticated": true,
  "email": "usuario@utec.edu.uy"
}
```

---

## Manejo de Errores

### Jerarquía de Excepciones

```
Exception
  |
  +-- RuntimeException
        |
        +-- AuthenticationException (Spring Security)
              |
              +-- InvalidCredentialsException
              |
              +-- AccountDisabledException
              |
              +-- TooManyAttemptsException
```

### GlobalExceptionHandler

Maneja todas las excepciones de forma centralizada:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
        InvalidCredentialsException ex
    ) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage(),
                LocalDateTime.now()
            ));
    }
    
    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(
        JwtAuthenticationException ex
    ) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Token inválido o expirado",
                LocalDateTime.now()
            ));
    }
}
```

### Mensajes de Error Internacionalizados

**Messages.properties**:
```properties
# Authentication Errors
auth.error.invalid-credentials=Credenciales inválidas
auth.error.user-not-authenticated=Usuario no autenticado
auth.error.account-disabled=La cuenta de usuario está deshabilitada
auth.error.account-locked=Cuenta temporalmente bloqueada. Intente nuevamente en {0} minutos
auth.error.too-many-attempts=Demasiados intentos fallidos. Intente nuevamente en {0} minutos
auth.error.incorrect-auth-provider=Este usuario debe autenticarse usando: {0}

# Provider-specific errors
auth.error.no-strategy=No hay una estrategia de autenticación disponible para: {0}

# LDAP-specific errors
auth.error.ldap-not-enabled=La autenticación LDAP no está habilitada en este sistema
auth.error.ldap-error=Error al autenticar con LDAP: {0}
```

### Logging de Auditoría

Todos los intentos de autenticación se registran:

**Login exitoso**:
```
INFO  - User authenticated successfully via LOCAL: usuario@utec.edu.uy
```

**Login fallido**:
```
WARN  - LOCAL authentication failed for usuario@utec.edu.uy: invalid password
```

**Ataque potencial**:
```
ERROR - Too many failed login attempts for usuario@utec.edu.uy from IP 192.168.1.100
```

---

## Consideraciones de Seguridad

### Buenas Prácticas Implementadas

1. **Passwords nunca se almacenan en texto plano**
   - Se usa BCrypt con salt automático
   - Factor de trabajo configurable (por defecto 10)

2. **Tokens JWT firmados con HS256**
   - Secret key almacenado en variable de entorno
   - Expiración configurable (por defecto 24 horas)

3. **HTTPS requerido en producción**
   - Configurar SSL/TLS en el servidor
   - Redirigir HTTP a HTTPS

4. **CORS configurado restrictivamente**
   - Solo origins específicos permitidos
   - Credentials habilitado solo si es necesario

5. **Rate limiting implementado**
   - Protección contra fuerza bruta
   - Bloqueo temporal tras intentos fallidos

6. **Sanitización de inputs**
   - Prevención de LDAP injection
   - Validación de emails y passwords

7. **Mensajes de error no revelan información sensible**
   - No se indica si el usuario existe
   - Mensajes genéricos para fallos de autenticación

8. **Logging exhaustivo para auditoría**
   - Todos los intentos de login registrados
   - Incluye IP, timestamp, resultado

### Recomendaciones para Producción

1. **Rotar el JWT secret regularmente**
   - Implementar mecanismo de rotación
   - Invalidar tokens antiguos

2. **Implementar refresh tokens**
   - Token de corta duración (15 min)
   - Refresh token de larga duración (7 días)

3. **Usar Redis para blacklist de tokens**
   - Permitir invalidación real de tokens
   - Mejorar rate limiting distribuido

4. **Implementar 2FA (Two-Factor Authentication)**
   - TOTP (Google Authenticator)
   - SMS o email como segundo factor

5. **Monitorear intentos fallidos**
   - Alertas automáticas por patrones sospechosos
   - Integración con SIEM

6. **Actualizar dependencias regularmente**
   - Spring Security patches
   - JWT library updates

---

## Diagrama de Componentes

```
┌─────────────────────────────────────────────────────────────────┐
│                         Cliente (Frontend)                      │
│                                                                 │
│  - Envía credenciales (email, password, authProvider)           │
│  - Almacena JWT token                                           │
│  - Incluye token en header Authorization                        │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │ HTTPS
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                    Spring Security Filter Chain                 │
│                                                                 │
│  1. CorsFilter                                                  │
│  2. JwtAuthenticationFilter ◄─────┐                             │
│  3. UsernamePasswordAuthenticationFilter                        │
│  4. ExceptionTranslationFilter                                  │
│  5. FilterSecurityInterceptor                                   │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                       AuthController                            │
│                                                                 │
│  POST /auth/login                                               │
│  GET  /auth/me                                                  │
│  POST /auth/logout                                              │
│  GET  /auth/status                                              │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             │
┌────────────────────────────▼────────────────────────────────────┐
│                   AuthenticationService                         │
│                                                                 │
│  - Selecciona estrategia según AuthProvider                     │
│  - Valida rate limiting                                         │
│  - Genera JWT token                                             │
│  - Registra intentos de login                                   │
└───────────┬──────────────────────────────────┬──────────────────┘
            │                                  │
            │                                  │
┌───────────▼────────────────┐   ┌─────────────▼─────────────────┐
│ LocalAuthenticationStrategy│   │ LdapAuthenticationStrategy    │
│                            │   │                               │
│ 1. Buscar usuario en BD    │   │ 1. Autenticar con LDAP        │
│ 2. Validar authProvider    │   │ 2. Buscar usuario en BD       │
│ 3. Validar cuenta activa   │   │ 3. Validar authProvider       │
│ 4. Verificar password      │   │ 4. Validar cuenta activa      │
│    con BCrypt              │   │                               │
└───────────┬────────────────┘   └────────────┬──────────────────┘
            │                                 │
            │                                 │
            └────────────┬────────────────────┘
                         │
                         │ User entity
                         │
┌────────────────────────▼────────────────────────────────────────┐
│                     JwtTokenProvider                            │
│                                                                 │
│  - Genera token JWT con claims                                  │
│  - Valida firma y expiración                                    │
│  - Extrae información del token                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Conclusión

La implementación de autenticación en el sistema combina las mejores prácticas de seguridad con flexibilidad para soportar múltiples proveedores de autenticación. El uso del patrón Strategy permite agregar nuevos métodos de autenticación sin modificar el código existente, mientras que la arquitectura de capas asegura múltiples validaciones y protección defense-in-depth.

El sistema está diseñado para ser escalable, auditable y seguro, cumpliendo con los estándares de la industria para aplicaciones empresariales.
