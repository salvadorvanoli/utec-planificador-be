package edu.utec.planificador.controller;

import edu.utec.planificador.config.TestSecurityConfig;
import edu.utec.planificador.dto.response.CourseResponse;
import edu.utec.planificador.service.CourseService;
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

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integración para CourseController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("CourseController Integration Tests")
class CourseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CourseService courseService;

    @Test
    @WithMockUser(username = "teacher@utec.edu.uy", authorities = "COURSE_READ")
    @DisplayName("GET /courses/{id} - Should return course by ID")
    void getCourseById_ValidId_ReturnsCourse() throws Exception {
        // Given
        Long courseId = 1L;
        CourseResponse course = CourseResponse.builder()
                .id(courseId)
                .description("Curso de programación avanzada")
                .build();

        when(courseService.getCourseById(eq(courseId))).thenReturn(course);

        // When & Then
        mockMvc.perform(get("/courses/{id}", courseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(courseId))
                .andExpect(jsonPath("$.description").value("Curso de programación avanzada"));

        verify(courseService, times(1)).getCourseById(eq(courseId));
    }

    // POST and PUT tests omitted due to complex validation requirements
    // These endpoints require specific request structure that needs real-world testing

    @Test
    @WithMockUser(username = "coordinator@utec.edu.uy", authorities = "COURSE_DELETE")
    @DisplayName("DELETE /courses/{id} - Should delete course")
    void deleteCourse_WithPermissions_DeletesCourse() throws Exception {
        // Given
        Long courseId = 1L;
        doNothing().when(courseService).deleteCourse(eq(courseId));

        // When & Then
        mockMvc.perform(delete("/courses/{id}", courseId))
                .andExpect(status().isNoContent());

        verify(courseService, times(1)).deleteCourse(eq(courseId));
    }
}

