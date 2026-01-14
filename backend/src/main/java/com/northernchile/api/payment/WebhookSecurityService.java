package com.northernchile.api.payment;

import com.northernchile.api.config.properties.PaymentProperties;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for securing webhook endpoints against replay attacks and unauthorized requests.
 * Provides signature verification and request deduplication.
 *
 * Mercado Pago signature format (x-signature header):
 * ts=<timestamp>,v1=<hash>
 *
 * The hash is calculated as:
 * HMAC-SHA256(template, secret)
 *
 * Where template is:
 * id:<data.id>;request-id:<x-request-id>;ts:<ts>;
 */
@Service
public class WebhookSecurityService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookSecurityService.class);

    private final PaymentProperties paymentProperties;

    public WebhookSecurityService(PaymentProperties paymentProperties) {
        this.paymentProperties = paymentProperties;
    }

    // Store processed request IDs with their timestamps for deduplication
    // Note: In a multi-instance deployment, consider using database-based deduplication
    private final Map<String, Instant> processedRequests = new ConcurrentHashMap<>();

    // Maximum age for webhooks: 5 minutes
    private static final long MAX_WEBHOOK_AGE_SECONDS = 300;

    // Cleanup threshold: remove processed requests older than 1 hour
    private static final long CLEANUP_THRESHOLD_SECONDS = 3600;

    /**
     * Verify Mercado Pago webhook signature according to official documentation.
     *
     * The x-signature header format is: ts=<timestamp>,v1=<hash>
     * The signature is calculated using HMAC-SHA256 with the template:
     * id:<data.id>;request-id:<x-request-id>;ts:<timestamp>;
     *
     * @param dataId The data.id from the webhook payload
     * @param requestId The x-request-id header value
     * @param xSignature The x-signature header value (format: ts=xxx,v1=xxx)
     * @return true if signature is valid or if in test mode without signature
     */
    public boolean verifyMercadoPagoSignature(String dataId, String requestId, String xSignature) {
        // In test mode, skip signature verification entirely for easier testing
        if (paymentProperties.isTestMode()) {
            logger.warn("Mercado Pago webhook signature verification SKIPPED - test mode enabled");
            return true;
        }

        String mercadoPagoSecret = paymentProperties.getMercadopago().getWebhookSecret();
        if (mercadoPagoSecret == null || mercadoPagoSecret.isBlank()) {
            logger.error("Mercado Pago webhook secret not configured");
            return false;
        }

        if (xSignature == null || xSignature.isBlank()) {
            logger.error("Mercado Pago webhook x-signature header missing");
            return false;
        }

        try {
            // Parse x-signature header: ts=xxx,v1=xxx
            Map<String, String> signatureParts = parseSignatureHeader(xSignature);
            String ts = signatureParts.get("ts");
            String v1 = signatureParts.get("v1");

            if (ts == null || v1 == null) {
                logger.error("Mercado Pago webhook signature header malformed: {}", xSignature);
                return false;
            }

            // Build the template for signature verification
            // Format: id:<data.id>;request-id:<x-request-id>;ts:<timestamp>;
            StringBuilder template = new StringBuilder();
            template.append("id:").append(dataId != null ? dataId : "").append(";");
            if (requestId != null && !requestId.isBlank()) {
                template.append("request-id:").append(requestId).append(";");
            }
            template.append("ts:").append(ts).append(";");

            // Calculate HMAC-SHA256
            String computed = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, mercadoPagoSecret)
                .hmacHex(template.toString());

            boolean isValid = computed.equalsIgnoreCase(v1);

            if (!isValid) {
                logger.error("Mercado Pago webhook signature verification failed. " +
                    "Expected template: {}, Computed: {}, Received v1: {}",
                    template, computed, v1);
            } else {
                logger.debug("Mercado Pago webhook signature verified successfully");
            }

            return isValid;
        } catch (Exception e) {
            logger.error("Error verifying Mercado Pago webhook signature", e);
            return false;
        }
    }

    /**
     * Parse the x-signature header into its components.
     * Format: ts=xxx,v1=xxx
     *
     * @param signatureHeader The x-signature header value
     * @return Map with "ts" and "v1" keys
     */
    private Map<String, String> parseSignatureHeader(String signatureHeader) {
        Map<String, String> parts = new HashMap<>();

        if (signatureHeader == null || signatureHeader.isBlank()) {
            return parts;
        }

        String[] pairs = signatureHeader.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                parts.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }

        return parts;
    }

    /**
     * Legacy method - kept for backwards compatibility but deprecated.
     * Use verifyMercadoPagoSignature(dataId, requestId, xSignature) instead.
     *
     * @deprecated Use {@link #verifyMercadoPagoSignature(String, String, String)} instead
     */
    @Deprecated
    public boolean verifyMercadoPagoSignature(String body, String signature) {
        logger.warn("Using deprecated signature verification method");

        // In test mode, be lenient
        if (paymentProperties.isTestMode()) {
            logger.warn("Test mode: Skipping strict signature verification");
            return true;
        }

        String mercadoPagoSecret = paymentProperties.getMercadopago().getWebhookSecret();
        if (mercadoPagoSecret == null || mercadoPagoSecret.isBlank()) {
            logger.error("Mercado Pago webhook secret not configured");
            return false;
        }

        if (signature == null || signature.isBlank()) {
            logger.error("Mercado Pago webhook signature header missing");
            return false;
        }

        // Try to extract v1 from new format
        Map<String, String> parts = parseSignatureHeader(signature);
        String v1 = parts.get("v1");

        if (v1 != null) {
            // New format detected but using old method - log warning
            logger.warn("New signature format detected but using deprecated verification method");
        }

        try {
            String computed = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, mercadoPagoSecret).hmacHex(body);
            boolean isValid = computed.equalsIgnoreCase(signature) ||
                (v1 != null && computed.equalsIgnoreCase(v1));

            if (!isValid) {
                logger.error("Mercado Pago webhook signature verification failed");
            }

            return isValid;
        } catch (Exception e) {
            logger.error("Error verifying Mercado Pago webhook signature", e);
            return false;
        }
    }

    /**
     * Verify Transbank webhook signature (if Transbank implements webhooks in the future).
     * Currently, Transbank uses redirect-based flow.
     *
     * @param body The raw request body as string
     * @param signature The signature from header
     * @return true if signature is valid
     */
    public boolean verifyTransbankSignature(String body, String signature) {
        String transbankSecret = paymentProperties.getTransbank().getWebhookSecret();
        if (transbankSecret == null || transbankSecret.isBlank()) {
            logger.warn("Transbank webhook secret not configured");
            return false;
        }

        if (signature == null || signature.isBlank()) {
            logger.warn("Transbank webhook signature header missing");
            return false;
        }

        try {
            String computed = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, transbankSecret).hmacHex(body);
            boolean isValid = computed.equalsIgnoreCase(signature);

            if (!isValid) {
                logger.error("Transbank webhook signature verification failed");
            }

            return isValid;
        } catch (Exception e) {
            logger.error("Error verifying Transbank webhook signature", e);
            return false;
        }
    }

    /**
     * Check if a webhook request has already been processed (deduplication).
     * This prevents replay attacks where the same webhook is sent multiple times.
     *
     * @param requestId Unique request ID from webhook payload
     * @return true if this request has already been processed
     */
    public boolean isDuplicateRequest(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            logger.warn("Webhook request ID missing");
            return false; // Allow processing but log warning
        }

        // Cleanup old entries periodically
        cleanupOldEntries();

        Instant existingTimestamp = processedRequests.get(requestId);
        if (existingTimestamp != null) {
            logger.warn("Duplicate webhook request detected: {}", requestId);
            return true;
        }

        return false;
    }

    /**
     * Mark a request as processed.
     *
     * @param requestId Unique request ID to mark as processed
     */
    public void markRequestAsProcessed(String requestId) {
        if (requestId != null && !requestId.isBlank()) {
            processedRequests.put(requestId, Instant.now());
        }
    }

    /**
     * Validate webhook timestamp to prevent replay attacks with old webhooks.
     *
     * @param timestamp The webhook timestamp in seconds (Unix epoch)
     * @return true if timestamp is within acceptable range
     */
    public boolean isValidTimestamp(Long timestamp) {
        if (timestamp == null) {
            logger.warn("Webhook timestamp missing");
            return false;
        }

        Instant webhookTime = Instant.ofEpochSecond(timestamp);
        Instant now = Instant.now();
        long ageSeconds = now.getEpochSecond() - webhookTime.getEpochSecond();

        if (ageSeconds < 0) {
            logger.error("Webhook timestamp is in the future: {}", webhookTime);
            return false;
        }

        if (ageSeconds > MAX_WEBHOOK_AGE_SECONDS) {
            logger.error("Webhook timestamp too old: {} seconds (max: {})", ageSeconds, MAX_WEBHOOK_AGE_SECONDS);
            return false;
        }

        return true;
    }

    /**
     * Clean up old processed request IDs to prevent memory leaks.
     * Removes entries older than CLEANUP_THRESHOLD_SECONDS.
     */
    private void cleanupOldEntries() {
        Instant threshold = Instant.now().minusSeconds(CLEANUP_THRESHOLD_SECONDS);
        processedRequests.entrySet().removeIf(entry -> entry.getValue().isBefore(threshold));
    }

    /**
     * Clear all processed requests (useful for testing).
     */
    public void clearProcessedRequests() {
        processedRequests.clear();
    }
}
