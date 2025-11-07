package edu.utec.planificador.dto.response;

import edu.utec.planificador.enumeration.DeliveryFormat;
import edu.utec.planificador.enumeration.PartialGradingSystem;
import edu.utec.planificador.enumeration.Shift;
import edu.utec.planificador.enumeration.SustainableDevelopmentGoal;
import edu.utec.planificador.enumeration.UniversalDesignLearningPrinciple;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Course information")
public class CourseResponse {

    @Schema(description = "Course ID", example = "1")
    private Long id;

    @Schema(description = "Course shift", example = "MORNING")
    private Shift shift;

    @Schema(description = "Course description", example = "Introduction to programming course")
    private String description;

    @Schema(description = "Course start date", example = "2025-03-01")
    private LocalDate startDate;

    @Schema(description = "Course end date", example = "2025-07-15")
    private LocalDate endDate;

    @Schema(description = "Partial grading system", example = "TWO_PARTIALS")
    private PartialGradingSystem partialGradingSystem;

    @Schema(description = "Hours per delivery format")
    @Builder.Default
    private Map<DeliveryFormat, Integer> hoursPerDeliveryFormat = new HashMap<>();

    @Schema(description = "Is related to investigation", example = "false")
    private Boolean isRelatedToInvestigation;

    @Schema(description = "Involves activities with productive sector", example = "false")
    private Boolean involvesActivitiesWithProductiveSector;

    @Schema(description = "Sustainable development goals")
    @Builder.Default
    private Set<SustainableDevelopmentGoal> sustainableDevelopmentGoals = new HashSet<>();

    @Schema(description = "Universal design learning principles")
    @Builder.Default
    private Set<UniversalDesignLearningPrinciple> universalDesignLearningPrinciples = new HashSet<>();

    @Schema(description = "Curricular unit ID", example = "1")
    private Long curricularUnitId;
}
