package com.northernchile.api.notification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

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
    public void sendVerificationEmail(String toEmail, String userName, String verificationUrl, String languageCode) {
        Locale locale = Locale.forLanguageTag(languageCode);

        Context context = new Context(locale);
        context.setVariable("userName", userName);
        context.setVariable("verificationUrl", verificationUrl);

        String subject = messageSource.getMessage("email.verification.title", null, locale);
        sendHtmlEmail(toEmail, subject, "email/verification", context, locale);
    }

    /**
     * Send password reset link to users
     */
    public void sendPasswordResetEmail(String toEmail, String userName, String resetUrl, String languageCode) {
        Locale locale = Locale.forLanguageTag(languageCode);

        Context context = new Context(locale);
        context.setVariable("userName", userName);
        context.setVariable("resetUrl", resetUrl);

        String subject = messageSource.getMessage("email.password.reset.title", null, locale);
        sendHtmlEmail(toEmail, subject, "email/password-reset", context, locale);
    }

    /**
     * Send booking confirmation email after checkout
     */
    public void sendBookingConfirmationEmail(String toEmail, String customerName, String bookingId,
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
    }

    /**
     * Send tour reminder email 24 hours before the tour
     */
    public void sendTourReminderEmail(String toEmail, String customerName, String bookingId,
                                     String tourName, String tourDateTime, String languageCode) {
        Locale locale = Locale.forLanguageTag(languageCode);

        Context context = new Context(locale);
        context.setVariable("customerName", customerName);
        context.setVariable("bookingId", bookingId);
        context.setVariable("tourName", tourName);
        context.setVariable("tourDateTime", tourDateTime);

        String subject = messageSource.getMessage("email.reminder.title", null, locale);
        sendHtmlEmail(toEmail, subject, "email/tour-reminder", context, locale);
    }

    /**
     * Send admin notification for new booking
     */
    public void sendNewBookingNotificationToAdmin(String bookingId) {
        log.info("Sending admin notification - New booking received: {}", bookingId);
        // TODO: Implement admin notification email
    }

    /**
     * Send admin notification for new private tour request
     */
    public void sendNewPrivateRequestNotificationToAdmin(String requestId) {
        log.info("Sending admin notification - New private tour request received: {}", requestId);
        // TODO: Implement admin notification email
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
