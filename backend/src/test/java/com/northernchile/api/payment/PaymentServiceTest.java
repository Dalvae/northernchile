package com.northernchile.api.payment;

import com.northernchile.api.payment.dto.PaymentInitReq;
import com.northernchile.api.payment.dto.PaymentInitRes;
import com.northernchile.api.payment.model.PaymentMethod;
import com.northernchile.api.payment.model.PaymentProvider;
import com.northernchile.api.payment.model.PaymentStatus;
import com.northernchile.api.payment.provider.PaymentProviderFactory;
import com.northernchile.api.payment.provider.PaymentProviderService;
import com.northernchile.api.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentService.
 */
@ExtendWith(MockitoExtension.class)
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

    @Test
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
