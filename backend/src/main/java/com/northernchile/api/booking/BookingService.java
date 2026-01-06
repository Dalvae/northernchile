package com.northernchile.api.booking;

import com.northernchile.api.audit.AuditLogService;
import com.northernchile.api.booking.dto.BookingClientUpdateReq;
import com.northernchile.api.booking.dto.BookingCreateReq;
import com.northernchile.api.booking.dto.BookingRes;
import com.northernchile.api.booking.dto.ParticipantRes;
import com.northernchile.api.exception.BookingCutoffException;
import com.northernchile.api.exception.InvalidBookingStateException;
import com.northernchile.api.exception.ResourceNotFoundException;
import com.northernchile.api.exception.ScheduleFullException;
import com.northernchile.api.model.Booking;
import com.northernchile.api.model.Participant;
import com.northernchile.api.model.User;
import com.northernchile.api.security.Role;
import com.northernchile.api.notification.EmailService;
import com.northernchile.api.pricing.PricingService;
import com.northernchile.api.tour.TourScheduleRepository;
import com.northernchile.api.tour.TourUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final EmailService emailService;
    private final AuditLogService auditLogService;
    private final BookingMapper bookingMapper;
    private final com.northernchile.api.availability.AvailabilityValidator availabilityValidator;
    private final PricingService pricingService;

    @Value("${notification.admin.email}")
    private String adminEmail;

    @Value("${booking.min-hours-before-tour:2}")
    private int minHoursBeforeTour;

    public BookingService(
            BookingRepository bookingRepository,
            TourScheduleRepository tourScheduleRepository,
            EmailService emailService,
            AuditLogService auditLogService,
            BookingMapper bookingMapper,
            com.northernchile.api.availability.AvailabilityValidator availabilityValidator,
            PricingService pricingService) {
        this.bookingRepository = bookingRepository;
        this.tourScheduleRepository = tourScheduleRepository;
        this.emailService = emailService;
        this.auditLogService = auditLogService;
        this.bookingMapper = bookingMapper;
        this.availabilityValidator = availabilityValidator;
        this.pricingService = pricingService;
    }

    @Transactional
    public BookingRes createBooking(BookingCreateReq req, User currentUser) {
        // Use pessimistic locking to prevent race conditions instead of SERIALIZABLE isolation
        var schedule = tourScheduleRepository.findByIdWithLock(req.scheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("TourSchedule", req.scheduleId()));

        // Validate booking is not too close to tour start time
        validateBookingCutoffTime(schedule);

        // Exclude current user's cart from availability check (they're converting cart to booking)
        UUID excludeUserId = currentUser != null ? currentUser.getId() : null;
        validateAvailability(schedule, req.participants().size(), excludeUserId);

        // Use centralized pricing service for consistent calculations
        var pricing = pricingService.calculateLineItem(schedule.getTour().getPrice(), req.participants().size());

        Booking booking = createBookingEntity(req, currentUser, schedule, pricing);
        List<Participant> participants = createParticipantEntities(req, booking);
        booking.setParticipants(participants);

        Booking savedBooking = bookingRepository.save(booking);
        
        // Note: Cart is cleared when payment is confirmed, not when booking is created.
        // This ensures the cart is preserved if payment fails.
        
        // Note: Confirmation emails are sent when booking status changes to CONFIRMED
        // (after successful payment), not when the booking is first created as PENDING.
        // Admin notification is also sent after payment confirmation.

        return bookingMapper.toBookingRes(savedBooking);
    }

    private void validateAvailability(com.northernchile.api.model.TourSchedule schedule, int requestedSlots, UUID excludeUserId) {
        // Exclude the current user's cart items from availability check
        // since those items are being converted to a booking
        var availabilityResult = availabilityValidator.validateAvailability(schedule, requestedSlots, null, excludeUserId);
        if (!availabilityResult.available()) {
            throw new ScheduleFullException(availabilityResult.errorMessage());
        }
    }

    private void validateBookingCutoffTime(com.northernchile.api.model.TourSchedule schedule) {
        Instant now = Instant.now();
        Instant cutoffTime = schedule.getStartDatetime().minus(minHoursBeforeTour, ChronoUnit.HOURS);

        if (now.isAfter(cutoffTime)) {
            throw new BookingCutoffException(schedule.getId(), cutoffTime, minHoursBeforeTour);
        }
    }

    private Booking createBookingEntity(BookingCreateReq req, User currentUser,
                                        com.northernchile.api.model.TourSchedule schedule,
                                        PricingService.PricingResult pricing) {
        Booking booking = new Booking();
        booking.setUser(currentUser);
        booking.setSchedule(schedule);
        booking.setTourDate(LocalDate.ofInstant(schedule.getStartDatetime(), java.time.ZoneId.of("America/Santiago")));
        booking.setStatus("PENDING");
        booking.setSubtotal(pricing.subtotal());
        booking.setTaxAmount(pricing.taxAmount());
        booking.setTotalAmount(pricing.totalAmount());
        booking.setLanguageCode(req.languageCode());
        booking.setSpecialRequests(req.specialRequests());
        return booking;
    }

    private List<Participant> createParticipantEntities(BookingCreateReq req, Booking booking) {
        List<Participant> participants = new ArrayList<>();
        for (var participantReq : req.participants()) {
            Participant participant = new Participant();
            participant.setBooking(booking);
            participant.setFullName(participantReq.fullName());
            participant.setDocumentId(participantReq.documentId());
            participant.setNationality(participantReq.nationality());

            if (participantReq.dateOfBirth() != null) {
                participant.setDateOfBirth(participantReq.dateOfBirth());
            } else if (participantReq.age() != null) {
                participant.setAge(participantReq.age());
            }

            participant.setPickupAddress(participantReq.pickupAddress());
            participant.setSpecialRequirements(participantReq.specialRequirements());
            participant.setPhoneNumber(participantReq.phoneNumber());
            participant.setEmail(participantReq.email());
            participants.add(participant);
        }
        return participants;
    }

    /**
     * Send booking confirmation emails to customer and admin.
     * Should only be called after payment is confirmed (status = CONFIRMED).
     */
    public void sendBookingConfirmationNotifications(Booking booking) {
        var schedule = booking.getSchedule();
        var tour = schedule.getTour();

        // Format date and time
        java.time.format.DateTimeFormatter dateFormatter =
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(java.time.ZoneId.of("America/Santiago"));
        java.time.format.DateTimeFormatter timeFormatter =
                java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                .withZone(java.time.ZoneId.of("America/Santiago"));

        String tourDate = dateFormatter.format(schedule.getStartDatetime());
        String tourTime = timeFormatter.format(schedule.getStartDatetime());

        // Get tour name in the correct language using centralized utility
        String tourName = TourUtils.getTourName(tour, booking.getLanguageCode());

        emailService.sendBookingConfirmationEmail(
                booking.getUser().getEmail(),
                booking.getUser().getFullName(),
                booking.getId().toString(),
                tourName,
                tourDate,
                tourTime,
                booking.getParticipants().size(),
                booking.getTotalAmount().toString(),
                booking.getLanguageCode() != null ? booking.getLanguageCode() : "es-CL"
        );

        // Send notification to tour owner (Partner Admin) if different from general admin
        User tourOwner = schedule.getTour().getOwner();
        if (tourOwner != null && tourOwner.getEmail() != null) {
            emailService.sendNewBookingNotificationToAdmin(booking, tourOwner.getEmail());
            log.info("Sent booking notification to tour owner: {}", tourOwner.getEmail());
        }

        // Also send to general admin (contacto@northernchile) if different from owner
        if (adminEmail != null && (tourOwner == null || !adminEmail.equalsIgnoreCase(tourOwner.getEmail()))) {
            emailService.sendNewBookingNotificationToAdmin(booking, adminEmail);
            log.info("Sent booking notification to general admin: {}", adminEmail);
        }
    }

    @Transactional(readOnly = true)
    public List<BookingRes> getAllBookingsForAdmin() {
        return bookingRepository.findAllWithDetails().stream()
                .map(bookingMapper::toBookingRes)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingRes> getBookingsByTourOwner(User owner) {
        return bookingRepository.findByTourOwnerId(owner.getId()).stream()
                .map(bookingMapper::toBookingRes)
                .collect(Collectors.toList());
    }

    /**
     * Get bookings for admin based on their role.
     * SUPER_ADMIN sees all bookings, PARTNER_ADMIN sees only their tours' bookings.
     */
    @Transactional(readOnly = true)
    public List<BookingRes> getBookingsForAdmin(User admin) {
        if (Role.SUPER_ADMIN.getRoleName().equals(admin.getRole())) {
            return getAllBookingsForAdmin();
        } else {
            return getBookingsByTourOwner(admin);
        }
    }

    @Transactional(readOnly = true)
    public List<BookingRes> getBookingsByUser(User user) {
        return getBookingsByUser(user.getId());
    }

    public Optional<BookingRes> getBookingById(UUID bookingId, User currentUser) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        return Optional.of(bookingMapper.toBookingRes(booking));
    }

    /**
     * Validates that a status transition is allowed.
     * Valid transitions:
     * - PENDING -> CONFIRMED (after payment)
     * - PENDING -> CANCELLED (before 24h)
     * - CONFIRMED -> COMPLETED (after tour date)
     * - CONFIRMED -> CANCELLED (before 24h, with refund)
     * - CANCELLED -> (no transitions allowed)
     * - COMPLETED -> (no transitions allowed)
     *
     * @param bookingId The booking ID (for error context)
     * @param currentStatus The current booking status
     * @param newStatus The desired new status
     * @throws InvalidBookingStateException if the transition is not allowed
     */
    private void validateStatusTransition(UUID bookingId, String currentStatus, String newStatus) {
        // Same status is always allowed (no-op)
        if (currentStatus.equals(newStatus)) {
            return;
        }

        // Define allowed transitions
        Map<String, List<String>> allowedTransitions = Map.of(
            "PENDING", List.of("CONFIRMED", "CANCELLED"),
            "CONFIRMED", List.of("COMPLETED", "CANCELLED"),
            "CANCELLED", List.of(), // No transitions from CANCELLED
            "COMPLETED", List.of()  // No transitions from COMPLETED
        );

        List<String> allowed = allowedTransitions.getOrDefault(currentStatus, List.of());

        if (!allowed.contains(newStatus)) {
            throw new InvalidBookingStateException(bookingId, currentStatus, newStatus, allowed);
        }
    }

    @Transactional
    public BookingRes updateBookingStatus(UUID bookingId, String newStatus, User currentUser) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        String oldStatus = booking.getStatus();

        // Validate status transition
        validateStatusTransition(bookingId, oldStatus, newStatus);

        booking.setStatus(newStatus);
        Booking updatedBooking = bookingRepository.save(booking);

        String tourName = booking.getSchedule().getTour().getDisplayName();
        String description = tourName + " - " + booking.getUser().getFullName();
        Map<String, Object> oldValues = Map.of("status", oldStatus);
        Map<String, Object> newValues = Map.of("status", newStatus);
        auditLogService.logUpdate(currentUser, "BOOKING", booking.getId(), description, oldValues, newValues);

        return bookingMapper.toBookingRes(updatedBooking);
    }

    @Transactional
    public void cancelBooking(UUID bookingId, User currentUser) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        String oldStatus = booking.getStatus();

        // Validate status transition
        validateStatusTransition(bookingId, oldStatus, "CANCELLED");

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        String tourName = booking.getSchedule().getTour().getDisplayName();
        String description = tourName + " - " + booking.getUser().getFullName();
        Map<String, Object> oldValues = Map.of("status", oldStatus);
        Map<String, Object> newValues = Map.of("status", "CANCELLED");
        auditLogService.logUpdate(currentUser, "BOOKING", booking.getId(), description, oldValues, newValues);
    }

    @Transactional(readOnly = true)
    public List<BookingRes> getBookingsByUser(UUID userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(bookingMapper::toBookingRes)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingRes confirmBookingAfterMockPayment(UUID bookingId, User currentUser) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (!"PENDING".equals(booking.getStatus())) {
            throw new InvalidBookingStateException(
                "Only PENDING bookings can be confirmed. Current status: " + booking.getStatus());
        }

        String oldStatus = booking.getStatus();
        booking.setStatus("CONFIRMED");
        Booking confirmedBooking = bookingRepository.save(booking);

        String tourName = booking.getSchedule().getTour().getDisplayName();
        String description = "Mock payment confirmation - " + tourName + " - " + booking.getUser().getFullName();
        Map<String, Object> oldValues = Map.of("status", oldStatus);
        Map<String, Object> newValues = Map.of("status", "CONFIRMED");
        auditLogService.logUpdate(currentUser, "BOOKING", booking.getId(), description, oldValues, newValues);

        return bookingMapper.toBookingRes(confirmedBooking);
    }

    @Transactional
    public BookingRes updateBookingDetails(UUID bookingId, BookingClientUpdateReq req, User currentUser) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        // Verify ownership
        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("You can only update your own bookings");
        }

        // Only allow updates for PENDING or CONFIRMED bookings
        if (!"PENDING".equals(booking.getStatus()) && !"CONFIRMED".equals(booking.getStatus())) {
            throw new InvalidBookingStateException(
                "Cannot update booking with status: " + booking.getStatus());
        }

        // Update special requests at booking level
        booking.setSpecialRequests(req.specialRequests());

        // Update participants
        for (BookingClientUpdateReq.ParticipantUpdateReq participantReq : req.participants()) {
            Participant participant = booking.getParticipants().stream()
                    .filter(p -> p.getId().equals(participantReq.id()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Participant", participantReq.id()));

            participant.setPickupAddress(participantReq.pickupAddress());
            participant.setSpecialRequirements(participantReq.specialRequirements());
            participant.setPhoneNumber(participantReq.phoneNumber());
            participant.setEmail(participantReq.email());
        }

        Booking updatedBooking = bookingRepository.save(booking);

        // Audit log
        String tourName = booking.getSchedule().getTour().getDisplayName();
        String description = tourName + " - " + booking.getUser().getFullName() + " (Updated details)";
        auditLogService.logUpdate(currentUser, "BOOKING", booking.getId(), description, null, null);

        return bookingMapper.toBookingRes(updatedBooking);
    }
}
