package edu.utec.planificador.config;

import edu.utec.planificador.security.JwtTokenProvider;
import edu.utec.planificador.util.CookieUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import static org.mockito.Mockito.mock;

/**
 * Configuración de seguridad para tests.
 * Proporciona usuarios en memoria y configuración simplificada.
 */
@TestConfiguration
public class TestSecurityConfig {

    /**
     * PasswordEncoder con baja complejidad (4 rounds) para tests más rápidos.
     * Producción usa 12 rounds, pero para tests 4 es suficiente (256x más rápido).
     */
    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder(4);
    }

    /**
     * UserDetailsService con usuarios en memoria para tests.
     * Incluye usuarios con diferentes roles para probar autorización.
     */
    @Bean
    @Primary
    public UserDetailsService testUserDetailsService() {
        // Usuario administrador
        var admin = org.springframework.security.core.userdetails.User.builder()
                .username("admin@utec.edu.uy")
                .password(testPasswordEncoder().encode("admin123"))
                .authorities(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_EDUCATION_MANAGER"),
                        new SimpleGrantedAuthority("ROLE_TEACHER")
                )
                .build();

        // Usuario manager
        var manager = org.springframework.security.core.userdetails.User.builder()
                .username("manager@utec.edu.uy")
                .password(testPasswordEncoder().encode("manager123"))
                .authorities(
                        new SimpleGrantedAuthority("ROLE_EDUCATION_MANAGER"),
                        new SimpleGrantedAuthority("ROLE_TEACHER")
                )
                .build();

        // Usuario teacher
        var teacher = org.springframework.security.core.userdetails.User.builder()
                .username("teacher@utec.edu.uy")
                .password(testPasswordEncoder().encode("teacher123"))
                .authorities(new SimpleGrantedAuthority("ROLE_TEACHER"))
                .build();

        // Usuario student
        var student = org.springframework.security.core.userdetails.User.builder()
                .username("student@utec.edu.uy")
                .password(testPasswordEncoder().encode("student123"))
                .authorities(new SimpleGrantedAuthority("ROLE_STUDENT"))
                .build();

        return new InMemoryUserDetailsManager(admin, manager, teacher, student);
    }

    /**
     * Mock de JwtTokenProvider para evitar dependencias de JWT en tests de controladores.
     * En tests @WebMvcTest, este bean es necesario pero no se usa realmente.
     */
    @Bean
    @Primary
    public JwtTokenProvider testJwtTokenProvider() {
        return mock(JwtTokenProvider.class);
    }

    /**
     * Mock de CookieUtil para evitar dependencias de manejo de cookies en tests.
     * En tests @WebMvcTest, este bean es necesario pero no se usa realmente.
     */
    @Bean
    @Primary
    public CookieUtil testCookieUtil() {
        return mock(CookieUtil.class);
    }
}

