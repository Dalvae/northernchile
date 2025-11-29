package com.northernchile.api.payment;

import com.northernchile.api.model.Booking;
import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.payment.dto.PaymentStatusRes;
import com.northernchile.api.payment.model.Payment;
import com.northernchile.api.payment.model.PaymentStatus;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentService.
 * Tests status retrieval, refunds, and test payment management.
 * Note: Primary payment flow is tested in PaymentSessionServiceTest.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository);
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
            payment.setBooking(booking);

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
            when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            PaymentStatusRes response = paymentService.refundPayment(paymentId, null);

            // Then
            assertThat(response.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
            verify(paymentRepository).save(argThat(p -> p.getStatus() == PaymentStatus.REFUNDED));
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
                    .hasMessageContaining("Cannot refund payment");
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
