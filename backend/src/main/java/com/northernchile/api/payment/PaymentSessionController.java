package com.northernchile.api.payment;

import com.northernchile.api.model.User;
import com.northernchile.api.payment.dto.PaymentSessionReq;
import com.northernchile.api.payment.dto.PaymentSessionRes;
import com.northernchile.api.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for payment sessions.
 * Implements the "payment first" checkout flow where bookings are created only after successful payment.
 */
@RestController
@RequestMapping("/api/payment-sessions")
@Tag(name = "Payment Sessions", description = "Checkout and payment session operations")
@SecurityRequirement(name = "bearerAuth")
public class PaymentSessionController {

    private static final Logger log = LoggerFactory.getLogger(PaymentSessionController.class);

    private final PaymentSessionService sessionService;
    private final UserRepository userRepository;

    public PaymentSessionController(PaymentSessionService sessionService, UserRepository userRepository) {
        this.sessionService = sessionService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create payment session", description = "Create a new payment session with cart items and participant data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment session created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or items not available"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<PaymentSessionRes> createSession(
            @Valid @RequestBody PaymentSessionReq request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Creating payment session for user: {}", userDetails.getUsername());

        User user = userRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new IllegalStateException("User not found"));

        PaymentSessionRes response = sessionService.createSession(request, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get session status", description = "Get the current status of a payment session")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Session status retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<PaymentSessionRes> getSessionStatus(@PathVariable("id") java.util.UUID sessionId) {
        log.info("Session status request for: {}", sessionId);
        PaymentSessionRes response = sessionService.getSessionStatus(sessionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/confirm")
    @Operation(summary = "Confirm payment (Transbank)", description = "Confirm a Transbank payment after redirect")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment confirmed, bookings created"),
        @ApiResponse(responseCode = "400", description = "Invalid or expired session"),
        @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<PaymentSessionRes> confirmPayment(
            @RequestParam(value = "token_ws", required = false) String webpayToken,
            @RequestParam(value = "token", required = false) String genericToken) {

        String token = webpayToken != null ? webpayToken : genericToken;

        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        log.info("Confirming payment session with token");
        PaymentSessionRes response = sessionService.confirmSession(token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/confirm/mercadopago")
    @Operation(summary = "Confirm payment (MercadoPago)", description = "Confirm a MercadoPago payment after redirect")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment confirmed, bookings created"),
        @ApiResponse(responseCode = "400", description = "Missing payment_id or external_reference"),
        @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<PaymentSessionRes> confirmMercadoPagoPayment(
            @RequestParam(value = "preference_id", required = false) String preferenceId,
            @RequestParam(value = "payment_id", required = false) String mpPaymentId,
            @RequestParam(value = "collection_status", required = false) String collectionStatus,
            @RequestParam(value = "external_reference", required = false) String externalReference) {

        log.info("Confirming MercadoPago session - preference: {}, payment: {}, status: {}, external_ref: {}",
            preferenceId, mpPaymentId, collectionStatus, externalReference);

        // We need payment_id to verify with MercadoPago API, or external_reference to find session directly
        if (mpPaymentId == null && externalReference == null) {
            log.warn("MercadoPago callback without payment_id or external_reference");
            return ResponseEntity.badRequest().build();
        }

        // Pass mpPaymentId as first argument - confirmMercadoPagoSession uses it to verify with MP API
        // and extract external_reference (our session ID) from the payment
        PaymentSessionRes response = sessionService.confirmMercadoPagoSession(mpPaymentId, externalReference);
        return ResponseEntity.ok(response);
    }
}
