package com.northernchile.api.auth;

import com.northernchile.api.model.EmailVerificationToken;
import com.northernchile.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {
    Optional<EmailVerificationToken> findByToken(String token);
    Optional<EmailVerificationToken> findByUserAndUsedFalseAndExpiresAtAfter(User user, Instant now);
    void deleteByExpiresAtBefore(Instant now);
}
