package com.northernchile.api.payment;

import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.booking.BookingService;
import com.northernchile.api.cart.CartRepository;
import com.northernchile.api.exception.PaymentProviderException;
import com.northernchile.api.model.Booking;
import com.northernchile.api.model.Participant;
import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.model.User;
import com.northernchile.api.payment.dto.PaymentSessionReq;
import com.northernchile.api.payment.dto.PaymentSessionRes;
import com.northernchile.api.payment.model.*;
import com.northernchile.api.payment.repository.PaymentSessionRepository;
import com.northernchile.api.tour.TourScheduleRepository;
import com.northernchile.api.tour.TourUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing payment sessions.
 * Implements the "payment first" flow where bookings are only created after successful payment.
 */
@Service
public class PaymentSessionService {

    private static final Logger log = LoggerFactory.getLogger(PaymentSessionService.class);
    private static final int SESSION_EXPIRATION_MINUTES = 30;

    @PersistenceContext
    private EntityManager entityManager;

    private final PaymentSessionRepository sessionRepository;
    private final TourScheduleRepository scheduleRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final CartRepository cartRepository;
    private final PaymentSessionPaymentAdapter paymentAdapter;

    @Value("${payment.test-mode:false}")
    private boolean testMode;

    public PaymentSessionService(
            PaymentSessionRepository sessionRepository,
            TourScheduleRepository scheduleRepository,
            BookingRepository bookingRepository,
            @Lazy BookingService bookingService,
            CartRepository cartRepository,
            PaymentSessionPaymentAdapter paymentAdapter) {
        this.sessionRepository = sessionRepository;
        this.scheduleRepository = scheduleRepository;
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
        this.cartRepository = cartRepository;
        this.paymentAdapter = paymentAdapter;
    }

    /**
     * Create a new payment session from checkout data.
     * Validates availability, stores cart snapshot, and initializes payment with provider.
     */
    @Transactional
    public PaymentSessionRes createSession(PaymentSessionReq request, User user) {
        log.info("Creating payment session for user: {} with {} items", user.getId(), request.items().size());

        // Validate all schedules exist and have availability
        validateAvailability(request);

        // Calculate total from schedule prices (don't trust client amounts)
        BigDecimal trustedTotal = calculateTrustedTotal(request);
        
        if (request.totalAmount().compareTo(trustedTotal) != 0) {
            log.warn("SECURITY: Client total ({}) differs from calculated total ({}). Using calculated.",
                request.totalAmount(), trustedTotal);
        }

        // Convert request items to session items
        List<PaymentSessionItem> sessionItems = request.items().stream()
            .map(this::convertToSessionItem)
            .collect(Collectors.toList());

        // Create payment session
        PaymentSession session = new PaymentSession();
        session.setUser(user);
        session.setStatus(PaymentSessionStatus.PENDING);
        session.setItems(sessionItems);
        session.setTotalAmount(trustedTotal);
        session.setCurrency(request.currency() != null ? request.currency() : "CLP");
        session.setLanguageCode(request.languageCode() != null ? request.languageCode() : "es");
        session.setUserEmail(request.userEmail());
        session.setProvider(request.provider());
        session.setPaymentMethod(request.paymentMethod());
        session.setReturnUrl(request.returnUrl());
        session.setCancelUrl(request.cancelUrl());
        session.setExpiresAt(Instant.now().plus(SESSION_EXPIRATION_MINUTES, ChronoUnit.MINUTES));
        session.setTest(testMode);

        session = sessionRepository.save(session);
        log.info("Payment session created: {}", session.getId());

        // Initialize payment with provider
        try {
            PaymentSessionRes response = paymentAdapter.initializePayment(session, request);
            
            // Update session with payment details
            session.setToken(response.token());
            session.setPaymentUrl(response.paymentUrl());
            session.setQrCode(response.qrCode());
            session.setPixCode(response.pixCode());
            session.setExternalPaymentId(response.sessionId() != null ? response.sessionId().toString() : null);
            sessionRepository.save(session);

            // Adapter already returns complete response, just return it
            return response;

        } catch (Exception e) {
            log.error("Failed to initialize payment for session: {}", session.getId(), e);
            session.setStatus(PaymentSessionStatus.FAILED);
            session.setErrorMessage(e.getMessage());
            sessionRepository.save(session);
            throw new PaymentProviderException(
                "Failed to initialize payment",
                request.provider().name(),
                e.getMessage(),
                e
            );
        }
    }

