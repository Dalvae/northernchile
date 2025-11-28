package com.northernchile.api.exception;

/**
 * Base exception for business logic errors.
 * Subclasses should represent specific business rule violations.
 * 
 * All business exceptions include:
 * - errorCode: A stable code for frontend i18n (e.g., "SCHEDULE_FULL")
 * - message: Human-readable description
 */
public abstract class BusinessException extends RuntimeException {

    private final String errorCode;

    protected BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
