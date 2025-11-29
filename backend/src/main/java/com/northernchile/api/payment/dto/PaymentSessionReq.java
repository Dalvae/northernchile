package com.northernchile.api.payment.dto;

import com.northernchile.api.payment.model.PaymentMethod;
import com.northernchile.api.payment.model.PaymentProvider;
import com.northernchile.api.validation.ValidReturnUrl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Request to create a payment session.
 * Contains all checkout data: cart items with participants.
 */
public class PaymentSessionReq {

    @NotNull(message = "Payment provider is required")
    private PaymentProvider provider;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    private BigDecimal totalAmount;

    private String currency = "CLP";

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<PaymentSessionItemReq> items;

    @ValidReturnUrl(nullable = true)
    private String returnUrl;

    @ValidReturnUrl(nullable = true)
    private String cancelUrl;

    private String userEmail;

    private String description;

    private String languageCode = "es";

    // Getters and Setters

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

    public List<PaymentSessionItemReq> getItems() {
        return items;
    }

    public void setItems(List<PaymentSessionItemReq> items) {
        this.items = items;
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

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    /**
     * Single item in the payment session (one tour booking)
     */
    public static class PaymentSessionItemReq {

        @NotNull(message = "Schedule ID is required")
        private UUID scheduleId;

        private String tourName;

        private LocalDate tourDate;

        @Positive(message = "Number of participants must be positive")
        private int numParticipants;

        private BigDecimal pricePerPerson;

        private BigDecimal itemTotal;

        private String specialRequests;

        @NotEmpty(message = "Participants are required")
        @Valid
        private List<ParticipantReq> participants;

        // Getters and Setters

        public UUID getScheduleId() {
            return scheduleId;
        }

        public void setScheduleId(UUID scheduleId) {
            this.scheduleId = scheduleId;
        }

        public String getTourName() {
            return tourName;
        }

        public void setTourName(String tourName) {
            this.tourName = tourName;
        }

        public LocalDate getTourDate() {
            return tourDate;
        }

        public void setTourDate(LocalDate tourDate) {
            this.tourDate = tourDate;
        }

        public int getNumParticipants() {
            return numParticipants;
        }

        public void setNumParticipants(int numParticipants) {
            this.numParticipants = numParticipants;
        }

        public BigDecimal getPricePerPerson() {
            return pricePerPerson;
        }

        public void setPricePerPerson(BigDecimal pricePerPerson) {
            this.pricePerPerson = pricePerPerson;
        }

        public BigDecimal getItemTotal() {
            return itemTotal;
        }

        public void setItemTotal(BigDecimal itemTotal) {
            this.itemTotal = itemTotal;
        }

        public String getSpecialRequests() {
            return specialRequests;
        }

        public void setSpecialRequests(String specialRequests) {
            this.specialRequests = specialRequests;
        }

        public List<ParticipantReq> getParticipants() {
            return participants;
        }

        public void setParticipants(List<ParticipantReq> participants) {
            this.participants = participants;
        }
    }

    /**
     * Participant data in the request
     */
    public static class ParticipantReq {

        @NotNull(message = "Full name is required")
        private String fullName;

        @NotNull(message = "Document ID is required")
        private String documentId;

        private String nationality;

        private LocalDate dateOfBirth;

        private String pickupAddress;

        private String specialRequirements;

        private String phoneNumber;

        private String email;

        // Getters and Setters

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getDocumentId() {
            return documentId;
        }

        public void setDocumentId(String documentId) {
            this.documentId = documentId;
        }

        public String getNationality() {
            return nationality;
        }

        public void setNationality(String nationality) {
            this.nationality = nationality;
        }

        public LocalDate getDateOfBirth() {
            return dateOfBirth;
        }

        public void setDateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
        }

        public String getPickupAddress() {
            return pickupAddress;
        }

        public void setPickupAddress(String pickupAddress) {
            this.pickupAddress = pickupAddress;
        }

        public String getSpecialRequirements() {
            return specialRequirements;
        }

        public void setSpecialRequirements(String specialRequirements) {
            this.specialRequirements = specialRequirements;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
