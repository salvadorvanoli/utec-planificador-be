package edu.utec.planificador.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.utec.planificador.config.TestSecurityConfig;
import edu.utec.planificador.dto.response.CurricularUnitResponse;
import edu.utec.planificador.service.CurricularUnitService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.ArgumentMatchers.eq;

/**
 * Tests de integración para CurricularUnitController.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("CurricularUnitController Integration Tests")
class CurricularUnitControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CurricularUnitService curricularUnitService;

    @Test
    @DisplayName("GET /curricular-units/{id} - Should return curricular unit by ID")
    void getCurricularUnitById_ValidId_ReturnsUnit() throws Exception {
        // Given
        Long unitId = 1L;
        CurricularUnitResponse unit = CurricularUnitResponse.builder()
                .id(unitId)
                .name("Ingeniería de Software")
                .credits(8)
                .build();

        when(curricularUnitService.getCurricularUnitById(eq(unitId))).thenReturn(unit);

        // When & Then
        mockMvc.perform(get("/curricular-units/{id}", unitId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(unitId))
                .andExpect(jsonPath("$.name").value("Ingeniería de Software"))
                .andExpect(jsonPath("$.credits").value(8));

        verify(curricularUnitService, times(1)).getCurricularUnitById(eq(unitId));
    }
}

