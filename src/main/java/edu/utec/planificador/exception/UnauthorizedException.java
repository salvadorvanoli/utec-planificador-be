package edu.utec.planificador.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a user is not authenticated.
 * Results in HTTP 401 Unauthorized response.
 */
public class UnauthorizedException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "UNAUTHORIZED";

    /**
     * Constructor with authentication error message.
     *
     * @param message Authentication error message
     */
    public UnauthorizedException(String message) {
        super(message, DEFAULT_ERROR_CODE, HttpStatus.UNAUTHORIZED);
    }
}
