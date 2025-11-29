package edu.utec.planificador.controller;

import edu.utec.planificador.config.TestSecurityConfig;
import edu.utec.planificador.dto.response.UserBasicResponse;
import edu.utec.planificador.dto.response.UserPositionsResponse;
import edu.utec.planificador.enumeration.Role;
import edu.utec.planificador.service.UserPositionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integración para UserController.
 *
 * Prueba los endpoints:
 * - GET /users/positions (autenticado)
 * - GET /users/teachers (público)
 * - GET /users (autenticado con permiso)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("UserController Integration Tests")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserPositionService userPositionService;

    @Test
    @WithMockUser(username = "teacher@utec.edu.uy", roles = "TEACHER")
    @DisplayName("GET /users/positions - Should return current user positions when authenticated")
    void getCurrentUserPositions_Authenticated_ReturnsPositions() throws Exception {
        // Given
        UserPositionsResponse response = UserPositionsResponse.builder()
                .userId(1L)
                .email("teacher@utec.edu.uy")
                .fullName("John Doe")
                .positions(List.of())
                .build();

        when(userPositionService.getCurrentUserPositions()).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/users/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("teacher@utec.edu.uy"))
                .andExpect(jsonPath("$.fullName").value("John Doe"));

        verify(userPositionService, times(1)).getCurrentUserPositions();
    }

    @Test
    @DisplayName("GET /users/positions - Should return 401 when not authenticated")
    void getCurrentUserPositions_NotAuthenticated_ReturnsUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/users/positions"))
                .andExpect(status().isUnauthorized());

        verify(userPositionService, never()).getCurrentUserPositions();
    }

    @Test
    @DisplayName("GET /users/teachers - Should return all teachers without campus filter")
    void getTeachers_WithoutCampusFilter_ReturnsAllTeachers() throws Exception {
        // Given
        List<UserBasicResponse> teachers = List.of(
                UserBasicResponse.builder()
                        .id(1L)
                        .email("teacher1@utec.edu.uy")
                        .fullName("Teacher One")
                        .build(),
                UserBasicResponse.builder()
                        .id(2L)
                        .email("teacher2@utec.edu.uy")
                        .fullName("Teacher Two")
                        .build()
        );

        when(userPositionService.getUsers(eq(Role.TEACHER), eq(null), eq(null))).thenReturn(teachers);

        // When & Then
        mockMvc.perform(get("/users/teachers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fullName").value("Teacher One"))
                .andExpect(jsonPath("$[1].fullName").value("Teacher Two"));

        verify(userPositionService, times(1)).getUsers(eq(Role.TEACHER), eq(null), eq(null));
    }

    @Test
    @DisplayName("GET /users/teachers?campusId=1 - Should return teachers filtered by campus")
    void getTeachers_WithCampusFilter_ReturnsFilteredTeachers() throws Exception {
        // Given
        Long campusId = 1L;
        List<UserBasicResponse> teachers = List.of(
                UserBasicResponse.builder()
                        .id(1L)
                        .email("teacher1@utec.edu.uy")
                        .fullName("Teacher One")
                        .build()
        );

        when(userPositionService.getUsers(eq(Role.TEACHER), eq(campusId), eq(null))).thenReturn(teachers);

        // When & Then
        mockMvc.perform(get("/users/teachers")
                        .param("campusId", campusId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fullName").value("Teacher One"));

        verify(userPositionService, times(1)).getUsers(eq(Role.TEACHER), eq(campusId), eq(null));
    }

    @Test
    @DisplayName("GET /users/teachers - Should return empty list when no teachers found")
    void getTeachers_NoTeachers_ReturnsEmptyList() throws Exception {
        // Given
        when(userPositionService.getUsers(eq(Role.TEACHER), eq(null), eq(null))).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/users/teachers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(userPositionService, times(1)).getUsers(eq(Role.TEACHER), eq(null), eq(null));
    }

    @Test
    @WithMockUser(username = "admin@utec.edu.uy", authorities = "USER_READ")
    @DisplayName("GET /users - Should return all users when no filters provided")
    void getUsers_NoFilters_ReturnsAllUsers() throws Exception {
        // Given
        List<UserBasicResponse> users = List.of(
                UserBasicResponse.builder()
                        .id(1L)
                        .email("user1@utec.edu.uy")
                        .fullName("User One")
                        .build(),
                UserBasicResponse.builder()
                        .id(2L)
                        .email("user2@utec.edu.uy")
                        .fullName("User Two")
                        .build()
        );

        when(userPositionService.getUsers(eq(null), eq(null), eq(null))).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(userPositionService, times(1)).getUsers(eq(null), eq(null), eq(null));
    }

    @Test
    @WithMockUser(username = "admin@utec.edu.uy", authorities = "USER_READ")
    @DisplayName("GET /users?role=COORDINATOR - Should return users filtered by role")
    void getUsers_WithRoleFilter_ReturnsFilteredUsers() throws Exception {
        // Given
        List<UserBasicResponse> users = List.of(
                UserBasicResponse.builder()
                        .id(1L)
                        .email("coordinator@utec.edu.uy")
                        .fullName("Coordinator One")
                        .build()
        );

        when(userPositionService.getUsers(eq(Role.COORDINATOR), eq(null), eq(null))).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/users")
                        .param("role", "COORDINATOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(userPositionService, times(1)).getUsers(eq(Role.COORDINATOR), eq(null), eq(null));
    }

    @Test
    @WithMockUser(username = "user@utec.edu.uy", roles = "TEACHER")
    @DisplayName("GET /users - Should return 403 when user lacks USER_READ permission")
    void getUsers_WithoutPermission_ReturnsForbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());

        verify(userPositionService, never()).getUsers(any(), any(), any());
    }
}

