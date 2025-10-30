
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

    private Integer age;

    @Column(name = "pickup_address", length = 500)
    private String pickupAddress;

    @Column(name = "special_requirements", columnDefinition = "TEXT")
    private String specialRequirements;

    @CreationTimestamp
    private Instant createdAt;

    public Participant() {
    }

    public Participant(UUID id, Booking booking, String fullName, String documentId, String nationality, Integer age, String pickupAddress, String specialRequirements, Instant createdAt) {
        this.id = id;
        this.booking = booking;
        this.fullName = fullName;
        this.documentId = documentId;
        this.nationality = nationality;
        this.age = age;
        this.pickupAddress = pickupAddress;
        this.specialRequirements = specialRequirements;
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
        Participant that = (Participant) o;
        return Objects.equals(id, that.id) && Objects.equals(booking, that.booking) && Objects.equals(fullName, that.fullName) && Objects.equals(documentId, that.documentId) && Objects.equals(nationality, that.nationality) && Objects.equals(age, that.age) && Objects.equals(pickupAddress, that.pickupAddress) && Objects.equals(specialRequirements, that.specialRequirements) && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, booking, fullName, documentId, nationality, age, pickupAddress, specialRequirements, createdAt);
    }

    @Override
    public String toString() {
        return "Participant{" +
                "id=" + id +
                ", booking=" + booking +
                ", fullName='" + fullName + '\'' +
                ", documentId='" + documentId + '\'' +
                ", nationality='" + nationality + '\'' +
                ", age=" + age +
                ", pickupAddress='" + pickupAddress + '\'' +
                ", specialRequirements='" + specialRequirements + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
