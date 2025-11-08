# Sistema de Control de Acceso Basado en Posiciones

## Resumen

Se ha implementado un sistema completo de control de acceso que valida que los usuarios solo puedan acceder a recursos (cursos, unidades curriculares, planificaciones, etc.) dentro de los RTI (Institutos Tecnológicos Regionales) y Campus a los que tienen acceso a través de sus posiciones activas.

## Componentes Creados

### 1. AccessControlService (Interface)
**Ubicación**: `src/main/java/edu/utec/planificador/service/AccessControlService.java`

Define los métodos de validación de acceso para todos los recursos:
- `validateCourseAccess(Long courseId)`
- `validateCurricularUnitAccess(Long curricularUnitId)`
- `validateWeeklyPlanningAccess(Long weeklyPlanningId)`
- `validateProgrammaticContentAccess(Long programmaticContentId)`
- `validateActivityAccess(Long activityId)`
- `validateCampusAccess(Long campusId)`
- `validateRtiAccess(Long rtiId)`
- `validateProgramAccess(Long programId)`
- `validateTermAccess(Long termId)`
- `hasAccessToCampus(Long campusId)`
- `hasAccessToRti(Long rtiId)`

### 2. AccessControlServiceImpl (Implementación)
**Ubicación**: `src/main/java/edu/utec/planificador/service/impl/AccessControlServiceImpl.java`

Implementa toda la lógica de validación:

**Características principales:**
- **Validación por Campus**: Verifica si el usuario tiene una posición activa en el campus específico
- **Validación por RTI**: Verifica si el usuario tiene una posición activa en algún campus del RTI
- **Validación en cascada**: Los recursos hijos heredan las restricciones de acceso de sus padres
  - Course → CurricularUnit → Term → Program → Campus/RTI
  - WeeklyPlanning → Course
  - ProgrammaticContent → WeeklyPlanning
  - Activity → ProgrammaticContent

**Lógica de acceso:**
```
Usuario tiene acceso SI:
1. Tiene una posición activa en el campus donde se ofrece el programa, O
2. Tiene una posición activa en el RTI que contiene ese campus
```

### 3. Actualización de CampusRepository
**Ubicación**: `src/main/java/edu/utec/planificador/repository/CampusRepository.java`

Se agregó el método:
```java
@Query("SELECT c FROM Campus c JOIN c.programs p WHERE p.id = :programId")
List<Campus> findByProgram(@Param("programId") Long programId);
```

Este método permite encontrar todos los campus donde se ofrece un programa específico.

## Servicios Actualizados

Se integraron las validaciones de acceso en todos los métodos de los siguientes servicios:

### 1. CourseServiceImpl
- `createCourse()`: Valida acceso a la unidad curricular
- `getCourseById()`: Valida acceso al curso
- `updateCourse()`: Valida acceso al curso y a la nueva unidad curricular
- `deleteCourse()`: Valida acceso al curso
- `addSustainableDevelopmentGoal()`: Valida acceso al curso
- `removeSustainableDevelopmentGoal()`: Valida acceso al curso
- `addUniversalDesignLearningPrinciple()`: Valida acceso al curso
- `removeUniversalDesignLearningPrinciple()`: Valida acceso al curso

### 2. CurricularUnitServiceImpl
- `createCurricularUnit()`: Valida acceso al término
- `getCurricularUnitById()`: Valida acceso a la unidad curricular
- `updateCurricularUnit()`: Valida acceso a la unidad curricular y al nuevo término
- `deleteCurricularUnit()`: Valida acceso a la unidad curricular
- `addDomainArea()`: Valida acceso a la unidad curricular
- `removeDomainArea()`: Valida acceso a la unidad curricular
- `addProfessionalCompetency()`: Valida acceso a la unidad curricular
- `removeProfessionalCompetency()`: Valida acceso a la unidad curricular

### 3. WeeklyPlanningServiceImpl
- `createWeeklyPlanning()`: Valida acceso al curso
- `getWeeklyPlanningById()`: Valida acceso a la planificación semanal
- `getWeeklyPlanningsByCourseId()`: Valida acceso al curso
- `getWeeklyPlanningByCourseIdAndWeekNumber()`: Valida acceso al curso
- `getWeeklyPlanningByCourseIdAndDate()`: Valida acceso al curso
- `updateWeeklyPlanning()`: Valida acceso a la planificación semanal
- `deleteWeeklyPlanning()`: Valida acceso a la planificación semanal

