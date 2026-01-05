package com.northernchile.api.payment;

import cl.transbank.webpay.exception.TransactionCommitException;
import cl.transbank.webpay.exception.TransactionCreateException;
import cl.transbank.webpay.webpayplus.WebpayPlus;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCommitResponse;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCreateResponse;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.*;
import com.mercadopago.resources.preference.Preference;
import com.northernchile.api.payment.dto.PaymentSessionReq;
import com.northernchile.api.payment.dto.PaymentSessionRes;
import com.northernchile.api.payment.model.PaymentProvider;
import com.northernchile.api.payment.model.PaymentSession;
import com.northernchile.api.payment.model.PaymentSessionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter to connect PaymentSession with existing payment providers.
 * Handles the provider-specific logic for creating and confirming payments.
 */
@Component
public class PaymentSessionPaymentAdapter {

    private static final Logger log = LoggerFactory.getLogger(PaymentSessionPaymentAdapter.class);

    @Value("${transbank.commerce-code:597055555532}")
    private String transbankCommerceCode;

    @Value("${transbank.api-key:579B532A7440BB0C9079DED94D31EA1615BACEB56610332264630D42D0A36B1C}")
    private String transbankApiKey;

    @Value("${transbank.environment:INTEGRATION}")
    private String transbankEnvironment;

    @Value("${mercadopago.access-token:}")
    private String mercadoPagoAccessToken;

    @Value("${payment.test-mode:false}")
    private boolean testMode;

    /**
     * Initialize payment with the appropriate provider.
     */
    public PaymentSessionRes initializePayment(PaymentSession session, PaymentSessionReq request) {
        return switch (session.getProvider()) {
            case TRANSBANK -> initializeTransbankPayment(session, request);
            case MERCADOPAGO -> initializeMercadoPagoPayment(session, request);
            default -> throw new IllegalArgumentException("Unsupported payment provider: " + session.getProvider());
        };
    }

    /**
     * Confirm payment with Transbank.
     */
    public PaymentSessionRes confirmPayment(PaymentSession session) {
        if (session.getProvider() == PaymentProvider.TRANSBANK) {
            return confirmTransbankPayment(session);
        }
        throw new IllegalArgumentException("Use confirmMercadoPagoPayment for MercadoPago");
    }

    /**
     * Confirm MercadoPago payment.
     */
    public PaymentSessionRes confirmMercadoPagoPayment(PaymentSession session, String preferenceId, String mpPaymentId) {
        return confirmMercadoPagoCheckout(session, preferenceId, mpPaymentId);
    }

    // === Transbank ===

    private PaymentSessionRes initializeTransbankPayment(PaymentSession session, PaymentSessionReq request) {
        log.info("Initializing Transbank payment for session: {}", session.getId());

        try {
            WebpayPlus.Transaction transaction = getTransbankTransaction();

            String buyOrder = "SESSION-" + session.getId().toString().substring(0, 8);
            String sessionId = "SID-" + System.currentTimeMillis();
            double amount = session.getTotalAmount().doubleValue();

            WebpayPlusTransactionCreateResponse response = transaction.create(
                buyOrder,
                sessionId,
                amount,
                request.returnUrl()
            );

            return new PaymentSessionRes(
                session.getId(),
                PaymentSessionStatus.PENDING,
                response.getUrl(),
                response.getToken(),
                null,
                null,
                session.getExpiresAt(),
                session.isTest(),
                null
            );

        } catch (TransactionCreateException | java.io.IOException e) {
            log.error("Error creating Transbank transaction for session: {}", session.getId(), e);
            throw new RuntimeException("Failed to create Transbank payment: " + e.getMessage(), e);
        }
    }

