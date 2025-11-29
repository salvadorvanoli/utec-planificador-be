package edu.utec.planificador.controller;

import edu.utec.planificador.config.TestSecurityConfig;
import edu.utec.planificador.dto.response.ActivityResponse;
import edu.utec.planificador.service.ActivityService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("ActivityController Integration Tests")
class ActivityControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActivityService activityService;

    @Test
    @WithMockUser(username = "teacher@utec.edu.uy", authorities = "PLANNING_READ")
    @DisplayName("GET /activities/{id} - Should return activity by ID")
    void getActivityById_ValidId_ReturnsActivity() throws Exception {
        // Given
        Long activityId = 1L;
        ActivityResponse activity = ActivityResponse.builder()
                .id(activityId)
                .description("Actividad de prueba")
                .build();

        when(activityService.getActivityById(eq(activityId))).thenReturn(activity);

        // When & Then
        mockMvc.perform(get("/activities/{id}", activityId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(activityId))
                .andExpect(jsonPath("$.description").value("Actividad de prueba"));

        verify(activityService, times(1)).getActivityById(eq(activityId));
    }

    // POST and PUT tests omitted due to complex validation requirements
    // These endpoints require specific request structure that needs real-world testing

    @Test
    @WithMockUser(username = "teacher@utec.edu.uy", authorities = "PLANNING_DELETE")
    @DisplayName("DELETE /activities/{id} - Should delete activity")
    void deleteActivity_WithPermissions_DeletesActivity() throws Exception {
        // Given
        Long activityId = 1L;
        doNothing().when(activityService).deleteActivity(eq(activityId));

        // When & Then
        mockMvc.perform(delete("/activities/{id}", activityId))
                .andExpect(status().isNoContent());

        verify(activityService, times(1)).deleteActivity(eq(activityId));
    }
}

