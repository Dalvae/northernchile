
package com.northernchile.api.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BookingClientUpdateReq {
    private String specialRequests;

    @NotNull(message = "Participants list is required")
    @Valid
    private List<ParticipantUpdateReq> participants;

    public BookingClientUpdateReq() {
    }

    public BookingClientUpdateReq(String specialRequests, List<ParticipantUpdateReq> participants) {
        this.specialRequests = specialRequests;
        this.participants = participants;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public List<ParticipantUpdateReq> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ParticipantUpdateReq> participants) {
        this.participants = participants;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookingClientUpdateReq that = (BookingClientUpdateReq) o;
        return Objects.equals(specialRequests, that.specialRequests) && Objects.equals(participants, that.participants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(specialRequests, participants);
    }

    @Override
    public String toString() {
        return "BookingClientUpdateReq{" +
                "specialRequests='" + specialRequests + '\'' +
                ", participants=" + participants +
                '}';
    }

    public static class ParticipantUpdateReq {
        @NotNull(message = "Participant ID is required")
        private UUID id;
        private String pickupAddress;
        private String specialRequirements;
        private String phoneNumber;
        private String email;

        public ParticipantUpdateReq() {
        }

        public ParticipantUpdateReq(UUID id, String pickupAddress, String specialRequirements, String phoneNumber, String email) {
            this.id = id;
            this.pickupAddress = pickupAddress;
            this.specialRequirements = specialRequirements;
            this.phoneNumber = phoneNumber;
            this.email = email;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ParticipantUpdateReq that = (ParticipantUpdateReq) o;
            return Objects.equals(id, that.id) && Objects.equals(pickupAddress, that.pickupAddress) && Objects.equals(specialRequirements, that.specialRequirements) && Objects.equals(phoneNumber, that.phoneNumber) && Objects.equals(email, that.email);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, pickupAddress, specialRequirements, phoneNumber, email);
        }

        @Override
        public String toString() {
            return "ParticipantUpdateReq{" +
                    "id=" + id +
                    ", pickupAddress='" + pickupAddress + '\'' +
                    ", specialRequirements='" + specialRequirements + '\'' +
                    ", phoneNumber='" + phoneNumber + '\'' +
                    ", email='" + email + '\'' +
                    '}';
        }
    }
}
