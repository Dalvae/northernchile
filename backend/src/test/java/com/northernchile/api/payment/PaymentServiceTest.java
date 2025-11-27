package com.northernchile.api.payment;

import com.northernchile.api.model.Booking;
import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.payment.dto.PaymentInitReq;
import com.northernchile.api.payment.dto.PaymentInitRes;
import com.northernchile.api.payment.dto.PaymentStatusRes;
import com.northernchile.api.payment.model.Payment;
import com.northernchile.api.payment.model.PaymentMethod;
import com.northernchile.api.payment.model.PaymentProvider;
import com.northernchile.api.payment.model.PaymentStatus;
import com.northernchile.api.payment.provider.PaymentProviderFactory;
import com.northernchile.api.payment.provider.PaymentProviderService;
import com.northernchile.api.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentService.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentProviderFactory providerFactory;

    @Mock
    private PaymentProviderService mockProvider;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, providerFactory);
    }

    @Nested
    @DisplayName("Create Payment Tests")
    class CreatePaymentTests {

        @Test
        @DisplayName("Should create payment successfully with Transbank")
        void testCreatePayment_Success() {
            // Arrange
            UUID bookingId = UUID.randomUUID();
            PaymentInitReq request = new PaymentInitReq();
            request.setBookingId(bookingId);
            request.setProvider(PaymentProvider.TRANSBANK);
            request.setPaymentMethod(PaymentMethod.WEBPAY);
            request.setAmount(new BigDecimal("100000"));
            request.setCurrency("CLP");

            PaymentInitRes expectedResponse = new PaymentInitRes();
            expectedResponse.setPaymentId(UUID.randomUUID());
            expectedResponse.setStatus(PaymentStatus.PENDING);
            expectedResponse.setPaymentUrl("https://webpay3g.transbank.cl/webpayserver/initTransaction");

            when(paymentRepository.findActivePaymentForBooking(any(), any())).thenReturn(Optional.empty());
            when(providerFactory.getProvider(PaymentProvider.TRANSBANK)).thenReturn(mockProvider);
            when(mockProvider.createPayment(any(PaymentInitReq.class))).thenReturn(expectedResponse);

            // Act
            PaymentInitRes response = paymentService.createPayment(request);

            // Assert
            assertNotNull(response);
            assertEquals(PaymentStatus.PENDING, response.getStatus());
            assertNotNull(response.getPaymentUrl());
            verify(providerFactory).getProvider(PaymentProvider.TRANSBANK);
            verify(mockProvider).createPayment(request);
        }

        @Test
        @DisplayName("Should reject payment with null booking ID")
        void testCreatePayment_InvalidRequest_NullBookingId() {
            // Arrange
            PaymentInitReq request = new PaymentInitReq();
            request.setProvider(PaymentProvider.TRANSBANK);
            request.setPaymentMethod(PaymentMethod.WEBPAY);
            request.setAmount(new BigDecimal("100000"));

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(request));
        }

        @Test
        @DisplayName("Should reject payment with null provider")
        void testCreatePayment_InvalidRequest_NullProvider() {
            // Arrange
            PaymentInitReq request = new PaymentInitReq();
            request.setBookingId(UUID.randomUUID());
            request.setPaymentMethod(PaymentMethod.WEBPAY);
            request.setAmount(new BigDecimal("100000"));

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(request));
        }

        @Test
        @DisplayName("Should reject payment with negative amount")
        void testCreatePayment_InvalidRequest_NegativeAmount() {
            // Arrange
            PaymentInitReq request = new PaymentInitReq();
            request.setBookingId(UUID.randomUUID());
            request.setProvider(PaymentProvider.TRANSBANK);
            request.setPaymentMethod(PaymentMethod.WEBPAY);
            request.setAmount(new BigDecimal("-100"));

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(request));
        }

        @Test
        @DisplayName("Should reject payment with zero amount")
        void testCreatePayment_InvalidRequest_ZeroAmount() {
            // Arrange
            PaymentInitReq request = new PaymentInitReq();
            request.setBookingId(UUID.randomUUID());
            request.setProvider(PaymentProvider.TRANSBANK);
            request.setPaymentMethod(PaymentMethod.WEBPAY);
            request.setAmount(BigDecimal.ZERO);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(request));
        }

        @Test
        @DisplayName("Should reject payment with null payment method")
        void testCreatePayment_InvalidRequest_NullPaymentMethod() {
            // Arrange
            PaymentInitReq request = new PaymentInitReq();
            request.setBookingId(UUID.randomUUID());
            request.setProvider(PaymentProvider.TRANSBANK);
            request.setPaymentMethod(null);
            request.setAmount(new BigDecimal("100000"));

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> paymentService.createPayment(request));
        }

        @Test
        @DisplayName("Should reject payment with unsupported currency")
        void testCreatePayment_InvalidRequest_UnsupportedCurrency() {
            // Arrange
            PaymentInitReq request = new PaymentInitReq();
            request.setBookingId(UUID.randomUUID());
            request.setProvider(PaymentProvider.TRANSBANK);
            request.setPaymentMethod(PaymentMethod.WEBPAY);
            request.setAmount(new BigDecimal("100000"));
            request.setCurrency("EUR");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> paymentService.createPayment(request)
            );
            assertThat(exception.getMessage()).contains("Unsupported currency");
        }

        @Test
        @DisplayName("Should accept CLP currency")
        void testCreatePayment_ValidCurrency_CLP() {
            // Arrange
            UUID bookingId = UUID.randomUUID();
            PaymentInitReq request = new PaymentInitReq();
            request.setBookingId(bookingId);
            request.setProvider(PaymentProvider.TRANSBANK);
            request.setPaymentMethod(PaymentMethod.WEBPAY);
            request.setAmount(new BigDecimal("100000"));
            request.setCurrency("CLP");

            PaymentInitRes expectedResponse = new PaymentInitRes();
            expectedResponse.setPaymentId(UUID.randomUUID());
            expectedResponse.setStatus(PaymentStatus.PENDING);

            when(paymentRepository.findActivePaymentForBooking(any(), any())).thenReturn(Optional.empty());
            when(providerFactory.getProvider(PaymentProvider.TRANSBANK)).thenReturn(mockProvider);
            when(mockProvider.createPayment(any(PaymentInitReq.class))).thenReturn(expectedResponse);

            // Act
            PaymentInitRes response = paymentService.createPayment(request);

            // Assert
            assertNotNull(response);
            assertEquals(PaymentStatus.PENDING, response.getStatus());
        }

        @Test
        @DisplayName("Should accept BRL currency")
        void testCreatePayment_ValidCurrency_BRL() {
            // Arrange
            UUID bookingId = UUID.randomUUID();
            PaymentInitReq request = new PaymentInitReq();
            request.setBookingId(bookingId);
            request.setProvider(PaymentProvider.MERCADOPAGO);
            request.setPaymentMethod(PaymentMethod.PIX);
            request.setAmount(new BigDecimal("500"));
            request.setCurrency("BRL");

            PaymentInitRes expectedResponse = new PaymentInitRes();
            expectedResponse.setPaymentId(UUID.randomUUID());
            expectedResponse.setStatus(PaymentStatus.PENDING);

            when(paymentRepository.findActivePaymentForBooking(any(), any())).thenReturn(Optional.empty());
            when(providerFactory.getProvider(PaymentProvider.MERCADOPAGO)).thenReturn(mockProvider);
            when(mockProvider.createPayment(any(PaymentInitReq.class))).thenReturn(expectedResponse);

            // Act
            PaymentInitRes response = paymentService.createPayment(request);

            // Assert
            assertNotNull(response);
            assertEquals(PaymentStatus.PENDING, response.getStatus());
        }

        @Test
        @DisplayName("Should create MercadoPago PIX payment successfully")
        void testCreatePayment_MercadoPago_PIX() {
            // Arrange
            UUID bookingId = UUID.randomUUID();
            PaymentInitReq request = new PaymentInitReq();
            request.setBookingId(bookingId);
            request.setProvider(PaymentProvider.MERCADOPAGO);
            request.setPaymentMethod(PaymentMethod.PIX);
            request.setAmount(new BigDecimal("250.00"));
            request.setCurrency("BRL");
            request.setExpirationMinutes(30);

            PaymentInitRes expectedResponse = new PaymentInitRes();
            expectedResponse.setPaymentId(UUID.randomUUID());
            expectedResponse.setStatus(PaymentStatus.PENDING);
            expectedResponse.setQrCode("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...");
            expectedResponse.setPixCode("00020126580014br.gov.bcb.pix...");

            when(paymentRepository.findActivePaymentForBooking(any(), any())).thenReturn(Optional.empty());
            when(providerFactory.getProvider(PaymentProvider.MERCADOPAGO)).thenReturn(mockProvider);
            when(mockProvider.createPayment(any(PaymentInitReq.class))).thenReturn(expectedResponse);

            // Act
            PaymentInitRes response = paymentService.createPayment(request);

            // Assert
            assertNotNull(response);
            assertEquals(PaymentStatus.PENDING, response.getStatus());
            assertNotNull(response.getQrCode());
            assertNotNull(response.getPixCode());
            verify(providerFactory).getProvider(PaymentProvider.MERCADOPAGO);
            verify(mockProvider).createPayment(request);
        }
    }

    @Nested
    @DisplayName("Idempotency Tests")
    class IdempotencyTests {

        @Test
        @DisplayName("Should return existing payment when idempotency key matches")
        void shouldReturnExistingPaymentWhenIdempotencyKeyMatches() {
            // Given
            String idempotencyKey = "idem-key-123";
            Payment existingPayment = new Payment();
            existingPayment.setId(UUID.randomUUID());
            existingPayment.setStatus(PaymentStatus.PENDING);
            existingPayment.setPaymentUrl("https://existing-url.com");

            PaymentInitReq request = new PaymentInitReq();
            request.setBookingId(UUID.randomUUID());
            request.setProvider(PaymentProvider.TRANSBANK);
            request.setPaymentMethod(PaymentMethod.WEBPAY);
            request.setAmount(new BigDecimal("100000"));
            request.setIdempotencyKey(idempotencyKey);

            when(paymentRepository.findByIdempotencyKey(idempotencyKey))
                    .thenReturn(Optional.of(existingPayment));

            // When
            PaymentInitRes response = paymentService.createPayment(request);

            // Then
            assertThat(response.getPaymentId()).isEqualTo(existingPayment.getId());
            verify(providerFactory, never()).getProvider(any());
        }

        @Test
        @DisplayName("Should return existing active payment for same booking")
        void shouldReturnExistingActivePaymentForSameBooking() {
            // Given
            UUID bookingId = UUID.randomUUID();
            Payment activePayment = new Payment();
            activePayment.setId(UUID.randomUUID());
            activePayment.setStatus(PaymentStatus.PENDING);

            PaymentInitReq request = new PaymentInitReq();
            request.setBookingId(bookingId);
            request.setProvider(PaymentProvider.TRANSBANK);
            request.setPaymentMethod(PaymentMethod.WEBPAY);
            request.setAmount(new BigDecimal("100000"));

            when(paymentRepository.findActivePaymentForBooking(eq(bookingId), any()))
                    .thenReturn(Optional.of(activePayment));

            // When
            PaymentInitRes response = paymentService.createPayment(request);

            // Then
            assertThat(response.getPaymentId()).isEqualTo(activePayment.getId());
            verify(providerFactory, never()).getProvider(any());
        }
    }

    @Nested
    @DisplayName("Confirm Payment Tests")
    class ConfirmPaymentTests {

        @Test
        @DisplayName("Should confirm payment successfully")
        void shouldConfirmPaymentSuccessfully() {
            // Given
            String token = "webpay-token-123";
            Payment payment = new Payment();
            payment.setId(UUID.randomUUID());
            payment.setProvider(PaymentProvider.TRANSBANK);

            PaymentStatusRes expectedResponse = new PaymentStatusRes();
            expectedResponse.setPaymentId(payment.getId());
            expectedResponse.setStatus(PaymentStatus.COMPLETED);

            when(paymentRepository.findByToken(token)).thenReturn(Optional.of(payment));
            when(providerFactory.getProvider(PaymentProvider.TRANSBANK)).thenReturn(mockProvider);
            when(mockProvider.confirmPayment(token)).thenReturn(expectedResponse);

            // When
            PaymentStatusRes response = paymentService.confirmPayment(token);

            // Then
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }

        @Test
        @DisplayName("Should throw exception when payment token not found")
        void shouldThrowExceptionWhenPaymentTokenNotFound() {
            // Given
            when(paymentRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> paymentService.confirmPayment("invalid-token"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Payment not found");
        }
    }

    @Nested
    @DisplayName("Get Payment Status Tests")
    class GetPaymentStatusTests {

        @Test
        @DisplayName("Should get payment status by ID")
        void shouldGetPaymentStatusById() {
            // Given
            UUID paymentId = UUID.randomUUID();
            Payment payment = new Payment();
            payment.setId(paymentId);
            payment.setExternalPaymentId("ext-123");
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setAmount(new BigDecimal("50000"));
            payment.setCurrency("CLP");
            payment.setUpdatedAt(Instant.now());

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

            // When
            PaymentStatusRes response = paymentService.getPaymentStatus(paymentId);

            // Then
            assertThat(response.getPaymentId()).isEqualTo(paymentId);
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
            assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("50000"));
        }

        @Test
        @DisplayName("Should throw exception when payment ID not found")
        void shouldThrowExceptionWhenPaymentIdNotFound() {
            // Given
            UUID unknownId = UUID.randomUUID();
            when(paymentRepository.findById(unknownId)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> paymentService.getPaymentStatus(unknownId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Payment not found");
        }
    }

    @Nested
    @DisplayName("Refund Payment Tests")
    class RefundPaymentTests {

        @Test
        @DisplayName("Should refund payment successfully")
        void shouldRefundPaymentSuccessfully() {
            // Given
            UUID paymentId = UUID.randomUUID();
            TourSchedule schedule = new TourSchedule();
            schedule.setStartDatetime(Instant.now().plus(48, ChronoUnit.HOURS));

            Booking booking = new Booking();
            booking.setSchedule(schedule);

            Payment payment = new Payment();
            payment.setId(paymentId);
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setProvider(PaymentProvider.MERCADOPAGO);
            payment.setBooking(booking);

            PaymentStatusRes expectedResponse = new PaymentStatusRes();
            expectedResponse.setPaymentId(paymentId);
            expectedResponse.setStatus(PaymentStatus.REFUNDED);

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
            when(providerFactory.getProvider(PaymentProvider.MERCADOPAGO)).thenReturn(mockProvider);
            when(mockProvider.refundPayment(payment, null)).thenReturn(expectedResponse);

            // When
            PaymentStatusRes response = paymentService.refundPayment(paymentId, null);

            // Then
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            verify(paymentRepository).save(argThat(p -> p.getStatus() == PaymentStatus.REFUND_PENDING));
        }

        @Test
        @DisplayName("Should reject refund for non-completed payment")
        void shouldRejectRefundForNonCompletedPayment() {
            // Given
            UUID paymentId = UUID.randomUUID();
            Payment payment = new Payment();
            payment.setId(paymentId);
            payment.setStatus(PaymentStatus.PENDING);

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

            // When/Then
            assertThatThrownBy(() -> paymentService.refundPayment(paymentId, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("cannot be refunded");
        }

        @Test
        @DisplayName("Should reject refund when tour starts in less than 24 hours")
        void shouldRejectRefundWhenTourStartsSoon() {
            // Given
            UUID paymentId = UUID.randomUUID();
            TourSchedule schedule = new TourSchedule();
            schedule.setStartDatetime(Instant.now().plus(12, ChronoUnit.HOURS)); // 12 hours from now

            Booking booking = new Booking();
            booking.setSchedule(schedule);

            Payment payment = new Payment();
            payment.setId(paymentId);
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setBooking(booking);

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

            // When/Then
            assertThatThrownBy(() -> paymentService.refundPayment(paymentId, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("24 hours");
        }
    }

    @Nested
    @DisplayName("Webhook Processing Tests")
    class WebhookProcessingTests {

        @Test
        @DisplayName("Should process webhook successfully")
        void shouldProcessWebhookSuccessfully() {
            // Given
            Map<String, Object> payload = Map.of(
                    "type", "payment",
                    "id", "12345"
            );

            PaymentStatusRes expectedResponse = new PaymentStatusRes();
            expectedResponse.setStatus(PaymentStatus.COMPLETED);

            when(providerFactory.getProvider(PaymentProvider.MERCADOPAGO)).thenReturn(mockProvider);
            when(mockProvider.processWebhook(payload)).thenReturn(expectedResponse);

            // When
            PaymentStatusRes response = paymentService.processWebhook("MERCADOPAGO", payload);

            // Then
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        }

        @Test
        @DisplayName("Should throw exception for unsupported provider")
        void shouldThrowExceptionForUnsupportedProvider() {
            // Given
            Map<String, Object> payload = Map.of("id", "123");

            // When/Then
            assertThatThrownBy(() -> paymentService.processWebhook("UNKNOWN_PROVIDER", payload))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unsupported payment provider");
        }
    }

    @Nested
    @DisplayName("Test Payment Management Tests")
    class TestPaymentManagementTests {

        @Test
        @DisplayName("Should get all test payments")
        void shouldGetAllTestPayments() {
            // Given
            Payment testPayment1 = new Payment();
            testPayment1.setId(UUID.randomUUID());
            testPayment1.setTest(true);

            Payment testPayment2 = new Payment();
            testPayment2.setId(UUID.randomUUID());
            testPayment2.setTest(true);

            when(paymentRepository.findByIsTest(true)).thenReturn(List.of(testPayment1, testPayment2));

            // When
            List<Payment> testPayments = paymentService.getTestPayments();

            // Then
            assertThat(testPayments).hasSize(2);
        }

        @Test
        @DisplayName("Should delete all test payments")
        void shouldDeleteAllTestPayments() {
            // Given
            Payment testPayment = new Payment();
            testPayment.setId(UUID.randomUUID());
            testPayment.setTest(true);

            when(paymentRepository.findByIsTest(true)).thenReturn(List.of(testPayment));

            // When
            int deleted = paymentService.deleteTestPayments();

            // Then
            assertThat(deleted).isEqualTo(1);
            verify(paymentRepository).deleteByIsTest(true);
        }

        @Test
        @DisplayName("Should return zero when no test payments to delete")
        void shouldReturnZeroWhenNoTestPaymentsToDelete() {
            // Given
            when(paymentRepository.findByIsTest(true)).thenReturn(List.of());

            // When
            int deleted = paymentService.deleteTestPayments();

            // Then
            assertThat(deleted).isEqualTo(0);
            verify(paymentRepository, never()).deleteByIsTest(anyBoolean());
        }
    }

    @Nested
    @DisplayName("Get Booking Payments Tests")
    class GetBookingPaymentsTests {

        @Test
        @DisplayName("Should get all payments for a booking")
        void shouldGetAllPaymentsForBooking() {
            // Given
            UUID bookingId = UUID.randomUUID();
            Payment payment1 = new Payment();
            payment1.setId(UUID.randomUUID());
            Payment payment2 = new Payment();
            payment2.setId(UUID.randomUUID());

            when(paymentRepository.findByBookingId(bookingId)).thenReturn(List.of(payment1, payment2));

            // When
            List<Payment> payments = paymentService.getBookingPayments(bookingId);

            // Then
            assertThat(payments).hasSize(2);
        }
    }
}
