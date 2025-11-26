package com.northernchile.api.payment.dto;

import com.northernchile.api.payment.model.PaymentStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Payment initialization response DTO.
 * Contains the payment details needed for the client to complete the payment.
 */
public class PaymentInitRes {

    private UUID paymentId;
    private PaymentStatus status;
    private String paymentUrl;
    private String detailsUrl;
    private String qrCode;
    private String pixCode;
    private String token;
    private Instant expiresAt;
    private String message;
    private boolean isTest;

    public PaymentInitRes() {
    }

    public PaymentInitRes(UUID paymentId, PaymentStatus status, String paymentUrl, String detailsUrl,
                          String qrCode, String pixCode, String token, Instant expiresAt, String message) {
        this.paymentId = paymentId;
        this.status = status;
        this.paymentUrl = paymentUrl;
        this.detailsUrl = detailsUrl;
        this.qrCode = qrCode;
        this.pixCode = pixCode;
        this.token = token;
        this.expiresAt = expiresAt;
        this.message = message;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public String getDetailsUrl() {
        return detailsUrl;
    }

    public void setDetailsUrl(String detailsUrl) {
        this.detailsUrl = detailsUrl;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public String getPixCode() {
        return pixCode;
    }

    public void setPixCode(String pixCode) {
        this.pixCode = pixCode;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isTest() {
        return isTest;
    }

    public void setTest(boolean test) {
        isTest = test;
    }

    @Override
    public String toString() {
        return "PaymentInitRes{" +
            "paymentId=" + paymentId +
            ", status=" + status +
            ", paymentUrl='" + paymentUrl + '\'' +
            ", expiresAt=" + expiresAt +
            '}';
    }
}
