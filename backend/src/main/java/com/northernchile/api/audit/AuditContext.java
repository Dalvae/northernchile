package com.northernchile.api.audit;

import com.northernchile.api.model.User;

/**
 * Thread-local context for passing the current user to audit listeners.
 *
 * JPA Entity Listeners don't have access to Spring's security context directly,
 * so we use this ThreadLocal to pass the authenticated user from the filter
 * chain to the entity listener.
 *
 * Usage:
 * 1. AuditContextFilter sets the current user at the start of each request
 * 2. AuditEntityListener reads the current user when auditing operations
 * 3. AuditContextFilter clears the context at the end of each request
 *
 * This ensures proper audit attribution even in async operations, as long as
 * the async context is properly propagated.
 */
public final class AuditContext {

    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();

    private AuditContext() {
        // Static utility class
    }

    /**
     * Sets the current user for this thread.
     * Should be called at the start of each request.
     */
    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }

    /**
     * Gets the current user for this thread.
     *
     * @return The current user, or null if not set
     */
    public static User getCurrentUser() {
        return currentUser.get();
    }

    /**
     * Clears the current user from this thread.
     * Should be called at the end of each request to prevent memory leaks.
     */
    public static void clear() {
        currentUser.remove();
    }

    /**
     * Checks if a user is currently set.
     *
     * @return true if a user is set, false otherwise
     */
    public static boolean hasCurrentUser() {
        return currentUser.get() != null;
    }
}
