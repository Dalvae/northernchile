package com.northernchile.api.booking;

import com.northernchile.api.availability.AvailabilityValidator;
import com.northernchile.api.booking.event.BookingEventPublisher;
import com.northernchile.api.exception.ScheduleFullException;
import com.northernchile.api.model.Booking;
import com.northernchile.api.model.BookingStatus;
import com.northernchile.api.model.Participant;
import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.model.User;
import com.northernchile.api.payment.model.PaymentSession;
import com.northernchile.api.payment.model.PaymentSessionItem;
import com.northernchile.api.pricing.PricingService;
import com.northernchile.api.tour.TourScheduleRepository;
import com.northernchile.api.user.SavedParticipantService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of BookingCreationService.
 * Creates bookings from completed payment sessions with proper validation and notifications.
 */
@Service
public class BookingCreationServiceImpl implements BookingCreationService {

    private static final Logger log = LoggerFactory.getLogger(BookingCreationServiceImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final TourScheduleRepository scheduleRepository;
    private final BookingRepository bookingRepository;
    private final AvailabilityValidator availabilityValidator;
    private final PricingService pricingService;
    private final SavedParticipantService savedParticipantService;
    private final BookingEventPublisher eventPublisher;

    public BookingCreationServiceImpl(
            TourScheduleRepository scheduleRepository,
            BookingRepository bookingRepository,
            AvailabilityValidator availabilityValidator,
            PricingService pricingService,
            SavedParticipantService savedParticipantService,
            BookingEventPublisher eventPublisher) {
        this.scheduleRepository = scheduleRepository;
        this.bookingRepository = bookingRepository;
        this.availabilityValidator = availabilityValidator;
        this.pricingService = pricingService;
        this.savedParticipantService = savedParticipantService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public List<UUID> createBookingsFromPaymentSession(PaymentSession session, User user) {
        List<UUID> bookingIds = new ArrayList<>();

        for (PaymentSessionItem item : session.getItems()) {
            UUID bookingId = createBookingForItem(session, item, user);
            bookingIds.add(bookingId);
        }

        return bookingIds;
    }

    /**
     * Creates a single booking from a payment session item.
     */
    private UUID createBookingForItem(PaymentSession session, PaymentSessionItem item, User user) {
        // Use pessimistic lock to prevent overbooking race conditions
        TourSchedule schedule = scheduleRepository.findByIdWithLock(item.scheduleId())
                .orElseThrow(() -> new IllegalStateException("Schedule not found: " + item.scheduleId()));

        // Re-validate availability with lock held (critical for preventing overbooking!)
        // Exclude this user's cart since it's being converted to booking
        var availability = availabilityValidator.validateAvailability(
                schedule, item.numParticipants(), null, user.getId());

        if (!availability.available()) {
            log.error("Overbooking prevented! Session {} item for schedule {} - {}",
                    session.getId(), schedule.getId(), availability.errorMessage());
            throw new ScheduleFullException(
                    "No hay suficientes cupos disponibles para " + item.tourName() +
                            ". Disponibles: " + availability.availableSlots() +
                            ", Solicitados: " + item.numParticipants());
        }

        // Create booking with proper tax calculation using PricingService
        // Tour prices are tax-inclusive, so we need to back-calculate subtotal and tax
        var pricing = pricingService.calculateFromTaxInclusiveAmount(item.itemTotal());

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setSchedule(schedule);
        booking.setTourDate(item.tourDate());
        booking.setStatus(BookingStatus.CONFIRMED); // Already paid!
        booking.setSubtotal(pricing.subtotal());
        booking.setTaxAmount(pricing.taxAmount());
        booking.setTotalAmount(pricing.totalAmount());
        booking.setLanguageCode(session.getLanguageCode());
        booking.setSpecialRequests(item.specialRequests());
        // Set createdAt manually to ensure it's available for async email
        // (Hibernate's @CreationTimestamp only populates during flush)
        booking.setCreatedAt(Instant.now());

        // Create participants
        List<Participant> participants = createParticipants(booking, item, user);
        booking.setParticipants(participants);

        booking = bookingRepository.saveAndFlush(booking);
        UUID bookingId = booking.getId();

        log.info("Created booking {} from payment session {}", bookingId, session.getId());

        // Publish event to trigger confirmation notifications asynchronously
        eventPublisher.publishBookingCreated(bookingId);

        return bookingId;
    }

    /**
     * Creates participant records from payment session item data.
     */
    private List<Participant> createParticipants(Booking booking, PaymentSessionItem item, User user) {
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

            // Process markAsSelf and saveForFuture flags
            // This syncs participant data to user profile and/or saves for future bookings
            saveParticipantDataIfRequested(pd, user);
        }

        return participants;
    }

    /**
     * Saves participant data to user profile if requested.
     */
    private void saveParticipantDataIfRequested(PaymentSessionItem.ParticipantData pd, User user) {
        boolean shouldSave = Boolean.TRUE.equals(pd.saveForFuture()) || Boolean.TRUE.equals(pd.markAsSelf());
        if (!shouldSave) {
            return;
        }

        try {
            savedParticipantService.createFromBookingData(
                    user,
                    pd.fullName(),
                    pd.documentId(),
                    pd.dateOfBirth(),
                    pd.nationality(),
                    pd.phoneNumber(),
                    pd.email(),
                    Boolean.TRUE.equals(pd.markAsSelf())
            );
            log.info("Saved participant data for user {}, markAsSelf={}, saveForFuture={}",
                    user.getEmail(), pd.markAsSelf(), pd.saveForFuture());
        } catch (Exception e) {
            // Don't fail the booking if saved participant creation fails
            log.error("Failed to save participant data for user {}: {}",
                    user.getEmail(), e.getMessage());
        }
    }

}