    /**
     * Get payment session status.
     */
    public PaymentSessionRes getSessionStatus(UUID sessionId) {
        PaymentSession session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Payment session not found: " + sessionId));

        return new PaymentSessionRes(
            session.getId(),
            session.getStatus(),
            session.getPaymentUrl(),
            session.getToken(),
            session.getQrCode(),
            session.getPixCode(),
            session.getExpiresAt(),
            session.isTest(),
            null
        );
    }

    /**
     * Confirm a payment session after successful payment.
     * Creates bookings from the session data.
     */
    @Transactional
    public PaymentSessionRes confirmSession(String token) {
        log.info("Confirming payment session with token");

        PaymentSession session = sessionRepository.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Payment session not found"));

        if (session.getStatus() == PaymentSessionStatus.COMPLETED) {
            log.info("Session {} already completed, returning existing bookings", session.getId());
            return buildCompletedResponse(session);
        }

        if (session.getStatus() != PaymentSessionStatus.PENDING) {
            throw new IllegalStateException("Payment session is not pending: " + session.getStatus());
        }

        if (session.getExpiresAt().isBefore(Instant.now())) {
            session.setStatus(PaymentSessionStatus.EXPIRED);
            sessionRepository.save(session);
            throw new IllegalStateException("Payment session has expired");
        }

        // Confirm with payment provider
        PaymentSessionRes providerResult = paymentAdapter.confirmPayment(session);

        if (providerResult.status() == PaymentSessionStatus.CANCELLED) {
            // User cancelled the payment
            session.setStatus(PaymentSessionStatus.CANCELLED);
            session.setErrorMessage("Payment cancelled by user");
            sessionRepository.save(session);
            
            log.info("Payment session {} cancelled by user", session.getId());
            
            return new PaymentSessionRes(
                session.getId(),
                PaymentSessionStatus.CANCELLED,
                null,
                null,
                null,
                null,
                session.getExpiresAt(),
                session.isTest(),
                null
            );
        }

        if (providerResult.status() != PaymentSessionStatus.COMPLETED) {
            session.setStatus(PaymentSessionStatus.FAILED);
            session.setErrorMessage("Payment not completed by provider");
            sessionRepository.save(session);
            
            return new PaymentSessionRes(
                session.getId(),
                session.getStatus(),
                null,
                null,
                null,
                null,
                session.getExpiresAt(),
                session.isTest(),
                null
            );
        }

        // Payment successful - create bookings
        List<UUID> bookingIds = createBookingsFromSession(session);

        // Update session status
        session.setStatus(PaymentSessionStatus.COMPLETED);
        sessionRepository.save(session);

        // Clear user's cart
        cartRepository.findByUserId(session.getUser().getId())
            .ifPresent(cart -> {
                cartRepository.delete(cart);
                log.info("Cart cleared for user {} after successful payment", session.getUser().getId());
            });

        // Build response
        PaymentSessionRes response = new PaymentSessionRes(
            session.getId(),
            PaymentSessionStatus.COMPLETED,
            null,
            null,
            null,
            null,
            session.getExpiresAt(),
            session.isTest(),
            bookingIds
        );

        log.info("Payment session {} completed with {} bookings", session.getId(), bookingIds.size());
        return response;
    }

