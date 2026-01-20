package com.northernchile.api.checkout.dto;

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
 * Unified checkout request.
 * All bookings flow through checkout, which creates a PaymentSession first.
 *
 * This is the single entry point for all booking creation:
 * - Real payments (Transbank, MercadoPago): Creates session, user completes payment
 * - Mock payments (testing): Creates session, immediately confirms
 * - Admin bypass: Creates session with immediate confirmation
 */
@Schema(description = "Request to initiate checkout process")
public record CheckoutRequest(
    @NotNull(message = "Payment provider is required")
    @Schema(description = "Payment provider to use", requiredMode = Schema.RequiredMode.REQUIRED)
    PaymentProvider provider,

    @NotNull(message = "Payment method is required")
    @Schema(description = "Payment method", requiredMode = Schema.RequiredMode.REQUIRED)
    PaymentMethod paymentMethod,

    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    @Schema(description = "Total amount including taxes", requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal totalAmount,

    @Schema(description = "Currency code (default: CLP)")
    String currency,

    @NotEmpty(message = "At least one item is required")
    @Valid
    @Schema(description = "Items to checkout", requiredMode = Schema.RequiredMode.REQUIRED)
    List<CheckoutItemRequest> items,

    @ValidReturnUrl(nullable = true)
    @Schema(description = "URL to redirect after successful payment")
    String returnUrl,

    @ValidReturnUrl(nullable = true)
    @Schema(description = "URL to redirect after cancelled payment")
    String cancelUrl,

    @Schema(description = "Customer email for notifications")
    String userEmail,

    @Schema(description = "Payment description")
    String description,

    @Schema(description = "Language code for emails and UI (es, en, pt)")
    String languageCode,

    @Schema(description = "If true, creates booking immediately without real payment (for testing/admin)")
    Boolean mockPayment,

    @Schema(description = "If true, skips payment entirely and creates confirmed booking (admin only)")
    Boolean adminBypass
) {
    public CheckoutRequest {
        if (currency == null) currency = "CLP";
        if (languageCode == null) languageCode = "es";
        if (mockPayment == null) mockPayment = false;
        if (adminBypass == null) adminBypass = false;
    }

    /**
     * Item in the checkout request.
     */
    @Schema(description = "Individual item in checkout")
    public record CheckoutItemRequest(
        @NotNull(message = "Schedule ID is required")
        @Schema(description = "Tour schedule ID", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID scheduleId,

        @Schema(description = "Tour name (optional, will be fetched from schedule)")
        String tourName,

        @Schema(description = "Tour date (optional, will be fetched from schedule)")
        LocalDate tourDate,

        @Positive(message = "Number of participants must be positive")
        @Schema(description = "Number of participants", requiredMode = Schema.RequiredMode.REQUIRED)
        int numParticipants,

        @Schema(description = "Price per person (optional, will be validated against schedule)")
        BigDecimal pricePerPerson,

        @Schema(description = "Item total (optional, will be calculated)")
        BigDecimal itemTotal,

        @Schema(description = "Special requests for this booking")
        String specialRequests,

        @NotEmpty(message = "Participants are required")
        @Valid
        @Schema(description = "Participant details", requiredMode = Schema.RequiredMode.REQUIRED)
        List<ParticipantReq> participants
    ) {}
}
