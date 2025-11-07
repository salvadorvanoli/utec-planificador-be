package edu.utec.planificador.dto.aiagent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoursePlanningDto {

    private Long id;
    private String shift;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String partialGradingSystem;
    private Map<String, Integer> hoursPerDeliveryFormat;
    private Boolean isRelatedToInvestigation;
    private Boolean involvesActivitiesWithProductiveSector;
    private Set<String> sustainableDevelopmentGoals;
    private Set<String> universalDesignLearningPrinciples;
    private CurricularUnitDto curricularUnit;
    private List<WeeklyPlanningDto> weeklyPlannings;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CurricularUnitDto {
        private Long id;
        private String name;
        private Integer credits;
        private Set<String> domainAreas;
        private Set<String> professionalCompetencies;
        private TermDto term;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TermDto {
        private Long id;
        private Integer number;
        private ProgramDto program;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProgramDto {
        private Long id;
        private String name;
        private Integer durationInTerms;
        private Integer totalCredits;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WeeklyPlanningDto {
        private Long id;
        private Integer weekNumber;
        private LocalDate startDate;
        private List<String> bibliographicReferences;
        private List<ProgrammaticContentDto> programmaticContents;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ProgrammaticContentDto {
        private Long id;
        private String content;
        private List<ActivityDto> activities;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ActivityDto {
        private Long id;
        private String description;
        private Integer durationInMinutes;
        private Set<String> cognitiveProcesses;
        private Set<String> transversalCompetencies;
        private String learningModality;
        private Set<String> teachingStrategies;
        private Set<String> learningResources;
    }
}

