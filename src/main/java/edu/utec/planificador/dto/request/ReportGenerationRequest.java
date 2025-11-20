package edu.utec.planificador.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request for course report generation")
public class ReportGenerationRequest {

    @Schema(description = "Course ID to generate report", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El ID del curso es obligatorio")
    private Long courseId;
}

