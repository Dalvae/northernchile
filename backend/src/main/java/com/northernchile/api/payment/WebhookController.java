package com.northernchile.api.payment;

import com.northernchile.api.payment.dto.PaymentSessionRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/**
 * Webhook controller for payment providers.
 * Handles webhook notifications from MercadoPago.
 * Note: Transbank uses redirect-based flow, not webhooks.
 */
@RestController
@RequestMapping("/api/webhooks")
@Tag(name = "Webhooks", description = "Payment provider webhook endpoints")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final PaymentSessionService paymentSessionService;
    private final WebhookSecurityService webhookSecurityService;

    public WebhookController(PaymentSessionService paymentSessionService, WebhookSecurityService webhookSecurityService) {
        this.paymentSessionService = paymentSessionService;
        this.webhookSecurityService = webhookSecurityService;
    }

    @PostMapping("/mercadopago")
    @Operation(summary = "Mercado Pago webhook", description = "Handle Mercado Pago payment notifications")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Webhook processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid webhook payload"),
        @ApiResponse(responseCode = "401", description = "Invalid signature"),
        @ApiResponse(responseCode = "409", description = "Duplicate request")
    })
    public ResponseEntity<Map<String, String>> handleMercadoPagoWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String requestId) {

        log.info("Received Mercado Pago webhook with request ID: {}", requestId);

        try {
            // 1. Parse payload first to extract data.id for signature verification
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = new com.fasterxml.jackson.databind.ObjectMapper().readValue(rawBody, Map.class);

            // 2. Extract data.id from payload for signature verification
            String dataId = null;
            Object dataObj = payload.get("data");
            if (dataObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) dataObj;
                Object id = data.get("id");
                if (id != null) {
                    dataId = id.toString();
                }
            }

            // 3. Verify signature using the correct method with data.id, request-id, and x-signature
            if (!webhookSecurityService.verifyMercadoPagoSignature(dataId, requestId, xSignature)) {
                log.error("Mercado Pago webhook signature verification failed");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid signature"));
            }

            // 4. Check for duplicate requests
            if (requestId != null && webhookSecurityService.isDuplicateRequest(requestId)) {
                log.warn("Duplicate Mercado Pago webhook request detected: {}", requestId);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Duplicate request"));
            }

            // 5. Validate timestamp (if present in payload)
            if (dataObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) dataObj;
                Object dateCreated = data.get("date_created");
                if (dateCreated != null) {
                    try {
                        Instant timestamp = Instant.parse(dateCreated.toString());
                        if (!webhookSecurityService.isValidTimestamp(timestamp.getEpochSecond())) {
                            log.error("Mercado Pago webhook timestamp validation failed");
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("error", "Invalid or expired timestamp"));
                        }
                    } catch (Exception e) {
                        log.warn("Could not parse Mercado Pago timestamp: {}", dateCreated);
                    }
                }
            }

            // 6. Process webhook - confirm the payment session
            String lookupId = dataId; // data.id from MP notification
            PaymentSessionRes response = paymentSessionService.confirmMercadoPagoSession(lookupId, dataId);
            log.info("Mercado Pago webhook processed successfully: {}", response.sessionId());

            // 7. Mark request as processed
            if (requestId != null) {
                webhookSecurityService.markRequestAsProcessed(requestId);
            }

            return ResponseEntity.ok(Map.of("status", "processed"));

        } catch (Exception e) {
            log.error("Error processing Mercado Pago webhook", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/transbank")
    @Operation(summary = "Transbank webhook", description = "Handle Transbank payment notifications (if needed)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Webhook acknowledged"),
        @ApiResponse(responseCode = "501", description = "Transbank uses redirect-based flow, webhooks not supported")
    })
    public ResponseEntity<Map<String, String>> handleTransbankWebhook(@RequestBody Map<String, Object> payload) {
        log.info("Received Transbank webhook (note: Transbank uses redirect flow): {}", payload);

        // Transbank uses redirect-based flow, so webhooks are not typically used
        // Return 200 to acknowledge receipt but don't process
        return ResponseEntity.ok(Map.of(
            "message", "Transbank uses redirect-based flow, webhooks not required"
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Invalid webhook payload", e);
        return ResponseEntity.badRequest().body(Map.of("error", "Invalid webhook payload"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        log.error("Webhook processing error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "Webhook processing error"));
    }
}
