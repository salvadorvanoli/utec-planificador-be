package edu.utec.planificador.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when authentication credentials are invalid.
 * Results in HTTP 401 Unauthorized response.
 */
public class InvalidCredentialsException extends BusinessException {

    private static final String DEFAULT_ERROR_CODE = "INVALID_CREDENTIALS";

    /**
     * Constructor with custom error message.
     *
     * @param message Authentication error message
     */
    public InvalidCredentialsException(String message) {
        super(message, DEFAULT_ERROR_CODE, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Constructor for wrapping other exceptions.
     *
     * @param message Authentication error message
     * @param cause The original exception
     */
    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, DEFAULT_ERROR_CODE, HttpStatus.UNAUTHORIZED, cause);
    }
}

