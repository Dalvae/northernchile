package com.northernchile.api.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final MessageSource messageSource;

    @Value("${mail.from.email}")
    private String fromEmail;

    @Value("${mail.from.name}")
    private String fromName;

    @Value("${mail.enabled:false}")
    private boolean mailEnabled;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine, MessageSource messageSource) {
        this.mailSender = mailSender;
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
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(adminEmail);
            helper.setSubject("New Booking Received - Northern Chile");

            // Format dates for display
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            String tourName = (booking.getSchedule() != null && booking.getSchedule().getTour() != null)
                ? booking.getSchedule().getTour().getNameTranslations().getOrDefault("es", "Unknown Tour")
                : "Unknown Tour";

            String userName = (booking.getUser() != null)
                ? booking.getUser().getFullName() + " (" + booking.getUser().getEmail() + ")"
                : "Unknown User";

            String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <h2 style="color: #d97706;">New Booking Received</h2>

                    <h3>Booking Details</h3>
                    <p><strong>Booking ID:</strong> %s</p>
                    <p><strong>Status:</strong> <span style="color: #059669; font-weight: bold;">%s</span></p>
                    <p><strong>Tour:</strong> %s</p>
                    <p><strong>Tour Date:</strong> %s</p>

                    <h3>Customer Information</h3>
                    <p><strong>Customer:</strong> %s</p>
                    <p><strong>Number of Participants:</strong> %d</p>

                    <h3>Payment Information</h3>
                    <p><strong>Subtotal:</strong> $%,.2f</p>
                    <p><strong>Tax (IVA):</strong> $%,.2f</p>
                    <p><strong>Total Amount:</strong> <strong style="color: #059669;">$%,.2f</strong></p>

                    %s

                    <p><strong>Booking Created:</strong> %s</p>

                    <hr style="margin: 20px 0; border: none; border-top: 1px solid #e5e7eb;">
                    <p style="font-size: 12px; color: #6b7280;">
                        This is an automated notification from Northern Chile Tours.<br>
                        Please review and process this booking in your admin dashboard.
                    </p>
                </body>
                </html>
                """,
                booking.getId(),
                booking.getStatus(),
                tourName,
                booking.getTourDate().format(dateFormatter),
                userName,
                (booking.getParticipants() != null) ? booking.getParticipants().size() : 0,
                booking.getSubtotal(),
                booking.getTaxAmount(),
                booking.getTotalAmount(),
                (booking.getSpecialRequests() != null && !booking.getSpecialRequests().isBlank())
                    ? "<h3>Special Requests</h3><p>" + booking.getSpecialRequests() + "</p>"
                    : "",
                booking.getCreatedAt().toString()
            );

            helper.setText(htmlContent, true);
            mailSender.send(message);

            log.info("Booking notification email sent successfully to: {}", adminEmail);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
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
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(adminEmail);
            helper.setSubject("New Private Tour Request - Northern Chile");

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <h2 style="color: #d97706;">New Private Tour Request</h2>

                    <h3>Request Details</h3>
                    <p><strong>Request ID:</strong> %s</p>
                    <p><strong>Status:</strong> <span style="color: #3b82f6; font-weight: bold;">%s</span></p>
                    <p><strong>Tour Type:</strong> %s</p>
                    <p><strong>Requested Date:</strong> %s</p>
                    <p><strong>Number of Participants:</strong> %d</p>

                    <h3>Customer Information</h3>
                    <p><strong>Name:</strong> %s</p>
                    <p><strong>Email:</strong> <a href="mailto:%s">%s</a></p>
                    <p><strong>Phone:</strong> %s</p>

                    %s

                    %s

                    <p><strong>Request Created:</strong> %s</p>

                    <hr style="margin: 20px 0; border: none; border-top: 1px solid #e5e7eb;">
                    <p style="font-size: 12px; color: #6b7280;">
                        This is an automated notification from Northern Chile Tours.<br>
                        Please review this private tour request and contact the customer to provide a quote.
                    </p>
                </body>
                </html>
                """,
                request.getId(),
                request.getStatus(),
                request.getRequestedTourType(),
                (request.getRequestedDatetime() != null)
                    ? java.time.LocalDateTime.ofInstant(request.getRequestedDatetime(), java.time.ZoneId.of("America/Santiago")).format(dateTimeFormatter)
                    : "Not specified",
                request.getNumParticipants(),
                request.getCustomerName(),
                request.getCustomerEmail(),
                request.getCustomerEmail(),
                (request.getCustomerPhone() != null && !request.getCustomerPhone().isBlank())
                    ? request.getCustomerPhone()
                    : "Not provided",
                (request.getSpecialRequests() != null && !request.getSpecialRequests().isBlank())
                    ? "<h3>Special Requests</h3><p>" + request.getSpecialRequests() + "</p>"
                    : "",
                (request.getQuotedPrice() != null)
                    ? "<p><strong>Quoted Price:</strong> $" + String.format("%,.2f", request.getQuotedPrice()) + "</p>"
                    : "",
                request.getCreatedAt().toString()
            );

            helper.setText(htmlContent, true);
            mailSender.send(message);

            log.info("Private tour request notification email sent successfully to: {}", adminEmail);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
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
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(adminEmail);
            helper.setSubject("New Contact Form Message - Northern Chile");

            String htmlContent = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <h2 style="color: #d97706;">New Contact Form Message</h2>
                    <p><strong>Name:</strong> %s</p>
                    <p><strong>Email:</strong> %s</p>
                    <p><strong>Phone:</strong> %s</p>
                    <p><strong>Message:</strong></p>
                    <div style="background: #f3f4f6; padding: 15px; border-left: 4px solid #d97706; margin: 10px 0;">
                        %s
                    </div>
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 20px 0;">
                    <p style="color: #6b7280; font-size: 12px;">
                        Received: %s<br>
                        ID: %s
                    </p>
                </body>
                </html>
                """,
                contactMessage.getName(),
                contactMessage.getEmail(),
                contactMessage.getPhone() != null ? contactMessage.getPhone() : "Not provided",
                contactMessage.getMessage().replace("\n", "<br>"),
                contactMessage.getCreatedAt().toString(),
                contactMessage.getId().toString()
            );

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Contact notification email sent successfully to: {}", adminEmail);
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
     * Generic method to send HTML emails using Thymeleaf templates
     */
    private void sendHtmlEmail(String toEmail, String subject, String templateName, Context context, Locale locale) {
        if (!mailEnabled) {
            log.warn("Email sending is disabled. Would have sent email to: {} with subject: {}", toEmail, subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(toEmail);
            helper.setSubject(subject);

            String htmlContent = templateEngine.process(templateName, context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent successfully to: {} with subject: {}", toEmail, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {} with subject: {}", toEmail, subject, e);
            // Don't throw exception - graceful degradation
        } catch (Exception e) {
            log.error("Unexpected error while sending email to: {}", toEmail, e);
        }
    }
}
