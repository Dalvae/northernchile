package com.northernchile.api.payment.dto;

import com.northernchile.api.payment.model.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Payment initialization response DTO.
 * Contains the payment details needed for the client to complete the payment.
 */
public record PaymentInitRes(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID paymentId,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) PaymentStatus status,
    String paymentUrl,
    String detailsUrl,
    String qrCode,
    String pixCode,
    String token,
    Instant expiresAt,
    String message,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean isTest
) {}
