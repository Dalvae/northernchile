package com.northernchile.api.notification;

import com.northernchile.api.util.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final SesEmailSender sesEmailSender;
    private final TemplateEngine templateEngine;
    private final MessageSource messageSource;

    @Value("${mail.enabled:false}")
    private boolean mailEnabled;

    public EmailService(SesEmailSender sesEmailSender, TemplateEngine templateEngine, MessageSource messageSource) {
        this.sesEmailSender = sesEmailSender;
        this.templateEngine = templateEngine;
        this.messageSource = messageSource;
    }

    /**
     * Send email verification link to new users
     */
    @Async
    public CompletableFuture<Void> sendVerificationEmail(String toEmail, String userName, String verificationUrl, String languageCode) {
        Locale locale = Locale.forLanguageTag(languageCode);

        Context context = new Context(locale);
        context.setVariable("userName", userName);
        context.setVariable("verificationUrl", verificationUrl);

        String subject = messageSource.getMessage("email.verification.title", null, locale);
        sendHtmlEmail(toEmail, subject, "email/verification", context, locale);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send password reset link to users
     */
    @Async
    public CompletableFuture<Void> sendPasswordResetEmail(String toEmail, String userName, String resetUrl, String languageCode) {
        Locale locale = Locale.forLanguageTag(languageCode);

        Context context = new Context(locale);
        context.setVariable("userName", userName);
        context.setVariable("resetUrl", resetUrl);

        String subject = messageSource.getMessage("email.password.reset.title", null, locale);
        sendHtmlEmail(toEmail, subject, "email/password-reset", context, locale);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send booking confirmation email after checkout
     */
    @Async
    public CompletableFuture<Void> sendBookingConfirmationEmail(String toEmail, String customerName, String bookingId,
                                            String tourName, String tourDate, String tourTime,
                                            int participantCount, String totalAmount, String languageCode) {
        Locale locale = Locale.forLanguageTag(languageCode);

        Context context = new Context(locale);
        context.setVariable("customerName", customerName);
        context.setVariable("bookingId", bookingId);
        context.setVariable("tourName", tourName);
        context.setVariable("tourDate", tourDate);
        context.setVariable("tourTime", tourTime);
        context.setVariable("participantCount", participantCount);
        context.setVariable("totalAmount", totalAmount);

        String subject = messageSource.getMessage("email.booking.confirmation.title", null, locale);
        sendHtmlEmail(toEmail, subject, "email/booking-confirmation", context, locale);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send tour reminder email 24 hours before the tour
     */
    @Async
    public CompletableFuture<Void> sendTourReminderEmail(String toEmail, String customerName, String bookingId,
                                     String tourName, String tourDateTime, String languageCode) {
        Locale locale = Locale.forLanguageTag(languageCode);

        Context context = new Context(locale);
        context.setVariable("customerName", customerName);
        context.setVariable("bookingId", bookingId);
        context.setVariable("tourName", tourName);
        context.setVariable("tourDateTime", tourDateTime);

        String subject = messageSource.getMessage("email.reminder.title", null, locale);
        sendHtmlEmail(toEmail, subject, "email/tour-reminder", context, locale);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send admin notification for new booking
     */
    @Async
    public CompletableFuture<Void> sendNewBookingNotificationToAdmin(com.northernchile.api.model.Booking booking, String adminEmail) {
        log.info("Sending booking notification to admin: {} - Booking ID: {}", adminEmail, booking.getId());

        if (!mailEnabled) {
            log.warn("Email sending is disabled. Would have sent booking notification to: {}", adminEmail);
            return CompletableFuture.completedFuture(null);
        }

        try {
            String tourName = (booking.getSchedule() != null && booking.getSchedule().getTour() != null)
                ? (String) booking.getSchedule().getTour().getNameTranslations().getOrDefault("es", "Tour desconocido")
                : "Tour desconocido";

            String customerName = (booking.getUser() != null) ? booking.getUser().getFullName() : "Cliente desconocido";
            String customerEmail = (booking.getUser() != null) ? booking.getUser().getEmail() : "";

            Context context = new Context(Locale.forLanguageTag("es"));
            context.setVariable("bookingId", booking.getId().toString());
            context.setVariable("status", booking.getStatus().toString());
            context.setVariable("tourName", tourName);
            context.setVariable("tourDate", DateTimeUtils.formatForDisplay(
                    DateTimeUtils.toInstantStartOfDay(booking.getTourDate()), "dd/MM/yyyy"));
            context.setVariable("customerName", customerName);
            context.setVariable("customerEmail", customerEmail);
            context.setVariable("participantCount", (booking.getParticipants() != null) ? booking.getParticipants().size() : 0);
            context.setVariable("subtotal", String.format("$%,.0f", booking.getSubtotal()));
            context.setVariable("taxAmount", String.format("$%,.0f", booking.getTaxAmount()));
            context.setVariable("totalAmount", String.format("$%,.0f", booking.getTotalAmount()));
            context.setVariable("specialRequests", booking.getSpecialRequests());
            context.setVariable("createdAt", DateTimeUtils.formatForDisplay(booking.getCreatedAt(), "dd/MM/yyyy HH:mm"));

            sendHtmlEmail(adminEmail, "Nueva Reserva - Northern Chile", "email/admin-new-booking", context, Locale.forLanguageTag("es"));
        } catch (Exception e) {
            log.error("Failed to send booking notification email", e);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send admin notification for new private tour request
     */
    @Async
    public CompletableFuture<Void> sendNewPrivateRequestNotificationToAdmin(com.northernchile.api.model.PrivateTourRequest request, String adminEmail) {
        log.info("Sending private tour request notification to admin: {} - Request ID: {}", adminEmail, request.getId());

        if (!mailEnabled) {
            log.warn("Email sending is disabled. Would have sent private tour request notification to: {}", adminEmail);
            return CompletableFuture.completedFuture(null);
        }

        try {
            Context context = new Context(Locale.forLanguageTag("es"));
            context.setVariable("requestId", request.getId().toString());
            context.setVariable("status", request.getStatus());
            context.setVariable("tourType", request.getRequestedTourType());
            context.setVariable("requestedDate", (request.getRequestedDatetime() != null)
                ? DateTimeUtils.formatForDisplay(request.getRequestedDatetime(), "dd/MM/yyyy HH:mm")
                : "No especificada");
            context.setVariable("participantCount", request.getNumParticipants());
            context.setVariable("customerName", request.getCustomerName());
            context.setVariable("customerEmail", request.getCustomerEmail());
            context.setVariable("customerPhone", request.getCustomerPhone());
            context.setVariable("specialRequests", request.getSpecialRequests());
            context.setVariable("createdAt", DateTimeUtils.formatForDisplay(request.getCreatedAt(), "dd/MM/yyyy HH:mm"));

            sendHtmlEmail(adminEmail, "Nueva Solicitud de Tour Privado - Northern Chile", "email/admin-private-request", context, Locale.forLanguageTag("es"));
        } catch (Exception e) {
            log.error("Failed to send private tour request notification email", e);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send admin notification for new contact form message
     */
    @Async
    public CompletableFuture<Void> sendContactNotificationToAdmin(com.northernchile.api.model.ContactMessage contactMessage, String adminEmail) {
        log.info("Sending contact notification to admin: {} from: {}", adminEmail, contactMessage.getEmail());

        if (!mailEnabled) {
            log.warn("Email sending is disabled. Would have sent contact notification to: {}", adminEmail);
            return CompletableFuture.completedFuture(null);
        }

        try {
            Context context = new Context(Locale.forLanguageTag("es"));
            context.setVariable("name", contactMessage.getName());
            context.setVariable("email", contactMessage.getEmail());
            context.setVariable("phone", contactMessage.getPhone());
            context.setVariable("message", contactMessage.getMessage());
            context.setVariable("createdAt", DateTimeUtils.formatForDisplay(contactMessage.getCreatedAt(), "dd/MM/yyyy HH:mm"));
            context.setVariable("messageId", contactMessage.getId().toString());

            sendHtmlEmail(adminEmail, "Nuevo Mensaje de Contacto - Northern Chile", "email/admin-contact", context, Locale.forLanguageTag("es"));
        } catch (Exception e) {
            log.error("Failed to send contact notification email to: {}", adminEmail, e);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send manifest email to operator when schedule is closed
     */
    @Async
    public CompletableFuture<Void> sendManifestEmail(String operatorEmail, String tourName, String tourDate,
                                                      String tourTime, String guideName, int totalParticipants,
                                                      java.util.List<com.northernchile.api.tour.ManifestService.ManifestParticipant> participants) {
        log.info("Sending manifest email to: {} for tour: {}", operatorEmail, tourName);

        if (!mailEnabled) {
            log.warn("Email sending is disabled. Would have sent manifest to: {}", operatorEmail);
            return CompletableFuture.completedFuture(null);
        }

        try {
            Locale locale = Locale.forLanguageTag("es");
            Context context = new Context(locale);
            context.setVariable("tourName", tourName);
            context.setVariable("tourDate", tourDate);
            context.setVariable("tourTime", tourTime);
            context.setVariable("guideName", guideName);
            context.setVariable("totalParticipants", totalParticipants);
            context.setVariable("participants", participants);

            String subject = "Manifiesto: " + tourName + " - " + tourDate + " " + tourTime;
            sendHtmlEmail(operatorEmail, subject, "email/manifest", context, locale);
        } catch (Exception e) {
            log.error("Failed to send manifest email to: {}", operatorEmail, e);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send pickup reminder to participant 2 hours before tour
     */
    @Async
    public CompletableFuture<Void> sendPickupReminderToParticipant(String toEmail, String participantName,
                                                                    String tourName, String tourDate, String tourTime,
                                                                    String pickupAddress, String equipment,
                                                                    String languageCode) {
        log.info("Sending pickup reminder to: {} for tour: {}", toEmail, tourName);

        if (!mailEnabled) {
            log.warn("Email sending is disabled. Would have sent pickup reminder to: {}", toEmail);
            return CompletableFuture.completedFuture(null);
        }

        try {
            // Normalize language code (es-CL -> es)
            String lang = languageCode != null && languageCode.length() >= 2 
                    ? languageCode.substring(0, 2) 
                    : "es";
            Locale locale = Locale.forLanguageTag(lang);

            Context context = new Context(locale);
            context.setVariable("participantName", participantName);
            context.setVariable("tourName", tourName);
            context.setVariable("tourDate", tourDate);
            context.setVariable("tourTime", tourTime);
            context.setVariable("pickupAddress", pickupAddress);
            context.setVariable("equipment", equipment);

            String subject = messageSource.getMessage("pickup.subject", null, locale);
            sendHtmlEmail(toEmail, subject, "email/pickup-reminder", context, locale);
        } catch (Exception e) {
            log.error("Failed to send pickup reminder to: {}", toEmail, e);
        }
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send booking cancellation email to customer
     */
    @Async
    public CompletableFuture<Void> sendBookingCancelledEmail(String toEmail, String customerName, String bookingId,
                                                              String tourName, String tourDate, int participantCount,
                                                              String reason, String refundAmount, String languageCode) {
        log.info("Sending booking cancellation email to: {} for booking: {}", toEmail, bookingId);

        Locale locale = Locale.forLanguageTag(languageCode != null ? languageCode : "es");

        Context context = new Context(locale);
        context.setVariable("customerName", customerName);
        context.setVariable("bookingId", bookingId);
        context.setVariable("tourName", tourName);
        context.setVariable("tourDate", tourDate);
        context.setVariable("participantCount", participantCount);
        context.setVariable("reason", reason);
        context.setVariable("refundAmount", refundAmount);

        String subject = messageSource.getMessage("email.cancelled.title", null, locale);
        sendHtmlEmail(toEmail, subject, "email/booking-cancelled", context, locale);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send refund confirmation email to customer
     */
    @Async
    public CompletableFuture<Void> sendRefundConfirmationEmail(String toEmail, String customerName, String bookingId,
                                                                String tourName, String refundAmount,
                                                                String paymentMethod, String languageCode) {
        log.info("Sending refund confirmation email to: {} for booking: {}", toEmail, bookingId);

        Locale locale = Locale.forLanguageTag(languageCode != null ? languageCode : "es");

        Context context = new Context(locale);
        context.setVariable("customerName", customerName);
        context.setVariable("bookingId", bookingId);
        context.setVariable("tourName", tourName);
        context.setVariable("refundAmount", refundAmount);
        context.setVariable("paymentMethod", paymentMethod);

        String subject = messageSource.getMessage("email.refund.title", null, locale);
        sendHtmlEmail(toEmail, subject, "email/refund-confirmation", context, locale);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send private tour quote email to customer
     */
    @Async
    public CompletableFuture<Void> sendPrivateTourQuoteEmail(String toEmail, String customerName, String tourType,
                                                              String requestedDate, int participantCount,
                                                              String quotedPrice, String specialRequests,
                                                              String adminNotes, String languageCode) {
        log.info("Sending private tour quote email to: {}", toEmail);

        Locale locale = Locale.forLanguageTag(languageCode != null ? languageCode : "es");

        Context context = new Context(locale);
        context.setVariable("customerName", customerName);
        context.setVariable("tourType", tourType);
        context.setVariable("requestedDate", requestedDate);
        context.setVariable("participantCount", participantCount);
        context.setVariable("quotedPrice", quotedPrice);
        context.setVariable("specialRequests", specialRequests);
        context.setVariable("adminNotes", adminNotes);

        String subject = messageSource.getMessage("email.private.quote.title", null, locale);
        sendHtmlEmail(toEmail, subject, "email/private-tour-quote", context, locale);
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Generic method to send HTML emails using Thymeleaf templates via Amazon SES
     */
    private void sendHtmlEmail(String toEmail, String subject, String templateName, Context context, Locale locale) {
        if (!mailEnabled) {
            log.warn("Email sending is disabled. Would have sent email to: {} with subject: {}", toEmail, subject);
            return;
        }

        try {
            String htmlContent = templateEngine.process(templateName, context);
            sesEmailSender.sendHtmlEmail(toEmail, subject, htmlContent);
        } catch (Exception e) {
            log.error("Unexpected error while sending email to: {}", toEmail, e);
        }
    }
}
