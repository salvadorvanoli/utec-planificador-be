package edu.utec.planificador.dto.response.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard error response")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error code for client-side handling", example = "VALIDATION_ERROR")
    private String errorCode;

    @Schema(description = "Human-readable error message", example = "Validation failed for the request")
    private String message;

    @Schema(description = "Detailed error description (optional)", example = "The field 'email' is required")
    private String details;

    @Schema(description = "API path where the error occurred", example = "/api/v1/courses")
    private String path;

    @Schema(description = "Timestamp when the error occurred", example = "2025-10-22T14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    public static ErrorResponse of(int status, String errorCode, String message, String path) {
        return ErrorResponse.builder()
            .status(status)
            .errorCode(errorCode)
            .message(message)
            .path(path)
            .timestamp(LocalDateTime.now())
            .build();
    }

    public static ErrorResponse of(int status, String errorCode, String message, String details, String path) {
        return ErrorResponse.builder()
            .status(status)
            .errorCode(errorCode)
            .message(message)
            .details(details)
            .path(path)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
