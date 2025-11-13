package com.northernchile.api.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final MessageSource messageSource;

    public EmailService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    // In a real application, you would use JavaMailSender to send emails.
    // This is a placeholder implementation.
    public void sendBookingConfirmationEmail(String languageCode, String bookingId, String customerName, String tourName) {
        Locale userLocale = Locale.forLanguageTag(languageCode);

        String subject = messageSource.getMessage(
                "email.confirmation.subject",
                new Object[]{bookingId},
                userLocale
        );

        String greeting = messageSource.getMessage(
                "email.confirmation.greeting",
                new Object[]{customerName},
                userLocale
        );

        String body = messageSource.getMessage(
                "email.confirmation.body",
                new Object[]{tourName},
                userLocale
        );

        log.info("Sending booking confirmation email - To: {}, Subject: {}, Language: {}",
                customerName, subject, languageCode);
        log.debug("Email body: {} {}", greeting, body);
    }

    public void sendNewBookingNotificationToAdmin(String bookingId) {
        log.info("Sending admin notification - New booking received: {}", bookingId);
    }

     public void sendNewPrivateRequestNotificationToAdmin(String requestId) {
        log.info("Sending admin notification - New private tour request received: {}", requestId);
    }
}
