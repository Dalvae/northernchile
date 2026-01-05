package com.northernchile.api.payment.dto;

import com.northernchile.api.payment.model.PaymentMethod;
import com.northernchile.api.payment.model.PaymentProvider;
import com.northernchile.api.payment.model.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentRes(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID bookingId,
    UUID paymentSessionId,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) PaymentProvider provider,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) PaymentMethod paymentMethod,
    String externalPaymentId,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) PaymentStatus status,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal amount,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String currency,
    String paymentUrl,
    String detailsUrl,
    String qrCode,
    String pixCode,
    String token,
    Instant expiresAt,
    String errorMessage,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean isTest,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant createdAt,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant updatedAt
) {}
