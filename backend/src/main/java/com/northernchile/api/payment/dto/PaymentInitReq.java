package com.northernchile.api.payment.dto;

import com.northernchile.api.payment.model.PaymentMethod;
import com.northernchile.api.payment.model.PaymentProvider;
import com.northernchile.api.validation.ValidReturnUrl;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Payment initialization request DTO.
 * Used to create a new payment transaction.
 */
public record PaymentInitReq(
    @NotNull(message = "Booking ID is required")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    UUID bookingId,

    @NotNull(message = "Payment provider is required")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    PaymentProvider provider,

    @NotNull(message = "Payment method is required")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    PaymentMethod paymentMethod,

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal amount,

    String currency,

    @ValidReturnUrl(nullable = true)
    String returnUrl,

    @ValidReturnUrl(nullable = true)
    String cancelUrl,

    String userEmail,
    String description,
    Integer expirationMinutes,
    List<UUID> additionalBookingIds,
    String idempotencyKey
) {
    public PaymentInitReq {
        if (currency == null) {
            currency = "CLP";
        }
    }
}
