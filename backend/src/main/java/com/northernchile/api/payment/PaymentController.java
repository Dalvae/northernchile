package com.northernchile.api.payment;

import com.northernchile.api.payment.dto.PaymentInitReq;
import com.northernchile.api.payment.dto.PaymentInitRes;
import com.northernchile.api.payment.dto.PaymentStatusRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Payment controller.
 * Handles payment-related operations including initiating payments, checking status, and processing refunds.
 */
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "Payment processing operations")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/init")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Initialize a payment", description = "Create a new payment transaction for a booking")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment initialized successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<PaymentInitRes> initializePayment(@Valid @RequestBody PaymentInitReq request) {
        log.info("Payment initialization request for booking: {}", request.getBookingId());
        PaymentInitRes response = paymentService.createPayment(request);
        return ResponseEntity.ok(response);
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

    @GetMapping("/confirm")
    @Operation(summary = "Confirm payment", description = "Confirm a payment after redirect (for Webpay, etc.)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment confirmed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid token"),
        @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentStatusRes> confirmPayment(
        @RequestParam(value = "token_ws", required = false) String webpayToken,
        @RequestParam(value = "token", required = false) String genericToken) {

        // Support both Webpay's token_ws and generic token parameter
        String token = webpayToken != null ? webpayToken : genericToken;

        if (token == null) {
            return ResponseEntity.badRequest().build();
        }

        log.info("Payment confirmation request with token");
        PaymentStatusRes response = paymentService.confirmPayment(token);
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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Illegal argument error", e);
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException e) {
        log.error("Illegal state error", e);
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        log.error("Runtime error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "Payment processing error: " + e.getMessage()));
    }
}
