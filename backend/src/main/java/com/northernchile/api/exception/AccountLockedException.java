package com.northernchile.api.exception;

/**
 * Exception thrown when an account is locked due to too many failed login attempts.
 */
public class AccountLockedException extends RuntimeException {

    private final String email;
    private final long remainingSeconds;

    public AccountLockedException(String email, long remainingSeconds) {
        super(String.format("Account locked for %s. Try again in %d minutes.", email, remainingSeconds / 60));
        this.email = email;
        this.remainingSeconds = remainingSeconds;
    }

    public String getEmail() {
        return email;
    }

    public long getRemainingSeconds() {
        return remainingSeconds;
    }
}
