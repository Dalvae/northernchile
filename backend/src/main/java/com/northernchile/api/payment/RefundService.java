package com.northernchile.api.payment;

import cl.transbank.webpay.webpayplus.WebpayPlus;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionRefundResponse;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentRefundClient;
import com.mercadopago.resources.payment.PaymentRefund;
import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.exception.RefundException;
import com.northernchile.api.model.Booking;
import com.northernchile.api.notification.EmailService;
import com.northernchile.api.payment.dto.RefundRes;
import com.northernchile.api.payment.model.PaymentProvider;
import com.northernchile.api.payment.model.PaymentSession;
import com.northernchile.api.payment.model.PaymentSessionStatus;
import com.northernchile.api.payment.repository.PaymentSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Service for processing refunds through payment providers.
 * Handles both Transbank and MercadoPago refunds.
 */
@Service
public class RefundService {

    private static final Logger log = LoggerFactory.getLogger(RefundService.class);
    private static final int REFUND_CUTOFF_HOURS = 24;

    private final BookingRepository bookingRepository;
    private final PaymentSessionRepository paymentSessionRepository;
    private final EmailService emailService;

    @Value("${transbank.commerce-code:597055555532}")
    private String transbankCommerceCode;

    @Value("${transbank.api-key:579B532A7440BB0C9079DED94D31EA1615BACEB56610332264630D42D0A36B1C}")
    private String transbankApiKey;

    @Value("${transbank.environment:INTEGRATION}")
    private String transbankEnvironment;

    @Value("${mercadopago.access-token:}")
    private String mercadoPagoAccessToken;

    public RefundService(
            BookingRepository bookingRepository,
            PaymentSessionRepository paymentSessionRepository,
            EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.paymentSessionRepository = paymentSessionRepository;
        this.emailService = emailService;
    }

    /**
     * Process a refund for a booking.
     * Validates the 24-hour policy, calls the payment provider, and updates statuses.
     *
     * @param bookingId The booking to refund
     * @param isAdminOverride If true, bypasses the 24-hour policy (admin only)
     * @return RefundRes with refund details
     */
    @Transactional
    public RefundRes refundBooking(UUID bookingId, boolean isAdminOverride) {
        log.info("Processing refund for booking: {} (admin override: {})", bookingId, isAdminOverride);

        // Load booking with schedule
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
            .orElseThrow(() -> new RefundException("Booking not found: " + bookingId));

        // Validate booking can be refunded
        validateRefundEligibility(booking, isAdminOverride);

        // Find the payment session for this booking
        PaymentSession paymentSession = findPaymentSessionForBooking(booking);

        if (paymentSession == null) {
            log.warn("No payment session found for booking {}. Marking as cancelled without provider refund.", bookingId);
            return cancelBookingWithoutProviderRefund(booking, "No payment record found");
        }

        // Process refund with payment provider
        RefundRes result = processProviderRefund(paymentSession, booking.getTotalAmount());

        // Update booking status
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        // Update payment session status
        paymentSession.setStatus(PaymentSessionStatus.REFUNDED);
        paymentSessionRepository.save(paymentSession);

        // Send refund confirmation email
        try {
            String customerEmail = booking.getUser().getEmail();
            String customerName = booking.getUser().getFullName();
            String tourName = booking.getSchedule().getTour().getNameTranslations().getOrDefault("es", "Tour").toString();
            String refundAmountFormatted = String.format("$%,.0f CLP", result.getRefundAmount());
            // Default to Spanish for email language
            String languageCode = "es";
            
            emailService.sendRefundConfirmationEmail(
                customerEmail,
                customerName,
                booking.getId().toString(),
                tourName,
                refundAmountFormatted,
                result.getProvider(),
                languageCode
            );
        } catch (Exception e) {
            log.error("Failed to send refund confirmation email for booking {}", bookingId, e);
        }

        log.info("Refund completed for booking {}: provider={}, refundId={}", 
            bookingId, result.getProvider(), result.getProviderRefundId());

        return result;
    }

    /**
     * Cancel a booking without processing a provider refund.
     * Used when the user cancels more than 24h before the tour (eligible for refund)
     * but the admin processes it manually, or when no payment record exists.
     */
    @Transactional
    public RefundRes cancelBookingWithoutProviderRefund(Booking booking, String reason) {
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        RefundRes result = new RefundRes();
        result.setBookingId(booking.getId());
        result.setStatus("CANCELLED_MANUAL");
        result.setRefundAmount(booking.getTotalAmount());
        result.setMessage(reason);

        return result;
    }

