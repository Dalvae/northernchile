package com.northernchile.api.payment.provider;

import com.northernchile.api.payment.model.PaymentProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PaymentProviderFactory.
 */
@ExtendWith(MockitoExtension.class)
class PaymentProviderFactoryTest {

    @Mock
    private TransbankPaymentService transbankPaymentService;

    @Mock
    private MercadoPagoPaymentService mercadoPagoPaymentService;

    private PaymentProviderFactory factory;

    @BeforeEach
    void setUp() {
        factory = new PaymentProviderFactory(transbankPaymentService, mercadoPagoPaymentService);
    }

    @Test
    void testGetProvider_Transbank() {
        // Act
        PaymentProviderService provider = factory.getProvider(PaymentProvider.TRANSBANK);

        // Assert
        assertNotNull(provider);
        assertEquals(transbankPaymentService, provider);
    }

    @Test
    void testGetProvider_MercadoPago() {
        // Act
        PaymentProviderService provider = factory.getProvider(PaymentProvider.MERCADOPAGO);

        // Assert
        assertNotNull(provider);
        assertEquals(mercadoPagoPaymentService, provider);
    }

    @Test
    void testGetProvider_UnsupportedProvider() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> factory.getProvider(PaymentProvider.STRIPE));
    }
}
