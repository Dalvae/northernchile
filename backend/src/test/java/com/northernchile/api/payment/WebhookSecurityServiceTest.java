package com.northernchile.api.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebhookSecurityService Tests")
class WebhookSecurityServiceTest {

    private WebhookSecurityService webhookSecurityService;

    private static final String TEST_MP_SECRET = "test-mercadopago-secret-key";
    private static final String TEST_TB_SECRET = "test-transbank-secret-key";

    @BeforeEach
    void setUp() {
        webhookSecurityService = new WebhookSecurityService();
        ReflectionTestUtils.setField(webhookSecurityService, "mercadoPagoSecret", TEST_MP_SECRET);
        ReflectionTestUtils.setField(webhookSecurityService, "transbankSecret", TEST_TB_SECRET);
    }

    @Nested
    @DisplayName("Mercado Pago Signature Verification")
    class MercadoPagoSignatureTests {

        @Test
        @DisplayName("Should accept valid Mercado Pago signature")
        void shouldAcceptValidMercadoPagoSignature() {
            // Given
            String body = "{\"type\":\"payment\",\"id\":\"12345\"}";
            // Generate valid HMAC-SHA256 signature
            String validSignature = generateHmacSha256(body, TEST_MP_SECRET);

            // When
            boolean result = webhookSecurityService.verifyMercadoPagoSignature(body, validSignature);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should reject invalid Mercado Pago signature")
        void shouldRejectInvalidMercadoPagoSignature() {
            // Given
            String body = "{\"type\":\"payment\",\"id\":\"12345\"}";
            String invalidSignature = "invalid-signature-value";

            // When
            boolean result = webhookSecurityService.verifyMercadoPagoSignature(body, invalidSignature);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should reject tampered payload")
        void shouldRejectTamperedPayload() {
            // Given
            String originalBody = "{\"type\":\"payment\",\"id\":\"12345\"}";
            String tamperedBody = "{\"type\":\"payment\",\"id\":\"99999\"}";
            String signatureForOriginal = generateHmacSha256(originalBody, TEST_MP_SECRET);

            // When - Use original signature with tampered body
            boolean result = webhookSecurityService.verifyMercadoPagoSignature(tamperedBody, signatureForOriginal);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should reject null signature")
        void shouldRejectNullSignature() {
            // Given
            String body = "{\"type\":\"payment\",\"id\":\"12345\"}";

            // When
            boolean result = webhookSecurityService.verifyMercadoPagoSignature(body, null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should reject empty signature")
        void shouldRejectEmptySignature() {
            // Given
            String body = "{\"type\":\"payment\",\"id\":\"12345\"}";

            // When
            boolean result = webhookSecurityService.verifyMercadoPagoSignature(body, "");

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should reject when secret not configured")
        void shouldRejectWhenSecretNotConfigured() {
            // Given
            ReflectionTestUtils.setField(webhookSecurityService, "mercadoPagoSecret", "");
            String body = "{\"type\":\"payment\",\"id\":\"12345\"}";
            String signature = "some-signature";

            // When
            boolean result = webhookSecurityService.verifyMercadoPagoSignature(body, signature);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Transbank Signature Verification")
    class TransbankSignatureTests {

        @Test
        @DisplayName("Should accept valid Transbank signature")
        void shouldAcceptValidTransbankSignature() {
            // Given
            String body = "{\"token\":\"abc123\",\"status\":\"AUTHORIZED\"}";
            String validSignature = generateHmacSha256(body, TEST_TB_SECRET);

            // When
            boolean result = webhookSecurityService.verifyTransbankSignature(body, validSignature);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should reject invalid Transbank signature")
        void shouldRejectInvalidTransbankSignature() {
            // Given
            String body = "{\"token\":\"abc123\",\"status\":\"AUTHORIZED\"}";
            String invalidSignature = "wrong-signature";

            // When
            boolean result = webhookSecurityService.verifyTransbankSignature(body, invalidSignature);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should reject when Transbank secret not configured")
        void shouldRejectWhenTransbankSecretNotConfigured() {
            // Given
            ReflectionTestUtils.setField(webhookSecurityService, "transbankSecret", null);
            String body = "{\"token\":\"abc123\"}";
            String signature = "some-signature";

            // When
            boolean result = webhookSecurityService.verifyTransbankSignature(body, signature);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Timestamp Validation (Replay Attack Prevention)")
    class TimestampValidationTests {

        @Test
        @DisplayName("Should accept valid timestamp within allowed range")
        void shouldAcceptValidTimestamp() {
            // Given - Current timestamp
            long currentTimestamp = Instant.now().getEpochSecond();

            // When
            boolean result = webhookSecurityService.isValidTimestamp(currentTimestamp);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should accept timestamp 4 minutes old (within 5 min limit)")
        void shouldAcceptRecentTimestamp() {
            // Given - 4 minutes ago (less than MAX_WEBHOOK_AGE_SECONDS=300)
            long recentTimestamp = Instant.now().minusSeconds(240).getEpochSecond();

            // When
            boolean result = webhookSecurityService.isValidTimestamp(recentTimestamp);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should reject timestamp older than 5 minutes")
        void shouldRejectOldTimestamp() {
            // Given - 10 minutes ago (more than MAX_WEBHOOK_AGE_SECONDS=300)
            long oldTimestamp = Instant.now().minusSeconds(600).getEpochSecond();

            // When
            boolean result = webhookSecurityService.isValidTimestamp(oldTimestamp);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should reject future timestamp")
        void shouldRejectFutureTimestamp() {
            // Given - 10 minutes in the future
            long futureTimestamp = Instant.now().plusSeconds(600).getEpochSecond();

            // When
            boolean result = webhookSecurityService.isValidTimestamp(futureTimestamp);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should reject null timestamp")
        void shouldRejectNullTimestamp() {
            // When
            boolean result = webhookSecurityService.isValidTimestamp(null);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Duplicate Request Detection (Idempotency)")
    class DuplicateRequestTests {

        @Test
        @DisplayName("Should not detect first request as duplicate")
        void shouldNotDetectFirstRequestAsDuplicate() {
            // Given
            String requestId = "unique-request-123";
            webhookSecurityService.clearProcessedRequests();

            // When
            boolean isDuplicate = webhookSecurityService.isDuplicateRequest(requestId);

            // Then
            assertThat(isDuplicate).isFalse();
        }

        @Test
        @DisplayName("Should detect second request with same ID as duplicate")
        void shouldDetectSecondRequestAsDuplicate() {
            // Given
            String requestId = "duplicate-request-456";
            webhookSecurityService.clearProcessedRequests();

            // First request
            webhookSecurityService.isDuplicateRequest(requestId);
            webhookSecurityService.markRequestAsProcessed(requestId);

            // When - Second request with same ID
            boolean isDuplicate = webhookSecurityService.isDuplicateRequest(requestId);

            // Then
            assertThat(isDuplicate).isTrue();
        }

        @Test
        @DisplayName("Should allow different request IDs")
        void shouldAllowDifferentRequestIds() {
            // Given
            webhookSecurityService.clearProcessedRequests();
            webhookSecurityService.markRequestAsProcessed("request-1");

            // When
            boolean isDuplicate = webhookSecurityService.isDuplicateRequest("request-2");

            // Then
            assertThat(isDuplicate).isFalse();
        }

        @Test
        @DisplayName("Should handle null request ID gracefully")
        void shouldHandleNullRequestId() {
            // When
            boolean isDuplicate = webhookSecurityService.isDuplicateRequest(null);

            // Then - Null is allowed but logged as warning
            assertThat(isDuplicate).isFalse();
        }

        @Test
        @DisplayName("Should handle blank request ID gracefully")
        void shouldHandleBlankRequestId() {
            // When
            boolean isDuplicate = webhookSecurityService.isDuplicateRequest("   ");

            // Then
            assertThat(isDuplicate).isFalse();
        }
    }

    /**
     * Helper method to generate HMAC-SHA256 signature for testing
     */
    private String generateHmacSha256(String data, String secret) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                    secret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC", e);
        }
    }
}
