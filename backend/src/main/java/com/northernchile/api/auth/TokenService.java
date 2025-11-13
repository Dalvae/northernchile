package com.northernchile.api.auth;

import com.northernchile.api.model.EmailVerificationToken;
import com.northernchile.api.model.PasswordResetToken;
import com.northernchile.api.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenService {

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${token.verification.expiration.hours:24}")
    private long verificationTokenExpirationHours;

    @Value("${token.password-reset.expiration.hours:2}")
    private long passwordResetTokenExpirationHours;

    public TokenService(EmailVerificationTokenRepository emailVerificationTokenRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository) {
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    /**
     * Create and save email verification token
     */
    @Transactional
    public EmailVerificationToken createEmailVerificationToken(User user) {
        String token = generateSecureToken();
        Instant expiresAt = Instant.now().plus(verificationTokenExpirationHours, ChronoUnit.HOURS);

        EmailVerificationToken verificationToken = new EmailVerificationToken(token, user, expiresAt);
        return emailVerificationTokenRepository.save(verificationToken);
    }

    /**
     * Create and save password reset token
     */
    @Transactional
    public PasswordResetToken createPasswordResetToken(User user) {
        String token = generateSecureToken();
        Instant expiresAt = Instant.now().plus(passwordResetTokenExpirationHours, ChronoUnit.HOURS);

        PasswordResetToken resetToken = new PasswordResetToken(token, user, expiresAt);
        return passwordResetTokenRepository.save(resetToken);
    }

    /**
     * Validate and return email verification token
     */
    public Optional<EmailVerificationToken> validateEmailVerificationToken(String token) {
        return emailVerificationTokenRepository.findByToken(token)
                .filter(EmailVerificationToken::isValid);
    }

    /**
     * Validate and return password reset token
     */
    public Optional<PasswordResetToken> validatePasswordResetToken(String token) {
        return passwordResetTokenRepository.findByToken(token)
                .filter(PasswordResetToken::isValid);
    }

    /**
     * Mark email verification token as used
     */
    @Transactional
    public void markEmailVerificationTokenAsUsed(EmailVerificationToken token) {
        token.setUsed(true);
        emailVerificationTokenRepository.save(token);
    }

    /**
     * Mark password reset token as used
     */
    @Transactional
    public void markPasswordResetTokenAsUsed(PasswordResetToken token) {
        token.setUsed(true);
        passwordResetTokenRepository.save(token);
    }

    /**
     * Delete expired tokens (runs daily at 3 AM)
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteExpiredTokens() {
        Instant now = Instant.now();
        emailVerificationTokenRepository.deleteByExpiresAtBefore(now);
        passwordResetTokenRepository.deleteByExpiresAtBefore(now);
    }

    /**
     * Generate a secure random token
     */
    private String generateSecureToken() {
        return UUID.randomUUID().toString() + UUID.randomUUID().toString().replace("-", "");
    }
}
