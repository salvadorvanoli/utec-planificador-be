# Guía de Implementación de Roles y Permisos

## Tabla de Contenidos

1. [Descripción General](#descripción-general)
2. [Arquitectura](#arquitectura)
3. [Definición de Roles](#definición-de-roles)
4. [Sistema de Permisos](#sistema-de-permisos)
5. [Implementación del Control de Acceso](#implementación-del-control-de-acceso)
6. [Ejemplos de Uso](#ejemplos-de-uso)
7. [Mejores Prácticas](#mejores-prácticas)
8. [Resolución de Problemas](#resolución-de-problemas)

---

## Descripción General

Este documento describe la implementación del sistema de Control de Acceso Basado en Roles (RBAC) en el backend del Planificador Docente de UTEC. El sistema utiliza un modelo jerárquico de permisos donde los roles contienen conjuntos de permisos granulares, y los usuarios pueden tener múltiples posiciones con diferentes roles.

### Conceptos Clave

- **Usuario**: Una persona registrada en el sistema
- **Posición**: Una asignación de rol con campus asociados
- **Rol**: Una colección de permisos que define qué acciones se pueden realizar
- **Permiso**: Una autorización granular para realizar una acción específica
- **Autoridad**: Representación de Spring Security de un permiso o rol

---

## Arquitectura

### Relación de Entidades

```
Usuario (1) ─────< (N) Posición
                        │
                        │ (1)
                        │
                        └─── Rol (enum)
                             │
                             └─── Set<Permiso> (enum)
```

### Componentes Principales

1. **Entidad User** (`edu.utec.planificador.entity.User`)
   - Implementa la interfaz `UserDetails`
   - Puede tener múltiples entidades `Position`
   - Agrega todas las autoridades de las posiciones activas

2. **Entidad Position** (`edu.utec.planificador.entity.Position`)
   - Clase base abstracta para diferentes tipos de posición
   - Contiene un enum `Role`
   - Asociada con una o más entidades `Campus`
   - Tiene un flag `isActive` para permisos temporales

3. **Enum Role** (`edu.utec.planificador.enumeration.Role`)
   - Define los roles disponibles en el sistema
   - Cada rol contiene un conjunto de permisos
   - Proporciona método para convertir a autoridades de Spring Security

4. **Enum Permission** (`edu.utec.planificador.enumeration.Role.Permission`)
   - Enum anidado dentro de `Role`
   - Define todos los permisos disponibles en el sistema
   - Organizado por dominio funcional

---

## Definición de Roles

### Roles Disponibles

El sistema define cinco roles principales:

#### 1. ADMINISTRATOR
Acceso completo al sistema con todos los permisos.

**Nombre de Visualización**: Administrador

**Permisos**:
- Gestión de Usuarios: READ, WRITE, DELETE
- Estructura Organizacional: Acceso completo (ITR, Campus)
- Estructura Académica: Acceso completo (Programa, Período, Unidad Curricular)
- Gestión de Cursos: Acceso completo
- Planificación: Acceso completo
- Configuración: Acceso completo

#### 2. EDUCATION_MANAGER
Responsable de la gestión académica y supervisión de la planificación.

**Nombre de Visualización**: Responsable de Educación

**Permisos**:
- Gestión de Usuarios: Solo READ
- Estructura Organizacional: Solo READ
- Estructura Académica: Solo READ
- Gestión de Cursos: READ, WRITE
- Planificación: READ, WRITE
- Configuración: Solo READ

#### 3. COORDINATOR
Coordina cursos y planificación académica para su área.

**Nombre de Visualización**: Coordinador

**Permisos**:
- Gestión de Usuarios: Solo READ
- Estructura Organizacional: Solo READ
- Estructura Académica: Solo READ
- Gestión de Cursos: READ, WRITE
- Planificación: READ, WRITE
- Configuración: Solo READ

#### 4. ANALYST
Acceso de solo lectura para análisis de datos y reportes.

**Nombre de Visualización**: Analista

**Permisos**:
- Gestión de Usuarios: Solo READ
- Estructura Organizacional: Solo READ
- Estructura Académica: Solo READ
- Gestión de Cursos: Solo READ
- Planificación: Solo READ
- Configuración: Sin acceso

#### 5. TEACHER
Docente que crea y gestiona su propia planificación.

**Nombre de Visualización**: Docente

**Permisos**:
- Estructura Organizacional: Solo READ
- Estructura Académica: Solo READ
- Gestión de Cursos: READ, WRITE
- Planificación: READ, WRITE, DELETE (propias planificaciones)
- Configuración: Sin acceso

---

## Sistema de Permisos

### Categorías de Permisos

Los permisos están organizados en siete dominios funcionales:

#### Gestión de Usuarios
```
USER_READ          - Ver información de usuarios
USER_WRITE         - Crear/modificar usuarios
USER_DELETE        - Eliminar usuarios
```

#### Estructura Organizacional
```
REGIONAL_TECHNICAL_INSTITUTE_READ    - Ver información de ITR
REGIONAL_TECHNICAL_INSTITUTE_WRITE   - Crear/modificar ITRs
REGIONAL_TECHNICAL_INSTITUTE_DELETE  - Eliminar ITRs

CAMPUS_READ    - Ver información de campus
CAMPUS_WRITE   - Crear/modificar campus
CAMPUS_DELETE  - Eliminar campus
```

#### Estructura Académica
```
PROGRAM_READ    - Ver programas académicos
PROGRAM_WRITE   - Crear/modificar programas
PROGRAM_DELETE  - Eliminar programas

TERM_READ    - Ver períodos académicos
TERM_WRITE   - Crear/modificar períodos
TERM_DELETE  - Eliminar períodos

CURRICULAR_UNIT_READ    - Ver unidades curriculares
CURRICULAR_UNIT_WRITE   - Crear/modificar unidades curriculares
CURRICULAR_UNIT_DELETE  - Eliminar unidades curriculares
```

#### Gestión de Cursos
```
COURSE_READ    - Ver cursos
COURSE_WRITE   - Crear/modificar cursos
COURSE_DELETE  - Eliminar cursos
```

#### Planificación
```
PLANNING_READ    - Ver planificaciones académicas
PLANNING_WRITE   - Crear/modificar planificaciones
PLANNING_DELETE  - Eliminar planificaciones
```

#### Configuración
```
CONFIGURATION_READ    - Ver configuración del sistema
CONFIGURATION_WRITE   - Modificar configuración del sistema
```

### Resolución de Autoridades

Cuando un usuario se autentica, Spring Security llama a `User.getAuthorities()`:

```java
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    return positions.stream()
        .filter(Position::getIsActive)
        .flatMap(position -> position.getRole().getAuthorities().stream())
        .distinct()
        .collect(Collectors.toSet());
}
```

Este método:
1. Filtra solo las posiciones activas
2. Extrae todas las autoridades del rol de cada posición
3. Elimina duplicados
4. Retorna un conjunto unificado de autoridades

Cada rol proporciona autoridades a través de:

```java
public Set<GrantedAuthority> getAuthorities() {
    Set<GrantedAuthority> authorities = permissions.stream()
        .map(permission -> new SimpleGrantedAuthority(permission.name()))
        .collect(Collectors.toSet());
    
    authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
    
    return authorities;
}
```

Esto genera:
- Una autoridad por permiso (ej: `USER_READ`, `COURSE_READ`)
- Una autoridad de rol con prefijo `ROLE_` (ej: `ROLE_ADMINISTRATOR`, `ROLE_TEACHER`)

---

## Implementación del Control de Acceso

### Seguridad a Nivel de Controlador

Se utiliza la anotación `@PreAuthorize` de Spring Security para proteger endpoints.

#### Sintaxis Básica

```java
@PreAuthorize("hasAuthority('NOMBRE_PERMISO')")
@PreAuthorize("hasRole('NOMBRE_ROL')")
```

### hasAuthority vs hasRole

#### hasAuthority
- Verifica una coincidencia exacta del string de autoridad
- Usar para control de acceso basado en permisos
- Más granular y flexible
- Recomendado para la mayoría de casos de uso

```java
@PreAuthorize("hasAuthority('USER_READ')")
```

#### hasRole
- Agrega automáticamente el prefijo `ROLE_`
- Usar para control de acceso basado en roles
- Menos granular, verifica el rol completo
- Usar para características específicas de un rol

```java
@PreAuthorize("hasRole('ADMINISTRATOR')")  // Verifica ROLE_ADMINISTRATOR
```

### Escenario de Múltiples Posiciones

Los usuarios con múltiples posiciones activas agregan todos los permisos:

```java
User user = ...;
Position coordinator = new CoordinatorPosition(Role.COORDINATOR);
Position teacher = new TeacherPosition(Role.TEACHER);

user.addPosition(coordinator);
user.addPosition(teacher);

// El usuario tendrá TODOS los permisos de ambos roles
user.getAuthorities();  // Retorna la unión de permisos COORDINATOR + TEACHER
```

---

## Ejemplos de Uso

### Ejemplo 1: Permiso Simple

```java
@RestController
@RequestMapping("/users")
public class UserController {

    @PostMapping
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest request) {
        // Solo usuarios con permiso USER_WRITE pueden ejecutar esto
        // ADMINISTRATOR puede ejecutar
        // Otros roles no pueden
    }
}
```

### Ejemplo 2: Múltiples Permisos con AND

```java
@RestController
@RequestMapping("/courses")
public class CourseController {

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('COURSE_WRITE') AND hasAuthority('PLANNING_WRITE')")
    public ResponseEntity<CourseResponse> publishCourse(@PathVariable Long id) {
        // Requiere AMBOS permisos
        // ADMINISTRATOR, EDUCATION_MANAGER, COORDINATOR pueden ejecutar
        // TEACHER tiene ambos permisos
        // ANALYST no puede ejecutar (le falta COURSE_WRITE)
    }
}
```

### Ejemplo 3: Múltiples Permisos con OR

```java
@RestController
@RequestMapping("/plannings")
public class PlanningController {

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PLANNING_READ') OR hasRole('ADMINISTRATOR')")
    public ResponseEntity<PlanningResponse> getPlanning(@PathVariable Long id) {
        // Requiere O el permiso O el rol
        // Todos los roles excepto ANALYST tienen PLANNING_READ
        // ADMINISTRATOR también puede acceder vía rol
    }
}
```

### Ejemplo 4: Múltiples Roles con OR

```java
@RestController
@RequestMapping("/reports")
public class ReportController {

    @GetMapping("/advanced")
    @PreAuthorize("hasRole('ADMINISTRATOR') OR hasRole('EDUCATION_MANAGER')")
    public ResponseEntity<ReportResponse> getAdvancedReport() {
        // Solo para roles específicos
        // ADMINISTRATOR puede ejecutar
        // EDUCATION_MANAGER puede ejecutar
        // Otros roles no pueden
    }
}
```

### Ejemplo 5: Expresiones Complejas

```java
@RestController
@RequestMapping("/configuration")
public class ConfigurationController {

    @PutMapping("/system")
    @PreAuthorize("(hasAuthority('CONFIGURATION_WRITE') AND hasRole('ADMINISTRATOR')) OR hasRole('SUPER_ADMIN')")
    public ResponseEntity<ConfigResponse> updateSystemConfig(@RequestBody ConfigRequest request) {
        // Requiere: (permiso AND rol) OR rol específico
        // Muy restrictivo para operaciones críticas
    }
}
```

### Ejemplo 6: Acceso a Parámetros del Método

```java
@RestController
@RequestMapping("/courses")
public class CourseController {

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('COURSE_WRITE') AND @courseService.userHasAccessToCourse(#id, authentication)")
    public ResponseEntity<CourseResponse> updateCourse(
        @PathVariable Long id,
        @RequestBody CourseRequest request
    ) {
        // Verifica permiso Y lógica de negocio personalizada
        // @courseService es un bean de Spring
        // #id es la variable de path
        // authentication es el objeto de autenticación del usuario actual
    }
}
```

### Ejemplo 7: Múltiples Autoridades

```java
@RestController
@RequestMapping("/management")
public class ManagementController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('USER_READ', 'COURSE_READ', 'PLANNING_READ')")
    public ResponseEntity<DashboardResponse> getDashboard() {
        // Requiere CUALQUIERA de los permisos listados
        // Casi todos los roles pueden acceder (excepto roles muy restringidos)
    }
}
```

### Ejemplo 8: Endpoints Específicos de Rol

```java
@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @GetMapping("/my-courses")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<CourseResponse>> getMyCourses() {
        // Característica específica solo para docentes
        // Incluso ADMINISTRATOR necesitaría una posición TEACHER para acceder
    }
}
```

---

## Mejores Prácticas

### 1. Preferir hasAuthority para Permisos

Usar `hasAuthority()` para verificaciones de permisos granulares:

```java
// Bien
@PreAuthorize("hasAuthority('USER_WRITE')")

// Evitar (demasiado general)
@PreAuthorize("hasRole('ADMINISTRATOR')")  // A menos que sea característica específica de rol
```

### 2. Usar hasRole para Características Específicas de Rol

Reservar `hasRole()` para características inherentemente vinculadas a un rol:

```java
// Bien - Dashboard específico de docente
@PreAuthorize("hasRole('TEACHER')")
public ResponseEntity<?> getTeacherDashboard() { }

// Mal - Operación CRUD genérica
@PreAuthorize("hasRole('ADMINISTRATOR')")  // Usar hasAuthority en su lugar
public ResponseEntity<?> createUser() { }
```

### 3. Autorización en Capa de Servicio para Lógica Compleja

Para autorización compleja que involucra lógica de negocio:

```java
@Service
@RequiredArgsConstructor
public class CourseService {

    @PreAuthorize("hasAuthority('COURSE_WRITE')")
    public CourseResponse updateCourse(Long courseId, CourseRequest request) {
        Course course = findById(courseId);
        
        // Validación adicional de lógica de negocio
        User currentUser = securityService.getCurrentUser();
        if (!userHasAccessToCampus(currentUser, course.getCampus())) {
            throw new ForbiddenException("Sin acceso a este campus");
        }
        
        // Proceder con la actualización
    }
    
    private boolean userHasAccessToCampus(User user, Campus campus) {
        return user.getPositions().stream()
            .filter(Position::getIsActive)
            .flatMap(position -> position.getCampuses().stream())
            .anyMatch(c -> c.equals(campus));
    }
}
```

### 4. Documentar Requisitos de Permisos

Siempre documentar los permisos requeridos para cada endpoint:

```java
/**
 * Crea un nuevo usuario en el sistema.
 * 
 * @param request Datos de creación del usuario
 * @return Información del usuario creado
 * @throws ForbiddenException si el usuario carece del permiso USER_WRITE
 */
@PostMapping
@PreAuthorize("hasAuthority('USER_WRITE')")
public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest request) {
    // Implementación
}
```

### 5. Probar Combinaciones de Permisos

Siempre probar casos límite con múltiples posiciones:

```java
@Test
void userWithMultiplePositions_shouldHaveAggregatedPermissions() {
    User user = createUser();
    user.addPosition(createCoordinatorPosition());
    user.addPosition(createTeacherPosition());
    
    Set<String> authorities = user.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toSet());
    
    // Debe tener permisos de ambos roles
    assertTrue(authorities.contains("COURSE_WRITE"));
    assertTrue(authorities.contains("PLANNING_WRITE"));
}
```

### 6. Evitar Hardcodear Roles en Lógica de Negocio

```java
// Mal
if (user.getPositions().stream()
    .anyMatch(p -> p.getRole() == Role.ADMINISTRATOR)) {
    // Hacer algo
}

// Bien
if (SecurityContextHolder.getContext()
    .getAuthentication()
    .getAuthorities().stream()
    .anyMatch(a -> a.getAuthority().equals("USER_DELETE"))) {
    // Hacer algo
}
```

### 7. Usar Convenciones de Nomenclatura Consistentes

Seguir el patrón establecido:

```
ENTIDAD_OPERACION
USER_WRITE
COURSE_READ
PLANNING_DELETE
```

No:
```
WRITE_USER  // Orden incorrecto
read_course  // Mayúsculas incorrectas
PlanningDel  // Abreviatura inconsistente
```

---

## Resolución de Problemas

### Problema 1: Permiso Denegado a Pesar de Tener el Rol

**Síntoma**: Usuario con rol ADMINISTRATOR recibe 403 Forbidden

**Causas Posibles**:
1. La posición está marcada como `isActive = false`
2. Usando `hasAuthority('ROLE_ADMINISTRATOR')` en lugar de `hasRole('ADMINISTRATOR')`
3. Error tipográfico en el nombre del permiso

**Solución**:
```java
// Verificar las autoridades reales del usuario
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
auth.getAuthorities().forEach(System.out::println);

// Verificar que la posición esté activa
user.getPositions().forEach(p -> 
    System.out.println(p.getRole() + " - Activa: " + p.getIsActive())
);
```

### Problema 2: Múltiples Posiciones No Funcionan

**Síntoma**: El usuario tiene dos posiciones pero solo funcionan los permisos de un rol

**Causas Posibles**:
1. Una posición está inactiva
2. Problema con la implementación de `getAuthorities()`
3. Problema de caché

**Solución**:
- Verificar que ambas posiciones estén activas
- Limpiar caché del contexto de seguridad
- Verificar que `distinct()` funcione en la agregación de autoridades

### Problema 3: hasRole No Funciona

**Síntoma**: `hasRole('ADMINISTRATOR')` siempre deniega acceso

**Causa Posible**: Falta el prefijo `ROLE_` en las autoridades

**Solución**:
Verificar que `Role.getAuthorities()` agregue el prefijo:
```java
authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
```

### Problema 4: Acceso Específico por Campus

**Síntoma**: El usuario solo debería acceder a cursos de su campus

**Solución**: Implementar validación en capa de servicio:
```java
@PreAuthorize("hasAuthority('COURSE_WRITE')")
public void updateCourse(Long courseId, CourseRequest request) {
    Course course = findById(courseId);
    User user = getCurrentUser();
    
    boolean hasAccess = user.getPositions().stream()
        .filter(Position::getIsActive)
        .flatMap(p -> p.getCampuses().stream())
        .anyMatch(campus -> campus.equals(course.getCampus()));
    
    if (!hasAccess) {
        throw new ForbiddenException("Sin acceso a este campus");
    }
    
    // Proceder
}
```

---

## Resumen

El Planificador Docente de UTEC implementa un sistema de autorización flexible y multinivel:

- Los usuarios pueden tener múltiples posiciones con diferentes roles
- Cada rol contiene un conjunto de permisos granulares
- Los permisos se verifican a nivel de controlador usando `@PreAuthorize`
- La autorización de lógica de negocio compleja se maneja en la capa de servicio
- El sistema soporta control de acceso tanto basado en permisos como en roles

Este diseño permite:
- Control de acceso granular
- Asignación flexible de roles por campus
- Gestión sencilla de permisos
- Separación clara de responsabilidades
- Lógica de autorización testeable

Para preguntas o aclaraciones, consultar el código fuente en:
- `edu.utec.planificador.enumeration.Role`
- `edu.utec.planificador.entity.User`
- `edu.utec.planificador.entity.Position`