    /**
     * Confirm a MercadoPago payment session.
     * The webhook sends payment_id in data.id, so we need to fetch the payment
     * to get the external_reference (our session ID).
     * 
     * @param mpPaymentId The MercadoPago payment ID (payment_id from redirect or data.id from webhook)
     * @param externalReference Our session UUID (external_reference from redirect callback)
     */
    @Transactional
    public PaymentSessionRes confirmMercadoPagoSession(String mpPaymentId, String externalReference) {
        log.info("Confirming MercadoPago payment session - mpPaymentId: {}, externalReference: {}", 
            mpPaymentId, externalReference);

        // Validate we have at least one identifier
        if ((mpPaymentId == null || mpPaymentId.isEmpty()) && 
            (externalReference == null || externalReference.isEmpty())) {
            log.warn("MercadoPago confirmation without payment_id or external_reference - rejecting");
            throw new IllegalArgumentException("MercadoPago payment_id or external_reference is required");
        }

        PaymentSession session = null;
        
        // Strategy 1: If we have externalReference (our session UUID), use it directly
        if (externalReference != null && !externalReference.isEmpty()) {
            try {
                UUID sessionId = UUID.fromString(externalReference);
                session = sessionRepository.findById(sessionId).orElse(null);
                if (session != null) {
                    log.info("Found session from external_reference: {}", sessionId);
                }
            } catch (IllegalArgumentException e) {
                log.warn("external_reference is not a valid UUID: {}", externalReference);
            }
        }
        
        // Strategy 2: If we have mpPaymentId, fetch payment from MercadoPago API to get external_reference
        if (session == null && mpPaymentId != null && !mpPaymentId.isEmpty()) {
            try {
                String mpExternalRef = paymentAdapter.getExternalReferenceFromPayment(mpPaymentId);
                if (mpExternalRef != null) {
                    log.info("Found external_reference from MP payment API: {}", mpExternalRef);
                    try {
                        UUID sessionId = UUID.fromString(mpExternalRef);
                        session = sessionRepository.findById(sessionId).orElse(null);
                    } catch (IllegalArgumentException e) {
                        log.warn("MP external_reference is not a valid UUID: {}", mpExternalRef);
                    }
                }
            } catch (Exception e) {
                log.warn("Could not fetch MercadoPago payment {}: {}", mpPaymentId, e.getMessage());
            }
        }
        
        // Strategy 3: Try to find by externalPaymentId (preference_id stored when creating session)
        if (session == null && mpPaymentId != null) {
            session = sessionRepository.findByExternalPaymentId(mpPaymentId).orElse(null);
        }
        
        if (session == null) {
            String lookupInfo = mpPaymentId != null ? mpPaymentId : externalReference;
            throw new IllegalArgumentException("Payment session not found for MercadoPago: " + lookupInfo);
        }

        if (session.getStatus() == PaymentSessionStatus.COMPLETED) {
            log.info("Session {} already completed", session.getId());
            return buildCompletedResponse(session);
        }

        // Confirm with MercadoPago using the payment ID
        PaymentSessionRes providerResult = paymentAdapter.confirmMercadoPagoPayment(session, null, mpPaymentId);

        if (providerResult.status() != PaymentSessionStatus.COMPLETED) {
            session.setStatus(PaymentSessionStatus.FAILED);
            sessionRepository.save(session);
            return providerResult;
        }

        // Create bookings
        List<UUID> bookingIds = createBookingsFromSession(session);

        session.setStatus(PaymentSessionStatus.COMPLETED);
        sessionRepository.save(session);

        // Clear cart
        cartRepository.findByUserId(session.getUser().getId())
            .ifPresent(cartRepository::delete);

        PaymentSessionRes response = new PaymentSessionRes(
            session.getId(),
            PaymentSessionStatus.COMPLETED,
            null,
            null,
            null,
            null,
            session.getExpiresAt(),
            session.isTest(),
            bookingIds
        );

        log.info("MercadoPago payment confirmed for session {} with {} bookings", session.getId(), bookingIds.size());
        return response;
    }

    /**
     * Get the count of reserved slots for a schedule from pending payment sessions.
     */
    public int getReservedSlots(UUID scheduleId) {
        return sessionRepository.countReservedSlotsByScheduleId(scheduleId, Instant.now());
    }

    /**
     * Expire old pending sessions.
     * Only runs the UPDATE if there are expired sessions (saves DB compute).
     */
    @Transactional
    public int expirePendingSessions() {
        Instant now = Instant.now();
        // Quick check to avoid unnecessary UPDATE when no expired sessions exist
        if (!sessionRepository.existsExpiredPendingSessions(now)) {
            return 0;
        }
        int expired = sessionRepository.expirePendingSessions(now);
        if (expired > 0) {
            log.info("Expired {} pending payment sessions", expired);
        }
        return expired;
    }

    // === Private helper methods ===

    private void validateAvailability(PaymentSessionReq request) {
        for (var item : request.items()) {
            TourSchedule schedule = scheduleRepository.findById(item.scheduleId())
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + item.scheduleId()));

            // Check if schedule is bookable (must be OPEN)
            if (!"OPEN".equals(schedule.getStatus())) {
                throw new IllegalStateException("Schedule is not available for booking: " + item.scheduleId());
            }

            // Check capacity (including reserved slots from pending sessions)
            int bookedSlots = bookingRepository.countConfirmedParticipantsByScheduleId(item.scheduleId());
            int reservedSlots = getReservedSlots(item.scheduleId());
            int availableSlots = schedule.getMaxParticipants() - bookedSlots - reservedSlots;

