package com.northernchile.api.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record SavedParticipantReq(
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String fullName,

    @Size(max = 50, message = "Document ID must not exceed 50 characters")
    String documentId,

    @Past(message = "Date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(type = "string", format = "date", example = "1990-05-15")
    LocalDate dateOfBirth,

    @Size(max = 100, message = "Nationality must not exceed 100 characters")
    String nationality,

    @Pattern(regexp = "^$|^\\+?[0-9\\s\\-()]{7,50}$", message = "Invalid phone number format")
    String phoneNumber,

    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    String email
) {}
