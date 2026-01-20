package com.northernchile.api.booking;

import com.northernchile.api.audit.AuditLogService;
import com.northernchile.api.booking.dto.BookingClientUpdateReq;
import com.northernchile.api.booking.dto.BookingRes;
import com.northernchile.api.config.NotificationConfig;
import com.northernchile.api.exception.InvalidBookingStateException;
import com.northernchile.api.exception.ResourceNotFoundException;
import com.northernchile.api.model.Booking;
import com.northernchile.api.model.BookingStatus;
import com.northernchile.api.model.Participant;
import com.northernchile.api.model.User;
import com.northernchile.api.security.Role;
import org.springframework.security.access.AccessDeniedException;
import com.northernchile.api.notification.EmailService;
import com.northernchile.api.tour.TourUtils;
import com.northernchile.api.util.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
    private final EmailService emailService;
    private final AuditLogService auditLogService;
    private final BookingMapper bookingMapper;
    private final NotificationConfig notificationConfig;

    public BookingService(
            BookingRepository bookingRepository,
            EmailService emailService,
            AuditLogService auditLogService,
            BookingMapper bookingMapper,
            NotificationConfig notificationConfig) {
        this.bookingRepository = bookingRepository;
        this.emailService = emailService;
        this.auditLogService = auditLogService;
        this.bookingMapper = bookingMapper;
        this.notificationConfig = notificationConfig;
    }

    /**
     * Send booking confirmation emails to customer and admin.
     * Should only be called after payment is confirmed (status = CONFIRMED).
     *
     * <p><strong>Important:</strong> The booking parameter must be loaded with all required associations
     * (schedule, tour, owner, user, participants) to avoid N+1 queries. Use
     * {@code BookingRepository.findByIdWithDetails()} or ensure the booking was fetched
     * with appropriate JOIN FETCH clauses.</p>
     *
     * @param booking The booking entity with all associations eagerly loaded
     */
    public void sendBookingConfirmationNotifications(Booking booking) {
        var schedule = booking.getSchedule();
        var tour = schedule.getTour();

        // Format date and time
        java.time.format.DateTimeFormatter dateFormatter =
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(DateTimeUtils.CHILE_ZONE);
        java.time.format.DateTimeFormatter timeFormatter =
                java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                .withZone(DateTimeUtils.CHILE_ZONE);

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
                String.format("CLP %,.0f", booking.getTotalAmount()),
                booking.getLanguageCode() != null ? booking.getLanguageCode() : "es-CL"
        );

        // Send notification to tour owner (Partner Admin) if different from general admin
        User tourOwner = schedule.getTour().getOwner();
        if (tourOwner != null && tourOwner.getEmail() != null) {
            emailService.sendNewBookingNotificationToAdmin(booking, tourOwner.getEmail());
            log.info("Sent booking notification to tour owner: {}", tourOwner.getEmail());
        }

        // Also send to general admin (contacto@northernchile) if different from owner
        String adminEmail = notificationConfig.getAdminEmail();
        if (adminEmail != null && (tourOwner == null || !adminEmail.equalsIgnoreCase(tourOwner.getEmail()))) {
            emailService.sendNewBookingNotificationToAdmin(booking, adminEmail);
            log.info("Sent booking notification to general admin: {}", adminEmail);
        }
    }

    /**
     * Get paginated bookings for admin based on their role.
     * SUPER_ADMIN sees all bookings, PARTNER_ADMIN sees only their tours' bookings.
     */
    @Transactional(readOnly = true)
    public Page<BookingRes> getBookingsForAdminPaged(User admin, Pageable pageable) {
        if (Role.SUPER_ADMIN.getRoleName().equals(admin.getRole())) {
            return bookingRepository.findAllWithDetailsPaged(pageable)
                    .map(bookingMapper::toBookingRes);
        } else {
            return bookingRepository.findByTourOwnerIdPaged(admin.getId(), pageable)
                    .map(bookingMapper::toBookingRes);
        }
    }

    @Transactional(readOnly = true)
    public List<BookingRes> getBookingsByUser(User user) {
        return getBookingsByUser(user.getId());
    }

    @Transactional(readOnly = true)
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
    private void validateStatusTransition(UUID bookingId, BookingStatus currentStatus, BookingStatus newStatus) {
        // Same status is always allowed (no-op)
        if (currentStatus == newStatus) {
            return;
        }

        // Define allowed transitions
        Map<BookingStatus, List<BookingStatus>> allowedTransitions = Map.of(
            BookingStatus.PENDING, List.of(BookingStatus.CONFIRMED, BookingStatus.CANCELLED),
            BookingStatus.CONFIRMED, List.of(BookingStatus.COMPLETED, BookingStatus.CANCELLED),
            BookingStatus.CANCELLED, List.of(), // No transitions from CANCELLED
            BookingStatus.COMPLETED, List.of()  // No transitions from COMPLETED
        );

        List<BookingStatus> allowed = allowedTransitions.getOrDefault(currentStatus, List.of());

        if (!allowed.contains(newStatus)) {
            List<String> allowedStrings = allowed.stream().map(Enum::name).collect(Collectors.toList());
            throw new InvalidBookingStateException(bookingId, currentStatus.name(), newStatus.name(), allowedStrings);
        }
    }

    @Transactional
    public BookingRes updateBookingStatus(UUID bookingId, BookingStatus newStatus, User currentUser) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        BookingStatus oldStatus = booking.getStatus();

        // Validate status transition
        validateStatusTransition(bookingId, oldStatus, newStatus);

        booking.setStatus(newStatus);
        Booking updatedBooking = bookingRepository.save(booking);

        String tourName = booking.getSchedule().getTour().getDisplayName();
        String description = tourName + " - " + booking.getUser().getFullName();
        Map<String, Object> oldValues = Map.of("status", oldStatus.name());
        Map<String, Object> newValues = Map.of("status", newStatus.name());
        auditLogService.logUpdate(currentUser, "BOOKING", booking.getId(), description, oldValues, newValues);

        return bookingMapper.toBookingRes(updatedBooking);
    }

    @Transactional
    public void cancelBooking(UUID bookingId, User currentUser) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        BookingStatus oldStatus = booking.getStatus();

        // Validate status transition
        validateStatusTransition(bookingId, oldStatus, BookingStatus.CANCELLED);

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        String tourName = booking.getSchedule().getTour().getDisplayName();
        String description = tourName + " - " + booking.getUser().getFullName();
        Map<String, Object> oldValues = Map.of("status", oldStatus.name());
        Map<String, Object> newValues = Map.of("status", BookingStatus.CANCELLED.name());
        auditLogService.logUpdate(currentUser, "BOOKING", booking.getId(), description, oldValues, newValues);
    }

    @Transactional(readOnly = true)
    public List<BookingRes> getBookingsByUser(UUID userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(bookingMapper::toBookingRes)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingRes updateBookingDetails(UUID bookingId, BookingClientUpdateReq req, User currentUser) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        // Verify ownership
        if (!booking.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You can only update your own bookings");
        }

        // Only allow updates for PENDING or CONFIRMED bookings
        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.CONFIRMED) {
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
