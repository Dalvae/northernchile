package com.northernchile.api.payment;

import com.northernchile.api.payment.dto.PaymentStatusRes;
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
 * Handles webhook notifications from Transbank, Mercado Pago, and other payment providers.
 */
@RestController
@RequestMapping("/api/webhooks")
@Tag(name = "Webhooks", description = "Payment provider webhook endpoints")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final PaymentService paymentService;

    public WebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/mercadopago")
    @Operation(summary = "Mercado Pago webhook", description = "Handle Mercado Pago payment notifications")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Webhook processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid webhook payload")
    })
    public ResponseEntity<Void> handleMercadoPagoWebhook(@RequestBody Map<String, Object> payload) {
        log.info("Received Mercado Pago webhook: {}", payload);

        try {
            // Process webhook
            PaymentStatusRes response = paymentService.processWebhook("MERCADOPAGO", payload);
            log.info("Mercado Pago webhook processed successfully: {}", response.getPaymentId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error processing Mercado Pago webhook", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
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

    @PostMapping("/payment")
    @Operation(summary = "Generic payment webhook", description = "Handle generic payment notifications")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Webhook processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid webhook payload")
    })
    public ResponseEntity<Void> handleGenericPaymentWebhook(
        @RequestParam(value = "provider", required = false) String provider,
        @RequestBody Map<String, Object> payload) {

        log.info("Received generic payment webhook from provider: {} - {}", provider, payload);

        try {
            if (provider != null) {
                PaymentStatusRes response = paymentService.processWebhook(provider, payload);
                log.info("Generic webhook processed successfully: {}", response.getPaymentId());
            } else {
                log.warn("Webhook received without provider parameter");
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error processing generic payment webhook", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Invalid webhook payload", e);
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        log.error("Webhook processing error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "Webhook processing error"));
    }
}
