package com.northernchile.api.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record ParticipantReq(
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String fullName,

    @NotBlank(message = "Document ID is required")
    @Size(min = 3, max = 50, message = "Document ID must be between 3 and 50 characters")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String documentId,

    @Size(max = 2, message = "Nationality must be a 2-letter country code (ISO 3166-1 alpha-2)")
    String nationality,

    @Past(message = "Date of birth must be in the past")
    LocalDate dateOfBirth,

    @Min(value = 0, message = "Age cannot be negative")
    @Max(value = 150, message = "Age must be less than 150")
    Integer age,

    @Size(max = 200, message = "Pickup address must not exceed 200 characters")
    String pickupAddress,

    @Size(max = 500, message = "Special requirements must not exceed 500 characters")
    String specialRequirements,

    @Pattern(regexp = "^\\+?[0-9\\s\\-()]{7,20}$", message = "Invalid phone number format")
    String phoneNumber,

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    String email
) {}
