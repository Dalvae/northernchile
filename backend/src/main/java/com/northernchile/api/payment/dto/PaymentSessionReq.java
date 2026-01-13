package com.northernchile.api.payment.dto;

import com.northernchile.api.booking.dto.ParticipantReq;
import com.northernchile.api.payment.model.PaymentMethod;
import com.northernchile.api.payment.model.PaymentProvider;
import com.northernchile.api.validation.ValidReturnUrl;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Request to create a payment session.
 * Contains all checkout data: cart items with participants.
 */
public record PaymentSessionReq(
    @NotNull(message = "Payment provider is required")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    PaymentProvider provider,

    @NotNull(message = "Payment method is required")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    PaymentMethod paymentMethod,

    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal totalAmount,

    String currency,

    @NotEmpty(message = "At least one item is required")
    @Valid
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<PaymentSessionItemReq> items,

    @ValidReturnUrl(nullable = true)
    String returnUrl,

    @ValidReturnUrl(nullable = true)
    String cancelUrl,

    String userEmail,
    String description,
    String languageCode
) {
    public PaymentSessionReq {
        if (currency == null) currency = "CLP";
        if (languageCode == null) languageCode = "es";
    }

    public record PaymentSessionItemReq(
        @NotNull(message = "Schedule ID is required")
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        UUID scheduleId,

        String tourName,
        LocalDate tourDate,

        @Positive(message = "Number of participants must be positive")
        int numParticipants,

        BigDecimal pricePerPerson,
        BigDecimal itemTotal,
        String specialRequests,

        @NotEmpty(message = "Participants are required")
        @Valid
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        List<ParticipantReq> participants
    ) {}
}
