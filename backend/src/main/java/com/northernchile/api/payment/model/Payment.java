package com.northernchile.api.payment.model;

import com.northernchile.api.model.Booking;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Payment entity.
 * Stores payment transaction information for bookings.
 */
@Entity
@Table(name = "payments", indexes = {
    @Index(name = "idx_payment_booking", columnList = "booking_id"),
    @Index(name = "idx_payment_external_id", columnList = "external_payment_id"),
    @Index(name = "idx_payment_status", columnList = "status")
})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "external_payment_id", length = 255)
    private String externalPaymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "CLP";

    /**
     * URL for the user to complete the payment (e.g., Webpay redirect URL)
     */
    @Column(name = "payment_url", columnDefinition = "TEXT")
    private String paymentUrl;

    /**
     * URL for the user to view payment details (e.g., PIX QR code page)
     */
    @Column(name = "details_url", columnDefinition = "TEXT")
    private String detailsUrl;

    /**
     * QR code data for PIX payments (base64 or URL)
     */
    @Column(name = "qr_code", columnDefinition = "TEXT")
    private String qrCode;

    /**
     * PIX copy-paste code (for manual payment)
     */
    @Column(name = "pix_code", columnDefinition = "TEXT")
    private String pixCode;

    /**
     * Token provided by the payment gateway (e.g., Webpay token, Mercado Pago preference ID)
     */
    @Column(length = 500)
    private String token;

    /**
     * Expiration date for the payment (e.g., PIX QR code expiration)
     */
    @Column(name = "expires_at")
    private Instant expiresAt;

    /**
     * Raw response from the payment provider (stored as JSON)
     */
    @Type(JsonType.class)
    @Column(name = "provider_response", columnDefinition = "jsonb")
    private Map<String, Object> providerResponse;

    /**
     * Error message if payment failed
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Flag indicating if this is a test payment (using TEST credentials).
     * Test payments are excluded from financial reports and accounting.
     */
    @Column(name = "is_test", nullable = false)
    private boolean isTest = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public Payment() {
    }

    public Payment(UUID id, Booking booking, PaymentProvider provider, PaymentMethod paymentMethod,
                   String externalPaymentId, PaymentStatus status, BigDecimal amount, String currency,
                   String paymentUrl, String detailsUrl, String qrCode, String pixCode, String token,
                   Instant expiresAt, Map<String, Object> providerResponse, String errorMessage,
                   boolean isTest, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.booking = booking;
        this.provider = provider;
        this.paymentMethod = paymentMethod;
        this.externalPaymentId = externalPaymentId;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
        this.paymentUrl = paymentUrl;
        this.detailsUrl = detailsUrl;
        this.qrCode = qrCode;
        this.pixCode = pixCode;
        this.token = token;
        this.expiresAt = expiresAt;
        this.providerResponse = providerResponse;
        this.errorMessage = errorMessage;
        this.isTest = isTest;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public PaymentProvider getProvider() {
        return provider;
    }

    public void setProvider(PaymentProvider provider) {
        this.provider = provider;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
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

    public Map<String, Object> getProviderResponse() {
        return providerResponse;
    }

    public void setProviderResponse(Map<String, Object> providerResponse) {
        this.providerResponse = providerResponse;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isTest() {
        return isTest;
    }

    public void setTest(boolean test) {
        isTest = test;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(id, payment.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Payment{" +
            "id=" + id +
            ", provider=" + provider +
            ", paymentMethod=" + paymentMethod +
            ", externalPaymentId='" + externalPaymentId + '\'' +
            ", status=" + status +
            ", amount=" + amount +
            ", currency='" + currency + '\'' +
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            '}';
    }
}
