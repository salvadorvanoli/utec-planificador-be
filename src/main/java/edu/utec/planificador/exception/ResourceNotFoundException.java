package edu.utec.planificador.exception;

import org.springframework.http.HttpStatus;

import java.util.Objects;

/**
 * Exception thrown when a requested resource is not found.
 * Results in HTTP 404 Not Found response.
 */
public class ResourceNotFoundException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "RESOURCE_NOT_FOUND";

    /**
     * Constructor with custom error message.
     *
     * @param message Custom error message
     */
    public ResourceNotFoundException(String message) {
        super(message, DEFAULT_ERROR_CODE, HttpStatus.NOT_FOUND);
    }

    /**
     * Constructor with resource type and identifier.
     * Generates message: "{resourceName} no encontrado: {identifier}"
     *
     * @param resourceName Name of the resource type (e.g., "Course", "User")
     * @param identifier The identifier that was not found
     */
    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(
            String.format("%s no encontrado: %s",
                Objects.requireNonNull(resourceName, "Resource name cannot be null"),
                identifier != null ? identifier : "null"
            ),
            DEFAULT_ERROR_CODE,
            HttpStatus.NOT_FOUND
        );
    }
}
