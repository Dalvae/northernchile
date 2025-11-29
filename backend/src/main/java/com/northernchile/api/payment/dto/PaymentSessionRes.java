package com.northernchile.api.payment.dto;

import com.northernchile.api.payment.model.PaymentSessionStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response after creating a payment session.
 */
public class PaymentSessionRes {

    private UUID sessionId;
    private PaymentSessionStatus status;
    private String paymentUrl;
    private String token;
    private String qrCode;
    private String pixCode;
    private Instant expiresAt;
    private boolean isTest;

    /**
     * Booking IDs created after successful payment (only populated after confirmation)
     */
    private List<UUID> bookingIds;

    public PaymentSessionRes() {
    }

    // Getters and Setters

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public PaymentSessionStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentSessionStatus status) {
        this.status = status;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isTest() {
        return isTest;
    }

    public void setTest(boolean test) {
        isTest = test;
    }

    public List<UUID> getBookingIds() {
        return bookingIds;
    }

    public void setBookingIds(List<UUID> bookingIds) {
        this.bookingIds = bookingIds;
    }
}
