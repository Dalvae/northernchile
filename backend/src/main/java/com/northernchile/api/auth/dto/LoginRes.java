package com.northernchile.api.auth.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Response DTO for successful login.
 * Token is returned separately and set as HttpOnly cookie.
 */
public record LoginRes(
    String token,
    UserData user
) {
    public record UserData(
        UUID id,
        String email,
        String fullName,
        String nationality,
        String phoneNumber,
        LocalDate dateOfBirth,
        String role
    ) {}

    /**
     * Create response without token (for sending to client after cookie is set).
     */
    public LoginRes withoutToken() {
        return new LoginRes(null, this.user);
    }
}
