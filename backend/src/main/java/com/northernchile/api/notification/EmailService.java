package com.northernchile.api.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class EmailService {

    @Autowired
    private MessageSource messageSource;

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

        System.out.println("---- Sending Email ----");
        System.out.println("To: " + customerName);
        System.out.println("Subject: " + subject);
        System.out.println("Body: " + greeting + " " + body);
        System.out.println("-----------------------");
    }

    public void sendNewBookingNotificationToAdmin(String bookingId) {
        System.out.println("---- Admin Notification ----");
        System.out.println("New booking received: " + bookingId);
        System.out.println("--------------------------");
    }

     public void sendNewPrivateRequestNotificationToAdmin(String requestId) {
        System.out.println("---- Admin Notification ----");
        System.out.println("New private tour request received: " + requestId);
        System.out.println("--------------------------");
    }
}