    private void validateRefundEligibility(Booking booking, boolean isAdminOverride) {
        // Check booking status
        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new RefundException("Cannot refund booking with status: " + booking.getStatus());
        }

        // Check 24-hour policy (unless admin override)
        if (!isAdminOverride) {
            Instant tourStart = booking.getSchedule().getStartDatetime();
            long hoursUntilTour = ChronoUnit.HOURS.between(Instant.now(), tourStart);

            if (hoursUntilTour < REFUND_CUTOFF_HOURS) {
                throw new RefundException(
                    String.format("Cannot refund less than %d hours before tour. Hours remaining: %d",
                        REFUND_CUTOFF_HOURS, hoursUntilTour));
            }
        }
    }

    private PaymentSession findPaymentSessionForBooking(Booking booking) {
        // PaymentSession stores the booking's schedule ID in items
        // We need to find a completed session that contains this booking's schedule
        UUID scheduleId = booking.getSchedule().getId();
        
        // Find sessions for this user that are completed and contain this schedule
        return paymentSessionRepository.findCompletedSessionByUserAndSchedule(
            booking.getUser().getId(), 
            scheduleId
        ).orElse(null);
    }

    private RefundRes processProviderRefund(PaymentSession session, BigDecimal amount) {
        return switch (session.getProvider()) {
            case TRANSBANK -> processTransbankRefund(session, amount);
            case MERCADOPAGO -> processMercadoPagoRefund(session, amount);
            default -> throw new RefundException("Unsupported payment provider: " + session.getProvider());
        };
    }

    private RefundRes processTransbankRefund(PaymentSession session, BigDecimal amount) {
        log.info("Processing Transbank refund for session: {}, amount: {}", session.getId(), amount);

        try {
            WebpayPlus.Transaction transaction = getTransbankTransaction();
            
            // Transbank refund uses the original token
            WebpayPlusTransactionRefundResponse response = transaction.refund(
                session.getToken(),
                amount.doubleValue()
            );

            RefundRes result = new RefundRes();
            result.setProvider(PaymentProvider.TRANSBANK.name());
            result.setProviderRefundId(response.getAuthorizationCode());
            result.setRefundAmount(amount);
            result.setStatus("REFUNDED");
            result.setMessage("Transbank refund processed successfully");

            log.info("Transbank refund successful: authCode={}", response.getAuthorizationCode());
            return result;

        } catch (Exception e) {
            log.error("Transbank refund failed for session: {}", session.getId(), e);
            throw new RefundException("Transbank refund failed: " + e.getMessage(), e);
        }
    }

    private RefundRes processMercadoPagoRefund(PaymentSession session, BigDecimal amount) {
        log.info("Processing MercadoPago refund for session: {}, amount: {}", session.getId(), amount);

        try {
            MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);
            
            // MercadoPago uses the payment ID (stored in externalPaymentId or token)
            String paymentId = session.getExternalPaymentId();
            if (paymentId == null || paymentId.isEmpty()) {
                paymentId = session.getToken();
            }

            if (paymentId == null || paymentId.isEmpty()) {
                throw new RefundException("No MercadoPago payment ID found for refund");
            }

            PaymentRefundClient client = new PaymentRefundClient();
            PaymentRefund refund;

            // Try to parse as Long (MercadoPago payment IDs are numeric)
            try {
                Long mpPaymentId = Long.parseLong(paymentId);
                // Full refund
                refund = client.refund(mpPaymentId);
            } catch (NumberFormatException e) {
                // If it's not a numeric ID, it might be a preference ID
                // In this case, we need to find the actual payment
                throw new RefundException("Invalid MercadoPago payment ID format: " + paymentId);
            }

            RefundRes result = new RefundRes();
            result.setProvider(PaymentProvider.MERCADOPAGO.name());
            result.setProviderRefundId(refund.getId() != null ? refund.getId().toString() : null);
            result.setRefundAmount(amount);
            result.setStatus("REFUNDED");
            result.setMessage("MercadoPago refund processed successfully");

            log.info("MercadoPago refund successful: refundId={}", refund.getId());
            return result;

        } catch (RefundException e) {
            throw e;
        } catch (Exception e) {
            log.error("MercadoPago refund failed for session: {}", session.getId(), e);
            throw new RefundException("MercadoPago refund failed: " + e.getMessage(), e);
        }
    }

    private WebpayPlus.Transaction getTransbankTransaction() {
        if ("PRODUCTION".equalsIgnoreCase(transbankEnvironment)) {
            return WebpayPlus.Transaction.buildForProduction(transbankCommerceCode, transbankApiKey);
        }
        return WebpayPlus.Transaction.buildForIntegration(transbankCommerceCode, transbankApiKey);
    }
}
