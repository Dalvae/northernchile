package com.northernchile.api.payment;

import com.northernchile.api.payment.dto.PaymentStatusRes;
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
import java.util.Map;
import java.util.UUID;

/**
 * Payment controller.
 * Handles payment status, refunds, and admin operations for Payment entities.
 * Note: Primary payment flow now uses PaymentSessionController.
 */
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Payment status and admin operations")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get payment status", description = "Get the current status of a payment by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment status retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentStatusRes> getPaymentStatus(@PathVariable("id") UUID paymentId) {
        log.info("Payment status request for: {}", paymentId);
        PaymentStatusRes response = paymentService.getPaymentStatus(paymentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PARTNER_ADMIN')")
    @Operation(summary = "Refund payment", description = "Refund a payment (full or partial)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment refunded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or payment cannot be refunded"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - admin access required"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentStatusRes> refundPayment(
        @PathVariable("id") UUID paymentId,
        @RequestBody(required = false) Map<String, Object> body) {

        log.info("Payment refund request for: {}", paymentId);

        // Extract refund amount from body (if partial refund)
        BigDecimal amount = null;
        if (body != null && body.containsKey("amount")) {
            amount = new BigDecimal(body.get("amount").toString());
        }

        PaymentStatusRes response = paymentService.refundPayment(paymentId, amount);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/booking/{bookingId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get booking payments", description = "Get all payments for a booking")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payments retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<?> getBookingPayments(@PathVariable("bookingId") UUID bookingId) {
        log.info("Getting payments for booking: {}", bookingId);
        var payments = paymentService.getBookingPayments(bookingId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/test")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PARTNER_ADMIN')")
    @Operation(summary = "Get all test payments", description = "Get all payments marked as test (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Test payments retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - admin access required")
    })
    public ResponseEntity<?> getTestPayments() {
        log.info("Getting all test payments");
        var testPayments = paymentService.getTestPayments();
        return ResponseEntity.ok(Map.of(
            "count", testPayments.size(),
            "payments", testPayments
        ));
    }

    @DeleteMapping("/test")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Delete all test payments", description = "Delete all payments marked as test (super admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Test payments deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - super admin access required")
    })
    public ResponseEntity<?> deleteTestPayments() {
        log.warn("Deleting all test payments - SUPER ADMIN action");
        int deletedCount = paymentService.deleteTestPayments();
        return ResponseEntity.ok(Map.of(
            "message", "Test payments deleted successfully",
            "deletedCount", deletedCount
        ));
    }

    // Exception handling is managed by GlobalExceptionHandler for consistent error responses
}
