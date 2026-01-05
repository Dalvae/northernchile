package com.northernchile.api.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for refund operations.
 */
public record RefundRes(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID bookingId,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String provider,
    String providerRefundId,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BigDecimal refundAmount,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String status,
    String message
) {}
