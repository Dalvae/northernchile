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
import org.springframework.security.access.prepost.PreAuthorize;
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

    @Autowired
    private BookingMapper bookingMapper;

    @Value("${tax.rate}")
    private BigDecimal taxRate;

    @Transactional
    public BookingRes createBooking(BookingCreateReq req, User currentUser) {
        var schedule = tourScheduleRepository.findById(req.getScheduleId())
                .orElseThrow(() -> new EntityNotFoundException("TourSchedule not found with id: " + req.getScheduleId()));

        Integer bookedParticipants = bookingRepository.countParticipantsByScheduleId(req.getScheduleId());
        if (bookedParticipants == null) bookedParticipants = 0;
        int requestedSlots = req.getParticipants().size();
        if (schedule.getMaxParticipants() - bookedParticipants < requestedSlots) {
            throw new IllegalStateException("Not enough available slots for this tour schedule.");
        }

        int participantCount = req.getParticipants().size();
        BigDecimal pricePerParticipant = schedule.getTour().getPrice();

        BigDecimal totalAmount = pricePerParticipant.multiply(BigDecimal.valueOf(participantCount));

        BigDecimal subtotal = totalAmount.divide(BigDecimal.ONE.add(taxRate), 2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = totalAmount.subtract(subtotal);

        Booking booking = new Booking();
        booking.setUser(currentUser);
        booking.setSchedule(schedule);
        booking.setTourDate(LocalDate.ofInstant(schedule.getStartDatetime(), ZoneOffset.UTC));
        booking.setStatus("PENDING");
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

        emailService.sendBookingConfirmationEmail(
                savedBooking.getLanguageCode(),
                savedBooking.getId().toString(),
                savedBooking.getUser().getFullName(),
                savedBooking.getSchedule().getTour().getNameTranslations().get(savedBooking.getLanguageCode())
        );
        emailService.sendNewBookingNotificationToAdmin(savedBooking.getId().toString());

        return bookingMapper.toBookingRes(savedBooking);
    }

    public List<BookingRes> getAllBookings(User currentUser) {
        if ("ROLE_SUPER_ADMIN".equals(currentUser.getRole())) {
            return bookingRepository.findAll().stream()
                    .map(bookingMapper::toBookingRes)
                    .collect(Collectors.toList());
        } else if ("ROLE_PARTNER_ADMIN".equals(currentUser.getRole())) {
            return bookingRepository.findAll().stream()
                    .filter(booking -> booking.getSchedule().getTour().getOwner().getId().equals(currentUser.getId()))
                    .map(bookingMapper::toBookingRes)
                    .collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or @bookingSecurityService.isOwner(authentication, #bookingId) or @bookingSecurityService.isBookingUser(authentication, #bookingId)")
    public Optional<BookingRes> getBookingById(UUID bookingId, User currentUser) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        return Optional.of(bookingMapper.toBookingRes(booking));
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or @bookingSecurityService.isOwner(authentication, #bookingId)")
    public BookingRes updateBookingStatus(UUID bookingId, String newStatus, User currentUser) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        String oldStatus = booking.getStatus();

        booking.setStatus(newStatus);
        Booking updatedBooking = bookingRepository.save(booking);

        String tourName = booking.getSchedule().getTour().getNameTranslations().getOrDefault("es", "Tour");
        String description = tourName + " - " + booking.getUser().getFullName();
        Map<String, Object> oldValues = Map.of("status", oldStatus);
        Map<String, Object> newValues = Map.of("status", newStatus);
        auditLogService.logUpdate(currentUser, "BOOKING", booking.getId(), description, oldValues, newValues);

        return bookingMapper.toBookingRes(updatedBooking);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or @bookingSecurityService.isOwner(authentication, #bookingId)")
    public void cancelBooking(UUID bookingId, User currentUser) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        String oldStatus = booking.getStatus();

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

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
                .map(bookingMapper::toBookingRes)
                .collect(Collectors.toList());
    }

    @Transactional
    @PreAuthorize("@bookingSecurityService.isBookingUser(authentication, #bookingId)")
    public BookingRes confirmBookingAfterMockPayment(UUID bookingId, User currentUser) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        if (!"PENDING".equals(booking.getStatus())) {
            throw new IllegalStateException("Only PENDING bookings can be confirmed. Current status: " + booking.getStatus());
        }

        String oldStatus = booking.getStatus();
        booking.setStatus("CONFIRMED");
        Booking confirmedBooking = bookingRepository.save(booking);

        String tourName = booking.getSchedule().getTour().getNameTranslations().getOrDefault("es", "Tour");
        String description = "Mock payment confirmation - " + tourName + " - " + booking.getUser().getFullName();
        Map<String, Object> oldValues = Map.of("status", oldStatus);
        Map<String, Object> newValues = Map.of("status", "CONFIRMED");
        auditLogService.logUpdate(currentUser, "BOOKING", booking.getId(), description, oldValues, newValues);

        return bookingMapper.toBookingRes(confirmedBooking);
    }
}
