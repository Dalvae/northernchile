package com.northernchile.api.exception;

import com.northernchile.api.i18n.LocalizedMessageProvider;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    /**
     * Returns true only if explicitly running in dev or local profile.
     * Production (no profiles or 'prod' profile) returns false to protect stack traces.
     */
    private boolean isDevMode() {
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("dev".equals(profile) || "local".equals(profile)) {
                return true;
            }
        }
        return false;
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
     * Handles ScheduleFullException - when tour schedule has no availability
     * Returns HTTP 409 Conflict
     */
    @ExceptionHandler(ScheduleFullException.class)
    public ResponseEntity<BusinessErrorResponse> handleScheduleFull(
            ScheduleFullException ex,
            WebRequest request) {
        log.info("Schedule full: {} - Requested: {}, Available: {}",
            ex.getScheduleId(), ex.getRequestedSlots(), ex.getAvailableSlots());

        BusinessErrorResponse errorResponse = new BusinessErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Schedule Full",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                ex.getErrorCode()
        );
        errorResponse.setAvailableSlots(ex.getAvailableSlots());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handles TourNotActiveException - when tour is not active or published
     * Returns HTTP 409 Conflict
     */
    @ExceptionHandler(TourNotActiveException.class)
    public ResponseEntity<BusinessErrorResponse> handleTourNotActive(
            TourNotActiveException ex,
            WebRequest request) {
        log.info("Tour not active: {}", ex.getTourId());

        BusinessErrorResponse errorResponse = new BusinessErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Tour Not Available",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                ex.getErrorCode()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handles BookingCutoffException - when booking window has closed
     * Returns HTTP 409 Conflict
     */
    @ExceptionHandler(BookingCutoffException.class)
    public ResponseEntity<BusinessErrorResponse> handleBookingCutoff(
            BookingCutoffException ex,
            WebRequest request) {
        log.info("Booking cutoff passed for schedule: {}", ex.getScheduleId());

        BusinessErrorResponse errorResponse = new BusinessErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Booking Window Closed",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                ex.getErrorCode()
        );
        errorResponse.setHoursRequired(ex.getHoursRequired());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handles InvalidBookingStateException - when booking state transition is invalid
     * Returns HTTP 409 Conflict
     */
    @ExceptionHandler(InvalidBookingStateException.class)
    public ResponseEntity<BusinessErrorResponse> handleInvalidBookingState(
            InvalidBookingStateException ex,
            WebRequest request) {
        log.info("Invalid booking state transition: {} -> {}",
            ex.getCurrentStatus(), ex.getRequestedStatus());

        BusinessErrorResponse errorResponse = new BusinessErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Invalid Booking State",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                ex.getErrorCode()
        );
        errorResponse.setCurrentStatus(ex.getCurrentStatus());
        errorResponse.setAllowedTransitions(ex.getAllowedTransitions());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handles CartExpiredException - when cart has expired
     * Returns HTTP 410 Gone
     */
    @ExceptionHandler(CartExpiredException.class)
    public ResponseEntity<BusinessErrorResponse> handleCartExpired(
            CartExpiredException ex,
            WebRequest request) {
        log.info("Cart expired: {}", ex.getCartId());

        BusinessErrorResponse errorResponse = new BusinessErrorResponse(
                Instant.now(),
                HttpStatus.GONE.value(),
                "Cart Expired",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                ex.getErrorCode()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.GONE);
    }

    /**
     * Handles DuplicateBookingException - when user already has a booking for the schedule
     * Returns HTTP 409 Conflict
     */
    @ExceptionHandler(DuplicateBookingException.class)
    public ResponseEntity<BusinessErrorResponse> handleDuplicateBooking(
            DuplicateBookingException ex,
            WebRequest request) {
        log.info("Duplicate booking attempt for schedule: {}", ex.getScheduleId());

        BusinessErrorResponse errorResponse = new BusinessErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "Duplicate Booking",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                ex.getErrorCode()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    /**
     * Handles PaymentDeclinedException - when payment is rejected by bank/provider
     * Returns HTTP 402 Payment Required
     */
    @ExceptionHandler(PaymentDeclinedException.class)
    public ResponseEntity<PaymentErrorResponse> handlePaymentDeclined(
            PaymentDeclinedException ex,
            WebRequest request) {
        log.warn("Payment declined: {} - Reason: {}", ex.getMessage(), ex.getDeclineReason());

        PaymentErrorResponse errorResponse = new PaymentErrorResponse(
                Instant.now(),
                HttpStatus.PAYMENT_REQUIRED.value(),
                "Payment Declined",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                ex.getErrorCode(),
                ex.getDeclineReason()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.PAYMENT_REQUIRED);
    }

    /**
     * Handles PaymentProviderException - when there's an error with the payment provider
     * Returns HTTP 502 Bad Gateway
     */
    @ExceptionHandler(PaymentProviderException.class)
    public ResponseEntity<PaymentErrorResponse> handlePaymentProviderError(
            PaymentProviderException ex,
            WebRequest request) {
        log.error("Payment provider error [{}]: {}", ex.getProvider(), ex.getMessage(), ex);

        String userMessage = messageProvider.getMessage(
            "error.payment.provider",
            "Payment service temporarily unavailable. Please try again later."
        );

        PaymentErrorResponse errorResponse = new PaymentErrorResponse(
                Instant.now(),
                HttpStatus.BAD_GATEWAY.value(),
                "Payment Provider Error",
                userMessage,
                request.getDescription(false).replace("uri=", ""),
                ex.getErrorCode(),
                null
        );

        // Add provider details only in dev mode
        if (isDevMode()) {
            errorResponse.setProviderMessage(ex.getProviderMessage());
        }

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_GATEWAY);
    }

    /**
     * Handles PaymentExpiredException - when payment session expires
     * Returns HTTP 410 Gone
     */
    @ExceptionHandler(PaymentExpiredException.class)
    public ResponseEntity<PaymentErrorResponse> handlePaymentExpired(
            PaymentExpiredException ex,
            WebRequest request) {
        log.info("Payment expired: {}", ex.getMessage());

        PaymentErrorResponse errorResponse = new PaymentErrorResponse(
                Instant.now(),
                HttpStatus.GONE.value(),
                "Payment Expired",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                ex.getErrorCode(),
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.GONE);
    }

    /**
     * Handles RefundException - when refund operation fails
     * Returns HTTP 422 Unprocessable Entity
     */
    @ExceptionHandler(RefundException.class)
    public ResponseEntity<PaymentErrorResponse> handleRefundError(
            RefundException ex,
            WebRequest request) {
        log.error("Refund error: {}", ex.getMessage(), ex);

        PaymentErrorResponse errorResponse = new PaymentErrorResponse(
                Instant.now(),
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Refund Failed",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                ex.getErrorCode(),
                null
        );

        if (isDevMode()) {
            errorResponse.setProviderMessage(ex.getProviderMessage());
        }

        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * Handles generic PaymentException
     * Returns HTTP 400 Bad Request
     */
    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<PaymentErrorResponse> handlePaymentError(
            PaymentException ex,
            WebRequest request) {
        log.error("Payment error: {}", ex.getMessage(), ex);

        PaymentErrorResponse errorResponse = new PaymentErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Payment Error",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                ex.getErrorCode(),
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
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
     * Payment-specific error response with additional fields
     */
    public static class PaymentErrorResponse extends ErrorResponse {
        private String errorCode;
        private String declineReason;
        private String providerMessage;

        public PaymentErrorResponse(Instant timestamp, int status, String error, String message, String path,
                                    String errorCode, String declineReason) {
            super(timestamp, status, error, message, path);
            this.errorCode = errorCode;
            this.declineReason = declineReason;
        }

        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

        public String getDeclineReason() { return declineReason; }
        public void setDeclineReason(String declineReason) { this.declineReason = declineReason; }

        public String getProviderMessage() { return providerMessage; }
        public void setProviderMessage(String providerMessage) { this.providerMessage = providerMessage; }
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

    /**
     * Business error response structure with error code for frontend i18n
     */
    public static class BusinessErrorResponse extends ErrorResponse {
        private String errorCode;
        private Integer availableSlots;
        private Integer hoursRequired;
        private String currentStatus;
        private java.util.List<String> allowedTransitions;

        public BusinessErrorResponse(Instant timestamp, int status, String error, String message, String path, String errorCode) {
            super(timestamp, status, error, message, path);
            this.errorCode = errorCode;
        }

        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

        public Integer getAvailableSlots() { return availableSlots; }
        public void setAvailableSlots(Integer availableSlots) { this.availableSlots = availableSlots; }

        public Integer getHoursRequired() { return hoursRequired; }
        public void setHoursRequired(Integer hoursRequired) { this.hoursRequired = hoursRequired; }

        public String getCurrentStatus() { return currentStatus; }
        public void setCurrentStatus(String currentStatus) { this.currentStatus = currentStatus; }

        public java.util.List<String> getAllowedTransitions() { return allowedTransitions; }
        public void setAllowedTransitions(java.util.List<String> allowedTransitions) { this.allowedTransitions = allowedTransitions; }
    }
}
