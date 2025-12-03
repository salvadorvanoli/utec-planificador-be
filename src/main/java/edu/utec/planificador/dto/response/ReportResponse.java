package edu.utec.planificador.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Course report response")
public class ReportResponse {

    @Schema(description = "Report generation success status")
    private Boolean success;

    @Schema(description = "Detailed course report")
    private CourseReport report;

    @Schema(description = "List of recommendations")
    private List<String> recommendations;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CourseReport {
        private String courseId;
        private String analysisDate;
        private String message;
        private ExecutiveSummary executiveSummary;
        private DetailedAnalysis detailedAnalysis;
        private List<String> strengths;
        private List<String> improvementAreas;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ExecutiveSummary {
        private Integer totalWeeks;
        private Integer totalHours;
        private Integer inPersonHours;
        private Integer virtualHours;
        private Integer hybridHours;
        private String averageActivityDuration;
        private Integer totalActivitiesAnalyzed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DetailedAnalysis {
        private String cognitiveProcesses;
        private String transversalCompetencies;
        private String modalityBalance;
        private String teachingStrategies;
        private String resources;
        private String sdgLinkage;
    }
}

