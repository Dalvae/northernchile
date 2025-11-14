package com.northernchile.api.payment;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for securing webhook endpoints against replay attacks and unauthorized requests.
 * Provides signature verification and request deduplication.
 */
@Service
public class WebhookSecurityService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookSecurityService.class);

    @Value("${mercadopago.webhook-secret:${MERCADOPAGO_ACCESS_TOKEN:}}")
    private String mercadoPagoSecret;

    @Value("${transbank.webhook-secret:${TRANSBANK_API_KEY:}}")
    private String transbankSecret;

    // Store processed request IDs with their timestamps for deduplication
    // In production, use Redis or a database table for distributed systems
    private final Map<String, Instant> processedRequests = new ConcurrentHashMap<>();

    // Maximum age for webhooks: 5 minutes
    private static final long MAX_WEBHOOK_AGE_SECONDS = 300;

    // Cleanup threshold: remove processed requests older than 1 hour
    private static final long CLEANUP_THRESHOLD_SECONDS = 3600;

    /**
     * Verify Mercado Pago webhook signature.
     *
     * @param body The raw request body as string
     * @param signature The signature from x-signature header
     * @return true if signature is valid
     */
    public boolean verifyMercadoPagoSignature(String body, String signature) {
        if (mercadoPagoSecret == null || mercadoPagoSecret.isBlank()) {
            logger.warn("Mercado Pago webhook secret not configured");
            return false;
        }

        if (signature == null || signature.isBlank()) {
            logger.warn("Mercado Pago webhook signature header missing");
            return false;
        }

        try {
            String computed = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, mercadoPagoSecret).hmacHex(body);
            boolean isValid = computed.equalsIgnoreCase(signature);

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
