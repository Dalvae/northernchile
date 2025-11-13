package com.northernchile.api.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    // TODO: Implementar integración con pasarela de pago (Transbank, Mercado Pago).
    // De momento, este servicio es un placeholder.
    public String createPaymentIntent(BigDecimal amount, UUID bookingId) {
        log.info("Creating payment intent (placeholder) - Booking ID: {}, Amount: {}", bookingId, amount);
        // En una implementación real, esto devolvería un client_secret o una URL de pago.
        return "payment_intent_placeholder_" + UUID.randomUUID();
    }
}
