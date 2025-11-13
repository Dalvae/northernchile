package com.northernchile.api.exception;

/**
 * Exception thrown when a user attempts an action without proper authentication.
 * This should be used instead of generic RuntimeException.
 *
 * Returns HTTP 401 Unauthorized.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException() {
        super("Authentication required");
    }
}
