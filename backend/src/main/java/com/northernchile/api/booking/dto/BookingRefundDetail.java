package com.northernchile.api.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Details of a single booking refund processed during schedule cancellation.
 */
@Schema(description = "Result of processing a single booking refund")
public record BookingRefundDetail(
    @Schema(description = "Booking ID", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID bookingId,

    @Schema(description = "Customer full name", requiredMode = Schema.RequiredMode.REQUIRED)
    String customerName,

    @Schema(description = "Customer email address", requiredMode = Schema.RequiredMode.REQUIRED)
    String customerEmail,

    @Schema(description = "Amount refunded (after retention)", requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal refundAmount,

    @Schema(description = "Refund status: SUCCESS, FAILED, NO_PAYMENT, ALREADY_CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED)
    String status,

    @Schema(description = "Error message if refund failed")
    String errorMessage
) {
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_NO_PAYMENT = "NO_PAYMENT";
    public static final String STATUS_ALREADY_CANCELLED = "ALREADY_CANCELLED";

    public static BookingRefundDetail success(UUID bookingId, String customerName, String customerEmail, BigDecimal refundAmount) {
        return new BookingRefundDetail(bookingId, customerName, customerEmail, refundAmount, STATUS_SUCCESS, null);
    }

    public static BookingRefundDetail failed(UUID bookingId, String customerName, String customerEmail, String errorMessage) {
        return new BookingRefundDetail(bookingId, customerName, customerEmail, BigDecimal.ZERO, STATUS_FAILED, errorMessage);
    }

    public static BookingRefundDetail noPayment(UUID bookingId, String customerName, String customerEmail) {
        return new BookingRefundDetail(bookingId, customerName, customerEmail, BigDecimal.ZERO, STATUS_NO_PAYMENT, "No payment record found");
    }

    public static BookingRefundDetail alreadyCancelled(UUID bookingId, String customerName, String customerEmail) {
        return new BookingRefundDetail(bookingId, customerName, customerEmail, BigDecimal.ZERO, STATUS_ALREADY_CANCELLED, "Booking was already cancelled");
    }
}
