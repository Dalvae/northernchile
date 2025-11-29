package com.northernchile.api.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;

/**
 * Controller for testing email templates in production.
 * Only accessible by SUPER_ADMIN users.
 */
@RestController
@RequestMapping("/api/admin/email-test")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class EmailTestController {

    private static final Logger log = LoggerFactory.getLogger(EmailTestController.class);

    private final SesEmailSender sesEmailSender;
    private final TemplateEngine templateEngine;

    public EmailTestController(SesEmailSender sesEmailSender, TemplateEngine templateEngine) {
        this.sesEmailSender = sesEmailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * Send all email templates to a test email address
     */
    @PostMapping("/send-all")
    public ResponseEntity<Map<String, Object>> sendAllTestEmails(@RequestBody TestEmailRequest request) {
        String testEmail = request.email();
        log.info("Sending all test emails to: {}", testEmail);

        List<String> sent = new ArrayList<>();
        List<String> failed = new ArrayList<>();

        // 1. Verification Email
        try {
            sendTestEmail(testEmail, "Verificar Email - TEST", "email/verification", createVerificationContext());
            sent.add("verification");
        } catch (Exception e) {
            log.error("Failed to send verification email", e);
            failed.add("verification: " + e.getMessage());
        }

        // 2. Password Reset Email
        try {
            sendTestEmail(testEmail, "Restablecer Contraseña - TEST", "email/password-reset", createPasswordResetContext());
            sent.add("password-reset");
        } catch (Exception e) {
            log.error("Failed to send password-reset email", e);
            failed.add("password-reset: " + e.getMessage());
        }

        // 3. Booking Confirmation Email
        try {
            sendTestEmail(testEmail, "Reserva Confirmada - TEST", "email/booking-confirmation", createBookingConfirmationContext());
            sent.add("booking-confirmation");
        } catch (Exception e) {
            log.error("Failed to send booking-confirmation email", e);
            failed.add("booking-confirmation: " + e.getMessage());
        }

        // 4. Tour Reminder Email
        try {
            sendTestEmail(testEmail, "Recordatorio de Tour - TEST", "email/tour-reminder", createTourReminderContext());
            sent.add("tour-reminder");
        } catch (Exception e) {
            log.error("Failed to send tour-reminder email", e);
            failed.add("tour-reminder: " + e.getMessage());
        }

        // 5. Pickup Reminder Email
        try {
            sendTestEmail(testEmail, "Recordatorio de Pickup - TEST", "email/pickup-reminder", createPickupReminderContext());
            sent.add("pickup-reminder");
        } catch (Exception e) {
            log.error("Failed to send pickup-reminder email", e);
            failed.add("pickup-reminder: " + e.getMessage());
        }

        // 6. Booking Cancelled Email
        try {
            sendTestEmail(testEmail, "Reserva Cancelada - TEST", "email/booking-cancelled", createBookingCancelledContext());
            sent.add("booking-cancelled");
        } catch (Exception e) {
            log.error("Failed to send booking-cancelled email", e);
            failed.add("booking-cancelled: " + e.getMessage());
        }

        // 7. Refund Confirmation Email
        try {
            sendTestEmail(testEmail, "Reembolso Confirmado - TEST", "email/refund-confirmation", createRefundConfirmationContext());
            sent.add("refund-confirmation");
        } catch (Exception e) {
            log.error("Failed to send refund-confirmation email", e);
            failed.add("refund-confirmation: " + e.getMessage());
        }

        // 8. Private Tour Quote Email
        try {
            sendTestEmail(testEmail, "Cotización Tour Privado - TEST", "email/private-tour-quote", createPrivateTourQuoteContext());
            sent.add("private-tour-quote");
        } catch (Exception e) {
            log.error("Failed to send private-tour-quote email", e);
            failed.add("private-tour-quote: " + e.getMessage());
        }

        // 9. Admin New Booking Email
        try {
            sendTestEmail(testEmail, "Nueva Reserva (Admin) - TEST", "email/admin-new-booking", createAdminNewBookingContext());
            sent.add("admin-new-booking");
        } catch (Exception e) {
            log.error("Failed to send admin-new-booking email", e);
            failed.add("admin-new-booking: " + e.getMessage());
        }

        // 10. Admin Private Request Email
        try {
            sendTestEmail(testEmail, "Nueva Solicitud Tour Privado (Admin) - TEST", "email/admin-private-request", createAdminPrivateRequestContext());
            sent.add("admin-private-request");
        } catch (Exception e) {
            log.error("Failed to send admin-private-request email", e);
            failed.add("admin-private-request: " + e.getMessage());
        }

        // 11. Admin Contact Email
        try {
            sendTestEmail(testEmail, "Nuevo Mensaje de Contacto (Admin) - TEST", "email/admin-contact", createAdminContactContext());
            sent.add("admin-contact");
        } catch (Exception e) {
            log.error("Failed to send admin-contact email", e);
            failed.add("admin-contact: " + e.getMessage());
        }

        // 12. Tour Manifest Email
        try {
            sendTestEmail(testEmail, "Manifiesto de Tour - TEST", "email/manifest", createManifestContext());
            sent.add("manifest");
        } catch (Exception e) {
            log.error("Failed to send manifest email", e);
            failed.add("manifest: " + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("testEmail", testEmail);
        result.put("totalSent", sent.size());
        result.put("totalFailed", failed.size());
        result.put("sent", sent);
        result.put("failed", failed);

        log.info("Test emails completed: {} sent, {} failed", sent.size(), failed.size());

        return ResponseEntity.ok(result);
    }

    /**
     * Send a specific email template
     */
    @PostMapping("/send/{templateName}")
    public ResponseEntity<Map<String, String>> sendSpecificTestEmail(
            @PathVariable String templateName,
            @RequestBody TestEmailRequest request) {
        String testEmail = request.email();
        log.info("Sending test email '{}' to: {}", templateName, testEmail);

        try {
            Context context = switch (templateName) {
                case "verification" -> createVerificationContext();
                case "password-reset" -> createPasswordResetContext();
                case "booking-confirmation" -> createBookingConfirmationContext();
                case "tour-reminder" -> createTourReminderContext();
                case "pickup-reminder" -> createPickupReminderContext();
                case "booking-cancelled" -> createBookingCancelledContext();
                case "refund-confirmation" -> createRefundConfirmationContext();
                case "private-tour-quote" -> createPrivateTourQuoteContext();
                case "admin-new-booking" -> createAdminNewBookingContext();
                case "admin-private-request" -> createAdminPrivateRequestContext();
                case "admin-contact" -> createAdminContactContext();
                case "manifest" -> createManifestContext();
                default -> throw new IllegalArgumentException("Unknown template: " + templateName);
            };

            String subject = templateName.toUpperCase().replace("-", " ") + " - TEST";
            sendTestEmail(testEmail, subject, "email/" + templateName, context);

            return ResponseEntity.ok(Map.of(
                    "status", "sent",
                    "template", templateName,
                    "email", testEmail
            ));
        } catch (Exception e) {
            log.error("Failed to send test email: {}", templateName, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "failed",
                    "template", templateName,
                    "error", e.getMessage()
            ));
        }
    }

    private void sendTestEmail(String toEmail, String subject, String templateName, Context context) {
        String htmlContent = templateEngine.process(templateName, context);
        sesEmailSender.sendHtmlEmail(toEmail, subject, htmlContent);
    }

    // ============ Context Builders ============

    private Context createVerificationContext() {
        Context ctx = new Context(Locale.forLanguageTag("es"));
        ctx.setVariable("userName", "Diego Alvarez");
        ctx.setVariable("verificationUrl", "https://www.northernchile.com/auth/verify?token=test-token-123");
        return ctx;
    }

    private Context createPasswordResetContext() {
        Context ctx = new Context(Locale.forLanguageTag("es"));
        ctx.setVariable("userName", "Diego Alvarez");
        ctx.setVariable("resetUrl", "https://www.northernchile.com/auth/reset-password?token=test-token-123");
        return ctx;
    }

    private Context createBookingConfirmationContext() {
        Context ctx = new Context(Locale.forLanguageTag("es"));
        ctx.setVariable("customerName", "Diego Alvarez");
        ctx.setVariable("bookingId", "NCH-TEST-12345");
        ctx.setVariable("tourName", "Tour Astronómico Valle de la Luna");
        ctx.setVariable("tourDate", "15 de Enero, 2025");
        ctx.setVariable("tourTime", "20:00");
        ctx.setVariable("participantCount", 2);
        ctx.setVariable("totalAmount", "$150.000 CLP");
        return ctx;
    }

    private Context createTourReminderContext() {
        Context ctx = new Context(Locale.forLanguageTag("es"));
        ctx.setVariable("customerName", "Diego Alvarez");
        ctx.setVariable("bookingId", "NCH-TEST-12345");
        ctx.setVariable("tourName", "Tour Astronómico Valle de la Luna");
        ctx.setVariable("tourDate", "15 de Enero, 2025");
        ctx.setVariable("tourTime", "20:00");
        return ctx;
    }

    private Context createPickupReminderContext() {
        Context ctx = new Context(Locale.forLanguageTag("es"));
        ctx.setVariable("customerName", "Diego Alvarez");
        ctx.setVariable("tourName", "Tour Astronómico Valle de la Luna");
        ctx.setVariable("tourDate", "15 de Enero, 2025");
        ctx.setVariable("tourTime", "20:00");
        ctx.setVariable("pickupLocation", "Hotel Explora, San Pedro de Atacama");
        ctx.setVariable("equipmentList", "• Ropa abrigada\n• Linterna\n• Agua\n• Cámara fotográfica");
        ctx.setVariable("emergencyContact", "+56 9 5765 5764");
        return ctx;
    }

    private Context createBookingCancelledContext() {
        Context ctx = new Context(Locale.forLanguageTag("es"));
        ctx.setVariable("customerName", "Diego Alvarez");
        ctx.setVariable("bookingId", "NCH-TEST-12345");
        ctx.setVariable("tourName", "Tour Astronómico Valle de la Luna");
        ctx.setVariable("tourDate", "15 de Enero, 2025");
        ctx.setVariable("reason", "Condiciones climáticas adversas");
        ctx.setVariable("refundAmount", "$150.000 CLP");
        return ctx;
    }

    private Context createRefundConfirmationContext() {
        Context ctx = new Context(Locale.forLanguageTag("es"));
        ctx.setVariable("customerName", "Diego Alvarez");
        ctx.setVariable("refundAmount", "$150.000 CLP");
        ctx.setVariable("paymentMethod", "Tarjeta Visa ****1234");
        return ctx;
    }

    private Context createPrivateTourQuoteContext() {
        Context ctx = new Context(Locale.forLanguageTag("es"));
        ctx.setVariable("customerName", "Diego Alvarez");
        ctx.setVariable("tourType", "Tour Astronómico Privado - Observación de Estrellas");
        ctx.setVariable("price", "$450.000 CLP");
        ctx.setVariable("requests", "Grupo de 4 personas, interesados en fotografía astronómica");
        ctx.setVariable("notes", "Incluye telescopios profesionales y guía experto en astrofotografía. Pickup en su hotel a las 19:30.");
        return ctx;
    }

    private Context createAdminNewBookingContext() {
        Context ctx = new Context(Locale.forLanguageTag("es"));
        ctx.setVariable("bookingId", "NCH-TEST-12345");
        ctx.setVariable("status", "PENDING");
        ctx.setVariable("tourName", "Tour Astronómico Valle de la Luna");
        ctx.setVariable("tourDate", "15/01/2025 20:00");
        ctx.setVariable("customerName", "Diego Alvarez");
        ctx.setVariable("customerEmail", "diego@example.com");
        ctx.setVariable("participantCount", 2);
        ctx.setVariable("subtotal", "$126.050 CLP");
        ctx.setVariable("taxAmount", "$23.950 CLP");
        ctx.setVariable("totalAmount", "$150.000 CLP");
        ctx.setVariable("specialRequests", "Necesitamos pickup en Hotel Explora. Uno de los participantes es vegetariano.");
        ctx.setVariable("createdAt", "10/01/2025 15:30");
        return ctx;
    }

    private Context createAdminPrivateRequestContext() {
        Context ctx = new Context(Locale.forLanguageTag("es"));
        ctx.setVariable("requestId", "PTR-TEST-12345");
        ctx.setVariable("status", "PENDING");
        ctx.setVariable("tourType", "Tour Astronómico Privado");
        ctx.setVariable("requestedDate", "20/01/2025 20:00");
        ctx.setVariable("participantCount", 6);
        ctx.setVariable("customerName", "María García");
        ctx.setVariable("customerEmail", "maria@example.com");
        ctx.setVariable("customerPhone", "+56 9 1234 5678");
        ctx.setVariable("specialRequests", "Queremos celebrar el cumpleaños de mi esposo durante el tour. Nos gustaría incluir una sorpresa con champagne bajo las estrellas.");
        ctx.setVariable("createdAt", "10/01/2025 15:30");
        return ctx;
    }

    private Context createAdminContactContext() {
        Context ctx = new Context(Locale.forLanguageTag("es"));
        ctx.setVariable("name", "Juan Pérez");
        ctx.setVariable("email", "juan@example.com");
        ctx.setVariable("phone", "+56 9 9876 5432");
        ctx.setVariable("message", "Hola, me gustaría saber más información sobre sus tours astronómicos. ¿Cuál es la mejor época del año para visitar? También quisiera saber si tienen tours privados disponibles para grupos grandes.");
        ctx.setVariable("createdAt", "10/01/2025 15:30");
        ctx.setVariable("messageId", "MSG-TEST-12345");
        return ctx;
    }

    private Context createManifestContext() {
        Context ctx = new Context(Locale.forLanguageTag("es"));
        ctx.setVariable("tourName", "Tour Astronómico Premium");
        ctx.setVariable("tourDate", "15/01/2025");
        ctx.setVariable("tourTime", "20:00");
        ctx.setVariable("guideName", "Carlos Rodríguez");
        ctx.setVariable("totalParticipants", 4);

        // Create sample participants
        List<Map<String, Object>> participants = new ArrayList<>();

        Map<String, Object> p1 = new HashMap<>();
        p1.put("number", 1);
        p1.put("fullName", "Diego Alvarez");
        p1.put("phoneNumber", "+56 9 1234 5678");
        p1.put("pickupAddress", "Hotel Explora, hab 204");
        p1.put("nationality", "Chileno");
        p1.put("documentId", "12.345.678-9");
        p1.put("specialRequirements", "Vegetariano");
        p1.put("bookingSpecialRequests", "");
        participants.add(p1);

        Map<String, Object> p2 = new HashMap<>();
        p2.put("number", 2);
        p2.put("fullName", "María García");
        p2.put("phoneNumber", "+54 9 8765 4321");
        p2.put("pickupAddress", "Hotel Explora, hab 204");
        p2.put("nationality", "Argentina");
        p2.put("documentId", "DNI 30.123.456");
        p2.put("specialRequirements", "");
        p2.put("bookingSpecialRequests", "Cumpleaños de María");
        participants.add(p2);

        Map<String, Object> p3 = new HashMap<>();
        p3.put("number", 3);
        p3.put("fullName", "John Smith");
        p3.put("phoneNumber", "+1 555 123 4567");
        p3.put("pickupAddress", "Hostal Desert Moon");
        p3.put("nationality", "Estados Unidos");
        p3.put("documentId", "Passport US12345678");
        p3.put("specialRequirements", "");
        p3.put("bookingSpecialRequests", "");
        participants.add(p3);

        Map<String, Object> p4 = new HashMap<>();
        p4.put("number", 4);
        p4.put("fullName", "Sophie Laurent");
        p4.put("phoneNumber", "+33 6 12 34 56 78");
        p4.put("pickupAddress", "Hostal Desert Moon");
        p4.put("nationality", "Francia");
        p4.put("documentId", "Passport FR98765432");
        p4.put("specialRequirements", "Alergia a nueces");
        p4.put("bookingSpecialRequests", "Primera vez observando estrellas");
        participants.add(p4);

        ctx.setVariable("participants", participants);
        return ctx;
    }

    public record TestEmailRequest(String email) {}
}
