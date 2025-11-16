package com.northernchile.api.exception;

import com.northernchile.api.i18n.LocalizedMessageProvider;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for consistent error responses across the API.
 * Supports internationalization (i18n) of error messages based on Accept-Language header.
 *
 * All error responses follow this structure:
 * {
 *   "timestamp": "2025-01-15T10:30:00Z",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Tour not found with id: abc-123",
 *   "path": "/api/tours/abc-123"
 * }
 *
 * For validation errors, includes additional "errors" field with field-specific messages.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Environment environment;
    private final LocalizedMessageProvider messageProvider;

    public GlobalExceptionHandler(Environment environment, LocalizedMessageProvider messageProvider) {
        this.environment = environment;
        this.messageProvider = messageProvider;
    }

    private boolean isDevMode() {
        return Arrays.asList(environment.getActiveProfiles()).contains("dev")
            || Arrays.asList(environment.getActiveProfiles()).contains("local")
            || Arrays.asList(environment.getActiveProfiles()).isEmpty(); // Default to dev if no profile
    }

    private String getStackTraceAsString(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Handles ResourceNotFoundException (custom)
     * Returns HTTP 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles EntityNotFoundException (JPA)
     * Returns HTTP 404 Not Found
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(
            EntityNotFoundException ex,
            WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles EmailAlreadyExistsException (custom)
     * Returns HTTP 409 Conflict
     */
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex,
            WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handles UnauthorizedException and InvalidCredentialsException (custom)
     * Returns HTTP 401 Unauthorized
     */
    @ExceptionHandler({UnauthorizedException.class, InvalidCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            RuntimeException ex,
            WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles AccessDeniedException (Spring Security)
     * Returns HTTP 403 Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles validation errors from @Valid annotations
     * Returns HTTP 400 Bad Request with field-specific error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String localizedMessage = messageProvider.getMessage("error.bad.request", "Validation failed");

        ValidationErrorResponse errorResponse = new ValidationErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                localizedMessage,
                request.getDescription(false).replace("uri=", ""),
                errors
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles IllegalArgumentException and IllegalStateException
     * Returns HTTP 400 Bad Request
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(
            RuntimeException ex,
            WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles all other unhandled exceptions
     * Returns HTTP 500 Internal Server Error
     * In development mode, includes full stacktrace for debugging
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            WebRequest request) {
        // Always log the full exception with stacktrace
        log.error("Unhandled exception occurred: {} - {}",
            request.getDescription(false),
            ex.getMessage(),
            ex
        );

        String detailedMessage = isDevMode()
            ? ex.getMessage()
            : messageProvider.getMessage("error.internal.server", "An unexpected error occurred");
        String stackTrace = isDevMode() ? getStackTraceAsString(ex) : null;

        ErrorResponse errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                detailedMessage,
                request.getDescription(false).replace("uri=", ""),
                ex.getClass().getName(),
                stackTrace
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Standard error response structure
     */
    public static class ErrorResponse {
        private Instant timestamp;
        private int status;
        private String error;
        private String message;
        private String path;
        private String exceptionType; // Optional: Exception class name (dev mode only)
        private String stackTrace;    // Optional: Full stacktrace (dev mode only)

        // Constructor for standard errors (without debug info)
        public ErrorResponse(Instant timestamp, int status, String error, String message, String path) {
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
        }

        // Constructor for errors with debug info (dev mode)
        public ErrorResponse(Instant timestamp, int status, String error, String message, String path, String exceptionType, String stackTrace) {
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
            this.exceptionType = exceptionType;
            this.stackTrace = stackTrace;
        }

        public Instant getTimestamp() { return timestamp; }
        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
        public String getPath() { return path; }
        public String getExceptionType() { return exceptionType; }
        public String getStackTrace() { return stackTrace; }

        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
        public void setStatus(int status) { this.status = status; }
        public void setError(String error) { this.error = error; }
        public void setMessage(String message) { this.message = message; }
        public void setPath(String path) { this.path = path; }
        public void setExceptionType(String exceptionType) { this.exceptionType = exceptionType; }
        public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }
    }

    /**
     * Validation error response structure (extends ErrorResponse with field errors)
     */
    public static class ValidationErrorResponse extends ErrorResponse {
        private Map<String, String> errors;

        public ValidationErrorResponse(Instant timestamp, int status, String error, String message, String path, Map<String, String> errors) {
            super(timestamp, status, error, message, path);
            this.errors = errors;
        }

        public Map<String, String> getErrors() { return errors; }
        public void setErrors(Map<String, String> errors) { this.errors = errors; }
    }
}
