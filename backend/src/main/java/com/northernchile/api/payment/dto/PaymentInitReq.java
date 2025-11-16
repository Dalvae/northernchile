package com.northernchile.api.payment.dto;

import com.northernchile.api.payment.model.PaymentMethod;
import com.northernchile.api.payment.model.PaymentProvider;
import com.northernchile.api.validation.ValidReturnUrl;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Payment initialization request DTO.
 * Used to create a new payment transaction.
 */
public class PaymentInitReq {

    @NotNull(message = "Booking ID is required")
    private UUID bookingId;

    @NotNull(message = "Payment provider is required")
    private PaymentProvider provider;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String currency = "CLP";

    /**
     * Return URL after payment completion (for redirect-based flows like Webpay)
     */
    @ValidReturnUrl(nullable = true)
    private String returnUrl;

    /**
     * Cancel URL if user cancels payment
     */
    @ValidReturnUrl(nullable = true)
    private String cancelUrl;

    /**
     * User's email for payment notifications
     */
    private String userEmail;

    /**
     * Description of the payment (e.g., "Tour booking #123")
     */
    private String description;

    /**
     * Payment expiration time in minutes (for PIX, etc.)
     */
    private Integer expirationMinutes;

    /**
     * Additional booking IDs to be confirmed with this payment (for multi-item cart checkout)
     */
    private List<UUID> additionalBookingIds;

    public PaymentInitReq() {
    }

    public PaymentInitReq(UUID bookingId, PaymentProvider provider, PaymentMethod paymentMethod,
                          BigDecimal amount, String currency, String returnUrl, String cancelUrl,
                          String userEmail, String description, Integer expirationMinutes) {
        this.bookingId = bookingId;
        this.provider = provider;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.currency = currency;
        this.returnUrl = returnUrl;
        this.cancelUrl = cancelUrl;
        this.userEmail = userEmail;
        this.description = description;
        this.expirationMinutes = expirationMinutes;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
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

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getExpirationMinutes() {
        return expirationMinutes;
    }

    public void setExpirationMinutes(Integer expirationMinutes) {
        this.expirationMinutes = expirationMinutes;
    }

    public List<UUID> getAdditionalBookingIds() {
        return additionalBookingIds;
    }

    public void setAdditionalBookingIds(List<UUID> additionalBookingIds) {
        this.additionalBookingIds = additionalBookingIds;
    }

    @Override
    public String toString() {
        return "PaymentInitReq{" +
            "bookingId=" + bookingId +
            ", provider=" + provider +
            ", paymentMethod=" + paymentMethod +
            ", amount=" + amount +
            ", currency='" + currency + '\'' +
            ", description='" + description + '\'' +
            '}';
    }
}
