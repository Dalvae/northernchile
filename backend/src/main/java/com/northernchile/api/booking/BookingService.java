package com.northernchile.api.booking;

import com.northernchile.api.audit.AuditLogService;
import com.northernchile.api.booking.dto.BookingCreateReq;
import com.northernchile.api.booking.dto.BookingRes;
import com.northernchile.api.booking.dto.ParticipantRes;
import com.northernchile.api.model.Booking;
import com.northernchile.api.model.Participant;
import com.northernchile.api.model.User;
import com.northernchile.api.notification.EmailService;
import com.northernchile.api.tour.TourScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TourScheduleRepository tourScheduleRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuditLogService auditLogService;

    @Value("${tax.rate}")
    private BigDecimal taxRate;

    @Transactional
    public BookingRes createBooking(BookingCreateReq req, User currentUser) {
        var schedule = tourScheduleRepository.findById(req.getScheduleId())
                .orElseThrow(() -> new EntityNotFoundException("TourSchedule not found with id: " + req.getScheduleId()));

        // 1. Validate available slots
        Integer bookedParticipants = bookingRepository.countParticipantsByScheduleId(req.getScheduleId());
        if (bookedParticipants == null) bookedParticipants = 0;
        int requestedSlots = req.getParticipants().size();
        if (schedule.getMaxParticipants() - bookedParticipants < requestedSlots) {
            throw new IllegalStateException("Not enough available slots for this tour schedule.");
        }

        // 2. Calculate prices and taxes
        int participantCount = req.getParticipants().size();
        BigDecimal pricePerParticipant = schedule.getTour().getPrice();

        BigDecimal totalAmount = pricePerParticipant.multiply(BigDecimal.valueOf(participantCount));

        BigDecimal subtotal = totalAmount.divide(BigDecimal.ONE.add(taxRate), 2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = totalAmount.subtract(subtotal);

        // 3. Create and save Booking and Participants
        Booking booking = new Booking();
        booking.setUser(currentUser);
        booking.setSchedule(schedule);
        booking.setTourDate(LocalDate.ofInstant(schedule.getStartDatetime(), ZoneOffset.UTC));
        booking.setStatus("PENDING"); // Status is pending until payment is confirmed
        booking.setSubtotal(subtotal);
        booking.setTaxAmount(taxAmount);
        booking.setTotalAmount(totalAmount);
        booking.setLanguageCode(req.getLanguageCode());
        booking.setSpecialRequests(req.getSpecialRequests());

        List<Participant> participants = new ArrayList<>();
        for (var participantReq : req.getParticipants()) {
            Participant participant = new Participant();
            participant.setBooking(booking);
            participant.setFullName(participantReq.getFullName());
            participant.setDocumentId(participantReq.getDocumentId());
            participant.setNationality(participantReq.getNationality());
            participant.setAge(participantReq.getAge());
            participant.setPickupAddress(participantReq.getPickupAddress());
            participant.setSpecialRequirements(participantReq.getSpecialRequirements());
            participants.add(participant);
        }
        booking.setParticipants(participants);

        Booking savedBooking = bookingRepository.save(booking);

        // 4. Send confirmation emails
        emailService.sendBookingConfirmationEmail(
                savedBooking.getLanguageCode(),
                savedBooking.getId().toString(),
                savedBooking.getUser().getFullName(),
                savedBooking.getSchedule().getTour().getNameTranslations().get(savedBooking.getLanguageCode())
        );
        emailService.sendNewBookingNotificationToAdmin(savedBooking.getId().toString());

        return toBookingRes(savedBooking);
    }

    // CRITICAL: Filter bookings for PARTNER_ADMIN to only show bookings for their tours
    public List<BookingRes> getAllBookings(User currentUser) {
        if ("ROLE_SUPER_ADMIN".equals(currentUser.getRole())) {
            // Super admin sees all bookings
            return bookingRepository.findAll().stream()
                    .map(this::toBookingRes)
                    .collect(Collectors.toList());
        } else if ("ROLE_PARTNER_ADMIN".equals(currentUser.getRole())) {
            // Partner admin only sees bookings for their tours
            return bookingRepository.findAll().stream()
                    .filter(booking -> booking.getSchedule().getTour().getOwner().getId().equals(currentUser.getId()))
                    .map(this::toBookingRes)
                    .collect(Collectors.toList());
        } else {
            // Regular users shouldn't use this endpoint, but return empty for safety
            return List.of();
        }
    }

    public Optional<BookingRes> getBookingById(UUID bookingId, User currentUser) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        // Check access: SUPER_ADMIN, tour owner (PARTNER_ADMIN), or the booking user
        boolean isSuperAdmin = "ROLE_SUPER_ADMIN".equals(currentUser.getRole());
        boolean isTourOwner = booking.getSchedule().getTour().getOwner().getId().equals(currentUser.getId());
        boolean isBookingUser = booking.getUser().getId().equals(currentUser.getId());

        if (!isSuperAdmin && !isTourOwner && !isBookingUser) {
            throw new AccessDeniedException("You do not have permission to view this booking.");
        }

        return Optional.of(toBookingRes(booking));
    }

    @Transactional
    public BookingRes updateBookingStatus(UUID bookingId, String newStatus, User currentUser) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        // Check access: SUPER_ADMIN or tour owner (PARTNER_ADMIN)
        boolean isSuperAdmin = "ROLE_SUPER_ADMIN".equals(currentUser.getRole());
        boolean isTourOwner = booking.getSchedule().getTour().getOwner().getId().equals(currentUser.getId());

        if (!isSuperAdmin && !isTourOwner) {
            throw new AccessDeniedException("You do not have permission to update this booking.");
        }

        // Capture old status for audit
        String oldStatus = booking.getStatus();

        booking.setStatus(newStatus);
        Booking updatedBooking = bookingRepository.save(booking);

        // Audit log
        String tourName = booking.getSchedule().getTour().getNameTranslations().getOrDefault("es", "Tour");
        String description = tourName + " - " + booking.getUser().getFullName();
        Map<String, Object> oldValues = Map.of("status", oldStatus);
        Map<String, Object> newValues = Map.of("status", newStatus);
        auditLogService.logUpdate(currentUser, "BOOKING", booking.getId(), description, oldValues, newValues);

        return toBookingRes(updatedBooking);
    }

    @Transactional
    public void cancelBooking(UUID bookingId, User currentUser) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        // Check access: SUPER_ADMIN or tour owner (PARTNER_ADMIN)
        boolean isSuperAdmin = "ROLE_SUPER_ADMIN".equals(currentUser.getRole());
        boolean isTourOwner = booking.getSchedule().getTour().getOwner().getId().equals(currentUser.getId());

        if (!isSuperAdmin && !isTourOwner) {
            throw new AccessDeniedException("You do not have permission to cancel this booking.");
        }

        // Capture old status for audit
        String oldStatus = booking.getStatus();

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        // Audit log
        String tourName = booking.getSchedule().getTour().getNameTranslations().getOrDefault("es", "Tour");
        String description = tourName + " - " + booking.getUser().getFullName();
        Map<String, Object> oldValues = Map.of("status", oldStatus);
        Map<String, Object> newValues = Map.of("status", "CANCELLED");
        auditLogService.logUpdate(currentUser, "BOOKING", booking.getId(), description, oldValues, newValues);
    }

    public List<BookingRes> getBookingsByUser(UUID userId) {
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(b -> b.getUser().getId().equals(userId))
                .collect(Collectors.toList());
        return bookings.stream()
                .map(this::toBookingRes)
                .collect(Collectors.toList());
    }

    private BookingRes toBookingRes(Booking booking) {
        BookingRes res = new BookingRes();
        res.setId(booking.getId());
        res.setUserId(booking.getUser().getId());
        res.setUserFullName(booking.getUser().getFullName());
        res.setScheduleId(booking.getSchedule().getId());
        res.setTourName(booking.getSchedule().getTour().getNameTranslations().get(booking.getLanguageCode()));
        res.setTourDate(booking.getTourDate());
        res.setStatus(booking.getStatus());
        res.setSubtotal(booking.getSubtotal());
        res.setTaxAmount(booking.getTaxAmount());
        res.setTotalAmount(booking.getTotalAmount());
        res.setLanguageCode(booking.getLanguageCode());
        res.setSpecialRequests(booking.getSpecialRequests());
        res.setCreatedAt(booking.getCreatedAt());

        res.setParticipants(booking.getParticipants().stream().map(p -> {
            ParticipantRes pRes = new ParticipantRes();
            pRes.setId(p.getId());
            pRes.setFullName(p.getFullName());
            pRes.setDocumentId(p.getDocumentId());
            pRes.setNationality(p.getNationality());
            pRes.setAge(p.getAge());
            pRes.setPickupAddress(p.getPickupAddress());
            pRes.setSpecialRequirements(p.getSpecialRequirements());
            return pRes;
        }).collect(Collectors.toList()));

        return res;
    }
}
