# Roles and Permissions Implementation Guide

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Role Definition](#role-definition)
4. [Permission System](#permission-system)
5. [Access Control Implementation](#access-control-implementation)
6. [Usage Examples](#usage-examples)
7. [Best Practices](#best-practices)
8. [Troubleshooting](#troubleshooting)

---

## Overview

This document describes the implementation of the Role-Based Access Control (RBAC) system in the UTEC Teaching Planner backend. The system uses a hierarchical permission model where roles contain sets of granular permissions, and users can have multiple positions with different roles.

### Key Concepts

- **User**: A person registered in the system
- **Position**: A role assignment with associated campuses
- **Role**: A collection of permissions that define what actions can be performed
- **Permission**: A granular authorization to perform a specific action
- **Authority**: Spring Security's representation of a permission or role

---

## Architecture

### Entity Relationship

```
User (1) ─────< (N) Position
                     │
                     │ (1)
                     │
                     └─── Role (enum)
                          │
                          └─── Set<Permission> (enum)
```

### Key Components

1. **User Entity** (`edu.utec.planificador.entity.User`)
   - Implements `UserDetails` interface
   - Can have multiple `Position` entities
   - Aggregates all authorities from active positions

2. **Position Entity** (`edu.utec.planificador.entity.Position`)
   - Abstract base class for different position types
   - Contains a `Role` enum
   - Associated with one or more `Campus` entities
   - Has an `isActive` flag for temporal permissions

3. **Role Enum** (`edu.utec.planificador.enumeration.Role`)
   - Defines available roles in the system
   - Each role contains a set of permissions
   - Provides method to convert to Spring Security authorities

4. **Permission Enum** (`edu.utec.planificador.enumeration.Role.Permission`)
   - Nested enum within `Role`
   - Defines all available permissions in the system
   - Organized by functional domain

---

## Role Definition

### Available Roles

The system defines five main roles:

#### 1. ADMINISTRATOR
Full system access with all permissions.

**Display Name**: Administrador

**Permissions**:
- User Management: READ, WRITE, DELETE
- Organizational Structure: Full access (ITR, Campus)
- Academic Structure: Full access (Program, Term, Curricular Unit)
- Course Management: Full access
- Planning: Full access
- Configuration: Full access

#### 2. EDUCATION_MANAGER
Responsible for academic management and planning oversight.

**Display Name**: Responsable de Educación

**Permissions**:
- User Management: READ only
- Organizational Structure: READ only
- Academic Structure: READ only
- Course Management: READ, WRITE
- Planning: READ, WRITE
- Configuration: READ only

#### 3. COORDINATOR
Coordinates courses and academic planning for their area.

**Display Name**: Coordinador

**Permissions**:
- User Management: READ only
- Organizational Structure: READ only
- Academic Structure: READ only
- Course Management: READ, WRITE
- Planning: READ, WRITE
- Configuration: READ only

#### 4. ANALYST
Read-only access for data analysis and reporting.

**Display Name**: Analista

**Permissions**:
- User Management: READ only
- Organizational Structure: READ only
- Academic Structure: READ only
- Course Management: READ only
- Planning: READ only
- Configuration: No access

#### 5. TEACHER
Faculty member who creates and manages their own planning.

**Display Name**: Docente

**Permissions**:
- Organizational Structure: READ only
- Academic Structure: READ only
- Course Management: READ, WRITE
- Planning: READ, WRITE, DELETE (own plannings)
- Configuration: No access

---

## Permission System

### Permission Categories

Permissions are organized into seven functional domains:

#### User Management
```
USER_READ          - View user information
USER_WRITE         - Create/modify users
USER_DELETE        - Delete users
```

#### Organizational Structure
```
REGIONAL_TECHNICAL_INSTITUTE_READ    - View ITR information
REGIONAL_TECHNICAL_INSTITUTE_WRITE   - Create/modify ITRs
REGIONAL_TECHNICAL_INSTITUTE_DELETE  - Delete ITRs

CAMPUS_READ    - View campus information
CAMPUS_WRITE   - Create/modify campuses
CAMPUS_DELETE  - Delete campuses
```

#### Academic Structure
```
PROGRAM_READ    - View academic programs
PROGRAM_WRITE   - Create/modify programs
PROGRAM_DELETE  - Delete programs

TERM_READ    - View academic terms
TERM_WRITE   - Create/modify terms
TERM_DELETE  - Delete terms

CURRICULAR_UNIT_READ    - View curricular units
CURRICULAR_UNIT_WRITE   - Create/modify curricular units
CURRICULAR_UNIT_DELETE  - Delete curricular units
```

#### Course Management
```
COURSE_READ    - View courses
COURSE_WRITE   - Create/modify courses
COURSE_DELETE  - Delete courses
```

#### Planning
```
PLANNING_READ    - View academic plannings
PLANNING_WRITE   - Create/modify plannings
PLANNING_DELETE  - Delete plannings
```

#### Configuration
```
CONFIGURATION_READ    - View system configuration
CONFIGURATION_WRITE   - Modify system configuration
```

### Authority Resolution

When a user authenticates, Spring Security calls `User.getAuthorities()`:

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

This method:
1. Filters only active positions
2. Extracts all authorities from each position's role
3. Removes duplicates
4. Returns a unified set of authorities

Each role provides authorities through:

```java
public Set<GrantedAuthority> getAuthorities() {
    Set<GrantedAuthority> authorities = permissions.stream()
        .map(permission -> new SimpleGrantedAuthority(permission.name()))
        .collect(Collectors.toSet());
    
    authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
    
    return authorities;
}
```

This generates:
- One authority per permission (e.g., `USER_READ`, `COURSE_READ`)
- One role authority with `ROLE_` prefix (e.g., `ROLE_ADMINISTRATOR`, `ROLE_TEACHER`)

---

## Access Control Implementation

### Controller-Level Security

Use Spring Security's `@PreAuthorize` annotation to protect endpoints.

#### Basic Syntax

```java
@PreAuthorize("hasAuthority('PERMISSION_NAME')")
@PreAuthorize("hasRole('ROLE_NAME')")
```

### hasAuthority vs hasRole

#### hasAuthority
- Checks for an exact match of the authority string
- Use for permission-based access control
- More granular and flexible
- Recommended for most use cases

```java
@PreAuthorize("hasAuthority('USER_READ')")
```

#### hasRole
- Automatically adds `ROLE_` prefix
- Use for role-based access control
- Less granular, checks entire role
- Use for role-specific features

```java
@PreAuthorize("hasRole('ADMINISTRATOR')")  // Checks for ROLE_ADMINISTRATOR
```

### Multiple Positions Scenario

Users with multiple active positions aggregate all permissions:

```java
User user = ...;
Position coordinator = new CoordinatorPosition(Role.COORDINATOR);
Position teacher = new TeacherPosition(Role.TEACHER);

user.addPosition(coordinator);
user.addPosition(teacher);

// User will have ALL permissions from both roles
user.getAuthorities();  // Returns union of COORDINATOR + TEACHER permissions
```

---

## Usage Examples

### Example 1: Single Permission

```java
@RestController
@RequestMapping("/users")
public class UserController {

    @PostMapping
    @PreAuthorize("hasAuthority('USER_WRITE')")
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest request) {
        // Only users with USER_WRITE permission can execute this
        // ADMINISTRATOR can execute
        // Other roles cannot
    }
}
```

### Example 2: Multiple Permissions with AND

```java
@RestController
@RequestMapping("/courses")
public class CourseController {

    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('COURSE_WRITE') AND hasAuthority('PLANNING_WRITE')")
    public ResponseEntity<CourseResponse> publishCourse(@PathVariable Long id) {
        // Requires BOTH permissions
        // ADMINISTRATOR, EDUCATION_MANAGER, COORDINATOR can execute
        // TEACHER has both permissions
        // ANALYST cannot execute (missing COURSE_WRITE)
    }
}
```

### Example 3: Multiple Permissions with OR

```java
@RestController
@RequestMapping("/plannings")
public class PlanningController {

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PLANNING_READ') OR hasRole('ADMINISTRATOR')")
    public ResponseEntity<PlanningResponse> getPlanning(@PathVariable Long id) {
        // Requires EITHER the permission OR the role
        // All roles except ANALYST have PLANNING_READ
        // ADMINISTRATOR can also access via role
    }
}
```

### Example 4: Multiple Roles with OR

```java
@RestController
@RequestMapping("/reports")
public class ReportController {

    @GetMapping("/advanced")
    @PreAuthorize("hasRole('ADMINISTRATOR') OR hasRole('EDUCATION_MANAGER')")
    public ResponseEntity<ReportResponse> getAdvancedReport() {
        // Only for specific roles
        // ADMINISTRATOR can execute
        // EDUCATION_MANAGER can execute
        // Other roles cannot
    }
}
```

### Example 5: Complex Expressions

```java
@RestController
@RequestMapping("/configuration")
public class ConfigurationController {

    @PutMapping("/system")
    @PreAuthorize("(hasAuthority('CONFIGURATION_WRITE') AND hasRole('ADMINISTRATOR')) OR hasRole('SUPER_ADMIN')")
    public ResponseEntity<ConfigResponse> updateSystemConfig(@RequestBody ConfigRequest request) {
        // Requires: (permission AND role) OR specific role
        // Very restrictive for critical operations
    }
}
```

### Example 6: Method Parameter Access

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
        // Checks permission AND custom business logic
        // @courseService is a Spring bean
        // #id is the path variable
        // authentication is the current user's authentication object
    }
}
```

### Example 7: Multiple Authorities

```java
@RestController
@RequestMapping("/management")
public class ManagementController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('USER_READ', 'COURSE_READ', 'PLANNING_READ')")
    public ResponseEntity<DashboardResponse> getDashboard() {
        // Requires ANY of the listed permissions
        // Almost all roles can access (except very restricted roles)
    }
}
```

### Example 8: Role-Specific Endpoints

```java
@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @GetMapping("/my-courses")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<CourseResponse>> getMyCourses() {
        // Specific feature only for teachers
        // Even ADMINISTRATOR would need a TEACHER position to access
    }
}
```

---

## Best Practices

### 1. Prefer hasAuthority for Permissions

Use `hasAuthority()` for granular permission checks:

```java
// Good
@PreAuthorize("hasAuthority('USER_WRITE')")

// Avoid (too coarse-grained)
@PreAuthorize("hasRole('ADMINISTRATOR')")  // Unless role-specific feature
```

### 2. Use hasRole for Role-Specific Features

Reserve `hasRole()` for features that are inherently tied to a role:

```java
// Good - Teacher-specific dashboard
@PreAuthorize("hasRole('TEACHER')")
public ResponseEntity<?> getTeacherDashboard() { }

// Bad - Generic CRUD operation
@PreAuthorize("hasRole('ADMINISTRATOR')")  // Use hasAuthority instead
public ResponseEntity<?> createUser() { }
```

### 3. Service-Layer Authorization for Complex Logic

For complex authorization that involves business logic:

```java
@Service
@RequiredArgsConstructor
public class CourseService {

    @PreAuthorize("hasAuthority('COURSE_WRITE')")
    public CourseResponse updateCourse(Long courseId, CourseRequest request) {
        Course course = findById(courseId);
        
        // Additional business logic validation
        User currentUser = securityService.getCurrentUser();
        if (!userHasAccessToCampus(currentUser, course.getCampus())) {
            throw new ForbiddenException("No access to this campus");
        }
        
        // Proceed with update
    }
    
    private boolean userHasAccessToCampus(User user, Campus campus) {
        return user.getPositions().stream()
            .filter(Position::getIsActive)
            .flatMap(position -> position.getCampuses().stream())
            .anyMatch(c -> c.equals(campus));
    }
}
```

### 4. Document Permission Requirements

Always document the required permissions for each endpoint:

```java
/**
 * Creates a new user in the system.
 * 
 * @param request User creation data
 * @return Created user information
 * @throws ForbiddenException if user lacks USER_WRITE permission
 */
@PostMapping
@PreAuthorize("hasAuthority('USER_WRITE')")
public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest request) {
    // Implementation
}
```

### 5. Test Permission Combinations

Always test edge cases with multiple positions:

```java
@Test
void userWithMultiplePositions_shouldHaveAggregatedPermissions() {
    User user = createUser();
    user.addPosition(createCoordinatorPosition());
    user.addPosition(createTeacherPosition());
    
    Set<String> authorities = user.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toSet());
    
    // Should have permissions from both roles
    assertTrue(authorities.contains("COURSE_WRITE"));
    assertTrue(authorities.contains("PLANNING_WRITE"));
}
```

### 6. Avoid Hardcoding Roles in Business Logic

```java
// Bad
if (user.getPositions().stream()
    .anyMatch(p -> p.getRole() == Role.ADMINISTRATOR)) {
    // Do something
}

