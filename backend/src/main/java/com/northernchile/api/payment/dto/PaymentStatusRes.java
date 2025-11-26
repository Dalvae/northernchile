package com.northernchile.api.payment.dto;

import com.northernchile.api.payment.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Payment status response DTO.
 * Contains the current status of a payment transaction.
 */
public class PaymentStatusRes {

    private UUID paymentId;
    private String externalPaymentId;
    private PaymentStatus status;
    private BigDecimal amount;
    private String currency;
    private String message;
    private Instant updatedAt;
    private boolean isTest;

    public PaymentStatusRes() {
    }

    public PaymentStatusRes(UUID paymentId, String externalPaymentId, PaymentStatus status,
                            BigDecimal amount, String currency, String message, Instant updatedAt) {
        this.paymentId = paymentId;
        this.externalPaymentId = externalPaymentId;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
        this.message = message;
        this.updatedAt = updatedAt;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public String getExternalPaymentId() {
        return externalPaymentId;
    }

    public void setExternalPaymentId(String externalPaymentId) {
        this.externalPaymentId = externalPaymentId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isTest() {
        return isTest;
    }

    public void setTest(boolean test) {
        isTest = test;
    }

    @Override
    public String toString() {
        return "PaymentStatusRes{" +
            "paymentId=" + paymentId +
            ", externalPaymentId='" + externalPaymentId + '\'' +
            ", status=" + status +
            ", amount=" + amount +
            ", currency='" + currency + '\'' +
            ", updatedAt=" + updatedAt +
            '}';
    }
}
