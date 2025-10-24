package com.northernchile.api.payment;

import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    public String createPaymentIntent(double amount) {
        // This is a placeholder. A real implementation would interact with a payment
        // gateway like Stripe or Mercado Pago to create a payment intent.
        return "pi_123456789";
    }
}
