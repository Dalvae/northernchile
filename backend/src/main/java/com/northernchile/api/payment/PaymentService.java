package com.northernchile.api.payment;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentService {

    // TODO: Implementar integración con pasarela de pago (Transbank, Mercado Pago).
    // De momento, este servicio es un placeholder.
    public String createPaymentIntent(BigDecimal amount, UUID bookingId) {
        System.out.println("--- PAYMENT INTENT (PLACEHOLDER) ---");
        System.out.println("Booking ID: " + bookingId);
        System.out.println("Amount: " + amount);
        System.out.println("------------------------------------");
        // En una implementación real, esto devolvería un client_secret o una URL de pago.
        return "payment_intent_placeholder_" + UUID.randomUUID();
    }
}