    private PaymentSessionRes confirmTransbankPayment(PaymentSession session) {
        log.info("Confirming Transbank payment for session: {}", session.getId());

        try {
            WebpayPlus.Transaction transaction = getTransbankTransaction();
            WebpayPlusTransactionCommitResponse response = transaction.commit(session.getToken());

            // Response code 0 = approved
            PaymentSessionStatus status = response.getResponseCode() == 0 ? 
                PaymentSessionStatus.COMPLETED : PaymentSessionStatus.FAILED;
            
            if (status == PaymentSessionStatus.COMPLETED) {
                log.info("Transbank payment approved for session: {}", session.getId());
            } else {
                log.warn("Transbank payment rejected for session: {} - response code: {}", 
                    session.getId(), response.getResponseCode());
            }

            return new PaymentSessionRes(
                session.getId(),
                status,
                null,
                session.getToken(),
                null,
                null,
                session.getExpiresAt(),
                session.isTest(),
                null
            );

        } catch (TransactionCommitException e) {
            // Check if transaction was aborted (user cancelled)
            String errorMessage = e.getMessage() != null ? e.getMessage() : "";
            if (errorMessage.contains("aborted")) {
                log.info("Transbank transaction aborted (user cancelled) for session: {}", session.getId());
                log.info("Transbank transaction aborted (user cancelled) for session: {}", session.getId());
                return new PaymentSessionRes(
                    session.getId(),
                    PaymentSessionStatus.CANCELLED,
                    null,
                    session.getToken(),
                    null,
                    null,
                    session.getExpiresAt(),
                    session.isTest(),
                    null
                );
            }
            
            log.error("Error confirming Transbank transaction for session: {}", session.getId(), e);
            return new PaymentSessionRes(
                session.getId(),
                PaymentSessionStatus.FAILED,
                null,
                session.getToken(),
                null,
                null,
                session.getExpiresAt(),
                session.isTest(),
                null
            );
        } catch (java.io.IOException e) {
            log.error("IO Error confirming Transbank transaction for session: {}", session.getId(), e);
            return new PaymentSessionRes(
                session.getId(),
                PaymentSessionStatus.FAILED,
                null,
                session.getToken(),
                null,
                null,
                session.getExpiresAt(),
                session.isTest(),
                null
            );
        }
    }

    private WebpayPlus.Transaction getTransbankTransaction() {
        if ("PRODUCTION".equalsIgnoreCase(transbankEnvironment)) {
            return WebpayPlus.Transaction.buildForProduction(transbankCommerceCode, transbankApiKey);
        }
        return WebpayPlus.Transaction.buildForIntegration(transbankCommerceCode, transbankApiKey);
    }

    // === MercadoPago ===

    private PaymentSessionRes initializeMercadoPagoPayment(PaymentSession session, PaymentSessionReq request) {
        log.info("Initializing MercadoPago payment for session: {} with method: {}",
            session.getId(), session.getPaymentMethod());

        // Use PIX API if payment method is PIX, otherwise use Checkout Pro
        if (session.getPaymentMethod() != null && "PIX".equals(session.getPaymentMethod().name())) {
            return initializeMercadoPagoPIX(session, request);
        } else {
            return initializeMercadoPagoCheckoutPro(session, request);
        }
    }

