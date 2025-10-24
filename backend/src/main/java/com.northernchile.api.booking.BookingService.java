package com.northernchile.api.booking;

import com.northernchile.api.model.Booking;
import com.northernchile.api.notification.EmailService;
import com.northernchile.api.tour.TourScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TourScheduleRepository tourScheduleRepository;

    @Autowired
    private EmailService emailService;

    public Booking createBooking(Booking booking) {
        // In a real application, this method would be much more complex.
        // It would need to:
        // 1. Verify the availability of the TourSchedule.
        // 2. Calculate the subtotal and taxAmount from the totalAmount.
        // 3. Create and save the associated Participant entities.
        // 4. Handle payment processing.

        Booking savedBooking = bookingRepository.save(booking);

        // Send confirmation emails
        emailService.sendBookingConfirmationEmail(
                savedBooking.getLanguageCode(),
                savedBooking.getId().toString(),
                savedBooking.getUser().getFullName(),
                savedBooking.getSchedule().getTour().getName()
        );
        emailService.sendNewBookingNotificationToAdmin(savedBooking.getId().toString());

        return savedBooking;
    }
}
