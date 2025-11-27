package edu.utec.planificador.dto.response;

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
@Schema(description = "Detailed course information including program, curricular unit, teachers, and competencies")
public class CourseDetailedInfoResponse {

    @Schema(description = "Course ID", example = "1")
    private Long courseId;

    @Schema(description = "Program name (career)", example = "Ingeniería en Computación")
    private String programName;

    @Schema(description = "Curricular unit name", example = "Programación 1")
    private String curricularUnitName;

    @Schema(description = "List of teachers assigned to the course")
    private List<TeacherInfo> teachers;

    @Schema(description = "Number of credits", example = "8")
    private Integer credits;

    @Schema(description = "Semester number", example = "3")
    private Integer semesterNumber;

    @Schema(description = "Domain areas with display values")
    private List<String> domainAreas;

    @Schema(description = "Professional competencies with display values")
    private List<String> professionalCompetencies;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Teacher information")
    public static class TeacherInfo {
        @Schema(description = "Teacher's first name", example = "Juan")
        private String name;

        @Schema(description = "Teacher's last name", example = "Pérez")
        private String lastName;

        @Schema(description = "Teacher's UTEC email", example = "juan.perez@utec.edu.uy")
        private String email;
    }
}
