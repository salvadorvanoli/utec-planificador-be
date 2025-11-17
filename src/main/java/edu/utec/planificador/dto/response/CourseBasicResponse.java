package edu.utec.planificador.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Basic course information for listing")
public class CourseBasicResponse {

    @Schema(description = "Course ID", example = "1")
    private Long id;

    @Schema(description = "Course description", example = "Curso de Programación Avanzada")
    private String description;

    @Schema(description = "Course start date", example = "2024-03-01")
    private LocalDate startDate;

    @Schema(description = "Course end date", example = "2024-06-01")
    private LocalDate endDate;

    @Schema(description = "Curricular unit name", example = "Programación Avanzada")
    private String curricularUnitName;

    @Schema(description = "List of teachers assigned to this course")
    private List<UserBasicResponse> teachers;

    @Schema(description = "Date of the last modification. Null if no modifications exist.", example = "2024-03-15T10:30:00")
    private LocalDateTime lastModificationDate;
}
