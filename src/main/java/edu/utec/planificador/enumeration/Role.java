package edu.utec.planificador.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum Role {

    ADMINISTRATOR("Administrador", Set.of(
        Permission.USER_CREATE,
        Permission.USER_READ,
        Permission.USER_UPDATE,
        Permission.USER_DELETE,
        Permission.REGIONAL_TECHNICAL_INSTITUTE_READ,
        Permission.REGIONAL_TECHNICAL_INSTITUTE_WRITE,
        Permission.REGIONAL_TECHNICAL_INSTITUTE_DELETE,
        Permission.CAMPUS_READ,
        Permission.CAMPUS_WRITE,
        Permission.CAMPUS_DELETE,
        Permission.TERM_READ,
        Permission.TERM_WRITE,
        Permission.TERM_DELETE,
        Permission.PROGRAM_READ,
        Permission.PROGRAM_WRITE,
        Permission.PROGRAM_DELETE,
        Permission.CURRICULAR_UNIT_READ,
        Permission.CURRICULAR_UNIT_WRITE,
        Permission.CURRICULAR_UNIT_DELETE,
        Permission.COURSE_READ,
        Permission.COURSE_WRITE,
        Permission.COURSE_DELETE,
        Permission.PLANNING_READ,
        Permission.PLANNING_WRITE,
        Permission.PLANNING_DELETE,
        Permission.CONFIGURATION_READ,
        Permission.CONFIGURATION_WRITE
    )),
    
    EDUCATION_MANAGER("Responsable de Educación", Set.of(
        Permission.USER_READ,
        Permission.REGIONAL_TECHNICAL_INSTITUTE_READ,
        Permission.CAMPUS_READ,
        Permission.PROGRAM_READ,
        Permission.TERM_READ,
        Permission.CURRICULAR_UNIT_READ,
        Permission.COURSE_READ,
        Permission.COURSE_WRITE,
        Permission.PLANNING_READ,
        Permission.PLANNING_WRITE,
        Permission.CONFIGURATION_READ
    )),
    
    COORDINATOR("Coordinador", Set.of(
        Permission.USER_READ,
        Permission.REGIONAL_TECHNICAL_INSTITUTE_READ,
        Permission.CAMPUS_READ,
        Permission.PROGRAM_READ,
        Permission.TERM_READ,
        Permission.CURRICULAR_UNIT_READ,
        Permission.COURSE_READ,
        Permission.COURSE_WRITE,
        Permission.PLANNING_READ,
        Permission.PLANNING_WRITE,
        Permission.CONFIGURATION_READ
    )),

    ANALYST("Analista", Set.of(
        Permission.USER_READ,
        Permission.REGIONAL_TECHNICAL_INSTITUTE_READ,
        Permission.CAMPUS_READ,
        Permission.PROGRAM_READ,
        Permission.TERM_READ,
        Permission.CURRICULAR_UNIT_READ,
        Permission.COURSE_READ,
        Permission.PLANNING_READ
    )),
    
    TEACHER("Docente", Set.of(
        Permission.REGIONAL_TECHNICAL_INSTITUTE_READ,
        Permission.CAMPUS_READ,
        Permission.PROGRAM_READ,
        Permission.TERM_READ,
        Permission.CURRICULAR_UNIT_READ,
        Permission.COURSE_READ,
        Permission.COURSE_WRITE,
        Permission.PLANNING_READ,
        Permission.PLANNING_WRITE,
        Permission.PLANNING_DELETE
    ));

    private final String displayName;
    private final Set<Permission> permissions;

    public Set<GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = permissions.stream()
            .map(permission -> new SimpleGrantedAuthority(permission.name()))
            .collect(Collectors.toSet());
        
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        
        return authorities;
    }

    public enum Permission {
        USER_CREATE,
        USER_READ,
        USER_UPDATE,
        USER_DELETE,

        // Organizational structure
        REGIONAL_TECHNICAL_INSTITUTE_READ,
        REGIONAL_TECHNICAL_INSTITUTE_WRITE,
        REGIONAL_TECHNICAL_INSTITUTE_DELETE,
        CAMPUS_READ,
        CAMPUS_WRITE,
        CAMPUS_DELETE,

        // Academic structure
        PROGRAM_READ,
        PROGRAM_WRITE,
        PROGRAM_DELETE,
        TERM_READ,
        TERM_WRITE,
        TERM_DELETE,
        CURRICULAR_UNIT_READ,
        CURRICULAR_UNIT_WRITE,
        CURRICULAR_UNIT_DELETE,

        // Courses and planning
        COURSE_READ,
        COURSE_WRITE,
        COURSE_DELETE,
        PLANNING_READ,
        PLANNING_WRITE,
        PLANNING_DELETE,
        
        CONFIGURATION_READ,
        CONFIGURATION_WRITE
    }
}
