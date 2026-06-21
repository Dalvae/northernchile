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
            @RequestBody(required = false) String rawBody,
            @RequestParam(required = false) Map<String, String> queryParams,
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String requestId) {

        log.info("Received Mercado Pago webhook with request ID: {}", requestId);

        try {
            Map<String, String> query = queryParams != null ? queryParams : Map.of();

            // MercadoPago delivers the resource id in the query string (data.id) for modern
            // webhooks, and/or in the JSON body. The "type"/"topic" field says what it is.
            // CRITICAL: the x-signature manifest is built from the query-string data.id.
            String queryDataId = query.get("data.id");
            String queryId = query.get("id");
            String type = firstNonBlank(query.get("type"), query.get("topic"));

            // Parse the body defensively. Possible shapes:
            //   { "data": { "id": "12345" }, "type": "payment" }   (modern)
            //   { "resource": "12345", "topic": "payment" }         (legacy IPN)
            //   { "resource": ".../merchant_orders/9", "topic": "merchant_order" }
            String bodyDataId = null;
            String bodyResource = null;
            if (rawBody != null && !rawBody.isBlank()) {
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> payload = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(rawBody, Map.class);
                    if (payload.get("data") instanceof Map<?, ?> data && data.get("id") != null) {
                        bodyDataId = data.get("id").toString();
                    }
                    if (type == null && payload.get("type") != null) {
                        type = payload.get("type").toString();
                    }
                    if (type == null && payload.get("topic") != null) {
                        type = payload.get("topic").toString();
                    }
                    if (payload.get("resource") != null) {
                        bodyResource = payload.get("resource").toString();
                    }
                } catch (Exception e) {
                    log.warn("Could not parse Mercado Pago webhook body: {}", e.getMessage());
                }
            }

            // Id MercadoPago used to build the signature manifest (query data.id preferred).
            String signatureId = firstNonBlank(queryDataId, bodyDataId, queryId);

            // 1. Verify signature
            if (!webhookSecurityService.verifyMercadoPagoSignature(signatureId, requestId, xSignature)) {
                log.error("Mercado Pago webhook signature verification failed");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid signature"));
            }

            // 2. Deduplicate by request id (replay protection). MercadoPago retries the same
            //    notification for a long time, so we do NOT reject by age here.
            if (requestId != null && webhookSecurityService.isDuplicateRequest(requestId)) {
                log.warn("Duplicate Mercado Pago webhook request detected: {}", requestId);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Duplicate request"));
            }

            // 3. We only act on payment notifications. Anything else (merchant_order, test
            //    pings, etc.) is acknowledged so MercadoPago stops retrying it.
            if (!"payment".equalsIgnoreCase(type)) {
                log.info("MercadoPago webhook type '{}' - acknowledging without processing", type);
                return ResponseEntity.ok(Map.of("status", "acknowledged", "reason", "not_a_payment"));
            }

            // 4. Resolve the MercadoPago payment id. For legacy IPN the numeric payment id
            //    can arrive in the body "resource".
            String resourceId = (bodyResource != null && bodyResource.matches("\\d+")) ? bodyResource : null;
            String paymentId = firstNonBlank(queryDataId, bodyDataId, queryId, resourceId);
            if (paymentId == null || paymentId.isBlank()) {
                log.info("MercadoPago payment webhook without payment id - acknowledging");
                return ResponseEntity.ok(Map.of("status", "acknowledged", "reason", "no_payment_id"));
            }

            // 5. Confirm the session. Pass ONLY the payment id; the service fetches the payment
            //    from MercadoPago to recover our session UUID (external_reference).
            PaymentSessionRes response = paymentSessionService.confirmMercadoPagoSession(paymentId, null);
            log.info("Mercado Pago webhook processed successfully: {}", response.sessionId());

            // 6. Mark request as processed
            if (requestId != null) {
                webhookSecurityService.markRequestAsProcessed(requestId);
            }

            return ResponseEntity.ok(Map.of("status", "processed"));

        } catch (Exception e) {
            log.error("Error processing Mercado Pago webhook", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "error"));
        }
    }

    /**
     * Returns the first non-null, non-blank value, or null if none qualify.
     */
    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
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
