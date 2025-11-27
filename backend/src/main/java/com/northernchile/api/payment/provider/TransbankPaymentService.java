package com.northernchile.api.payment.provider;

import cl.transbank.webpay.exception.TransactionCommitException;
import cl.transbank.webpay.exception.TransactionCreateException;
import cl.transbank.webpay.exception.TransactionStatusException;
import cl.transbank.webpay.webpayplus.WebpayPlus;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCommitResponse;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCreateResponse;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionStatusResponse;
import com.northernchile.api.model.Booking;
import com.northernchile.api.payment.dto.PaymentInitReq;
import com.northernchile.api.payment.dto.PaymentInitRes;
import com.northernchile.api.payment.dto.PaymentStatusRes;
import com.northernchile.api.payment.model.Payment;
import com.northernchile.api.payment.model.PaymentStatus;
import com.northernchile.api.payment.repository.PaymentRepository;
import com.northernchile.api.booking.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Transbank Webpay Plus payment provider implementation.
 * Handles payment processing for Chilean market using Transbank's Webpay Plus service.
 */
@Service("transbankPaymentService")
public class TransbankPaymentService implements PaymentProviderService {

    private static final Logger log = LoggerFactory.getLogger(TransbankPaymentService.class);

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    @Value("${transbank.commerce-code:597055555532}")
    private String commerceCode;

    @Value("${transbank.api-key:579B532A7440BB0C9079DED94D31EA1615BACEB56610332264630D42D0A36B1C}")
    private String apiKey;

    @Value("${transbank.environment:INTEGRATION}")
    private String environment;

    @Value("${payment.test-mode:false}")
    private boolean testMode;

