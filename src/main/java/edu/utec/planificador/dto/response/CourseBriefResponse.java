package edu.utec.planificador.dto.response;

import edu.utec.planificador.enumeration.Shift;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Brief course info for lists")
public class CourseBriefResponse {

    @Schema(description = "Course ID", example = "1")
    private Long id;

    @Schema(description = "Curricular unit name", example = "Fundamentos de Informática")
    private String curricularUnitName;

    @Schema(description = "Course start date", example = "2025-03-01")
    private LocalDate startDate;

    @Schema(description = "Course shift", example = "MORNING")
    private Shift shift;
}

