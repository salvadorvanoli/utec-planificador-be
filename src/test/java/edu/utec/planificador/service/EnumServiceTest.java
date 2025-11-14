package edu.utec.planificador.service;

import edu.utec.planificador.dto.response.EnumResponse;
import edu.utec.planificador.service.impl.EnumServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EnumService Unit Tests")
class EnumServiceTest {

    private EnumServiceImpl enumService;

    @BeforeEach
    void setUp() {
        enumService = new EnumServiceImpl();
    }

    @Test
    @DisplayName("Should get all enums")
    void getAllEnums_Success() {
        // When
        Map<String, List<EnumResponse>> result = enumService.getAllEnums();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).containsKeys(
            "domainAreas",
            "cognitiveProcesses",
            "shifts",
            "deliveryFormats",
            "transversalCompetencies",
            "partialGradingSystems",
            "professionalCompetencies",
            "sustainableDevelopmentGoals",
            "teachingStrategies",
            "learningModalities",
            "learningResources",
            "udlPrinciples"
        );
    }

    @Test
    @DisplayName("Should get domain areas")
    void getDomainAreas_Success() {
        // When
        List<EnumResponse> result = enumService.getDomainAreas();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(e -> e.getValue() != null && e.getDisplayValue() != null);
    }

    @Test
    @DisplayName("Should get cognitive processes")
    void getCognitiveProcesses_Success() {
        // When
        List<EnumResponse> result = enumService.getCognitiveProcesses();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(e -> e.getValue() != null && e.getDisplayValue() != null);
    }

    @Test
    @DisplayName("Should get shifts")
    void getShifts_Success() {
        // When
        List<EnumResponse> result = enumService.getShifts();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(e -> e.getValue() != null && e.getDisplayValue() != null);
    }

    @Test
    @DisplayName("Should get delivery formats")
    void getDeliveryFormats_Success() {
        // When
        List<EnumResponse> result = enumService.getDeliveryFormats();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(e -> e.getValue() != null && e.getDisplayValue() != null);
    }

    @Test
    @DisplayName("Should get transversal competencies")
    void getTransversalCompetencies_Success() {
        // When
        List<EnumResponse> result = enumService.getTransversalCompetencies();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(e -> e.getValue() != null && e.getDisplayValue() != null);
    }

    @Test
    @DisplayName("Should get partial grading systems")
    void getPartialGradingSystems_Success() {
        // When
        List<EnumResponse> result = enumService.getPartialGradingSystems();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(e -> e.getValue() != null && e.getDisplayValue() != null);
    }

    @Test
    @DisplayName("Should get professional competencies")
    void getProfessionalCompetencies_Success() {
        // When
        List<EnumResponse> result = enumService.getProfessionalCompetencies();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(e -> e.getValue() != null && e.getDisplayValue() != null);
    }

    @Test
    @DisplayName("Should get sustainable development goals")
    void getSustainableDevelopmentGoals_Success() {
        // When
        List<EnumResponse> result = enumService.getSustainableDevelopmentGoals();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(e -> e.getValue() != null && e.getDisplayValue() != null);
    }

    @Test
    @DisplayName("Should get teaching strategies")
    void getTeachingStrategies_Success() {
        // When
        List<EnumResponse> result = enumService.getTeachingStrategies();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(e -> e.getValue() != null && e.getDisplayValue() != null);
    }

    @Test
    @DisplayName("Should get learning modalities")
    void getLearningModalities_Success() {
        // When
        List<EnumResponse> result = enumService.getLearningModalities();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(e -> e.getValue() != null && e.getDisplayValue() != null);
    }

    @Test
    @DisplayName("Should get learning resources")
    void getLearningResources_Success() {
        // When
        List<EnumResponse> result = enumService.getLearningResources();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(e -> e.getValue() != null && e.getDisplayValue() != null);
    }

    @Test
    @DisplayName("Should get universal design learning principles")
    void getUniversalDesignLearningPrinciples_Success() {
        // When
        List<EnumResponse> result = enumService.getUniversalDesignLearningPrinciples();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(e -> e.getValue() != null && e.getDisplayValue() != null);
    }

    @Test
    @DisplayName("All enum lists should have consistent structure")
    void allEnums_ConsistentStructure() {
        // When
        Map<String, List<EnumResponse>> allEnums = enumService.getAllEnums();

        // Then
        allEnums.values().forEach(enumList -> {
            assertThat(enumList).isNotEmpty();
            enumList.forEach(enumResponse -> {
                assertThat(enumResponse.getValue()).isNotBlank();
                assertThat(enumResponse.getDisplayValue()).isNotBlank();
            });
        });
    }
}
