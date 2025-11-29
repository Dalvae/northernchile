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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
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
        log.info("Creating payment session for user: {} with {} items", user.getId(), request.getItems().size());

        // Validate all schedules exist and have availability
        validateAvailability(request);

        // Calculate total from schedule prices (don't trust client amounts)
        BigDecimal trustedTotal = calculateTrustedTotal(request);
        
        if (request.getTotalAmount().compareTo(trustedTotal) != 0) {
            log.warn("SECURITY: Client total ({}) differs from calculated total ({}). Using calculated.",
                request.getTotalAmount(), trustedTotal);
        }

        // Convert request items to session items
        List<PaymentSessionItem> sessionItems = request.getItems().stream()
            .map(this::convertToSessionItem)
            .collect(Collectors.toList());

        // Create payment session
        PaymentSession session = new PaymentSession();
        session.setUser(user);
        session.setStatus(PaymentSessionStatus.PENDING);
        session.setItems(sessionItems);
        session.setTotalAmount(trustedTotal);
        session.setCurrency(request.getCurrency() != null ? request.getCurrency() : "CLP");
        session.setLanguageCode(request.getLanguageCode() != null ? request.getLanguageCode() : "es");
        session.setUserEmail(request.getUserEmail());
        session.setProvider(request.getProvider());
        session.setPaymentMethod(request.getPaymentMethod());
        session.setReturnUrl(request.getReturnUrl());
        session.setCancelUrl(request.getCancelUrl());
        session.setExpiresAt(Instant.now().plus(SESSION_EXPIRATION_MINUTES, ChronoUnit.MINUTES));
        session.setTest(testMode);

        session = sessionRepository.save(session);
        log.info("Payment session created: {}", session.getId());

        // Initialize payment with provider
        try {
            PaymentSessionRes response = paymentAdapter.initializePayment(session, request);
            
            // Update session with payment details
            session.setToken(response.getToken());
            session.setPaymentUrl(response.getPaymentUrl());
            session.setQrCode(response.getQrCode());
            session.setPixCode(response.getPixCode());
            session.setExternalPaymentId(response.getSessionId() != null ? response.getSessionId().toString() : null);
            sessionRepository.save(session);

            // Return response with session ID
            response.setSessionId(session.getId());
            response.setStatus(session.getStatus());
            response.setExpiresAt(session.getExpiresAt());
            response.setTest(session.isTest());

            return response;

        } catch (Exception e) {
            log.error("Failed to initialize payment for session: {}", session.getId(), e);
            session.setStatus(PaymentSessionStatus.FAILED);
            session.setErrorMessage(e.getMessage());
            sessionRepository.save(session);
            throw new PaymentProviderException(
                "Failed to initialize payment",
                request.getProvider().name(),
                e.getMessage(),
                e
            );
        }
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

        if (providerResult.getStatus() != PaymentSessionStatus.COMPLETED) {
            session.setStatus(PaymentSessionStatus.FAILED);
            session.setErrorMessage("Payment not completed by provider");
            sessionRepository.save(session);
            
            PaymentSessionRes response = new PaymentSessionRes();
            response.setSessionId(session.getId());
            response.setStatus(session.getStatus());
            return response;
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
        PaymentSessionRes response = new PaymentSessionRes();
        response.setSessionId(session.getId());
        response.setStatus(PaymentSessionStatus.COMPLETED);
        response.setBookingIds(bookingIds);
        response.setTest(session.isTest());

        log.info("Payment session {} completed with {} bookings", session.getId(), bookingIds.size());
        return response;
    }

    /**
     * Confirm a MercadoPago payment session.
     * @param lookupId Can be either preference_id (stored in externalPaymentId) or session UUID (external_reference)
     * @param mpPaymentId The MercadoPago payment ID
     */
    @Transactional
    public PaymentSessionRes confirmMercadoPagoSession(String lookupId, String mpPaymentId) {
        log.info("Confirming MercadoPago payment session - lookupId: {}, payment: {}", lookupId, mpPaymentId);

        // Try to find by externalPaymentId (preference_id) first
        PaymentSession session = sessionRepository.findByExternalPaymentId(lookupId)
            .orElse(null);
        
        // If not found, try to find by session UUID (external_reference)
        if (session == null) {
            try {
                UUID sessionId = UUID.fromString(lookupId);
                session = sessionRepository.findById(sessionId).orElse(null);
            } catch (IllegalArgumentException e) {
                // Not a valid UUID, ignore
            }
        }
        
        if (session == null) {
            throw new IllegalArgumentException("Payment session not found for: " + lookupId);
        }

        if (session.getStatus() == PaymentSessionStatus.COMPLETED) {
            log.info("Session {} already completed", session.getId());
            return buildCompletedResponse(session);
        }

        // Confirm with MercadoPago
        PaymentSessionRes providerResult = paymentAdapter.confirmMercadoPagoPayment(session, lookupId, mpPaymentId);

        if (providerResult.getStatus() != PaymentSessionStatus.COMPLETED) {
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

        PaymentSessionRes response = new PaymentSessionRes();
        response.setSessionId(session.getId());
        response.setStatus(PaymentSessionStatus.COMPLETED);
        response.setBookingIds(bookingIds);
        response.setTest(session.isTest());

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
     */
    @Transactional
    public int expirePendingSessions() {
        int expired = sessionRepository.expirePendingSessions(Instant.now());
        if (expired > 0) {
            log.info("Expired {} pending payment sessions", expired);
        }
        return expired;
    }

    // === Private helper methods ===

    private void validateAvailability(PaymentSessionReq request) {
        for (var item : request.getItems()) {
            TourSchedule schedule = scheduleRepository.findById(item.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + item.getScheduleId()));

            // Check if schedule is bookable (must be OPEN)
            if (!"OPEN".equals(schedule.getStatus())) {
                throw new IllegalStateException("Schedule is not available for booking: " + item.getScheduleId());
            }

            // Check capacity (including reserved slots from pending sessions)
            int bookedSlots = bookingRepository.countConfirmedParticipantsByScheduleId(item.getScheduleId());
            int reservedSlots = getReservedSlots(item.getScheduleId());
            int availableSlots = schedule.getMaxParticipants() - bookedSlots - reservedSlots;

            if (item.getNumParticipants() > availableSlots) {
                throw new IllegalStateException(
                    String.format("Not enough availability for schedule %s. Requested: %d, Available: %d",
                        item.getScheduleId(), item.getNumParticipants(), availableSlots));
            }
        }
    }

    private BigDecimal calculateTrustedTotal(PaymentSessionReq request) {
        BigDecimal total = BigDecimal.ZERO;
        for (var item : request.getItems()) {
            TourSchedule schedule = scheduleRepository.findById(item.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));
            
            BigDecimal itemTotal = schedule.getTour().getPrice()
                .multiply(BigDecimal.valueOf(item.getNumParticipants()));
            total = total.add(itemTotal);
        }
        return total;
    }

    private PaymentSessionItem convertToSessionItem(PaymentSessionReq.PaymentSessionItemReq reqItem) {
        TourSchedule schedule = scheduleRepository.findById(reqItem.getScheduleId())
            .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));

        PaymentSessionItem item = new PaymentSessionItem();
        item.setScheduleId(reqItem.getScheduleId());
        // Get tour name (use default language or first available)
        String tourName = schedule.getTour().getNameTranslations() != null
            ? schedule.getTour().getNameTranslations().getOrDefault("es",
                schedule.getTour().getNameTranslations().values().iterator().next())
            : "Tour";
        item.setTourName(tourName);
        item.setTourDate(schedule.getStartDatetime().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
        item.setNumParticipants(reqItem.getNumParticipants());
        item.setPricePerPerson(schedule.getTour().getPrice());
        item.setItemTotal(schedule.getTour().getPrice().multiply(BigDecimal.valueOf(reqItem.getNumParticipants())));
        item.setSpecialRequests(reqItem.getSpecialRequests());

        // Convert participants
        List<PaymentSessionItem.ParticipantData> participants = reqItem.getParticipants().stream()
            .map(p -> {
                PaymentSessionItem.ParticipantData pd = new PaymentSessionItem.ParticipantData();
                pd.setFullName(p.getFullName());
                pd.setDocumentId(p.getDocumentId());
                pd.setNationality(p.getNationality());
                pd.setDateOfBirth(p.getDateOfBirth());
                pd.setPickupAddress(p.getPickupAddress());
                pd.setSpecialRequirements(p.getSpecialRequirements());
                pd.setPhoneNumber(p.getPhoneNumber());
                pd.setEmail(p.getEmail());
                return pd;
            })
            .collect(Collectors.toList());
        item.setParticipants(participants);

        return item;
    }

    private List<UUID> createBookingsFromSession(PaymentSession session) {
        List<UUID> bookingIds = new ArrayList<>();
        User user = session.getUser();

        for (PaymentSessionItem item : session.getItems()) {
            TourSchedule schedule = scheduleRepository.findById(item.getScheduleId())
                .orElseThrow(() -> new IllegalStateException("Schedule not found: " + item.getScheduleId()));

            // Create booking
            Booking booking = new Booking();
            booking.setUser(user);
            booking.setSchedule(schedule);
            booking.setTourDate(item.getTourDate());
            booking.setStatus("CONFIRMED"); // Already paid!
            booking.setSubtotal(item.getItemTotal());
            booking.setTaxAmount(BigDecimal.ZERO);
            booking.setTotalAmount(item.getItemTotal());
            booking.setLanguageCode(session.getLanguageCode());
            booking.setSpecialRequests(item.getSpecialRequests());

            // Create participants
            List<Participant> participants = new ArrayList<>();
            for (PaymentSessionItem.ParticipantData pd : item.getParticipants()) {
                Participant participant = new Participant();
                participant.setBooking(booking);
                participant.setFullName(pd.getFullName());
                participant.setDocumentId(pd.getDocumentId());
                participant.setNationality(pd.getNationality());
                participant.setDateOfBirth(pd.getDateOfBirth());
                participant.setPickupAddress(pd.getPickupAddress());
                participant.setSpecialRequirements(pd.getSpecialRequirements());
                participant.setPhoneNumber(pd.getPhoneNumber());
                participant.setEmail(pd.getEmail());
                participants.add(participant);
            }
            booking.setParticipants(participants);

            booking = bookingRepository.save(booking);
            bookingIds.add(booking.getId());

            log.info("Created booking {} from payment session {}", booking.getId(), session.getId());

            // Send confirmation email
            try {
                bookingService.sendBookingConfirmationNotifications(booking);
            } catch (Exception e) {
                log.error("Failed to send confirmation email for booking {}", booking.getId(), e);
            }
        }

        return bookingIds;
    }

    private PaymentSessionRes buildCompletedResponse(PaymentSession session) {
        // Find bookings created from this session (by user and time)
        // For now, return empty list - bookings are tracked by the caller
        PaymentSessionRes response = new PaymentSessionRes();
        response.setSessionId(session.getId());
        response.setStatus(session.getStatus());
        response.setTest(session.isTest());
        return response;
    }
}
