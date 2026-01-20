
package com.northernchile.api.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "participants")
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false)
    private String fullName;

    @Column(name = "document_id", length = 100, nullable = false)
    private String documentId;

    @Column(length = 100)
    private String nationality;

    @Column(name = "date_of_birth")
    private java.time.LocalDate dateOfBirth;

    private Integer age;

    @Column(name = "pickup_address", length = 500)
    private String pickupAddress;

    @Column(name = "special_requirements", columnDefinition = "TEXT")
    private String specialRequirements;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(length = 255)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saved_participant_id")
    private SavedParticipant savedParticipant;

    @CreationTimestamp
    private Instant createdAt;

    public Participant() {
    }

    public Participant(UUID id, Booking booking, String fullName, String documentId, String nationality, java.time.LocalDate dateOfBirth, Integer age, String pickupAddress, String specialRequirements, String phoneNumber, String email, Instant createdAt) {
        this.id = id;
        this.booking = booking;
        this.fullName = fullName;
        this.documentId = documentId;
        this.nationality = nationality;
        this.dateOfBirth = dateOfBirth;
        this.age = age;
        this.pickupAddress = pickupAddress;
        this.specialRequirements = specialRequirements;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.createdAt = createdAt;
    }

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

    public java.time.LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(java.time.LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        // Auto-calculate age when dateOfBirth is set
        if (dateOfBirth != null) {
            this.age = java.time.Period.between(dateOfBirth, java.time.LocalDate.now()).getYears();
        }
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
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

    public SavedParticipant getSavedParticipant() {
        return savedParticipant;
    }

    public void setSavedParticipant(SavedParticipant savedParticipant) {
        this.savedParticipant = savedParticipant;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Equals based on id only, following JPA best practices.
     * This prevents issues with lazy loading and detached entity comparison.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participant that = (Participant) o;
        // For new entities (id is null), use identity comparison
        if (id == null) return false;
        return Objects.equals(id, that.id);
    }

    /**
     * HashCode based on id only, following JPA best practices.
     * Returns a constant for new entities to maintain hashCode/equals contract.
     */
    @Override
    public int hashCode() {
        // Use a constant for new entities to maintain consistency
        // Once persisted, the id-based hash will be used
        return id != null ? Objects.hash(id) : 31;
    }

    @Override
    public String toString() {
        return "Participant{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", documentId='" + documentId + '\'' +
                ", nationality='" + nationality + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", age=" + age +
                ", pickupAddress='" + pickupAddress + '\'' +
                ", specialRequirements='" + specialRequirements + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
