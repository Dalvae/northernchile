package com.northernchile.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Centralized payment configuration properties.
 * Groups Transbank, MercadoPago, and general payment settings.
 */
@Component
@ConfigurationProperties(prefix = "payment")
public class PaymentProperties {

    private boolean testMode = false;
    private Transbank transbank = new Transbank();
    private MercadoPago mercadopago = new MercadoPago();
    private Refund refund = new Refund();

    public boolean isTestMode() {
        return testMode;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    public Transbank getTransbank() {
        return transbank;
    }

    public void setTransbank(Transbank transbank) {
        this.transbank = transbank;
    }

    public MercadoPago getMercadopago() {
        return mercadopago;
    }

    public void setMercadopago(MercadoPago mercadopago) {
        this.mercadopago = mercadopago;
    }

    public Refund getRefund() {
        return refund;
    }

    public void setRefund(Refund refund) {
        this.refund = refund;
    }

    public static class Transbank {
        private String commerceCode;
        private String apiKey;
        private String environment = "INTEGRATION";
        private String webhookSecret;
        private Fees fees = new Fees();

        public String getCommerceCode() {
            return commerceCode;
        }

        public void setCommerceCode(String commerceCode) {
            this.commerceCode = commerceCode;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getEnvironment() {
            return environment;
        }

        public void setEnvironment(String environment) {
            this.environment = environment;
        }

        public String getWebhookSecret() {
            return webhookSecret;
        }

        public void setWebhookSecret(String webhookSecret) {
            this.webhookSecret = webhookSecret;
        }

        public Fees getFees() {
            return fees;
        }

        public void setFees(Fees fees) {
            this.fees = fees;
        }

        public static class Fees {
            private double debit = 0.0177;
            private double credit = 0.0351;
            private double prepaid = 0.0177;

            public double getDebit() {
                return debit;
            }

            public void setDebit(double debit) {
                this.debit = debit;
            }

            public double getCredit() {
                return credit;
            }

            public void setCredit(double credit) {
                this.credit = credit;
            }

            public double getPrepaid() {
                return prepaid;
            }

            public void setPrepaid(double prepaid) {
                this.prepaid = prepaid;
            }
        }
    }

    public static class MercadoPago {
        private String accessToken;
        private String webhookSecret;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getWebhookSecret() {
            return webhookSecret;
        }

        public void setWebhookSecret(String webhookSecret) {
            this.webhookSecret = webhookSecret;
        }
    }

    public static class Refund {
        private int retentionPercentage = 5;

        public int getRetentionPercentage() {
            return retentionPercentage;
        }

        public void setRetentionPercentage(int retentionPercentage) {
            this.retentionPercentage = retentionPercentage;
        }
    }
}
