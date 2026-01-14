package com.northernchile.api.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Result of cancelling a schedule with cascade refunds.
 */
@Schema(description = "Result of schedule cancellation with cascade refunds")
public record ScheduleCancellationResult(
    @Schema(description = "Schedule ID that was cancelled", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID scheduleId,

    @Schema(description = "Tour name", requiredMode = Schema.RequiredMode.REQUIRED)
    String tourName,

    @Schema(description = "Original scheduled date/time", requiredMode = Schema.RequiredMode.REQUIRED)
    Instant scheduledDateTime,

    @Schema(description = "Reason for cancellation", requiredMode = Schema.RequiredMode.REQUIRED)
    CancellationReason reason,

    @Schema(description = "Total number of bookings affected", requiredMode = Schema.RequiredMode.REQUIRED)
    int totalBookings,

    @Schema(description = "Number of refunds successfully processed", requiredMode = Schema.RequiredMode.REQUIRED)
    int refundsProcessed,

    @Schema(description = "Number of refunds that failed", requiredMode = Schema.RequiredMode.REQUIRED)
    int refundsFailed,

    @Schema(description = "Total amount refunded across all bookings", requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal totalRefundedAmount,

    @Schema(description = "Details of each booking refund", requiredMode = Schema.RequiredMode.REQUIRED)
    List<BookingRefundDetail> bookingDetails
) {
    /**
     * Check if all refunds were successful.
     */
    public boolean isFullySuccessful() {
        return refundsFailed == 0;
    }

    /**
     * Check if the schedule had any bookings to process.
     */
    public boolean hadBookings() {
        return totalBookings > 0;
    }
}
