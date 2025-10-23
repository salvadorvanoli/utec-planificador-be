package edu.utec.planificador.dto.response.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Field validation error details")
public class FieldErrorResponse {

    @Schema(description = "Name of the field that failed validation", example = "email")
    private String field;

    @Schema(description = "Value that was rejected (can be null)", example = "invalid-email")
    private Object rejectedValue;

    @Schema(description = "Validation error message", example = "must be a well-formed email address")
    private String message;
}
