package com.northernchile.api.notification;

import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.model.Booking;
import com.northernchile.api.model.TourSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TourReminderService {

    private static final Logger log = LoggerFactory.getLogger(TourReminderService.class);

    private final BookingRepository bookingRepository;
    private final EmailService emailService;

    @Value("${mail.reminder.hours-before-tour:24}")
    private int hoursBeforeTour;

    @Value("${app.timezone:America/Santiago}")
    private String appTimezone;

    public TourReminderService(BookingRepository bookingRepository, EmailService emailService) {
        this.bookingRepository = bookingRepository;
        this.emailService = emailService;
    }

    /**
     * Send reminder emails for tours happening in X hours
     * Runs every hour at :00 minutes
     */
    @Scheduled(cron = "0 0 * * * *")
    public void sendTourReminders() {
        log.info("Starting tour reminder check...");

        Instant now = Instant.now();
        Instant reminderWindow = now.plus(hoursBeforeTour, ChronoUnit.HOURS);

        // Find confirmed bookings for tours happening in the reminder window
        List<Booking> upcomingBookings = bookingRepository.findByStatusAndTourSchedule_StartDateTimeBetween(
                "CONFIRMED",
                now,
                reminderWindow.plus(1, ChronoUnit.HOURS) // +1 hour buffer
        );

        log.info("Found {} bookings for tour reminders", upcomingBookings.size());

        for (Booking booking : upcomingBookings) {
            try {
                sendReminderEmail(booking);
            } catch (Exception e) {
                log.error("Failed to send reminder email for booking: {}", booking.getId(), e);
            }
        }

        log.info("Tour reminder check completed");
    }

    private void sendReminderEmail(Booking booking) {
        TourSchedule schedule = booking.getTourSchedule();

        // Format date and time
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm")
                .withZone(ZoneId.of(appTimezone));
        String tourDateTime = dateFormatter.format(schedule.getStartDateTime());

        emailService.sendTourReminderEmail(
                booking.getUser().getEmail(),
                booking.getUser().getFullName(),
                booking.getId().toString(),
                schedule.getTour().getName(),
                tourDateTime,
                booking.getLanguageCode() != null ? booking.getLanguageCode() : "es-CL"
        );

        log.info("Reminder email sent for booking: {}", booking.getId());
    }
}
