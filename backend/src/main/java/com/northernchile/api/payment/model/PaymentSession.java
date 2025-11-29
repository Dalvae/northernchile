package com.northernchile.api.payment.model;

import com.northernchile.api.model.User;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * PaymentSession stores checkout data temporarily until payment is completed.
 * Only after successful payment are actual Booking records created.
 */
@Entity
@Table(name = "payment_sessions", indexes = {
    @Index(name = "idx_payment_session_user", columnList = "user_id"),
    @Index(name = "idx_payment_session_status", columnList = "status"),
    @Index(name = "idx_payment_session_token", columnList = "token"),
    @Index(name = "idx_payment_session_expires", columnList = "expires_at")
})
public class PaymentSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentSessionStatus status = PaymentSessionStatus.PENDING;

    /**
     * Snapshot of cart items with participant data (stored as JSON)
     */
    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<PaymentSessionItem> items;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 3)
    private String currency = "CLP";

    @Column(name = "language_code", nullable = false, length = 5)
    private String languageCode = "es";

    @Column(name = "user_email", length = 255)
    private String userEmail;

    // Payment provider details
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "external_payment_id", length = 255)
    private String externalPaymentId;

    @Column(length = 500)
    private String token;

    @Column(name = "payment_url", columnDefinition = "TEXT")
    private String paymentUrl;

    @Column(name = "qr_code", columnDefinition = "TEXT")
    private String qrCode;

    @Column(name = "pix_code", columnDefinition = "TEXT")
    private String pixCode;

    @Column(name = "return_url", columnDefinition = "TEXT")
    private String returnUrl;

    @Column(name = "cancel_url", columnDefinition = "TEXT")
    private String cancelUrl;

    /**
     * Raw response from the payment provider
     */
    @Type(JsonType.class)
    @Column(name = "provider_response", columnDefinition = "jsonb")
    private Object providerResponse;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "is_test", nullable = false)
    private boolean isTest = false;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public PaymentSession() {
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PaymentSessionStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentSessionStatus status) {
        this.status = status;
    }

    public List<PaymentSessionItem> getItems() {
        return items;
    }

    public void setItems(List<PaymentSessionItem> items) {
        this.items = items;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
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

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }

    public Object getProviderResponse() {
        return providerResponse;
    }

    public void setProviderResponse(Object providerResponse) {
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

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
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
        PaymentSession that = (PaymentSession) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PaymentSession{" +
            "id=" + id +
            ", status=" + status +
            ", totalAmount=" + totalAmount +
            ", currency='" + currency + '\'' +
            ", provider=" + provider +
            ", paymentMethod=" + paymentMethod +
            ", expiresAt=" + expiresAt +
            '}';
    }
}