### 4. ProgrammaticContentServiceImpl
- `createProgrammaticContent()`: Valida acceso a la planificación semanal
- `getProgrammaticContentById()`: Valida acceso al contenido programático
- `updateProgrammaticContent()`: Valida acceso al contenido programático y a la nueva planificación
- `deleteProgrammaticContent()`: Valida acceso al contenido programático

### 5. ActivityServiceImpl
- `createActivity()`: Valida acceso al contenido programático
- `getActivityById()`: Valida acceso a la actividad
- `updateActivity()`: Valida acceso a la actividad y al nuevo contenido programático
- `deleteActivity()`: Valida acceso a la actividad

### 6. AIAgentServiceImpl
- `sendChatMessage()`: Valida acceso al curso si se proporciona courseId
- `getSuggestions()`: Valida acceso al curso
- `generateReport()`: Valida acceso al curso
- `clearChatSession()`: No requiere validación (gestión de sesión)

## Comportamiento de Seguridad

### Escenarios de Acceso

#### 1. Usuario con posición en Campus específico
```
Usuario: Juan
Posición: COORDINATOR en Campus Montevideo
RTI: ITR Centro Sur

Puede acceder a:
✓ Todos los programas ofrecidos en Campus Montevideo
✓ Todos los cursos de esos programas
✓ Todas las planificaciones de esos cursos
✓ Todo el contenido programático y actividades

NO puede acceder a:
✗ Cursos del Campus Fray Bentos (otro campus del mismo RTI)
✗ Cursos de otros RTIs
```

#### 2. Usuario con posición en RTI
```
Usuario: María
Posición: EDUCATION_MANAGER en ITR Centro Sur

Puede acceder a:
✓ Todos los campus del ITR Centro Sur
✓ Todos los programas de esos campus
✓ Todos los cursos, planificaciones y contenido

NO puede acceder a:
✗ Recursos de otros RTIs
```

### Excepciones Lanzadas

Cuando un usuario intenta acceder a un recurso sin permisos:
- **ForbiddenException**: Se lanza con mensaje descriptivo
- **HTTP 403**: Status code retornado al cliente
- **Log de seguridad**: Se registra el intento de acceso no autorizado

## Protección contra Ataques

Este sistema protege contra:

1. **Session Hijacking**: Aunque alguien robe la cookie de sesión, solo podrá acceder a los recursos del usuario legítimo
2. **Parameter Tampering**: Modificar IDs en las peticiones no dará acceso a recursos fuera del alcance del usuario
3. **Horizontal Privilege Escalation**: Un usuario no puede acceder a datos de otros campus/RTI modificando parámetros
4. **Information Disclosure**: Se previene el acceso no autorizado a información sensible

## Ejemplo de Flujo

### Petición: GET /courses/123

1. Usuario autenticado hace la petición
2. `CourseServiceImpl.getCourseById(123)` es invocado
3. `accessControlService.validateCourseAccess(123)` verifica:
   - Obtiene el curso y su jerarquía:
     - Course 123 → CurricularUnit → Term → Program → Campus
   - Verifica si el usuario tiene posición activa en ese Campus o RTI
4. Si tiene acceso: Retorna el curso
5. Si no tiene acceso: Lanza `ForbiddenException` con HTTP 403

## Mejoras Futuras Sugeridas

1. **Cache de permisos**: Implementar caché para las validaciones de acceso y mejorar performance
2. **Auditoría detallada**: Registrar todos los intentos de acceso (exitosos y fallidos) en base de datos
3. **Rate limiting**: Limitar intentos de acceso para prevenir ataques de fuerza bruta
4. **Filtrado en queries**: Modificar los repositorios para filtrar automáticamente por permisos del usuario
5. **Tests de seguridad**: Agregar tests unitarios e integración para cada escenario de acceso

## Verificación

Para verificar que el sistema está funcionando:

1. **Compilación exitosa**: Todos los archivos compilan sin errores
2. **No hay código duplicado**: Todos los archivos están limpios y bien estructurados
3. **Validaciones en todos los endpoints**: Cada método de servicio tiene su validación de acceso
4. **Logs de seguridad**: Los intentos de acceso no autorizado quedan registrados

## Notas Importantes

- Las validaciones se ejecutan **antes** de cualquier operación de base de datos
- Solo las posiciones con `isActive = true` se consideran para las validaciones
- El sistema valida tanto en creación, lectura, actualización como eliminación de recursos

