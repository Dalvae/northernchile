package com.northernchile.api.exception;

/**
 * Exception thrown when a requested resource (entity) is not found in the database.
 * This should be used instead of EntityNotFoundException or generic RuntimeException.
 *
 * Returns HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceType, Object id) {
        super(String.format("%s not found with id: %s", resourceType, id));
    }
}
