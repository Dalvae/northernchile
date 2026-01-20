package com.northernchile.api.checkout;

import com.northernchile.api.checkout.dto.CheckoutRequest;
import com.northernchile.api.checkout.dto.CheckoutResult;
import com.northernchile.api.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for unified checkout operations.
 *
 * This is the single entry point for all booking creation. All checkout
 * operations go through here, which ensures consistent validation and
 * payment session creation.
 */
@RestController
@RequestMapping("/api/checkout")
@Tag(name = "Checkout", description = "Unified checkout endpoint for all bookings")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping
    @Operation(
        summary = "Initiate checkout",
        description = """
            Initiates checkout process for tour bookings. This is the unified entry point
            for ALL booking creation.

            **Checkout types:**
            - **Real payment** (default): Creates a payment session. The response includes
              `paymentUrl` (for Transbank) or `qrCode`/`pixCode` (for PIX) that the frontend
              should use to redirect/display payment options.
            - **Mock payment** (`mockPayment=true`): For testing. Creates payment session and
              immediately confirms it, returning `bookingIds` of created bookings.
            - **Admin bypass** (`adminBypass=true`): For admin-created bookings without payment.
              Requires SUPER_ADMIN or PARTNER_ADMIN role. Creates confirmed bookings immediately.

            **Flow:**
            1. Client calls this endpoint with checkout data
            2. Backend validates availability and cutoff time
            3. Backend creates PaymentSession
            4. For real payments: Returns `paymentUrl`/`qrCode` - client redirects user
            5. For mock/admin: Returns `bookingIds` - bookings already confirmed
            """,
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Checkout initiated successfully",
            content = @Content(schema = @Schema(implementation = CheckoutResult.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request (validation errors, schedule not found, etc.)"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Admin bypass requested without admin role"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Schedule is full or cutoff time passed"
        )
    })
    public ResponseEntity<CheckoutResult> initiateCheckout(
            @Valid @RequestBody CheckoutRequest request,
            @AuthenticationPrincipal User user) {

        CheckoutResult result = checkoutService.initiateCheckout(request, user);
        return ResponseEntity.ok(result);
    }
}
