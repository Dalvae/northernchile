package com.northernchile.api.checkout;

import com.northernchile.api.checkout.dto.CheckoutRequest;
import com.northernchile.api.checkout.dto.CheckoutResult;
import com.northernchile.api.model.User;
import com.northernchile.api.payment.PaymentSessionService;
import com.northernchile.api.payment.dto.PaymentSessionReq;
import com.northernchile.api.payment.dto.PaymentSessionRes;
import com.northernchile.api.payment.model.PaymentSessionStatus;
import com.northernchile.api.security.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Unified checkout service - single entry point for all booking creation.
 *
 * ALL bookings flow through this service, which ensures:
 * 1. Consistent availability validation (including pending payment sessions)
 * 2. Consistent cutoff time validation
 * 3. Consistent tax calculation
 * 4. Proper audit trail through PaymentSession
 *
 * Checkout types:
 * - Real payment: Creates PaymentSession, user redirects to payment provider
 * - Mock payment (testing): Creates PaymentSession, immediately confirms
 * - Admin bypass: Creates PaymentSession, immediately confirms (admin only)
 */
@Service
public class CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutService.class);

    private final PaymentSessionService paymentSessionService;

    public CheckoutService(PaymentSessionService paymentSessionService) {
        this.paymentSessionService = paymentSessionService;
    }

    /**
     * Initiates checkout process.
     *
     * @param request The checkout request containing items and payment details
     * @param user The authenticated user making the checkout
     * @return CheckoutResult with payment URL/QR or booking IDs
     */
    @Transactional
    public CheckoutResult initiateCheckout(CheckoutRequest request, User user) {
        log.info("Initiating checkout for user: {} with {} items, mockPayment={}, adminBypass={}",
            user.getId(), request.items().size(), request.mockPayment(), request.adminBypass());

        // Validate admin bypass permission
        if (Boolean.TRUE.equals(request.adminBypass())) {
            validateAdminBypass(user);
        }

        // Convert to PaymentSessionReq and create session
        // All validation (availability, cutoff time) happens in PaymentSessionService
        PaymentSessionReq sessionReq = convertToSessionRequest(request);
        PaymentSessionRes sessionRes = paymentSessionService.createSession(sessionReq, user);

        // For mock payments or admin bypass, confirm immediately
        if (Boolean.TRUE.equals(request.mockPayment()) || Boolean.TRUE.equals(request.adminBypass())) {
            return handleImmediateConfirmation(sessionRes, request, user);
        }

        // Real payment - return session details for user to complete payment
        return CheckoutResult.forRealPayment(
            sessionRes.sessionId(),
            sessionRes.paymentUrl(),
            sessionRes.token(),
            sessionRes.qrCode(),
            sessionRes.pixCode(),
            sessionRes.expiresAt(),
            sessionRes.isTest()
        );
    }

    /**
     * Validates that the user has permission to use admin bypass.
     */
    private void validateAdminBypass(User user) {
        if (!Role.SUPER_ADMIN.getRoleName().equals(user.getRole()) &&
            !Role.PARTNER_ADMIN.getRoleName().equals(user.getRole())) {
            throw new AccessDeniedException("Admin bypass requires SUPER_ADMIN or PARTNER_ADMIN role");
        }
    }

    /**
     * Handles mock payment or admin bypass by confirming the session immediately.
     */
    private CheckoutResult handleImmediateConfirmation(
            PaymentSessionRes sessionRes,
            CheckoutRequest request,
            User user) {

        CheckoutResult.CheckoutType checkoutType = Boolean.TRUE.equals(request.adminBypass())
            ? CheckoutResult.CheckoutType.ADMIN_BYPASS
            : CheckoutResult.CheckoutType.MOCK_PAYMENT;

        log.info("Immediate confirmation for session {} ({})", sessionRes.sessionId(), checkoutType);

        // Confirm the session to create bookings
        PaymentSessionRes confirmedRes = paymentSessionService.confirmSession(sessionRes.token());

        if (confirmedRes.status() != PaymentSessionStatus.COMPLETED) {
            throw new IllegalStateException(
                "Failed to confirm mock/admin payment session: " + confirmedRes.status());
        }

        return CheckoutResult.forImmediateBooking(
            sessionRes.sessionId(),
            confirmedRes.bookingIds(),
            sessionRes.isTest(),
            checkoutType
        );
    }

    /**
     * Converts CheckoutRequest to PaymentSessionReq for the payment session service.
     */
    private PaymentSessionReq convertToSessionRequest(CheckoutRequest request) {
        List<PaymentSessionReq.PaymentSessionItemReq> items = request.items().stream()
            .map(item -> new PaymentSessionReq.PaymentSessionItemReq(
                item.scheduleId(),
                item.tourName(),
                item.tourDate(),
                item.numParticipants(),
                item.pricePerPerson(),
                item.itemTotal(),
                item.specialRequests(),
                item.participants()
            ))
            .collect(Collectors.toList());

        return new PaymentSessionReq(
            request.provider(),
            request.paymentMethod(),
            request.totalAmount(),
            request.currency(),
            items,
            request.returnUrl(),
            request.cancelUrl(),
            request.userEmail(),
            request.description(),
            request.languageCode()
        );
    }
}
