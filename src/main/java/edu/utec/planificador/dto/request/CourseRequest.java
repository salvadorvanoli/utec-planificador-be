package edu.utec.planificador.dto.request;

import edu.utec.planificador.enumeration.DeliveryFormat;
import edu.utec.planificador.enumeration.PartialGradingSystem;
import edu.utec.planificador.enumeration.Shift;
import edu.utec.planificador.enumeration.SustainableDevelopmentGoal;
import edu.utec.planificador.enumeration.UniversalDesignLearningPrinciple;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Course creation/update request")
public class CourseRequest {

    @Schema(description = "Course shift", example = "MORNING")
    @NotNull(message = "{validation.course.shift.required}")
    private Shift shift;

    @Schema(description = "Course description", example = "Introduction to programming course")
    @NotBlank(message = "{validation.course.description.required}")
    @Size(max = 2000, message = "{validation.course.description.size}")
    private String description;

    @Schema(description = "Course start date", example = "2025-03-01")
    @NotNull(message = "{validation.course.startDate.required}")
    private LocalDate startDate;

    @Schema(description = "Course end date", example = "2025-07-15")
    @NotNull(message = "{validation.course.endDate.required}")
    private LocalDate endDate;

    @Schema(description = "Partial grading system", example = "TWO_PARTIALS")
    @NotNull(message = "{validation.course.partialGradingSystem.required}")
    private PartialGradingSystem partialGradingSystem;

    @Schema(description = "Hours per delivery format")
    @Builder.Default
    private Map<DeliveryFormat, Integer> hoursPerDeliveryFormat = new HashMap<>();

    @Schema(description = "Is related to investigation", example = "false")
    @Builder.Default
    private Boolean isRelatedToInvestigation = false;

    @Schema(description = "Involves activities with productive sector", example = "false")
    @Builder.Default
    private Boolean involvesActivitiesWithProductiveSector = false;

    @Schema(description = "Sustainable development goals")
    @Builder.Default
    private Set<SustainableDevelopmentGoal> sustainableDevelopmentGoals = new HashSet<>();

    @Schema(description = "Universal design learning principles")
    @Builder.Default
    private Set<UniversalDesignLearningPrinciple> universalDesignLearningPrinciples = new HashSet<>();

    @Schema(description = "Curricular unit ID", example = "1")
    @NotNull(message = "{validation.course.curricularUnitId.required}")
    private Long curricularUnitId;
}
