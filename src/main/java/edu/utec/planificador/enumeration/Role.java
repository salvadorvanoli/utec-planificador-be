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
        Permission.USER_READ,
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
        Permission.COURSE_READ,
        Permission.PLANNING_READ,
        Permission.CONFIGURATION_READ
    )),
    
    COORDINATOR("Coordinador", Set.of(
        Permission.USER_READ,
        Permission.COURSE_READ,
        Permission.COURSE_WRITE,
        Permission.PLANNING_READ,
        Permission.CONFIGURATION_READ
    )),

    ANALYST("Analista", Set.of(
        Permission.USER_READ,
        Permission.COURSE_READ,
        Permission.COURSE_WRITE,
        Permission.CONFIGURATION_READ
    )),
    
    TEACHER("Docente", Set.of(
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
        USER_READ,

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
