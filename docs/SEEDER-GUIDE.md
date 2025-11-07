# Data Seeder - Guía de Uso

## ¿Qué es el Data Seeder?

El **DataSeeder** es un componente que carga datos iniciales en la base de datos cuando la aplicación se inicia. Es útil para desarrollo y testing.

## Datos que se cargan

El seeder crea automáticamente:

1. **1 Usuario (User)**
   - Email: `juan.perez@utec.edu.uy`
   - Nombre: Juan Pérez
   - CI: 12345678
   - Teléfono: +59899123456

2. **1 Teacher (Docente)**
   - Asociado al usuario creado
   - Rol: TEACHER

3. **1 Program (Programa)**
   - Nombre: Ingeniería en Tecnologías de la Información
   - Duración: 8 semestres
   - Créditos totales: 240

4. **1 Term (Semestre)**
   - Número: 1 (primer semestre)
   - Pertenece al programa creado

5. **1 Curricular Unit (Unidad Curricular)**
   - Nombre: Programación Avanzada
   - Créditos: 8
   - Pertenece al semestre 1

6. **1 Course (Curso)**
   - Descripción: Curso de Programación Avanzada - Grupo 1
   - Turno: MORNING (Matutino)
   - Fecha inicio: 01/03/2025
   - Fecha fin: 15/07/2025
   - Sistema de parciales: PGS_1 (PE 25% + SE 35% + EC 40%)
   - Docente asignado: Juan Pérez
   - Pertenece a la UC Programación Avanzada

## ¿Cuándo se ejecuta?

El seeder solo se ejecuta cuando:
- La aplicación se inicia con el perfil `dev`
- La base de datos está vacía (no hay cursos creados)

## Cómo usarlo

### Opción 1: Con Gradle (Recomendado)

```powershell
# Windows
.\gradlew.bat bootRun --args='--spring.profiles.active=dev'

# Linux/Mac
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Opción 2: Con el script run-local

```powershell
# Windows
.\run-local.ps1

# Linux/Mac
./run-local.sh
```

Asegúrate de que tu archivo `.env` tenga:
```
SPRING_PROFILES_ACTIVE=dev
```

### Opción 3: Desde IntelliJ IDEA

1. Ve a **Run** → **Edit Configurations**
2. En **Program arguments**, agrega: `--spring.profiles.active=dev`
3. O en **Environment variables**, agrega: `SPRING_PROFILES_ACTIVE=dev`
4. Click en **Run**

### Opción 4: Con Docker Compose

En `docker-compose.yml` o `docker-compose.override.yml`, asegúrate de tener:

```yaml
services:
  app:
    environment:
      - SPRING_PROFILES_ACTIVE=dev
```

## Log de ejecución

Cuando el seeder se ejecuta correctamente, verás en la consola:

```
==================================================
Starting data seeding...
==================================================
Creating user for teacher...
✓ Created user: juan.perez@utec.edu.uy (ID: 1)
Creating teacher...
✓ Created teacher for user: juan.perez@utec.edu.uy (ID: 1)
Creating program...
✓ Created program: Ingeniería en Tecnologías de la Información (ID: 1)
Creating term...
✓ Created term: Semestre 1 (ID: 1)
Creating curricular unit...
✓ Created curricular unit: Programación Avanzada - 8 créditos (ID: 1)
Creating course...
✓ Created course: Curso de Programación Avanzada - Grupo 1 (ID: 1)
  - Shift: MORNING
  - Start date: 2025-03-01
  - End date: 2025-07-15
  - Teacher: juan.perez@utec.edu.uy
==================================================
Data seeding completed successfully!
==================================================
```

Si los datos ya existen:
```
Data already exists. Skipping seeding.
```

## Resetear los datos

Si quieres volver a ejecutar el seeder:

### Método 1: Borrar la base de datos
```sql
DROP DATABASE planificador_db;
CREATE DATABASE planificador_db;
```

### Método 2: Cambiar configuración en application-dev.yml
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop  # Borra y recrea las tablas en cada inicio
```

**⚠️ ADVERTENCIA:** Esto borrará TODOS los datos cada vez que inicies la aplicación.

### Método 3: Limpiar manualmente
```sql
DELETE FROM course;
DELETE FROM curricular_unit;
DELETE FROM term;
DELETE FROM program;
DELETE FROM teacher;
DELETE FROM users;
```

## Desactivar el seeder

Si no quieres que se ejecute el seeder:

1. **Opción 1:** No uses el perfil `dev`
   ```bash
   --spring.profiles.active=prod
   ```

2. **Opción 2:** Comenta la anotación `@Profile` en `DataSeeder.java`
   ```java
   // @Profile({"dev"})
   ```

3. **Opción 3:** Comenta la anotación `@Component`
   ```java
   // @Component
   ```

## Modificar los datos del seeder

Para cambiar los datos que se cargan, edita el archivo:
```
src/main/java/edu/utec/planificador/config/DataSeeder.java
```

Puedes modificar:
- Nombre del docente
- Nombre del programa y unidad curricular
- Fechas del curso
- Cualquier otro dato

## Testing con el seeder

El seeder también está configurado para el perfil `test`, pero puedes quitarlo si no lo necesitas:

```java
@Profile({"dev"})  // Solo en dev, no en test
```

## Archivos relacionados

- **DataSeeder.java**: `src/main/java/edu/utec/planificador/config/DataSeeder.java`
- **ProgramRepository.java**: `src/main/java/edu/utec/planificador/repository/ProgramRepository.java`
- **application-dev.yml**: `src/main/resources/application-dev.yml`

## Troubleshooting

### Error: "The constructor X() is not visible"
- Asegúrate de que las entidades tengan constructores públicos o usa los constructores existentes.

### Error: "Data already exists"
- Es normal, significa que ya hay datos. Resetea la BD si quieres volver a ejecutar el seeder.

### El seeder no se ejecuta
- Verifica que estés usando el perfil `dev`
- Revisa los logs para ver si hay errores
- Asegúrate de que la BD esté accesible

### Error de relaciones
- Verifica que el orden de creación sea correcto (primero las entidades padre, luego las hijas)
