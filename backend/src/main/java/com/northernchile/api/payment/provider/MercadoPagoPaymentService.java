package com.northernchile.api.payment.provider;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.payment.PaymentCreateRequest;
import com.mercadopago.client.payment.PaymentPayerRequest;
import com.mercadopago.client.payment.PaymentRefundClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferencePaymentMethodsRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.payment.PaymentRefund;
import com.mercadopago.resources.preference.Preference;
import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.exception.PaymentDeclinedException;
import com.northernchile.api.exception.PaymentProviderException;
import com.northernchile.api.exception.RefundException;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    @Value("${payment.test-mode:false}")
    private boolean testMode;

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

    /**
     * Check if current configuration is in TEST mode.
     * Uses explicit payment.test-mode configuration property.
     * @return true if in test mode
     */
    private boolean isTestMode() {
        return testMode;
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

            // SECURITY: Always use the amount from the booking, never trust client-provided amount
            // This prevents price manipulation attacks where a malicious user could modify the HTTP request
            BigDecimal trustedAmount = booking.getTotalAmount();
            
            // If there are additional bookings, sum their amounts too
            if (request.getAdditionalBookingIds() != null && !request.getAdditionalBookingIds().isEmpty()) {
                for (java.util.UUID additionalId : request.getAdditionalBookingIds()) {
                    Booking additionalBooking = bookingRepository.findById(additionalId)
                        .orElseThrow(() -> new IllegalArgumentException("Additional booking not found: " + additionalId));
                    trustedAmount = trustedAmount.add(additionalBooking.getTotalAmount());
                }
            }
            
            // Log if client amount differs (potential attack or frontend bug)
            if (request.getAmount() != null && request.getAmount().compareTo(trustedAmount) != 0) {
                log.warn("SECURITY: Client-provided amount ({}) differs from booking total ({}). Using booking total.",
                    request.getAmount(), trustedAmount);
            }

            // Configure Mercado Pago SDK
            configureMercadoPago();

            // Use Checkout Pro (Preferences) for credit/debit cards - user is redirected to MercadoPago
            // Use Payment API for PIX - displays QR code inline
            if (request.getPaymentMethod() == PaymentMethod.CREDIT_CARD ||
                request.getPaymentMethod() == PaymentMethod.DEBIT_CARD) {
                return createCheckoutProPayment(request, booking, trustedAmount);
            } else {
                return createDirectPayment(request, booking, trustedAmount);
            }

        } catch (MPApiException e) {
            log.error("Mercado Pago API error: {} - {}", e.getApiResponse().getStatusCode(),
                e.getApiResponse().getContent(), e);

            // Check if it's a declined payment vs provider error
            int statusCode = e.getApiResponse().getStatusCode();
            if (statusCode == 400 || statusCode == 401) {
                throw new PaymentDeclinedException(
                    "Payment was declined by Mercado Pago",
                    mapMercadoPagoDeclineReason(e.getApiResponse().getContent()),
                    e.getApiResponse().getContent()
                );
            }

            throw new PaymentProviderException(
                "Failed to create payment with Mercado Pago",
                "MERCADOPAGO",
                e.getApiResponse().getContent(),
                e
            );
        } catch (MPException e) {
            log.error("Mercado Pago error", e);
            throw new PaymentProviderException(
                "Connection error with Mercado Pago payment service",
                "MERCADOPAGO",
                e.getMessage(),
                e
            );
        }
    }

    /**
     * Create a Checkout Pro payment (redirect to MercadoPago).
     * Used for credit/debit cards - more secure as card data never touches our servers.
     */
    private PaymentInitRes createCheckoutProPayment(PaymentInitReq request, Booking booking, BigDecimal trustedAmount)
            throws MPException, MPApiException {
        log.info("Creating Checkout Pro preference for booking: {}", booking.getId());

        // Build preference item
        PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
            .id(booking.getId().toString())
            .title(request.getDescription() != null ? request.getDescription() : "Tour booking")
            .description("Booking #" + booking.getId())
            .quantity(1)
            .currencyId(request.getCurrency() != null ? request.getCurrency() : "USD")
            .unitPrice(trustedAmount)
            .build();

        List<PreferenceItemRequest> items = new ArrayList<>();
        items.add(itemRequest);

        // Build payer info
        PreferencePayerRequest payerRequest = null;
        if (request.getUserEmail() != null) {
            payerRequest = PreferencePayerRequest.builder()
                .email(request.getUserEmail())
                .build();
        }

        // Build back URLs for redirect after payment
        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
            .success(request.getReturnUrl())
            .failure(request.getCancelUrl() != null ? request.getCancelUrl() : request.getReturnUrl())
            .pending(request.getReturnUrl())
            .build();

        // Configure accepted payment methods based on request
        PreferencePaymentMethodsRequest.PreferencePaymentMethodsRequestBuilder paymentMethodsBuilder = 
            PreferencePaymentMethodsRequest.builder()
            .installments(1); // Single payment only

        // Build preference request
        PreferenceRequest.PreferenceRequestBuilder preferenceBuilder = PreferenceRequest.builder()
            .items(items)
            .backUrls(backUrls)
            .autoReturn("approved")
            .externalReference(booking.getId().toString())
            .notificationUrl(request.getReturnUrl()) // Webhook URL
            .paymentMethods(paymentMethodsBuilder.build());

        if (payerRequest != null) {
            preferenceBuilder.payer(payerRequest);
        }

        // Create preference with MercadoPago
        PreferenceClient preferenceClient = new PreferenceClient();
        Preference preference = preferenceClient.create(preferenceBuilder.build());

        log.info("Checkout Pro preference created: {}", preference.getId());

        // Create payment record (status PENDING until user completes payment)
        com.northernchile.api.payment.model.Payment payment = new com.northernchile.api.payment.model.Payment();
        payment.setBooking(booking);
        payment.setProvider(request.getProvider());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setAmount(trustedAmount);
        payment.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        payment.setExternalPaymentId(preference.getId()); // Store preference ID
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTest(isTestMode());
        
        // Set the payment URL for redirect
        String paymentUrl = isTestMode() ? preference.getSandboxInitPoint() : preference.getInitPoint();
        payment.setPaymentUrl(paymentUrl);

        // Store provider response
        Map<String, Object> providerResponse = new HashMap<>();
        providerResponse.put("preference_id", preference.getId());
        providerResponse.put("init_point", preference.getInitPoint());
        providerResponse.put("sandbox_init_point", preference.getSandboxInitPoint());
        providerResponse.put("checkout_type", "CHECKOUT_PRO");
        
        // Store additional booking IDs for multi-item cart checkout
        if (request.getAdditionalBookingIds() != null && !request.getAdditionalBookingIds().isEmpty()) {
            providerResponse.put("additionalBookingIds", request.getAdditionalBookingIds());
            log.info("Payment includes {} additional bookings", request.getAdditionalBookingIds().size());
        }

        payment.setProviderResponse(providerResponse);
        payment = paymentRepository.save(payment);

        log.info("Checkout Pro payment record created: {} with preference: {}", payment.getId(), preference.getId());

        // Build response
        PaymentInitRes response = new PaymentInitRes();
        response.setPaymentId(payment.getId());
        response.setStatus(PaymentStatus.PENDING);
        response.setPaymentUrl(paymentUrl);
        response.setTest(payment.isTest());
        response.setMessage("Redirect to MercadoPago to complete your payment");

        return response;
    }

    /**
     * Create a direct payment (PIX, etc.) using Payment API.
     * Used for methods that display inline (QR code for PIX).
     */
    private PaymentInitRes createDirectPayment(PaymentInitReq request, Booking booking, BigDecimal trustedAmount)
            throws MPException, MPApiException {
        
        // Create payment request for PIX
        PaymentCreateRequest paymentRequest = buildPaymentRequest(request, booking, trustedAmount);

        // Create payment with Mercado Pago
        PaymentClient client = new PaymentClient();
        Payment mpPayment = client.create(paymentRequest);

        // Create payment record
        com.northernchile.api.payment.model.Payment payment = new com.northernchile.api.payment.model.Payment();
        payment.setBooking(booking);
        payment.setProvider(request.getProvider());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setAmount(trustedAmount);
        payment.setCurrency(request.getCurrency() != null ? request.getCurrency() : "BRL");
        payment.setExternalPaymentId(mpPayment.getId().toString());
        payment.setStatus(mapMercadoPagoStatus(mpPayment.getStatus()));
        payment.setTest(isTestMode());

        // Set payment details based on payment method
        if (request.getPaymentMethod() == PaymentMethod.PIX) {
            // For PIX, store QR code and payment code
            if (mpPayment.getPointOfInteraction() != null &&
                mpPayment.getPointOfInteraction().getTransactionData() != null) {
                String qrCodeBase64 = mpPayment.getPointOfInteraction().getTransactionData().getQrCodeBase64();
                String qrCode = mpPayment.getPointOfInteraction().getTransactionData().getQrCode();

                // Convert base64 to data URI for frontend display
                String qrCodeDataUri = "data:image/png;base64," + qrCodeBase64;
                payment.setQrCode(qrCodeDataUri);
                payment.setPixCode(qrCode);
            }

            // Set expiration time (default 30 minutes for PIX)
            int expirationMinutes = request.getExpirationMinutes() != null ?
                request.getExpirationMinutes() : 30;
            payment.setExpiresAt(Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES));
        }

        // Set payment URL (ticket URL for PIX or 3DS redirect)
        if (mpPayment.getPointOfInteraction() != null) {
            String ticketUrl = null;
            try {
                if (mpPayment.getTransactionDetails() != null &&
                    mpPayment.getTransactionDetails().getExternalResourceUrl() != null) {
                    ticketUrl = mpPayment.getTransactionDetails().getExternalResourceUrl();
                }
            } catch (Exception e) {
                log.debug("Could not retrieve ticket URL: {}", e.getMessage());
            }
            if (ticketUrl != null) {
                payment.setPaymentUrl(ticketUrl);
            }
        }

        // Store provider response
        Map<String, Object> providerResponse = new HashMap<>();
        providerResponse.put("id", mpPayment.getId());
        providerResponse.put("status", mpPayment.getStatus());
        providerResponse.put("status_detail", mpPayment.getStatusDetail());
        providerResponse.put("payment_type_id", mpPayment.getPaymentTypeId());

        // Store additional booking IDs for multi-item cart checkout
        if (request.getAdditionalBookingIds() != null && !request.getAdditionalBookingIds().isEmpty()) {
            providerResponse.put("additionalBookingIds", request.getAdditionalBookingIds());
            log.info("Payment includes {} additional bookings", request.getAdditionalBookingIds().size());
        }

        payment.setProviderResponse(providerResponse);
        payment = paymentRepository.save(payment);

        log.info("Mercado Pago PIX payment created: {} with external ID: {}", payment.getId(), mpPayment.getId());

        // Build response
        PaymentInitRes response = new PaymentInitRes();
        response.setPaymentId(payment.getId());
        response.setStatus(payment.getStatus());
        response.setPaymentUrl(payment.getPaymentUrl());
        response.setQrCode(payment.getQrCode());
        response.setPixCode(payment.getPixCode());
        response.setExpiresAt(payment.getExpiresAt());
        response.setTest(payment.isTest());
        response.setMessage(getPaymentInstructions(request.getPaymentMethod(), payment));

        return response;
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

            // Update provider response with financial details for reconciliation
            Map<String, Object> providerResponse = payment.getProviderResponse();
            if (providerResponse == null) {
                providerResponse = new HashMap<>();
            }
            providerResponse.put("status", mpPayment.getStatus());
            providerResponse.put("status_detail", mpPayment.getStatusDetail());
            providerResponse.put("last_updated", mpPayment.getDateLastUpdated());
            
            // Extract fee details for financial reconciliation
            if (mpPayment.getFeeDetails() != null && !mpPayment.getFeeDetails().isEmpty()) {
                var feeDetail = mpPayment.getFeeDetails().get(0);
                providerResponse.put("gateway_fee", feeDetail.getAmount());
                providerResponse.put("fee_type", feeDetail.getType());
            }
            
            // Extract net received amount
            if (mpPayment.getTransactionDetails() != null) {
                providerResponse.put("net_received_amount", mpPayment.getTransactionDetails().getNetReceivedAmount());
                providerResponse.put("total_paid_amount", mpPayment.getTransactionDetails().getTotalPaidAmount());
            }
            
            payment.setProviderResponse(providerResponse);

            payment = paymentRepository.save(payment);

            // Update booking status if payment completed
            if (newStatus == PaymentStatus.COMPLETED) {
                // Confirm primary booking
                Booking booking = payment.getBooking();
                booking.setStatus("CONFIRMED");
                bookingRepository.save(booking);
                log.info("Booking {} confirmed after successful payment", booking.getId());

                // Confirm additional bookings from multi-item cart (if any)
                Map<String, Object> storedResponse = payment.getProviderResponse();
                if (storedResponse != null && storedResponse.containsKey("additionalBookingIds")) {
                    @SuppressWarnings("unchecked")
                    java.util.List<String> additionalIds = (java.util.List<String>) storedResponse.get("additionalBookingIds");
                    for (String idStr : additionalIds) {
                        try {
                            java.util.UUID additionalBookingId = java.util.UUID.fromString(idStr);
                            bookingRepository.findById(additionalBookingId).ifPresent(additionalBooking -> {
                                additionalBooking.setStatus("CONFIRMED");
                                bookingRepository.save(additionalBooking);
                                log.info("Additional booking {} confirmed after successful payment", additionalBookingId);
                            });
                        } catch (IllegalArgumentException e) {
                            log.error("Invalid additional booking ID: {}", idStr, e);
                        }
                    }
                }
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

        } catch (MPApiException e) {
            log.error("Mercado Pago API error getting payment status: {} - {}",
                e.getApiResponse().getStatusCode(), e.getApiResponse().getContent(), e);
            throw new PaymentProviderException(
                "Failed to get payment status from Mercado Pago",
                "MERCADOPAGO",
                e.getApiResponse().getContent(),
                e
            );
        } catch (MPException e) {
            log.error("Error getting Mercado Pago payment status", e);
            throw new PaymentProviderException(
                "Connection error with Mercado Pago payment service",
                "MERCADOPAGO",
                e.getMessage(),
                e
            );
        }
    }

    @Override
    public PaymentStatusRes refundPayment(com.northernchile.api.payment.model.Payment payment,
                                          BigDecimal amount) {
        log.info("Refunding Mercado Pago payment: {}", payment.getId());

        try {
            // Configure Mercado Pago SDK
            configureMercadoPago();

            // Get payment ID from external payment ID
            Long paymentId = Long.parseLong(payment.getExternalPaymentId());

            // Create refund client
            PaymentRefundClient refundClient = new PaymentRefundClient();

            // Determine refund amount
            BigDecimal refundAmount = (amount != null) ? amount : payment.getAmount();

            // Create refund
            PaymentRefund refund;
            if (amount != null && amount.compareTo(payment.getAmount()) < 0) {
                // Partial refund
                log.info("Creating partial refund of {} for payment {}", amount, paymentId);
                refund = refundClient.refund(paymentId, amount);
            } else {
                // Full refund
                log.info("Creating full refund for payment {}", paymentId);
                refund = refundClient.refund(paymentId);
            }

            log.info("Mercado Pago refund created successfully: refund ID = {}", refund.getId());

            // Update payment status
            payment.setStatus(PaymentStatus.REFUNDED);

            // Store refund information in provider response
            Map<String, Object> providerResponse = payment.getProviderResponse();
            if (providerResponse == null) {
                providerResponse = new HashMap<>();
            }
            providerResponse.put("refund_id", refund.getId());
            providerResponse.put("refund_amount", refundAmount);
            providerResponse.put("refund_status", refund.getStatus());
            providerResponse.put("refund_date", refund.getDateCreated());
            payment.setProviderResponse(providerResponse);

            payment = paymentRepository.save(payment);

            // Update booking status
            Booking booking = payment.getBooking();
            booking.setStatus("CANCELLED");
            bookingRepository.save(booking);

            log.info("Mercado Pago payment refunded successfully: {}", payment.getId());

            // Build response
            PaymentStatusRes response = new PaymentStatusRes();
            response.setPaymentId(payment.getId());
            response.setExternalPaymentId(payment.getExternalPaymentId());
            response.setStatus(PaymentStatus.REFUNDED);
            response.setAmount(payment.getAmount());
            response.setCurrency(payment.getCurrency());
            response.setMessage("Payment refunded successfully via Mercado Pago. Refund ID: " + refund.getId());
            response.setUpdatedAt(payment.getUpdatedAt());

            return response;

        } catch (MPApiException e) {
            log.error("Mercado Pago API error refunding payment: {} - {}",
                e.getApiResponse().getStatusCode(), e.getApiResponse().getContent(), e);
            throw new RefundException(
                "Failed to refund payment via Mercado Pago",
                e.getApiResponse().getContent(),
                e
            );
        } catch (MPException e) {
            log.error("Mercado Pago error refunding payment", e);
            throw new RefundException(
                "Connection error refunding via Mercado Pago",
                e.getMessage(),
                e
            );
        } catch (NumberFormatException e) {
            log.error("Invalid external payment ID: {}", payment.getExternalPaymentId(), e);
            throw new RefundException(
                "Invalid payment ID format - cannot process refund",
                e
            );
        } catch (Exception e) {
            log.error("Error refunding Mercado Pago payment", e);
            throw new RefundException(
                "Unexpected error refunding payment via Mercado Pago",
                e.getMessage(),
                e
            );
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
            throw new PaymentProviderException(
                "Failed to process Mercado Pago webhook",
                "MERCADOPAGO",
                e.getMessage(),
                e
            );
        }
    }

    /**
     * Map Mercado Pago API error response to decline reason
     */
    private String mapMercadoPagoDeclineReason(String apiResponse) {
        if (apiResponse == null) {
            return PaymentDeclinedException.DeclineReason.UNKNOWN;
        }

        String lowerResponse = apiResponse.toLowerCase();
        if (lowerResponse.contains("insufficient_amount") || lowerResponse.contains("insufficient_funds")) {
            return PaymentDeclinedException.DeclineReason.INSUFFICIENT_FUNDS;
        }
        if (lowerResponse.contains("expired")) {
            return PaymentDeclinedException.DeclineReason.CARD_EXPIRED;
        }
        if (lowerResponse.contains("blocked") || lowerResponse.contains("disabled")) {
            return PaymentDeclinedException.DeclineReason.CARD_BLOCKED;
        }
        if (lowerResponse.contains("invalid_card") || lowerResponse.contains("invalid_number")) {
            return PaymentDeclinedException.DeclineReason.INVALID_CARD;
        }
        if (lowerResponse.contains("rejected") || lowerResponse.contains("denied")) {
            return PaymentDeclinedException.DeclineReason.BANK_REJECTED;
        }
        if (lowerResponse.contains("fraud") || lowerResponse.contains("high_risk")) {
            return PaymentDeclinedException.DeclineReason.FRAUD_SUSPECTED;
        }
        if (lowerResponse.contains("limit") || lowerResponse.contains("max_attempts")) {
            return PaymentDeclinedException.DeclineReason.LIMIT_EXCEEDED;
        }

        return PaymentDeclinedException.DeclineReason.UNKNOWN;
    }

    /**
     * Build Mercado Pago payment request for PIX payments.
     */
    private PaymentCreateRequest buildPaymentRequest(PaymentInitReq request, Booking booking, BigDecimal trustedAmount) {
        PaymentCreateRequest.PaymentCreateRequestBuilder builder = PaymentCreateRequest.builder()
            .transactionAmount(trustedAmount)
            .description(request.getDescription() != null ?
                request.getDescription() :
                "Tour booking #" + booking.getId())
            .installments(1);

        // Set payment method ID based on our enum (PIX for direct payments)
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
