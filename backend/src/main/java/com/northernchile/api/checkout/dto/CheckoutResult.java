package com.northernchile.api.checkout.dto;

import com.northernchile.api.payment.model.PaymentSessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Result of a checkout operation.
 *
 * The result varies based on the checkout type:
 * - Real payment: Contains paymentUrl/qrCode for user to complete payment
 * - Mock/Admin bypass: Contains bookingIds of immediately created bookings
 */
@Schema(description = "Result of checkout operation")
public record CheckoutResult(
    @Schema(description = "Payment session ID", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID sessionId,

    @Schema(description = "Current status of the checkout", requiredMode = Schema.RequiredMode.REQUIRED)
    PaymentSessionStatus status,

    @Schema(description = "URL to redirect user for payment (for redirect-based providers like Transbank)")
    String paymentUrl,

    @Schema(description = "Payment token")
    String token,

    @Schema(description = "QR code image (base64) for PIX payments")
    String qrCode,

    @Schema(description = "PIX code for copy/paste")
    String pixCode,

    @Schema(description = "When the payment session expires")
    Instant expiresAt,

    @Schema(description = "Whether this is a test/sandbox payment", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean isTest,

    @Schema(description = "Booking IDs created (populated for mock/admin bypass or after confirmation)")
    List<UUID> bookingIds,

    @Schema(description = "Type of checkout performed", requiredMode = Schema.RequiredMode.REQUIRED)
    CheckoutType checkoutType,

    @Schema(description = "Human-readable message about the checkout result")
    String message
) {
    /**
     * Type of checkout that was performed.
     */
    public enum CheckoutType {
        /**
         * Real payment - user needs to complete payment via paymentUrl or QR code.
         */
        REAL_PAYMENT,

        /**
         * Mock payment - booking created immediately for testing purposes.
         */
        MOCK_PAYMENT,

        /**
         * Admin bypass - booking created immediately by admin without payment.
         */
        ADMIN_BYPASS
    }

    /**
     * Creates a result for a real payment that requires user action.
     */
    public static CheckoutResult forRealPayment(
            UUID sessionId,
            String paymentUrl,
            String token,
            String qrCode,
            String pixCode,
            Instant expiresAt,
            boolean isTest
    ) {
        return new CheckoutResult(
            sessionId,
            PaymentSessionStatus.PENDING,
            paymentUrl,
            token,
            qrCode,
            pixCode,
            expiresAt,
            isTest,
            null,
            CheckoutType.REAL_PAYMENT,
            "Redirigir al usuario a paymentUrl o mostrar QR code para completar el pago"
        );
    }

    /**
     * Creates a result for mock/admin payments where bookings are created immediately.
     */
    public static CheckoutResult forImmediateBooking(
            UUID sessionId,
            List<UUID> bookingIds,
            boolean isTest,
            CheckoutType type
    ) {
        String message = type == CheckoutType.MOCK_PAYMENT
            ? "Pago de prueba completado. Reservas creadas inmediatamente."
            : "Reserva administrativa creada sin pago.";

        return new CheckoutResult(
            sessionId,
            PaymentSessionStatus.COMPLETED,
            null,
            null,
            null,
            null,
            null,
            isTest,
            bookingIds,
            type,
            message
        );
    }
}
