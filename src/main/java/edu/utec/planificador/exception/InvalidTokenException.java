package edu.utec.planificador.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a JWT token is invalid, expired, or malformed.
 * Results in HTTP 401 Unauthorized response.
 */
public class InvalidTokenException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "INVALID_TOKEN";

    /**
     * Constructor with token validation error message.
     *
     * @param message Token validation error message
     */
    public InvalidTokenException(String message) {
        super(message, DEFAULT_ERROR_CODE, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Constructor with token validation error message and cause.
     *
     * @param message Token validation error message
     * @param cause The underlying cause of the token validation failure
     */
    public InvalidTokenException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, HttpStatus.UNAUTHORIZED, cause);
    }
}
