package com.northernchile.api.notification;

import com.northernchile.api.model.Booking;
import com.northernchile.api.model.ContactMessage;
import com.northernchile.api.model.PrivateTourRequest;
import com.northernchile.api.model.Tour;
import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.model.User;
import com.northernchile.api.tour.ManifestService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Tests")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MessageSource messageSource;

    @Mock
    private MimeMessage mimeMessage;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender, templateEngine, messageSource);
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@northernchile.com");
        ReflectionTestUtils.setField(emailService, "fromName", "Northern Chile");
        ReflectionTestUtils.setField(emailService, "mailEnabled", true);
    }

    @Nested
    @DisplayName("Verification Email Tests")
    class VerificationEmailTests {

        @Test
        @DisplayName("Should send verification email with correct parameters")
        void shouldSendVerificationEmail() throws Exception {
            // Given
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(messageSource.getMessage(eq("email.verification.title"), any(), any(Locale.class)))
                    .thenReturn("Verificar tu correo");
            when(templateEngine.process(eq("email/verification"), any(Context.class)))
                    .thenReturn("<html>Verification email</html>");

            // When
            emailService.sendVerificationEmail(
                    "user@example.com",
                    "Test User",
                    "http://localhost:3000/verify?token=abc123",
                    "es-CL"
            );

            // Then
            verify(templateEngine).process(eq("email/verification"), any(Context.class));
            verify(mailSender).send(mimeMessage);
        }

        @Test
        @DisplayName("Should not send email when mail is disabled")
        void shouldNotSendEmailWhenDisabled() {
            // Given
            ReflectionTestUtils.setField(emailService, "mailEnabled", false);

            // When
            emailService.sendVerificationEmail(
                    "user@example.com",
                    "Test User",
                    "http://localhost:3000/verify?token=abc123",
                    "es-CL"
            );

            // Then
            verify(mailSender, never()).send(any(MimeMessage.class));
        }
    }

    @Nested
    @DisplayName("Password Reset Email Tests")
    class PasswordResetEmailTests {

        @Test
        @DisplayName("Should send password reset email")
        void shouldSendPasswordResetEmail() throws Exception {
            // Given
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(messageSource.getMessage(eq("email.password.reset.title"), any(), any(Locale.class)))
                    .thenReturn("Restablecer contraseña");
            when(templateEngine.process(eq("email/password-reset"), any(Context.class)))
                    .thenReturn("<html>Reset email</html>");

            // When
            emailService.sendPasswordResetEmail(
                    "user@example.com",
                    "Test User",
                    "http://localhost:3000/reset?token=xyz789",
                    "es-CL"
            );

            // Then
            verify(templateEngine).process(eq("email/password-reset"), any(Context.class));
            verify(mailSender).send(mimeMessage);
        }
    }

    @Nested
    @DisplayName("Booking Confirmation Email Tests")
    class BookingConfirmationEmailTests {

        @Test
        @DisplayName("Should send booking confirmation email with all details")
        void shouldSendBookingConfirmationEmail() throws Exception {
            // Given
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(messageSource.getMessage(eq("email.booking.confirmation.title"), any(), any(Locale.class)))
                    .thenReturn("Confirmación de Reserva");
            when(templateEngine.process(eq("email/booking-confirmation"), any(Context.class)))
                    .thenReturn("<html>Booking confirmation</html>");

            // When
            emailService.sendBookingConfirmationEmail(
                    "customer@example.com",
                    "John Doe",
                    "BK-12345",
                    "Tour Astronómico",
                    "2025-01-15",
                    "21:00",
                    2,
                    "$100,000",
                    "es-CL"
            );

            // Then
            verify(templateEngine).process(eq("email/booking-confirmation"), any(Context.class));
            verify(mailSender).send(mimeMessage);
        }
    }

    @Nested
    @DisplayName("Tour Reminder Email Tests")
    class TourReminderEmailTests {

        @Test
        @DisplayName("Should send tour reminder email")
        void shouldSendTourReminderEmail() throws Exception {
            // Given
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(messageSource.getMessage(eq("email.reminder.title"), any(), any(Locale.class)))
                    .thenReturn("Recordatorio de Tour");
            when(templateEngine.process(eq("email/tour-reminder"), any(Context.class)))
                    .thenReturn("<html>Reminder</html>");

            // When
            emailService.sendTourReminderEmail(
                    "customer@example.com",
                    "Jane Doe",
                    "BK-67890",
                    "Tour del Valle del Elqui",
                    "2025-01-16 10:00",
                    "en-US"
            );

            // Then
            verify(templateEngine).process(eq("email/tour-reminder"), any(Context.class));
            verify(mailSender).send(mimeMessage);
        }
    }

    @Nested
    @DisplayName("Admin Notification Email Tests")
    class AdminNotificationEmailTests {

        @Test
        @DisplayName("Should send new booking notification to admin")
        void shouldSendNewBookingNotificationToAdmin() throws Exception {
            // Given
            Booking booking = createTestBooking();
            
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("email/admin-new-booking"), any(Context.class)))
                    .thenReturn("<html>Admin notification</html>");

            // When
            emailService.sendNewBookingNotificationToAdmin(booking, "admin@northernchile.com");

            // Then
            verify(templateEngine).process(eq("email/admin-new-booking"), any(Context.class));
            verify(mailSender).send(mimeMessage);
        }

        @Test
        @DisplayName("Should send private tour request notification to admin")
        void shouldSendPrivateTourRequestNotificationToAdmin() throws Exception {
            // Given
            PrivateTourRequest request = new PrivateTourRequest();
            request.setId(UUID.randomUUID());
            request.setStatus("PENDING");
            request.setRequestedTourType("Astronomical");
            request.setNumParticipants(6);
            request.setCustomerName("Corporate Client");
            request.setCustomerEmail("corp@example.com");
            request.setCustomerPhone("+56912345678");
            request.setCreatedAt(Instant.now());

            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("email/admin-private-request"), any(Context.class)))
                    .thenReturn("<html>Private request</html>");

            // When
            emailService.sendNewPrivateRequestNotificationToAdmin(request, "admin@northernchile.com");

            // Then
            verify(templateEngine).process(eq("email/admin-private-request"), any(Context.class));
            verify(mailSender).send(mimeMessage);
        }

        @Test
        @DisplayName("Should send contact notification to admin")
        void shouldSendContactNotificationToAdmin() throws Exception {
            // Given
            ContactMessage contactMessage = new ContactMessage();
            contactMessage.setId(UUID.randomUUID());
            contactMessage.setName("Potential Customer");
            contactMessage.setEmail("potential@example.com");
            contactMessage.setPhone("+56987654321");
            contactMessage.setMessage("I'm interested in your tours");
            contactMessage.setCreatedAt(Instant.now());

            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("email/admin-contact"), any(Context.class)))
                    .thenReturn("<html>Contact message</html>");

            // When
            emailService.sendContactNotificationToAdmin(contactMessage, "admin@northernchile.com");

            // Then
            verify(templateEngine).process(eq("email/admin-contact"), any(Context.class));
            verify(mailSender).send(mimeMessage);
        }
    }

    @Nested
    @DisplayName("Manifest Email Tests")
    class ManifestEmailTests {

        @Test
        @DisplayName("Should send manifest email to operator")
        void shouldSendManifestEmailToOperator() throws Exception {
            // Given
            List<ManifestService.ManifestParticipant> participants = List.of(
                    createManifestParticipant("John Doe", "US", "john@example.com"),
                    createManifestParticipant("Jane Doe", "CL", "jane@example.com")
            );

            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("email/manifest"), any(Context.class)))
                    .thenReturn("<html>Manifest</html>");

            // When
            emailService.sendManifestEmail(
                    "operator@northernchile.com",
                    "Tour Astronómico Premium",
                    "2025-01-20",
                    "21:00",
                    "Carlos Guía",
                    2,
                    participants
            );

            // Then
            verify(templateEngine).process(eq("email/manifest"), any(Context.class));
            verify(mailSender).send(mimeMessage);
        }
    }

    @Nested
    @DisplayName("Pickup Reminder Email Tests")
    class PickupReminderEmailTests {

        @Test
        @DisplayName("Should send pickup reminder to participant")
        void shouldSendPickupReminderToParticipant() throws Exception {
            // Given
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(messageSource.getMessage(eq("pickup.subject"), any(), any(Locale.class)))
                    .thenReturn("Recordatorio de recogida");
            when(templateEngine.process(eq("email/pickup-reminder"), any(Context.class)))
                    .thenReturn("<html>Pickup reminder</html>");

            // When
            emailService.sendPickupReminderToParticipant(
                    "participant@example.com",
                    "Maria Garcia",
                    "Tour Valle del Elqui",
                    "2025-01-25",
                    "19:00",
                    "Hotel Plaza San Pedro",
                    "Ropa abrigada, cámara fotográfica",
                    "es-CL"
            );

            // Then
            verify(templateEngine).process(eq("email/pickup-reminder"), any(Context.class));
            verify(mailSender).send(mimeMessage);
        }
    }

    @Nested
    @DisplayName("Cancellation and Refund Email Tests")
    class CancellationRefundEmailTests {

        @Test
        @DisplayName("Should send booking cancelled email")
        void shouldSendBookingCancelledEmail() throws Exception {
            // Given
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(messageSource.getMessage(eq("email.cancelled.title"), any(), any(Locale.class)))
                    .thenReturn("Reserva Cancelada");
            when(templateEngine.process(eq("email/booking-cancelled"), any(Context.class)))
                    .thenReturn("<html>Cancellation</html>");

            // When
            emailService.sendBookingCancelledEmail(
                    "customer@example.com",
                    "Pedro Lopez",
                    "BK-99999",
                    "Tour Geysers del Tatio",
                    "2025-02-01",
                    3,
                    "Weather conditions",
                    "$150,000",
                    "es-CL"
            );

            // Then
            verify(templateEngine).process(eq("email/booking-cancelled"), any(Context.class));
            verify(mailSender).send(mimeMessage);
        }

        @Test
        @DisplayName("Should send refund confirmation email")
        void shouldSendRefundConfirmationEmail() throws Exception {
            // Given
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(messageSource.getMessage(eq("email.refund.title"), any(), any(Locale.class)))
                    .thenReturn("Confirmación de Reembolso");
            when(templateEngine.process(eq("email/refund-confirmation"), any(Context.class)))
                    .thenReturn("<html>Refund confirmation</html>");

            // When
            emailService.sendRefundConfirmationEmail(
                    "customer@example.com",
                    "Ana Martinez",
                    "BK-88888",
                    "Tour Astronómico",
                    "$75,000",
                    "Tarjeta de crédito",
                    "es-CL"
            );

            // Then
            verify(templateEngine).process(eq("email/refund-confirmation"), any(Context.class));
            verify(mailSender).send(mimeMessage);
        }
    }

    @Nested
    @DisplayName("Private Tour Quote Email Tests")
    class PrivateTourQuoteEmailTests {

        @Test
        @DisplayName("Should send private tour quote email")
        void shouldSendPrivateTourQuoteEmail() throws Exception {
            // Given
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(messageSource.getMessage(eq("email.private.quote.title"), any(), any(Locale.class)))
                    .thenReturn("Cotización Tour Privado");
            when(templateEngine.process(eq("email/private-tour-quote"), any(Context.class)))
                    .thenReturn("<html>Quote</html>");

            // When
            emailService.sendPrivateTourQuoteEmail(
                    "vip@example.com",
                    "VIP Customer",
                    "Custom Astronomical Experience",
                    "2025-03-15 21:00",
                    8,
                    "$800,000",
                    "Champagne on arrival",
                    "We'll provide a dedicated astronomer guide",
                    "es-CL"
            );

            // Then
            verify(templateEngine).process(eq("email/private-tour-quote"), any(Context.class));
            verify(mailSender).send(mimeMessage);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle messaging exception gracefully")
        void shouldHandleMessagingExceptionGracefully() throws Exception {
            // Given
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Subject");
            when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html></html>");
            doThrow(new RuntimeException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

            // When - Should not throw
            emailService.sendVerificationEmail(
                    "user@example.com",
                    "User",
                    "http://url",
                    "es-CL"
            );

            // Then - Graceful degradation, no exception thrown
            verify(mailSender).send(any(MimeMessage.class));
        }
    }

    // Helper methods
    private Booking createTestBooking() {
        User user = new User();
        user.setFullName("Test Customer");
        user.setEmail("customer@example.com");

        Tour tour = new Tour();
        tour.setNameTranslations(Map.of("es", "Tour de Prueba"));

        TourSchedule schedule = new TourSchedule();
        schedule.setTour(tour);

        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setUser(user);
        booking.setSchedule(schedule);
        booking.setStatus("PENDING");
        booking.setTourDate(LocalDate.now().plusDays(7));
        booking.setSubtotal(new BigDecimal("84034"));
        booking.setTaxAmount(new BigDecimal("15966"));
        booking.setTotalAmount(new BigDecimal("100000"));
        booking.setCreatedAt(Instant.now());
        booking.setParticipants(List.of());

        return booking;
    }

    private ManifestService.ManifestParticipant createManifestParticipant(String name, String nationality, String email) {
        return new ManifestService.ManifestParticipant(
            1,           // number
            name,        // fullName
            null,        // phoneNumber
            null,        // pickupAddress
            nationality, // nationality
            null,        // documentId
            null,        // specialRequirements
            null         // bookingSpecialRequests
        );
    }
}
