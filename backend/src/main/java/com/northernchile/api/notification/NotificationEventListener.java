package com.northernchile.api.notification;

import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.config.NotificationConfig;
import com.northernchile.api.contact.ContactMessageRepository;
import com.northernchile.api.model.Booking;
import com.northernchile.api.model.ContactMessage;
import com.northernchile.api.notification.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Central event listener for notification-related events.
 * Decouples business logic from email sending.
 *
 * Benefits:
 * - Services don't need direct dependency on EmailService
 * - Multiple listeners can react to same event
 * - Easy to add other notification channels (SMS, push)
 * - Simpler testing of business logic
 */
@Component
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final EmailService emailService;
    private final NotificationConfig notificationConfig;
    private final BookingRepository bookingRepository;
    private final ContactMessageRepository contactMessageRepository;

    public NotificationEventListener(
            EmailService emailService,
            NotificationConfig notificationConfig,
            BookingRepository bookingRepository,
            ContactMessageRepository contactMessageRepository) {
        this.emailService = emailService;
        this.notificationConfig = notificationConfig;
        this.bookingRepository = bookingRepository;
        this.contactMessageRepository = contactMessageRepository;
    }

    @Async
    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Handling UserRegisteredEvent for: {}", event.email());
        emailService.sendVerificationEmail(
                event.email(),
                event.fullName(),
                event.verificationUrl(),
                event.languageCode()
        );
    }

    @Async
    @EventListener
    public void handlePasswordResetRequested(PasswordResetRequestedEvent event) {
        log.info("Handling PasswordResetRequestedEvent for: {}", event.email());
        emailService.sendPasswordResetEmail(
                event.email(),
                event.fullName(),
                event.resetUrl(),
                event.languageCode()
        );
    }

    @Async
    @EventListener
    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        log.info("Handling BookingConfirmedEvent for booking: {}", event.bookingId());

        // Send confirmation to customer
        emailService.sendBookingConfirmationEmail(
                event.customerEmail(),
                event.customerName(),
                event.bookingId().toString(),
                event.tourName(),
                event.tourDate(),
                event.tourTime(),
                event.participantCount(),
                event.totalAmount(),
                event.languageCode()
        );

        // Send notification to admin
        Booking booking = bookingRepository.findById(event.bookingId()).orElse(null);
        if (booking != null) {
            emailService.sendNewBookingNotificationToAdmin(booking, notificationConfig.getAdminEmail());
        }
    }

    @Async
    @EventListener
    public void handleContactMessageReceived(ContactMessageReceivedEvent event) {
        log.info("Handling ContactMessageReceivedEvent from: {}", event.email());

        ContactMessage message = contactMessageRepository.findById(event.messageId()).orElse(null);
        if (message != null) {
            emailService.sendContactNotificationToAdmin(message, notificationConfig.getAdminEmail());
        }
    }
}
