package edu.utec.planificador.config;

import edu.utec.planificador.dto.response.error.ErrorResponse;
import edu.utec.planificador.dto.response.error.FieldErrorResponse;
import edu.utec.planificador.dto.response.error.ValidationErrorResponse;
import edu.utec.planificador.exception.AIAgentException;
import edu.utec.planificador.exception.BusinessException;
import edu.utec.planificador.exception.DuplicateResourceException;
import edu.utec.planificador.exception.ForbiddenException;
import edu.utec.planificador.exception.InvalidCredentialsException;
import edu.utec.planificador.exception.InvalidTokenException;
import edu.utec.planificador.exception.ResourceNotFoundException;
import edu.utec.planificador.exception.UnauthorizedException;
import edu.utec.planificador.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
        BusinessException ex,
        HttpServletRequest request
    ) {
        
        log.warn("Business exception: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            ex.getHttpStatus().value(),
            ex.getErrorCode(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
        ResourceNotFoundException ex,
        HttpServletRequest request
    ) {
        
        log.warn("Resource not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.NOT_FOUND.value(),
            ex.getErrorCode(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
        DuplicateResourceException ex,
        HttpServletRequest request
    ) {

        log.warn("Duplicate resource: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.CONFLICT.value(),
            ex.getErrorCode(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        ValidationException ex,
        HttpServletRequest request
    ) {
        
        log.warn("Validation exception: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            ex.getErrorCode(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        
        log.warn("Validation failed: {} field errors", ex.getBindingResult().getFieldErrorCount());
        
        List<FieldErrorResponse> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::mapFieldError)
            .collect(Collectors.toList());
        
        ValidationErrorResponse errorResponse = ValidationErrorResponse.of(
            "La validación de la solicitud ha fallado",
            request.getRequestURI(),
            fieldErrors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(
        ConstraintViolationException ex,
        HttpServletRequest request
    ) {
        
        log.warn("Constraint violation: {} violations", ex.getConstraintViolations().size());
        
        List<FieldErrorResponse> fieldErrors = ex.getConstraintViolations()
            .stream()
            .map(this::mapConstraintViolation)
            .collect(Collectors.toList());
        
        ValidationErrorResponse errorResponse = ValidationErrorResponse.of(
            "Violación de restricciones en la solicitud",
            request.getRequestURI(),
            fieldErrors
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
        AuthenticationException ex,
        HttpServletRequest request
    ) {
        
        log.warn("Authentication failed: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.UNAUTHORIZED.value(),
            "AUTHENTICATION_FAILED",
            "Se requiere autenticación para acceder a este recurso",
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
        UnauthorizedException ex,
        HttpServletRequest request
    ) {
        
        log.warn("Unauthorized access: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.UNAUTHORIZED.value(),
            ex.getErrorCode(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
        AccessDeniedException ex,
        HttpServletRequest request
    ) {

        log.warn("Access denied: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.FORBIDDEN.value(),
            "ACCESS_DENIED",
            "No tienes permiso para acceder a este recurso",
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(
        ForbiddenException ex,
        HttpServletRequest request
    ) {

        log.warn("Forbidden access: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.FORBIDDEN.value(),
            ex.getErrorCode(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(
        InvalidCredentialsException ex,
        HttpServletRequest request
    ) {
        
        log.warn("Invalid credentials: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.UNAUTHORIZED.value(),
            ex.getErrorCode(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(
        InvalidTokenException ex,
        HttpServletRequest request
    ) {
        
        log.warn("Invalid token: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.UNAUTHORIZED.value(),
            ex.getErrorCode(),
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(
        UsernameNotFoundException ex,
        HttpServletRequest request
    ) {
        
        log.warn("Username not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.UNAUTHORIZED.value(),
            "USER_NOT_FOUND",
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(AIAgentException.class)
    public ResponseEntity<ErrorResponse> handleAIAgentException(
        AIAgentException ex,
        HttpServletRequest request
    ) {
        
        log.error("AI Agent error: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "AI_AGENT_ERROR",
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
        IllegalArgumentException ex,
        HttpServletRequest request
    ) {
        
        log.warn("Illegal argument: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "INVALID_ARGUMENT",
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
        IllegalStateException ex,
        HttpServletRequest request
    ) {
        
        log.warn("Illegal state: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.CONFLICT.value(),
            "ILLEGAL_STATE",
            ex.getMessage(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
        DataIntegrityViolationException ex,
        HttpServletRequest request
    ) {
        
        log.error("Data integrity violation: {}", ex.getMessage());
        
        String message = extractSafeDataIntegrityMessage(ex);
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.CONFLICT.value(),
            "DATA_INTEGRITY_VIOLATION",
            message,
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
        HttpMessageNotReadableException ex,
        HttpServletRequest request
    ) {
        
        log.warn("Malformed request body: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "MALFORMED_REQUEST",
            "El cuerpo de la solicitud está mal formado o es inválido",
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
        MissingServletRequestParameterException ex,
        HttpServletRequest request
    ) {

        log.warn("Missing request parameter: {}", ex.getParameterName());
        
        String message = String.format("Falta el parámetro requerido '%s'", ex.getParameterName());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "MISSING_PARAMETER",
            message,
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
        MethodArgumentTypeMismatchException ex,
        HttpServletRequest request
    ) {
        
        log.warn("Method argument type mismatch: {}", ex.getName());
        
        Class<?> requiredType = ex.getRequiredType();
        String typeName = requiredType != null ? requiredType.getSimpleName() : "desconocido";
        
        String message = String.format(
            "El parámetro '%s' debe ser de tipo %s",
            ex.getName(),
            typeName
        );
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "TYPE_MISMATCH",
            message,
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(
        HttpRequestMethodNotSupportedException ex,
        HttpServletRequest request
    ) {
        
        log.warn("HTTP method not supported: {}", ex.getMethod());
        
        String message = String.format(
            "El método HTTP '%s' no está soportado para este endpoint",
            ex.getMethod()
        );
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.METHOD_NOT_ALLOWED.value(),
            "METHOD_NOT_ALLOWED",
            message,
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupported(
        HttpMediaTypeNotSupportedException ex,
        HttpServletRequest request
    ) {
        
        log.warn("Media type not supported: {}", ex.getContentType());
        
        String message = String.format(
            "El tipo de medio '%s' no está soportado. Tipos soportados: %s",
            ex.getContentType(),
            ex.getSupportedMediaTypes()
        );
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
            "UNSUPPORTED_MEDIA_TYPE",
            message,
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(errorResponse);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(
        NoHandlerFoundException ex,
        HttpServletRequest request
    ) {
        
        log.warn("No handler found for: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        
        String message = String.format(
            "No se encontró endpoint para %s %s",
            ex.getHttpMethod(),
            ex.getRequestURL()
        );
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.NOT_FOUND.value(),
            "ENDPOINT_NOT_FOUND",
            message,
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
        Exception ex,
        HttpServletRequest request
    ) {
        
        log.error("Unexpected error occurred", ex);
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "INTERNAL_SERVER_ERROR",
            "Ha ocurrido un error inesperado. Por favor, intenta de nuevo más tarde.",
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private FieldErrorResponse mapFieldError(FieldError fieldError) {
        return FieldErrorResponse.builder()
            .field(fieldError.getField())
            .rejectedValue(fieldError.getRejectedValue())
            .message(fieldError.getDefaultMessage())
            .build();
    }

    private FieldErrorResponse mapConstraintViolation(ConstraintViolation<?> violation) {
        String fieldName = violation.getPropertyPath().toString();

        int lastDotIndex = fieldName.lastIndexOf('.');
        if (lastDotIndex != -1) {
            fieldName = fieldName.substring(lastDotIndex + 1);
        }
        
        return FieldErrorResponse.builder()
            .field(fieldName)
            .rejectedValue(violation.getInvalidValue())
            .message(violation.getMessage())
            .build();
    }

    private String extractSafeDataIntegrityMessage(DataIntegrityViolationException ex) {
        String technicalMessage = ex.getMostSpecificCause().getMessage();
        
        if (technicalMessage == null) {
            return "Violación de restricción de base de datos";
        }
        
        String lowerMessage = technicalMessage.toLowerCase();
        
        return switch (lowerMessage) {
            case String msg when msg.contains("unique constraint") || msg.contains("duplicate key") ->
                "Ya existe un registro con este valor";
            case String msg when msg.contains("foreign key constraint") || msg.contains("violates foreign key") ->
                "No se puede realizar esta operación debido a registros relacionados";
            case String msg when msg.contains("not-null constraint") || msg.contains("null value") ->
                "Falta un campo requerido";
            default ->
                "Violación de restricción de base de datos";
        };
    }
}
