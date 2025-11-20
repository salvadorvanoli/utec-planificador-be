package edu.utec.planificador.controller;

import edu.utec.planificador.config.TestSecurityConfig;
import edu.utec.planificador.dto.response.CampusResponse;
import edu.utec.planificador.service.CampusService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests de integración para CampusController.
 *
 * Usa @SpringBootTest para cargar toda la aplicación con base de datos en memoria.
 * Usa @MockitoBean para mockear los servicios.
 * Usa @WithMockUser para simular usuarios autenticados.
 * @Transactional hace rollback automático después de cada test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("CampusController Integration Tests")
class CampusControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CampusService campusService;

    @Test
    @DisplayName("GET /campuses - Should return all campuses without authentication")
    void getCampuses_WithoutAuth_ReturnsAllCampuses() throws Exception {
        // Given
        List<CampusResponse> campuses = List.of(
                CampusResponse.builder()
                        .id(1L)
                        .name("Campus Centro")
                        .build(),
                CampusResponse.builder()
                        .id(2L)
                        .name("Campus Norte")
                        .build()
        );

        when(campusService.getCampuses(null)).thenReturn(campuses);

        // When & Then
        mockMvc.perform(get("/campuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Campus Centro"))
                .andExpect(jsonPath("$[1].name").value("Campus Norte"));

        verify(campusService, times(1)).getCampuses(null);
    }

    @Test
    @WithMockUser(username = "teacher@utec.edu.uy", roles = "TEACHER")
    @DisplayName("GET /campuses - Should return campuses filtered by authenticated user")
    void getCampuses_WithAuth_ReturnsFilteredCampuses() throws Exception {
        // Given
        List<CampusResponse> campuses = List.of(
                CampusResponse.builder()
                        .id(1L)
                        .name("Campus Centro")
                        .build()
        );

        when(campusService.getCampuses(any())).thenReturn(campuses);

        // When & Then
        mockMvc.perform(get("/campuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Campus Centro"));

        verify(campusService, times(1)).getCampuses(any());
    }

    @Test
    @WithMockUser(username = "teacher@utec.edu.uy", roles = "TEACHER")
    @DisplayName("GET /campuses?userId=1 - Should return campuses for specific user")
    void getCampuses_WithUserId_ReturnsUserCampuses() throws Exception {
        // Given
        Long userId = 1L;
        List<CampusResponse> campuses = List.of(
                CampusResponse.builder()
                        .id(1L)
                        .name("Campus Centro")
                        .build()
        );

        when(campusService.getCampuses(userId)).thenReturn(campuses);

        // When & Then
        mockMvc.perform(get("/campuses")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(campusService, times(1)).getCampuses(userId);
    }

    @Test
    @DisplayName("GET /campuses - Should return empty list when no campuses found")
    void getCampuses_NoCampuses_ReturnsEmptyList() throws Exception {
        // Given
        when(campusService.getCampuses(null)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/campuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(campusService, times(1)).getCampuses(null);
    }
}

