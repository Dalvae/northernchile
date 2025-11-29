package com.northernchile.api.payment.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Represents a single item in a payment session (one tour booking).
 * This is stored as JSON in the payment_sessions.items column.
 */
public class PaymentSessionItem implements Serializable {

    private UUID scheduleId;
    private String tourName;
    private LocalDate tourDate;
    private int numParticipants;
    private BigDecimal pricePerPerson;
    private BigDecimal itemTotal;
    private String specialRequests;
    private List<ParticipantData> participants;

    public PaymentSessionItem() {
    }

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

    public List<ParticipantData> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ParticipantData> participants) {
        this.participants = participants;
    }

    /**
     * Participant data snapshot stored in the payment session.
     */
    public static class ParticipantData implements Serializable {
        private String fullName;
        private String documentId;
        private String nationality;
        private LocalDate dateOfBirth;
        private String pickupAddress;
        private String specialRequirements;
        private String phoneNumber;
        private String email;

        public ParticipantData() {
        }

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
