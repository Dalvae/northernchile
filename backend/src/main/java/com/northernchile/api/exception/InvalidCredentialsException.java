package com.northernchile.api.exception;

/**
 * Exception thrown when login credentials are invalid.
 * This should be used instead of generic RuntimeException.
 *
 * Returns HTTP 401 Unauthorized.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