            if (item.numParticipants() > availableSlots) {
                throw new IllegalStateException(
                    String.format("Not enough availability for schedule %s. Requested: %d, Available: %d",
                        item.scheduleId(), item.numParticipants(), availableSlots));
            }
        }
    }

    private BigDecimal calculateTrustedTotal(PaymentSessionReq request) {
        BigDecimal total = BigDecimal.ZERO;
        for (var item : request.items()) {
            TourSchedule schedule = scheduleRepository.findById(item.scheduleId())
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
            
            BigDecimal itemTotal = schedule.getTour().getPrice()
                .multiply(BigDecimal.valueOf(item.numParticipants()));
            total = total.add(itemTotal);
        }
        return total;
    }

    private PaymentSessionItem convertToSessionItem(PaymentSessionReq.PaymentSessionItemReq reqItem) {
        TourSchedule schedule = scheduleRepository.findById(reqItem.scheduleId())
            .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));

        // Get tour name using centralized utility
        String tourName = TourUtils.getTourName(schedule.getTour());
        LocalDate tourDate = schedule.getStartDatetime().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        BigDecimal pricePerPerson = schedule.getTour().getPrice();
        BigDecimal itemTotal = pricePerPerson.multiply(BigDecimal.valueOf(reqItem.numParticipants()));

        // Convert participants to record instances
        List<PaymentSessionItem.ParticipantData> participants = reqItem.participants().stream()
            .map(p -> new PaymentSessionItem.ParticipantData(
                p.fullName(),
                p.documentId(),
                p.nationality(),
                p.dateOfBirth(),
                p.pickupAddress(),
                p.specialRequirements(),
                p.phoneNumber(),
                p.email()
            ))
            .collect(Collectors.toList());

        return new PaymentSessionItem(
            reqItem.scheduleId(),
            tourName,
            tourDate,
            reqItem.numParticipants(),
            pricePerPerson,
            itemTotal,
            reqItem.specialRequests(),
            participants
        );
    }

    private List<UUID> createBookingsFromSession(PaymentSession session) {
        List<UUID> bookingIds = new ArrayList<>();
        User user = session.getUser();

        for (PaymentSessionItem item : session.getItems()) {
            TourSchedule schedule = scheduleRepository.findById(item.scheduleId())
                .orElseThrow(() -> new IllegalStateException("Schedule not found: " + item.scheduleId()));

            // Create booking
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setSchedule(schedule);
            booking.setTourDate(item.tourDate());
            booking.setStatus("CONFIRMED"); // Already paid!
            booking.setSubtotal(item.itemTotal());
            booking.setTaxAmount(BigDecimal.ZERO);
            booking.setTotalAmount(item.itemTotal());
            booking.setLanguageCode(session.getLanguageCode());
            booking.setSpecialRequests(item.specialRequests());
            // Set createdAt manually to ensure it's available for async email
            // (Hibernate's @CreationTimestamp only populates during flush)
            booking.setCreatedAt(java.time.Instant.now());

            // Create participants
            List<Participant> participants = new ArrayList<>();
            for (PaymentSessionItem.ParticipantData pd : item.participants()) {
                Participant participant = new Participant();
                participant.setBooking(booking);
                participant.setFullName(pd.fullName());
                participant.setDocumentId(pd.documentId());
                participant.setNationality(pd.nationality());
                participant.setDateOfBirth(pd.dateOfBirth());
                participant.setPickupAddress(pd.pickupAddress());
                participant.setSpecialRequirements(pd.specialRequirements());
                participant.setPhoneNumber(pd.phoneNumber());
                participant.setEmail(pd.email());
                participants.add(participant);
            }
            booking.setParticipants(participants);

            booking = bookingRepository.saveAndFlush(booking);
            bookingIds.add(booking.getId());

            log.info("Created booking {} from payment session {}", booking.getId(), session.getId());

            // Detach entity from persistence context to force fresh load with all relations
            // This ensures lazy associations are properly loaded for async email processing
            UUID bookingId = booking.getId();
            entityManager.detach(booking);

            try {
                Booking reloadedBooking = bookingRepository.findByIdWithDetails(bookingId)
                    .orElseThrow(() -> new IllegalStateException("Booking not found after save: " + bookingId));
                bookingService.sendBookingConfirmationNotifications(reloadedBooking);
            } catch (Exception e) {
                log.error("Failed to send confirmation email for booking {}", bookingId, e);
            }
        }

        return bookingIds;
    }

    private PaymentSessionRes buildCompletedResponse(PaymentSession session) {
        // Find bookings created from this session (by user and time)
        // For now, return empty list - bookings are tracked by the caller
        return new PaymentSessionRes(
            session.getId(),
            session.getStatus(),
            null,
            null,
            null,
            null,
            session.getExpiresAt(),
            session.isTest(),
            null
        );
    }
}
