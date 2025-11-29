package com.northernchile.api.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "private_tour_requests")
public class PrivateTourRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "requested_tour_type", nullable = false)
    private String requestedTourType;

    @Column(name = "requested_start_datetime")
    private LocalDate requestedDatetime;

    @Column(name = "num_participants", nullable = false)
    private int numParticipants;

    @Column(columnDefinition = "TEXT")
    private String specialRequests;

    private String status = "PENDING";
    private BigDecimal quotedPrice;
    private String paymentLinkId;

    @CreationTimestamp
    private Instant createdAt;

    public PrivateTourRequest() {
    }

    public PrivateTourRequest(UUID id, User user, String customerName, String customerEmail, String customerPhone, String requestedTourType, LocalDate requestedDatetime, int numParticipants, String specialRequests, String status, BigDecimal quotedPrice, String paymentLinkId, Instant createdAt) {
        this.id = id;
        this.user = user;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.requestedTourType = requestedTourType;
        this.requestedDatetime = requestedDatetime;
        this.numParticipants = numParticipants;
        this.specialRequests = specialRequests;
        this.status = status;
        this.quotedPrice = quotedPrice;
        this.paymentLinkId = paymentLinkId;
        this.createdAt = createdAt;
    }

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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getRequestedTourType() {
        return requestedTourType;
    }

    public void setRequestedTourType(String requestedTourType) {
        this.requestedTourType = requestedTourType;
    }

    public LocalDate getRequestedDatetime() {
        return requestedDatetime;
    }

    public void setRequestedDatetime(LocalDate requestedDatetime) {
        this.requestedDatetime = requestedDatetime;
    }

    public int getNumParticipants() {
        return numParticipants;
    }

    public void setNumParticipants(int numParticipants) {
        this.numParticipants = numParticipants;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getQuotedPrice() {
        return quotedPrice;
    }

    public void setQuotedPrice(BigDecimal quotedPrice) {
        this.quotedPrice = quotedPrice;
    }

    public String getPaymentLinkId() {
        return paymentLinkId;
    }

    public void setPaymentLinkId(String paymentLinkId) {
        this.paymentLinkId = paymentLinkId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivateTourRequest that = (PrivateTourRequest) o;
        return numParticipants == that.numParticipants &&
            Objects.equals(id, that.id) &&
            Objects.equals(user, that.user) &&
            Objects.equals(customerName, that.customerName) &&
            Objects.equals(customerEmail, that.customerEmail) &&
            Objects.equals(customerPhone, that.customerPhone) &&
            Objects.equals(requestedTourType, that.requestedTourType) &&
            Objects.equals(requestedDatetime, that.requestedDatetime) &&
            Objects.equals(specialRequests, that.specialRequests) &&
            Objects.equals(status, that.status) &&
            Objects.equals(quotedPrice, that.quotedPrice) &&
            Objects.equals(paymentLinkId, that.paymentLinkId) &&
            Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, customerName, customerEmail, customerPhone, requestedTourType, requestedDatetime, numParticipants, specialRequests, status, quotedPrice, paymentLinkId, createdAt);
    }

    @Override
    public String toString() {
        return "PrivateTourRequest{" +
            "id=" + id +
            ", user=" + user +
            ", customerName='" + customerName + '\'' +
            ", customerEmail='" + customerEmail + '\'' +
            ", customerPhone='" + customerPhone + '\'' +
            ", requestedTourType='" + requestedTourType + '\'' +
            ", requestedDatetime=" + requestedDatetime +
            ", numParticipants=" + numParticipants +
            ", specialRequests='" + specialRequests + '\'' +
            ", status='" + status + '\'' +
            ", quotedPrice=" + quotedPrice +
            ", paymentLinkId='" + paymentLinkId + '\'' +
            ", createdAt=" + createdAt +
            '}';
    }
}