    private PaymentSessionRes initializeMercadoPagoCheckoutPro(PaymentSession session, PaymentSessionReq request) {
        log.info("Initializing MercadoPago Checkout Pro for session: {}", session.getId());

        try {
            MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);

            // Build items
            List<PreferenceItemRequest> items = new ArrayList<>();
            for (var sessionItem : session.getItems()) {
                PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .id(sessionItem.scheduleId().toString())
                    .title(sessionItem.tourName())
                    .quantity(sessionItem.numParticipants())
                    .unitPrice(sessionItem.pricePerPerson())
                    .currencyId(session.getCurrency())
                    .build();
                items.add(item);
            }

            // Build payer
            PreferencePayerRequest payer = PreferencePayerRequest.builder()
                .email(session.getUserEmail())
                .build();

            // Build back URLs
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(request.returnUrl())
                .pending(request.returnUrl())
                .failure(request.cancelUrl())
                .build();

            // Build preference
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(items)
                .payer(payer)
                .backUrls(backUrls)
                .autoReturn("approved")
                .externalReference(session.getId().toString())
                .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            // Store preference ID as external payment ID
            session.setExternalPaymentId(preference.getId());

            return new PaymentSessionRes(
                session.getId(),
                PaymentSessionStatus.PENDING,
                testMode ? preference.getSandboxInitPoint() : preference.getInitPoint(),
                preference.getId(),
                null,
                null,
                session.getExpiresAt(),
                session.isTest(),
                null
            );

        } catch (Exception e) {
            log.error("Error creating MercadoPago preference for session: {}", session.getId(), e);
            throw new RuntimeException("Failed to create MercadoPago payment: " + e.getMessage(), e);
        }
    }

    private PaymentSessionRes initializeMercadoPagoPIX(PaymentSession session, PaymentSessionReq request) {
        log.info("Initializing MercadoPago PIX payment for session: {}", session.getId());

        try {
            MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);

            // Use Payment API for PIX
            com.mercadopago.client.payment.PaymentClient client = new com.mercadopago.client.payment.PaymentClient();

            // Build payment request for PIX
            com.mercadopago.client.payment.PaymentCreateRequest paymentRequest =
                com.mercadopago.client.payment.PaymentCreateRequest.builder()
                    .transactionAmount(session.getTotalAmount())
                    .description(request.description() != null ? request.description() : "Tour booking")
                    .paymentMethodId("pix")
                    .payer(
                        com.mercadopago.client.payment.PaymentPayerRequest.builder()
                            .email(session.getUserEmail())
                            .build()
                    )
                    .externalReference(session.getId().toString())
                    .build();

            com.mercadopago.resources.payment.Payment payment = client.create(paymentRequest);

            // Store MercadoPago payment ID as external payment ID
            session.setExternalPaymentId(payment.getId().toString());

            log.info("PIX payment created: {} for session: {}", payment.getId(), session.getId());

            String qrCode = null;
            String pixCode = null;

            // Extract QR code from point_of_interaction
            if (payment.getPointOfInteraction() != null
                && payment.getPointOfInteraction().getTransactionData() != null) {

                var transactionData = payment.getPointOfInteraction().getTransactionData();

                // QR code base64 image
                if (transactionData.getQrCodeBase64() != null) {
                    qrCode = "data:image/png;base64," + transactionData.getQrCodeBase64();
                }

                // PIX copy-paste code
                if (transactionData.getQrCode() != null) {
                    pixCode = transactionData.getQrCode();
                }
            }

            return new PaymentSessionRes(
                session.getId(),
                PaymentSessionStatus.PENDING,
                null,
                payment.getId().toString(),
                qrCode,
                pixCode,
                session.getExpiresAt(),
                session.isTest(),
                null
            );

        } catch (Exception e) {
            log.error("Error creating MercadoPago PIX payment for session: {}", session.getId(), e);
            throw new RuntimeException("Failed to create MercadoPago PIX payment: " + e.getMessage(), e);
        }
    }

    private PaymentSessionRes confirmMercadoPagoCheckout(PaymentSession session, String preferenceId, String mpPaymentId) {
        log.info("Confirming MercadoPago payment for session: {} - payment_id: {}", session.getId(), mpPaymentId);

        try {
            MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);

            PaymentSessionStatus status = PaymentSessionStatus.PENDING;

            if (mpPaymentId != null && !mpPaymentId.isEmpty()) {
                // Verify payment status with MercadoPago
                com.mercadopago.client.payment.PaymentClient paymentClient = new com.mercadopago.client.payment.PaymentClient();
                com.mercadopago.resources.payment.Payment mpPayment = paymentClient.get(Long.parseLong(mpPaymentId));

                String mpStatus = mpPayment.getStatus();
                if ("approved".equals(mpStatus)) {
                    status = PaymentSessionStatus.COMPLETED;
                } else if ("pending".equals(mpStatus) || "in_process".equals(mpStatus)) {
                    status = PaymentSessionStatus.PENDING;
                } else {
                    status = PaymentSessionStatus.FAILED;
                }
            }

            return new PaymentSessionRes(
                session.getId(),
                status,
                null,
                session.getToken(),
                null,
                null,
                session.getExpiresAt(),
                session.isTest(),
                null
            );

        } catch (Exception e) {
            log.error("Error confirming MercadoPago payment for session: {}", session.getId(), e);
            return new PaymentSessionRes(
                session.getId(),
                PaymentSessionStatus.FAILED,
                null,
                session.getToken(),
                null,
                null,
                session.getExpiresAt(),
                session.isTest(),
                null
            );
        }
    }
}