// Good
if (SecurityContextHolder.getContext()
    .getAuthentication()
    .getAuthorities().stream()
    .anyMatch(a -> a.getAuthority().equals("USER_DELETE"))) {
    // Do something
}
```

### 7. Use Consistent Naming Conventions

Follow the established pattern:

```
ENTITY_OPERATION
USER_WRITE
COURSE_READ
PLANNING_DELETE
```

Not:
```
WRITE_USER  // Wrong order
read_course  // Wrong case
PlanningDel  // Inconsistent abbreviation
```

---

## Troubleshooting

### Issue 1: Permission Denied Despite Having Role

**Symptom**: User with ADMINISTRATOR role gets 403 Forbidden

**Possible Causes**:
1. Position is marked as `isActive = false`
2. Using `hasAuthority('ROLE_ADMINISTRATOR')` instead of `hasRole('ADMINISTRATOR')`
3. Typo in permission name

**Solution**:
```java
// Check user's actual authorities
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
auth.getAuthorities().forEach(System.out::println);

// Verify position is active
user.getPositions().forEach(p -> 
    System.out.println(p.getRole() + " - Active: " + p.getIsActive())
);
```

### Issue 2: Multiple Positions Not Working

**Symptom**: User has two positions but only one role's permissions work

**Possible Causes**:
1. One position is inactive
2. Issue with `getAuthorities()` implementation
3. Caching problem

**Solution**:
- Verify both positions are active
- Clear security context cache
- Check `distinct()` is working in authority aggregation

### Issue 3: hasRole Not Working

**Symptom**: `hasRole('ADMINISTRATOR')` always denies access

**Possible Cause**: Missing `ROLE_` prefix in authorities

**Solution**:
Verify `Role.getAuthorities()` adds the prefix:
```java
authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
```

### Issue 4: Campus-Specific Access

**Symptom**: User should only access courses from their campus

**Solution**: Implement service-layer validation:
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
        throw new ForbiddenException("No access to this campus");
    }
    
    // Proceed
}
```

---

## Summary

The UTEC Teaching Planner implements a flexible, multi-level authorization system:

- Users can have multiple positions with different roles
- Each role contains a set of granular permissions
- Permissions are checked at the controller level using `@PreAuthorize`
- Complex business logic authorization is handled at the service layer
- The system supports both permission-based and role-based access control

This design allows for:
- Fine-grained access control
- Flexible role assignment per campus
- Easy permission management
- Clear separation of concerns
- Testable authorization logic

For questions or clarifications, refer to the source code in:
- `edu.utec.planificador.enumeration.Role`
- `edu.utec.planificador.entity.User`
- `edu.utec.planificador.entity.Position`
