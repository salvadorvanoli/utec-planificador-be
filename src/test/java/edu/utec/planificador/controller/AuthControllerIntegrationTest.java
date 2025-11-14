package edu.utec.planificador.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.utec.planificador.config.TestSecurityConfig;
import edu.utec.planificador.dto.request.LoginRequest;
import edu.utec.planificador.dto.response.AuthResponse;
import edu.utec.planificador.dto.response.UserResponse;
import edu.utec.planificador.service.AuthenticationService;
import edu.utec.planificador.util.CookieUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para AuthController.
 *
 * Incluye ejemplos de:
 * - POST requests con JSON
 * - Validación de request bodies
 * - Autenticación con @WithMockUser
 * - Manejo de cookies
 *
 * Usa @SpringBootTest para cargar toda la aplicación con base de datos en memoria.
 * @Transactional hace rollback automático después de cada test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController Integration Tests")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private CookieUtil cookieUtil;

    @Test
    @DisplayName("POST /auth/login - Should login successfully with valid credentials")
    void login_ValidCredentials_ReturnsSuccess() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("teacher@utec.edu.uy");
        loginRequest.setPassword("teacher123");

        AuthResponse authResponse = AuthResponse.builder()
                .email("teacher@utec.edu.uy")
                .accessToken("mock-jwt-token")
                .tokenType("Bearer")
                .expiresIn(3600L)
                .build();

        when(authenticationService.login(any(LoginRequest.class)))
                .thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("teacher@utec.edu.uy"))
                .andExpect(jsonPath("$.accessToken").value("mock-jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        verify(authenticationService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /auth/login - Should return 400 when email is null")
    void login_NullEmail_ReturnsBadRequest() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(null);
        loginRequest.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /auth/login - Should return 400 when password is null")
    void login_NullPassword_ReturnsBadRequest() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("teacher@utec.edu.uy");
        loginRequest.setPassword(null);

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("POST /auth/login - Should return 400 when email format is invalid")
    void login_InvalidEmailFormat_ReturnsBadRequest() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("invalid-email");
        loginRequest.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).login(any(LoginRequest.class));
    }

    @Test
    @WithMockUser(username = "teacher@utec.edu.uy", roles = "TEACHER")
    @DisplayName("GET /auth/me - Should return current user when authenticated")
    void getCurrentUser_Authenticated_ReturnsUser() throws Exception {
        // Given
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .email("teacher@utec.edu.uy")
                .fullName("John Doe")
                .build();

        when(authenticationService.getCurrentUser()).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("teacher@utec.edu.uy"))
                .andExpect(jsonPath("$.fullName").value("John Doe"));

        verify(authenticationService, times(1)).getCurrentUser();
    }

    @Test
    @DisplayName("GET /auth/me - Should return 401 when not authenticated")
    void getCurrentUser_NotAuthenticated_ReturnsUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());

        verify(authenticationService, never()).getCurrentUser();
    }
}

