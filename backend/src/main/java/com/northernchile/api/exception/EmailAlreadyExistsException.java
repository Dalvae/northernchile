package com.northernchile.api.exception;

/**
 * Exception thrown when attempting to create a user with an email that already exists.
 * This should be used instead of RuntimeException or IllegalStateException.
 *
 * Returns HTTP 409 Conflict.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super(String.format("Email already in use: %s", email));
    }
}
