package edu.utec.planificador.controller;

import edu.utec.planificador.config.TestSecurityConfig;
import edu.utec.planificador.dto.response.EnumResponse;
import edu.utec.planificador.service.EnumService;
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
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integración para EnumController.
 *
 * Prueba los endpoints públicos de enumeraciones:
 * - GET /enums (todos)
 * - GET /enums/domain-areas
 * - GET /enums/professional-competencies
 * - etc.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("EnumController Integration Tests")
class EnumControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnumService enumService;

    @Test
    @DisplayName("GET /enums - Should return all enumerations")
    void getAllEnums_ReturnsAllEnumerations() throws Exception {
        // Given
        Map<String, List<EnumResponse>> allEnums = Map.of(
                "domainAreas", List.of(
                        new EnumResponse("MATHEMATICS", "Matemáticas")
                ),
                "roles", List.of(
                        new EnumResponse("TEACHER", "Docente")
                )
        );

        when(enumService.getAllEnums()).thenReturn(allEnums);

        // When & Then
        mockMvc.perform(get("/enums"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.domainAreas").isArray())
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(header().exists("Cache-Control"));

        verify(enumService, times(1)).getAllEnums();
    }

    @Test
    @DisplayName("GET /enums/domain-areas - Should return domain areas")
    void getDomainAreas_ReturnsDomainAreas() throws Exception {
        // Given
        List<EnumResponse> domainAreas = List.of(
                new EnumResponse("MATHEMATICS", "Matemáticas"),
                new EnumResponse("PHYSICS", "Física")
        );

        when(enumService.getDomainAreas()).thenReturn(domainAreas);

        // When & Then
        mockMvc.perform(get("/enums/domain-areas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].value").value("MATHEMATICS"))
                .andExpect(jsonPath("$[0].displayValue").value("Matemáticas"));

        verify(enumService, times(1)).getDomainAreas();
    }

    @Test
    @DisplayName("GET /enums/professional-competencies - Should return professional competencies")
    void getProfessionalCompetencies_ReturnsProfessionalCompetencies() throws Exception {
        // Given
        List<EnumResponse> competencies = List.of(
                new EnumResponse("LEADERSHIP", "Liderazgo")
        );

        when(enumService.getProfessionalCompetencies()).thenReturn(competencies);

        // When & Then
        mockMvc.perform(get("/enums/professional-competencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(enumService, times(1)).getProfessionalCompetencies();
    }

    @Test
    @DisplayName("GET /enums/transversal-competencies - Should return transversal competencies")
    void getTransversalCompetencies_ReturnsTransversalCompetencies() throws Exception {
        // Given
        List<EnumResponse> competencies = List.of(
                new EnumResponse("COMMUNICATION", "Comunicación")
        );

        when(enumService.getTransversalCompetencies()).thenReturn(competencies);

        // When & Then
        mockMvc.perform(get("/enums/transversal-competencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(enumService, times(1)).getTransversalCompetencies();
    }

    @Test
    @DisplayName("GET /enums/cognitive-processes - Should return cognitive processes")
    void getCognitiveProcesses_ReturnsCognitiveProcesses() throws Exception {
        // Given
        List<EnumResponse> processes = List.of(
                new EnumResponse("REMEMBER", "Recordar")
        );

        when(enumService.getCognitiveProcesses()).thenReturn(processes);

        // When & Then
        mockMvc.perform(get("/enums/cognitive-processes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(enumService, times(1)).getCognitiveProcesses();
    }

    @Test
    @DisplayName("GET /enums/teaching-strategies - Should return teaching strategies")
    void getTeachingStrategies_ReturnsTeachingStrategies() throws Exception {
        // Given
        List<EnumResponse> strategies = List.of(
                new EnumResponse("LECTURE", "Clase magistral")
        );

        when(enumService.getTeachingStrategies()).thenReturn(strategies);

        // When & Then
        mockMvc.perform(get("/enums/teaching-strategies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(enumService, times(1)).getTeachingStrategies();
    }

    @Test
    @DisplayName("GET /enums/learning-resources - Should return learning resources")
    void getLearningResources_ReturnsLearningResources() throws Exception {
        // Given
        List<EnumResponse> resources = List.of(
                new EnumResponse("TEXTBOOK", "Libro de texto")
        );

        when(enumService.getLearningResources()).thenReturn(resources);

        // When & Then
        mockMvc.perform(get("/enums/learning-resources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(enumService, times(1)).getLearningResources();
    }

    @Test
    @DisplayName("GET /enums/delivery-formats - Should return delivery formats")
    void getDeliveryFormats_ReturnsDeliveryFormats() throws Exception {
        // Given
        List<EnumResponse> formats = List.of(
                new EnumResponse("IN_PERSON", "Presencial")
        );

        when(enumService.getDeliveryFormats()).thenReturn(formats);

        // When & Then
        mockMvc.perform(get("/enums/delivery-formats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(enumService, times(1)).getDeliveryFormats();
    }

    @Test
    @DisplayName("GET /enums/learning-modalities - Should return learning modalities")
    void getLearningModalities_ReturnsLearningModalities() throws Exception {
        // Given
        List<EnumResponse> modalities = List.of(
                new EnumResponse("SYNCHRONOUS", "Sincrónico")
        );

        when(enumService.getLearningModalities()).thenReturn(modalities);

        // When & Then
        mockMvc.perform(get("/enums/learning-modalities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(enumService, times(1)).getLearningModalities();
    }

    @Test
    @DisplayName("GET /enums/partial-grading-systems - Should return partial grading systems")
    void getPartialGradingSystems_ReturnsPartialGradingSystems() throws Exception {
        // Given
        List<EnumResponse> systems = List.of(
                new EnumResponse("NUMERIC", "Numérico")
        );

        when(enumService.getPartialGradingSystems()).thenReturn(systems);

        // When & Then
        mockMvc.perform(get("/enums/partial-grading-systems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(enumService, times(1)).getPartialGradingSystems();
    }

    @Test
    @DisplayName("GET /enums/shifts - Should return shifts")
    void getShifts_ReturnsShifts() throws Exception {
        // Given
        List<EnumResponse> shifts = List.of(
                new EnumResponse("MORNING", "Mañana")
        );

        when(enumService.getShifts()).thenReturn(shifts);

        // When & Then
        mockMvc.perform(get("/enums/shifts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(enumService, times(1)).getShifts();
    }

    @Test
    @DisplayName("GET /enums/sustainable-development-goals - Should return SDGs")
    void getSustainableDevelopmentGoals_ReturnsSDGs() throws Exception {
        // Given
        List<EnumResponse> goals = List.of(
                new EnumResponse("NO_POVERTY", "Fin de la pobreza")
        );

        when(enumService.getSustainableDevelopmentGoals()).thenReturn(goals);

        // When & Then
        mockMvc.perform(get("/enums/sustainable-development-goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(enumService, times(1)).getSustainableDevelopmentGoals();
    }

    @Test
    @DisplayName("GET /enums/universal-design-learning-principles - Should return UDL principles")
    void getUniversalDesignLearningPrinciples_ReturnsUDLPrinciples() throws Exception {
        // Given
        List<EnumResponse> principles = List.of(
                new EnumResponse("ENGAGEMENT", "Compromiso")
        );

        when(enumService.getUniversalDesignLearningPrinciples()).thenReturn(principles);

        // When & Then
        mockMvc.perform(get("/enums/universal-design-learning-principles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(enumService, times(1)).getUniversalDesignLearningPrinciples();
    }
}

