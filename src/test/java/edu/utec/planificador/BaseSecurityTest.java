package edu.utec.planificador;

import edu.utec.planificador.config.TestSecurityConfig;
import edu.utec.planificador.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Clase base para tests de integración con seguridad.
 * Proporciona métodos utilitarios para simular usuarios autenticados.
 *
 * Uso:
 * <pre>
 * class MyServiceTest extends BaseSecurityTest {
 *
 *     @Test
 *     void testWithAuthenticatedUser() {
 *         authenticateAsTeacher();
 *         // Tu test aquí
 *     }
 * }
 * </pre>
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
public abstract class BaseSecurityTest {

    protected User mockUser;

    @BeforeEach
    void setUpSecurity() {
        // Limpiar contexto de seguridad antes de cada test
        SecurityContextHolder.clearContext();
    }

    /**
     * Autentica un usuario como TEACHER.
     */
    protected void authenticateAsTeacher() {
        authenticateAs("teacher@utec.edu.uy", 1L, "ROLE_TEACHER");
    }

    /**
     * Autentica un usuario como EDUCATION_MANAGER.
     */
    protected void authenticateAsManager() {
        authenticateAs("manager@utec.edu.uy", 2L, "ROLE_EDUCATION_MANAGER");
    }

    /**
     * Autentica un usuario como ADMIN.
     */
    protected void authenticateAsAdmin() {
        authenticateAs("admin@utec.edu.uy", 3L, "ROLE_ADMIN");
    }

    /**
     * Autentica un usuario como STUDENT.
     */
    protected void authenticateAsStudent() {
        authenticateAs("student@utec.edu.uy", 4L, "ROLE_STUDENT");
    }

    /**
     * Autentica un usuario con rol personalizado.
     */
    @SuppressWarnings("unchecked")
    protected void authenticateAs(String email, Long userId, String... roles) {
        mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(userId);
        when(mockUser.getUsername()).thenReturn(email);
        when(mockUser.isEnabled()).thenReturn(true);
        when(mockUser.isAccountNonExpired()).thenReturn(true);
        when(mockUser.isAccountNonLocked()).thenReturn(true);
        when(mockUser.isCredentialsNonExpired()).thenReturn(true);

        List<SimpleGrantedAuthority> authorities = List.of(roles).stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        when(mockUser.getAuthorities()).thenReturn((Collection) authorities);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                mockUser,
                null,
                authorities
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * Obtiene el usuario autenticado actual del contexto de seguridad.
     */
    protected User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * Limpia el contexto de seguridad.
     */
    protected void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
}

