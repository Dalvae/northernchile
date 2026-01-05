package com.northernchile.api.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.northernchile.api.payment.model.PaymentSessionStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response after creating a payment session.
 */
public record PaymentSessionRes(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID sessionId,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) PaymentSessionStatus status,
    String paymentUrl,
    String token,
    String qrCode,
    String pixCode,
    Instant expiresAt,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean isTest,
    List<UUID> bookingIds
) {}
