package com.northernchile.api.payment;

import com.northernchile.api.payment.dto.RefundRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Controller for refund operations.
 */
@RestController
@RequestMapping("/api/refunds")
@Tag(name = "Refunds", description = "Refund operations for bookings")
@SecurityRequirement(name = "bearerAuth")
public class RefundController {

    private static final Logger log = LoggerFactory.getLogger(RefundController.class);

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    /**
     * Process a refund for a booking (admin only).
     * Admins can override the 24-hour cancellation policy.
     * Supports partial refunds by specifying an amount less than the total.
     */
    @PostMapping("/booking/{bookingId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PARTNER_ADMIN')")
    @Operation(summary = "Refund a booking", description = "Process a full or partial refund for a booking (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Refund processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or refund not possible"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - admin access required"),
        @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<RefundRes> refundBooking(
            @PathVariable("bookingId") UUID bookingId,
            @RequestParam(value = "adminOverride", defaultValue = "false") boolean adminOverride,
            @RequestParam(value = "amount", required = false) BigDecimal amount) {

        log.info("Admin refund request for booking: {} (override: {}, amount: {})", 
            bookingId, adminOverride, amount != null ? amount : "FULL");
        
        RefundRes result = refundService.refundBooking(bookingId, adminOverride, amount);
        
        return ResponseEntity.ok(result);
    }

    /**
     * User-initiated cancellation with refund.
     * Only allowed if more than 24 hours before the tour.
     */
    @PostMapping("/booking/{bookingId}/cancel")
    @PreAuthorize("@authorizationService.isBookingUser(authentication, #bookingId)")
    @Operation(summary = "Cancel booking with refund", description = "User cancels their booking and requests refund (24h policy applies)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cancellation and refund processed"),
        @ApiResponse(responseCode = "400", description = "Cannot cancel - less than 24h before tour"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Not the booking owner"),
        @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<RefundRes> cancelBookingWithRefund(@PathVariable("bookingId") UUID bookingId) {
        log.info("User cancellation request for booking: {}", bookingId);
        
        // User requests - no admin override, 24h policy applies
        RefundRes result = refundService.refundBooking(bookingId, false);
        
        return ResponseEntity.ok(result);
    }
}
