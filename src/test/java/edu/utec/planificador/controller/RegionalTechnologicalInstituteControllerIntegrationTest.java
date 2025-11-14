package edu.utec.planificador.controller;

import edu.utec.planificador.config.TestSecurityConfig;
import edu.utec.planificador.dto.response.RegionalTechnologicalInstituteResponse;
import edu.utec.planificador.service.RegionalTechnologicalInstituteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("RegionalTechnologicalInstituteController Integration Tests")
class RegionalTechnologicalInstituteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegionalTechnologicalInstituteService rtiService;

    @Test
    @DisplayName("GET /regional-technological-institutes - Should return all RTIs without authentication")
    void getRegionalTechnologicalInstitutes_WithoutUserId_ReturnsAllRTIs() throws Exception {
        // Given
        List<RegionalTechnologicalInstituteResponse> rtis = List.of(
                RegionalTechnologicalInstituteResponse.builder()
                        .id(1L)
                        .name("ITR Centro Sur")
                        .build(),
                RegionalTechnologicalInstituteResponse.builder()
                        .id(2L)
                        .name("ITR Norte")
                        .build()
        );

        when(rtiService.getRegionalTechnologicalInstitutes(null)).thenReturn(rtis);

        // When & Then
        mockMvc.perform(get("/regional-technological-institutes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("ITR Centro Sur"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("ITR Norte"));

        verify(rtiService, times(1)).getRegionalTechnologicalInstitutes(null);
    }

    @Test
    @DisplayName("GET /regional-technological-institutes?userId={id} - Should return RTIs filtered by user")
    void getRegionalTechnologicalInstitutes_WithUserId_ReturnsFilteredRTIs() throws Exception {
        // Given
        Long userId = 1L;
        List<RegionalTechnologicalInstituteResponse> rtis = List.of(
                RegionalTechnologicalInstituteResponse.builder()
                        .id(1L)
                        .name("ITR Centro Sur")
                        .build()
        );

        when(rtiService.getRegionalTechnologicalInstitutes(userId)).thenReturn(rtis);

        // When & Then
        mockMvc.perform(get("/regional-technological-institutes")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("ITR Centro Sur"));

        verify(rtiService, times(1)).getRegionalTechnologicalInstitutes(userId);
    }

    @Test
    @DisplayName("GET /regional-technological-institutes - Should return empty list when no RTIs found")
    void getRegionalTechnologicalInstitutes_NoRTIsFound_ReturnsEmptyList() throws Exception {
        // Given
        when(rtiService.getRegionalTechnologicalInstitutes(null)).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/regional-technological-institutes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(rtiService, times(1)).getRegionalTechnologicalInstitutes(null);
    }
}
