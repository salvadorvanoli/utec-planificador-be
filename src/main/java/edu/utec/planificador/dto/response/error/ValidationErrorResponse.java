package edu.utec.planificador.dto.response.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Validation error response with field-level details")
public class ValidationErrorResponse {

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error code for client-side handling", example = "VALIDATION_ERROR")
    private String errorCode;

    @Schema(description = "Human-readable error message", example = "Validation failed")
    private String message;

    @Schema(description = "API path where the error occurred", example = "/api/v1/courses")
    private String path;

    @Schema(description = "Timestamp when the error occurred", example = "2025-10-22T14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    @Schema(description = "List of field validation errors")
    @Builder.Default
    private List<FieldErrorResponse> fieldErrors = new ArrayList<>();

    public static ValidationErrorResponse of(String message, String path, List<FieldErrorResponse> fieldErrors) {
        return ValidationErrorResponse.builder()
                .status(400)
                .errorCode("VALIDATION_ERROR")
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .fieldErrors(fieldErrors != null ? fieldErrors : new ArrayList<>())
                .build();
    }

    public void addFieldError(FieldErrorResponse fieldError) {
        if (this.fieldErrors == null) {
            this.fieldErrors = new ArrayList<>();
        }
        this.fieldErrors.add(fieldError);
    }
}
