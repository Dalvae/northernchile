package com.northernchile.api.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to track failed login attempts and implement account lockout.
 * Uses in-memory cache for simplicity - resets on server restart.
 */
@Service
public class LoginAttemptService {

    private static final Logger logger = LoggerFactory.getLogger(LoginAttemptService.class);

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MINUTES = 15;

    private final Map<String, LoginAttemptInfo> attemptsCache = new ConcurrentHashMap<>();

    /**
     * Record a failed login attempt for the given email.
     *
     * @param email The email that failed to login
     */
    public void loginFailed(String email) {
        String key = email.toLowerCase();
        LoginAttemptInfo info = attemptsCache.computeIfAbsent(key, k -> new LoginAttemptInfo());
        info.incrementAttempts();

        logger.warn("Failed login attempt for email: {} - Attempt #{}", email, info.getAttempts());

        if (info.getAttempts() >= MAX_ATTEMPTS) {
            info.lockUntil(Instant.now().plusSeconds(LOCKOUT_DURATION_MINUTES * 60));
            logger.warn("Account locked for email: {} - Too many failed attempts ({}/{}). Locked for {} minutes.",
                    email, info.getAttempts(), MAX_ATTEMPTS, LOCKOUT_DURATION_MINUTES);
        }
    }

    /**
     * Record a successful login - resets the attempt counter.
     *
     * @param email The email that successfully logged in
     */
    public void loginSucceeded(String email) {
        String key = email.toLowerCase();
        attemptsCache.remove(key);
        logger.debug("Login succeeded for email: {} - Attempts reset", email);
    }

    /**
     * Check if the account is currently locked.
     *
     * @param email The email to check
     * @return true if account is locked, false otherwise
     */
    public boolean isLocked(String email) {
        String key = email.toLowerCase();
        LoginAttemptInfo info = attemptsCache.get(key);

        if (info == null) {
            return false;
        }

        // Check if lockout has expired
        if (info.isLocked() && info.getLockoutExpiry().isBefore(Instant.now())) {
            // Lockout expired - reset attempts
            attemptsCache.remove(key);
            logger.info("Lockout expired for email: {} - Account unlocked", email);
            return false;
        }

        return info.isLocked();
    }

    /**
     * Get remaining lockout time in seconds.
     *
     * @param email The email to check
     * @return Remaining seconds, or 0 if not locked
     */
    public long getRemainingLockoutSeconds(String email) {
        String key = email.toLowerCase();
        LoginAttemptInfo info = attemptsCache.get(key);

        if (info == null || !info.isLocked()) {
            return 0;
        }

        long remaining = info.getLockoutExpiry().getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(0, remaining);
    }

    /**
     * Get remaining attempts before lockout.
     *
     * @param email The email to check
     * @return Remaining attempts (0 if locked)
     */
    public int getRemainingAttempts(String email) {
        String key = email.toLowerCase();
        LoginAttemptInfo info = attemptsCache.get(key);

        if (info == null) {
            return MAX_ATTEMPTS;
        }

        if (info.isLocked()) {
            return 0;
        }

        return Math.max(0, MAX_ATTEMPTS - info.getAttempts());
    }

    /**
     * Internal class to track login attempts for a single email.
     */
    private static class LoginAttemptInfo {
        private int attempts = 0;
        private Instant lockoutExpiry = null;

        public void incrementAttempts() {
            this.attempts++;
        }

        public int getAttempts() {
            return attempts;
        }

        public void lockUntil(Instant expiry) {
            this.lockoutExpiry = expiry;
        }

        public boolean isLocked() {
            return lockoutExpiry != null;
        }

        public Instant getLockoutExpiry() {
            return lockoutExpiry;
        }
    }
}
