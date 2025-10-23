package edu.utec.planificador.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Objects;

/**
 * Base exception for business logic errors.
 * All custom business exceptions should extend this class.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    /**
     * Constructor with full error details.
     *
     * @param message Human-readable error message
     * @param errorCode Machine-readable error code
     * @param httpStatus HTTP status code to return
     */
    public BusinessException(String message, String errorCode, HttpStatus httpStatus) {
        super(Objects.requireNonNull(message, "Error message cannot be null"));
        this.errorCode = Objects.requireNonNull(errorCode, "Error code cannot be null");
        this.httpStatus = Objects.requireNonNull(httpStatus, "HTTP status cannot be null");
    }

    /**
     * Simplified constructor with default error code and BAD_REQUEST status.
     *
     * @param message Human-readable error message
     */
    public BusinessException(String message) {
        this(message, "BUSINESS_ERROR", HttpStatus.BAD_REQUEST);
    }

    /**
     * Constructor for wrapping other exceptions.
     *
     * @param message Human-readable error message
     * @param errorCode Machine-readable error code
     * @param httpStatus HTTP status code to return
     * @param cause The original exception
     */
    public BusinessException(String message, String errorCode, HttpStatus httpStatus, Throwable cause) {
        super(Objects.requireNonNull(message, "Error message cannot be null"), cause);
        this.errorCode = Objects.requireNonNull(errorCode, "Error code cannot be null");
        this.httpStatus = Objects.requireNonNull(httpStatus, "HTTP status cannot be null");
    }
}
