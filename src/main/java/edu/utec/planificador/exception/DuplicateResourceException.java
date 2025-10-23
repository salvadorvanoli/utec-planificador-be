package edu.utec.planificador.exception;

import org.springframework.http.HttpStatus;

import java.util.Objects;

/**
 * Exception thrown when attempting to create a resource that already exists.
 * Results in HTTP 409 Conflict response.
 */
public class DuplicateResourceException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "DUPLICATE_RESOURCE";

    /**
     * Constructor with custom error message.
     *
     * @param message Custom error message
     */
    public DuplicateResourceException(String message) {
        super(message, DEFAULT_ERROR_CODE, HttpStatus.CONFLICT);
    }

    /**
     * Constructor with resource details.
     * Generates message: "{resourceName} con {field} '{value}' ya existe"
     *
     * @param resourceName Name of the resource type (e.g., "Course", "User")
     * @param field The field with the duplicate value (e.g., "code", "email")
     * @param value The duplicate value
     */
    public DuplicateResourceException(String resourceName, String field, Object value) {
        super(
            String.format("%s con %s '%s' ya existe",
                Objects.requireNonNull(resourceName, "Resource name cannot be null"),
                Objects.requireNonNull(field, "Field name cannot be null"),
                value != null ? value : "null"
            ),
            DEFAULT_ERROR_CODE,
            HttpStatus.CONFLICT
        );
    }
}
