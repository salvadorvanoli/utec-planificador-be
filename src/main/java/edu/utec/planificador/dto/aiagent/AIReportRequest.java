package edu.utec.planificador.dto.aiagent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AIReportRequest {

    @JsonProperty("courseId")
    private String courseId;

    private CourseStatisticsDto statistics;

    @JsonProperty("coursePlanning")
    private CoursePlanningDto coursePlanning;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CourseStatisticsDto {
        private Map<String, Integer> cognitiveProcesses;
        private Map<String, Integer> transversalCompetencies;
        private Map<String, Integer> learningModalities;
        private Map<String, Integer> teachingStrategies;
        private List<String> mostUsedResources;
        private Map<String, Integer> linkedSDGs;
        private Integer averageActivityDurationInMinutes;
        private Integer totalWeeks;
        private Integer totalInPersonHours;
        private Integer totalVirtualHours;
        private Integer totalHybridHours;
    }
}

