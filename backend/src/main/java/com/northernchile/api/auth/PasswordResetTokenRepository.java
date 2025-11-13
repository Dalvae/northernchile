package com.northernchile.api.auth;

import com.northernchile.api.model.PasswordResetToken;
import com.northernchile.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUserAndUsedFalseAndExpiresAtAfter(User user, Instant now);
    void deleteByExpiresAtBefore(Instant now);
}
