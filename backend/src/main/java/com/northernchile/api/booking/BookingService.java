package com.northernchile.api.booking;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
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
        long adultCount = req.getParticipants().stream().filter(p -> "ADULT".equalsIgnoreCase(p.getType())).count();
        long childCount = req.getParticipants().stream().filter(p -> "CHILD".equalsIgnoreCase(p.getType())).count();

        BigDecimal priceAdult = schedule.getTour().getPriceAdult();
        BigDecimal priceChild = schedule.getTour().getPriceChild() != null ? schedule.getTour().getPriceChild() : BigDecimal.ZERO;

        BigDecimal totalAmount = (priceAdult.multiply(BigDecimal.valueOf(adultCount)))
                .add(priceChild.multiply(BigDecimal.valueOf(childCount)));

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
            participant.setType(participantReq.getType());
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

    public List<BookingRes> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::toBookingRes)
                .collect(Collectors.toList());
    }

    public Optional<BookingRes> getBookingById(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .map(this::toBookingRes);
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
            pRes.setType(p.getType());
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