    public TransbankPaymentService(PaymentRepository paymentRepository, BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Create WebpayPlus Transaction instance with credentials.
     * In production, this should use environment-specific credentials.
     */
    private WebpayPlus.Transaction getTransactionInstance() {
        if ("PRODUCTION".equalsIgnoreCase(environment)) {
            return WebpayPlus.Transaction.buildForProduction(commerceCode, apiKey);
        } else {
            return WebpayPlus.Transaction.buildForIntegration(commerceCode, apiKey);
        }
    }

    /**
     * Check if current configuration is in TEST mode.
     * Uses explicit payment.test-mode if set, otherwise falls back to environment check.
     * @return true if in test mode
     */
    private boolean isTestMode() {
        // If explicitly configured, use that
        if (testMode) {
            return true;
        }
        // Otherwise, fallback to environment detection
        return !"PRODUCTION".equalsIgnoreCase(environment);
    }

    @Override
    @Transactional
    public PaymentInitRes createPayment(PaymentInitReq request) {
        log.info("Creating Transbank payment for booking: {}", request.getBookingId());

        try {
            // Validate booking exists
            Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + request.getBookingId()));

            // Get Transaction instance
            WebpayPlus.Transaction transaction = getTransactionInstance();

            // Create transaction with Transbank
            String buyOrder = "ORDER-" + request.getBookingId().toString().substring(0, 8);
            String sessionId = "SESSION-" + System.currentTimeMillis();
            double amountInPesos = request.getAmount().doubleValue();

            WebpayPlusTransactionCreateResponse transbankResponse = transaction.create(
                buyOrder,
                sessionId,
                amountInPesos,
                request.getReturnUrl()
            );

            // Create payment record
            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setProvider(request.getProvider());
            payment.setPaymentMethod(request.getPaymentMethod());
            payment.setAmount(request.getAmount());
            payment.setCurrency(request.getCurrency());
            payment.setStatus(PaymentStatus.PENDING);
            payment.setToken(transbankResponse.getToken());
            payment.setPaymentUrl(transbankResponse.getUrl());
            payment.setTest(isTestMode()); // Mark as test if using INTEGRATION environment

            // Store provider response
            Map<String, Object> providerResponse = new HashMap<>();
            providerResponse.put("token", transbankResponse.getToken());
            providerResponse.put("url", transbankResponse.getUrl());
            providerResponse.put("buyOrder", buyOrder);
            providerResponse.put("sessionId", sessionId);

            // Store additional booking IDs for multi-item cart checkout
            if (request.getAdditionalBookingIds() != null && !request.getAdditionalBookingIds().isEmpty()) {
                providerResponse.put("additionalBookingIds", request.getAdditionalBookingIds());
                log.info("Payment includes {} additional bookings", request.getAdditionalBookingIds().size());
            }

            payment.setProviderResponse(providerResponse);

            payment = paymentRepository.save(payment);

            log.info("Transbank payment created successfully: {}", payment.getId());

            // Build response
            PaymentInitRes response = new PaymentInitRes();
            response.setPaymentId(payment.getId());
            response.setStatus(payment.getStatus());
            response.setPaymentUrl(transbankResponse.getUrl());
            response.setToken(transbankResponse.getToken());
            response.setTest(payment.isTest());
            response.setMessage("Redirect user to payment URL to complete transaction");

            return response;

        } catch (TransactionCreateException e) {
            log.error("Error creating Transbank transaction", e);
            throw new RuntimeException("Failed to create Transbank payment: " + e.getMessage(), e);
        } catch (java.io.IOException e) {
            log.error("IO error creating Transbank transaction", e);
            throw new RuntimeException("Failed to create Transbank payment: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public PaymentStatusRes confirmPayment(String token) {
        log.info("Confirming Transbank payment request");

        try {
            // Find payment by token
            Payment payment = paymentRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

            // Get Transaction instance
            WebpayPlus.Transaction transaction = getTransactionInstance();

            // Commit transaction with Transbank
            WebpayPlusTransactionCommitResponse commitResponse = transaction.commit(token);

            // Update payment status based on Transbank response
            PaymentStatus newStatus = mapTransbankStatus(commitResponse.getResponseCode());
            payment.setStatus(newStatus);
            payment.setExternalPaymentId(String.valueOf(commitResponse.getBuyOrder()));

            // Store commit response with payment type for fee estimation
            Map<String, Object> providerResponse = payment.getProviderResponse();
            if (providerResponse == null) {
                providerResponse = new HashMap<>();
            }
            providerResponse.put("responseCode", commitResponse.getResponseCode());
            providerResponse.put("authorizationCode", commitResponse.getAuthorizationCode());
            providerResponse.put("transactionDate", commitResponse.getTransactionDate());
            
            // Store payment_type_code for fee estimation (VD=debit, VN=credit, VC=installments, etc.)
            providerResponse.put("payment_type_code", commitResponse.getPaymentTypeCode());
            providerResponse.put("installments_number", commitResponse.getInstallmentsNumber());
            providerResponse.put("card_number", commitResponse.getCardDetail() != null ? 
                commitResponse.getCardDetail().getCardNumber() : null);
            
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
                    List<String> additionalIds = (List<String>) storedResponse.get("additionalBookingIds");
                    for (String idStr : additionalIds) {
                        try {
                            UUID additionalBookingId = UUID.fromString(idStr);
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

            log.info("Transbank payment confirmed: {} with status: {}", payment.getId(), newStatus);

            // Build response
            PaymentStatusRes response = new PaymentStatusRes();
            response.setPaymentId(payment.getId());
            response.setExternalPaymentId(payment.getExternalPaymentId());
            response.setStatus(newStatus);
            response.setAmount(payment.getAmount());
            response.setCurrency(payment.getCurrency());
            response.setMessage(getStatusMessage(commitResponse.getResponseCode()));
            response.setUpdatedAt(payment.getUpdatedAt());

            return response;

        } catch (TransactionCommitException e) {
            log.error("Error committing Transbank transaction", e);
            throw new RuntimeException("Failed to confirm Transbank payment: " + e.getMessage(), e);
        } catch (java.io.IOException e) {
            log.error("IO error committing Transbank transaction", e);
            throw new RuntimeException("Failed to confirm Transbank payment: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentStatusRes getPaymentStatus(String externalPaymentId) {
        log.info("Getting Transbank payment status: {}", externalPaymentId);

        try {
            // Get Transaction instance
            WebpayPlus.Transaction transaction = getTransactionInstance();

            // Get transaction status from Transbank
            WebpayPlusTransactionStatusResponse statusResponse = transaction.status(externalPaymentId);

            // Find payment in database
            Payment payment = paymentRepository.findByExternalPaymentId(externalPaymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + externalPaymentId));

            // Update payment status
            PaymentStatus newStatus = mapTransbankStatus(statusResponse.getResponseCode());
            payment.setStatus(newStatus);
            paymentRepository.save(payment);

            // Build response
            PaymentStatusRes response = new PaymentStatusRes();
            response.setPaymentId(payment.getId());
            response.setExternalPaymentId(externalPaymentId);
            response.setStatus(newStatus);
            response.setAmount(payment.getAmount());
            response.setCurrency(payment.getCurrency());
            response.setMessage(getStatusMessage(statusResponse.getResponseCode()));
            response.setUpdatedAt(payment.getUpdatedAt());

            return response;

        } catch (TransactionStatusException e) {
            log.error("Error getting Transbank transaction status", e);
            throw new RuntimeException("Failed to get Transbank payment status: " + e.getMessage(), e);
        } catch (java.io.IOException e) {
            log.error("IO error getting Transbank transaction status", e);
            throw new RuntimeException("Failed to get Transbank payment status: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentStatusRes refundPayment(Payment payment, java.math.BigDecimal amount) {
        log.info("Refunding Transbank payment: {}", payment.getId());

        try {
            // Get Transaction instance
            WebpayPlus.Transaction transaction = getTransactionInstance();

            // Determine refund amount
            double refundAmount = (amount != null) ? amount.doubleValue() : payment.getAmount().doubleValue();

            // Refund transaction with Transbank
            transaction.refund(
                payment.getToken(),
                refundAmount
            );

            // Update payment status
            payment.setStatus(PaymentStatus.REFUNDED);
            payment = paymentRepository.save(payment);

            // Update booking status
            Booking booking = payment.getBooking();
            booking.setStatus("CANCELLED");
            bookingRepository.save(booking);

            log.info("Transbank payment refunded successfully: {}", payment.getId());

            // Build response
            PaymentStatusRes response = new PaymentStatusRes();
            response.setPaymentId(payment.getId());
            response.setExternalPaymentId(payment.getExternalPaymentId());
            response.setStatus(PaymentStatus.REFUNDED);
            response.setAmount(payment.getAmount());
            response.setCurrency(payment.getCurrency());
            response.setMessage("Payment refunded successfully");
            response.setUpdatedAt(payment.getUpdatedAt());

            return response;

        } catch (Exception e) {
            log.error("Error refunding Transbank payment", e);
            throw new RuntimeException("Failed to refund Transbank payment: " + e.getMessage(), e);
        }
    }

    @Override
    public PaymentStatusRes processWebhook(Map<String, Object> payload) {
        log.info("Processing Transbank webhook: {}", payload);
        // Transbank uses redirect-based flow, so webhooks are not typically used
        // This method is here for interface compliance
        throw new UnsupportedOperationException("Transbank does not use webhooks");
    }

    /**
     * Map Transbank response code to internal payment status.
     */
    private PaymentStatus mapTransbankStatus(int responseCode) {
        return switch (responseCode) {
            case 0 -> PaymentStatus.COMPLETED; // Approved
            case -1 -> PaymentStatus.FAILED; // Rejected
            case -2 -> PaymentStatus.CANCELLED; // User cancelled
            case -3 -> PaymentStatus.EXPIRED; // Transaction expired
            case -4 -> PaymentStatus.FAILED; // Invalid transaction
            case -5 -> PaymentStatus.FAILED; // Error
            default -> PaymentStatus.PROCESSING; // Unknown status
        };
    }

    /**
     * Get user-friendly message for Transbank response code.
     */
    private String getStatusMessage(int responseCode) {
        return switch (responseCode) {
            case 0 -> "Payment approved successfully";
            case -1 -> "Payment rejected by bank";
            case -2 -> "Payment cancelled by user";
            case -3 -> "Payment session expired";
            case -4 -> "Invalid transaction";
            case -5 -> "Error processing payment";
            default -> "Unknown payment status";
        };
    }
}
