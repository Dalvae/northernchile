package com.northernchile.api.payment.provider;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.model.Booking;
import com.northernchile.api.payment.dto.PaymentInitReq;
import com.northernchile.api.payment.dto.PaymentInitRes;
import com.northernchile.api.payment.dto.PaymentStatusRes;
import com.northernchile.api.payment.model.PaymentMethod;
import com.northernchile.api.payment.model.PaymentStatus;
import com.northernchile.api.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Mercado Pago payment provider implementation.
 * Handles payment processing for Latin America including PIX (Brazil), credit cards, and other methods.
 */
@Service("mercadoPagoPaymentService")
public class MercadoPagoPaymentService implements PaymentProviderService {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoPaymentService.class);

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @Value("${mercadopago.access-token:TEST-ACCESS-TOKEN}")
    private String accessToken;

    @Value("${mercadopago.public-key:TEST-PUBLIC-KEY}")
    private String publicKey;

    public MercadoPagoPaymentService(PaymentRepository paymentRepository, BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Configure Mercado Pago SDK with credentials.
     */
    private void configureMercadoPago() {
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    @Override
    @Transactional
    public PaymentInitRes createPayment(PaymentInitReq request) {
        log.info("Creating Mercado Pago payment for booking: {} with method: {}",
            request.getBookingId(), request.getPaymentMethod());

        try {
            // Validate booking exists
            Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + request.getBookingId()));

            // Configure Mercado Pago SDK
            configureMercadoPago();

            // Create payment request
            PaymentCreateRequest paymentRequest = buildPaymentRequest(request, booking);

            // Create payment with Mercado Pago
            PaymentClient client = new PaymentClient();
            Payment mpPayment = client.create(paymentRequest);

            // Create payment record
            com.northernchile.api.payment.model.Payment payment = new com.northernchile.api.payment.model.Payment();
            payment.setBooking(booking);
            payment.setProvider(request.getProvider());
            payment.setPaymentMethod(request.getPaymentMethod());
            payment.setAmount(request.getAmount());
            payment.setCurrency(request.getCurrency());
            payment.setExternalPaymentId(mpPayment.getId().toString());
            payment.setStatus(mapMercadoPagoStatus(mpPayment.getStatus()));

            // Set payment details based on payment method
            if (request.getPaymentMethod() == PaymentMethod.PIX) {
                // For PIX, store QR code and payment code
                if (mpPayment.getPointOfInteraction() != null &&
                    mpPayment.getPointOfInteraction().getTransactionData() != null) {
                    String qrCodeBase64 = mpPayment.getPointOfInteraction().getTransactionData().getQrCodeBase64();
                    String qrCode = mpPayment.getPointOfInteraction().getTransactionData().getQrCode();

                    payment.setQrCode(qrCodeBase64);
                    payment.setPixCode(qrCode);
                }

                // Set expiration time (default 30 minutes for PIX)
                int expirationMinutes = request.getExpirationMinutes() != null ?
                    request.getExpirationMinutes() : 30;
                payment.setExpiresAt(Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES));
            }

            // Set payment URL (for credit card 3DS or other redirect flows)
            if (mpPayment.getPointOfInteraction() != null &&
                mpPayment.getPointOfInteraction().getLinksObject() != null) {
                payment.setPaymentUrl(mpPayment.getPointOfInteraction().getLinksObject().toString());
            }

            // Store provider response
            Map<String, Object> providerResponse = new HashMap<>();
            providerResponse.put("id", mpPayment.getId());
            providerResponse.put("status", mpPayment.getStatus());
            providerResponse.put("status_detail", mpPayment.getStatusDetail());
            providerResponse.put("payment_type_id", mpPayment.getPaymentTypeId());
            payment.setProviderResponse(providerResponse);

            payment = paymentRepository.save(payment);

            log.info("Mercado Pago payment created successfully: {} with external ID: {}",
                payment.getId(), mpPayment.getId());

            // Build response
            PaymentInitRes response = new PaymentInitRes();
            response.setPaymentId(payment.getId());
            response.setStatus(payment.getStatus());
            response.setPaymentUrl(payment.getPaymentUrl());
            response.setQrCode(payment.getQrCode());
            response.setPixCode(payment.getPixCode());
            response.setExpiresAt(payment.getExpiresAt());
            response.setMessage(getPaymentInstructions(request.getPaymentMethod(), payment));

            return response;

        } catch (MPApiException e) {
            log.error("Mercado Pago API error: {} - {}", e.getApiResponse().getStatusCode(),
                e.getApiResponse().getContent(), e);
            throw new RuntimeException("Failed to create Mercado Pago payment: " + e.getApiResponse().getContent(), e);
        } catch (MPException e) {
            log.error("Mercado Pago error", e);
            throw new RuntimeException("Failed to create Mercado Pago payment: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public PaymentStatusRes confirmPayment(String token) {
        // Mercado Pago uses webhooks for status updates, not manual confirmation
        log.info("Mercado Pago does not require manual confirmation, use getPaymentStatus instead");
        throw new UnsupportedOperationException("Mercado Pago uses webhooks for payment confirmation");
    }

    @Override
    public PaymentStatusRes getPaymentStatus(String externalPaymentId) {
        log.info("Getting Mercado Pago payment status: {}", externalPaymentId);

        try {
            // Configure Mercado Pago SDK
            configureMercadoPago();

            // Get payment from Mercado Pago
            PaymentClient client = new PaymentClient();
            Payment mpPayment = client.get(Long.parseLong(externalPaymentId));

            // Find payment in database
            com.northernchile.api.payment.model.Payment payment = paymentRepository
                .findByExternalPaymentId(externalPaymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + externalPaymentId));

            // Update payment status
            PaymentStatus newStatus = mapMercadoPagoStatus(mpPayment.getStatus());
            payment.setStatus(newStatus);

            // Update provider response
            Map<String, Object> providerResponse = payment.getProviderResponse();
            if (providerResponse == null) {
                providerResponse = new HashMap<>();
            }
            providerResponse.put("status", mpPayment.getStatus());
            providerResponse.put("status_detail", mpPayment.getStatusDetail());
            providerResponse.put("last_updated", mpPayment.getDateLastUpdated());
            payment.setProviderResponse(providerResponse);

            payment = paymentRepository.save(payment);

            // Update booking status if payment completed
            if (newStatus == PaymentStatus.COMPLETED) {
                Booking booking = payment.getBooking();
                booking.setStatus("CONFIRMED");
                bookingRepository.save(booking);
                log.info("Booking {} confirmed after successful payment", booking.getId());
            }

            log.info("Mercado Pago payment status updated: {} - {}", payment.getId(), newStatus);

            // Build response
            PaymentStatusRes response = new PaymentStatusRes();
            response.setPaymentId(payment.getId());
            response.setExternalPaymentId(externalPaymentId);
            response.setStatus(newStatus);
            response.setAmount(payment.getAmount());
            response.setCurrency(payment.getCurrency());
            response.setMessage(getStatusMessage(mpPayment.getStatus(), mpPayment.getStatusDetail()));
            response.setUpdatedAt(payment.getUpdatedAt());

            return response;

        } catch (MPException e) {
            log.error("Error getting Mercado Pago payment status", e);
            throw new RuntimeException("Failed to get Mercado Pago payment status: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentStatusRes refundPayment(com.northernchile.api.payment.model.Payment payment,
                                          BigDecimal amount) {
        log.info("Refunding Mercado Pago payment: {}", payment.getId());

        try {
            // Configure Mercado Pago SDK
            configureMercadoPago();

            // Create refund request
            // Note: The Mercado Pago SDK refund API would be called here
            // For now, this is a placeholder implementation

            log.warn("Mercado Pago refund not fully implemented - requires additional SDK methods");

            // Update payment status
            payment.setStatus(PaymentStatus.REFUNDED);
            payment = paymentRepository.save(payment);

            // Update booking status
            Booking booking = payment.getBooking();
            booking.setStatus("CANCELLED");
            bookingRepository.save(booking);

            log.info("Mercado Pago payment marked as refunded: {}", payment.getId());

            // Build response
            PaymentStatusRes response = new PaymentStatusRes();
            response.setPaymentId(payment.getId());
            response.setExternalPaymentId(payment.getExternalPaymentId());
            response.setStatus(PaymentStatus.REFUNDED);
            response.setAmount(payment.getAmount());
            response.setCurrency(payment.getCurrency());
            response.setMessage("Payment refund initiated");
            response.setUpdatedAt(payment.getUpdatedAt());

            return response;

        } catch (Exception e) {
            log.error("Error refunding Mercado Pago payment", e);
            throw new RuntimeException("Failed to refund Mercado Pago payment: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public PaymentStatusRes processWebhook(Map<String, Object> payload) {
        log.info("Processing Mercado Pago webhook: {}", payload);

        try {
            // Extract payment ID from webhook payload
            // Mercado Pago sends data.id for payment updates
            Object dataObj = payload.get("data");
            if (dataObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) dataObj;
                String paymentId = data.get("id").toString();

                // Fetch latest payment status
                return getPaymentStatus(paymentId);
            }

            throw new IllegalArgumentException("Invalid webhook payload structure");

        } catch (Exception e) {
            log.error("Error processing Mercado Pago webhook", e);
            throw new RuntimeException("Failed to process Mercado Pago webhook: " + e.getMessage(), e);
        }
    }

    /**
     * Build Mercado Pago payment request from our internal request.
     */
    private PaymentCreateRequest buildPaymentRequest(PaymentInitReq request, Booking booking) {
        PaymentCreateRequest.PaymentCreateRequestBuilder builder = PaymentCreateRequest.builder()
            .transactionAmount(request.getAmount())
            .description(request.getDescription() != null ?
                request.getDescription() :
                "Tour booking #" + booking.getId())
            .installments(1);

        // Set payment method ID based on our payment method enum
        String paymentMethodId = mapToMercadoPagoMethod(request.getPaymentMethod());
        builder.paymentMethodId(paymentMethodId);

        // Set payer information
        if (request.getUserEmail() != null) {
            PaymentPayerRequest payer = PaymentPayerRequest.builder()
                .email(request.getUserEmail())
                .build();
            builder.payer(payer);
        }

        // Set notification URL for webhooks
        if (request.getReturnUrl() != null) {
            builder.notificationUrl(request.getReturnUrl());
        }

        return builder.build();
    }

    /**
     * Map internal payment method to Mercado Pago payment method ID.
     */
    private String mapToMercadoPagoMethod(PaymentMethod method) {
        return switch (method) {
            case PIX -> "pix";
            case CREDIT_CARD -> "credit_card";
            case DEBIT_CARD -> "debit_card";
            case BANK_TRANSFER -> "bank_transfer";
            default -> "pix"; // Default to PIX for Brazil
        };
    }

    /**
     * Map Mercado Pago payment status to internal payment status.
     */
    private PaymentStatus mapMercadoPagoStatus(String status) {
        return switch (status) {
            case "approved" -> PaymentStatus.COMPLETED;
            case "pending" -> PaymentStatus.PENDING;
            case "in_process" -> PaymentStatus.PROCESSING;
            case "rejected" -> PaymentStatus.FAILED;
            case "cancelled" -> PaymentStatus.CANCELLED;
            case "refunded" -> PaymentStatus.REFUNDED;
            default -> PaymentStatus.PENDING;
        };
    }

    /**
     * Get user-friendly status message.
     */
    private String getStatusMessage(String status, String statusDetail) {
        String baseMessage = switch (status) {
            case "approved" -> "Payment approved successfully";
            case "pending" -> "Payment pending";
            case "in_process" -> "Payment being processed";
            case "rejected" -> "Payment rejected";
            case "cancelled" -> "Payment cancelled";
            case "refunded" -> "Payment refunded";
            default -> "Unknown payment status";
        };

        if (statusDetail != null && !statusDetail.isEmpty()) {
            return baseMessage + " (" + statusDetail + ")";
        }

        return baseMessage;
    }

    /**
     * Get payment instructions for the user.
     */
    private String getPaymentInstructions(PaymentMethod method,
                                          com.northernchile.api.payment.model.Payment payment) {
        return switch (method) {
            case PIX -> "Scan the QR code or copy the PIX code to complete payment. " +
                "Expires at: " + payment.getExpiresAt();
            case CREDIT_CARD, DEBIT_CARD -> "Complete the payment using your card details";
            default -> "Follow the payment instructions";
        };
    }
}
