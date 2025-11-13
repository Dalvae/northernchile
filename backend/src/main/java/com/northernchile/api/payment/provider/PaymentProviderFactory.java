package com.northernchile.api.payment.provider;

import com.northernchile.api.payment.model.PaymentProvider;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Factory for creating payment provider services.
 * Uses dependency injection to provide the correct payment provider implementation.
 */
@Component
public class PaymentProviderFactory {

    private final Map<String, PaymentProviderService> providers;

    public PaymentProviderFactory(TransbankPaymentService transbankPaymentService,
                                  MercadoPagoPaymentService mercadoPagoPaymentService) {
        this.providers = Map.of(
            PaymentProvider.TRANSBANK.name(), transbankPaymentService,
            PaymentProvider.MERCADOPAGO.name(), mercadoPagoPaymentService
            // Add more providers here as needed
            // PaymentProvider.STRIPE.name(), stripePaymentService
        );
    }

    /**
     * Get the appropriate payment provider service based on the provider enum.
     *
     * @param provider Payment provider enum
     * @return Payment provider service implementation
     * @throws IllegalArgumentException if provider is not supported
     */
    public PaymentProviderService getProvider(PaymentProvider provider) {
        PaymentProviderService service = providers.get(provider.name());
        if (service == null) {
            throw new IllegalArgumentException("Unsupported payment provider: " + provider);
        }
        return service;
    }
}
