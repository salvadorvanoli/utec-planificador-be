package edu.utec.planificador.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user lacks permission to access a resource.
 * Results in HTTP 403 Forbidden response.
 */
public class ForbiddenException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "FORBIDDEN";

    /**
     * Constructor with authorization error message.
     *
     * @param message Authorization error message
     */
    public ForbiddenException(String message) {
        super(message, DEFAULT_ERROR_CODE, HttpStatus.FORBIDDEN);
    }
}
