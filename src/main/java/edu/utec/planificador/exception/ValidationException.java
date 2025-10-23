package edu.utec.planificador.exception;

import org.springframework.http.HttpStatus;

import java.util.Objects;

/**
 * Exception thrown when business validation fails.
 * Results in HTTP 400 Bad Request response.
 * 
 * Note: For DTO validation use @Valid annotation, which is handled automatically.
 */
public class ValidationException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "VALIDATION_ERROR";

    /**
     * Constructor with validation error message.
     *
     * @param message Validation error message
     */
    public ValidationException(String message) {
        super(message, DEFAULT_ERROR_CODE, HttpStatus.BAD_REQUEST);
    }

    /**
     * Constructor with custom error code.
     *
     * @param message Validation error message
     * @param errorCode Custom error code for specific validation scenarios
     */
    public ValidationException(String message, String errorCode) {
        super(
            message,
            Objects.requireNonNull(errorCode, "Error code cannot be null"),
            HttpStatus.BAD_REQUEST
        );
    }
}
