
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

    @Column(nullable = false, length = 20)
    private String type; // ADULT o CHILD

    @CreationTimestamp
    private Instant createdAt;

    public Participant() {
    }

    public Participant(UUID id, Booking booking, String fullName, String type, Instant createdAt) {
        this.id = id;
        this.booking = booking;
        this.fullName = fullName;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
        return Objects.equals(id, that.id) && Objects.equals(booking, that.booking) && Objects.equals(fullName, that.fullName) && Objects.equals(type, that.type) && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, booking, fullName, type, createdAt);
    }

    @Override
    public String toString() {
        return "Participant{" +
                "id=" + id +
                ", booking=" + booking +
                ", fullName='" + fullName + ''' +
                ", type='" + type + ''' +
                ", createdAt=" + createdAt +
                '}';
    }
}
