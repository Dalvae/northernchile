package com.northernchile.api.model;

import com.northernchile.api.audit.AuditableEntity;
import com.northernchile.api.audit.AuditEntityListener;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@EntityListeners(AuditEntityListener.class)
public class Booking implements AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private TourSchedule schedule;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    private List<Participant> participants;

    @Column(name = "tour_date", nullable = false)
    private LocalDate tourDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal taxAmount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "language_code", nullable = false, length = 5)
    private String languageCode = "es";

    @Column(columnDefinition = "TEXT")
    private String specialRequests;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "reminder_sent_at")
    private Instant reminderSentAt;

    public Booking() {
    }

    public Booking(UUID id, User user, TourSchedule schedule, List<Participant> participants, LocalDate tourDate, BookingStatus status, BigDecimal subtotal, BigDecimal taxAmount, BigDecimal totalAmount, String languageCode, String specialRequests, Instant createdAt, Instant updatedAt, Instant deletedAt, Instant reminderSentAt) {
        this.id = id;
        this.user = user;
        this.schedule = schedule;
        this.participants = participants;
        this.tourDate = tourDate;
        this.status = status;
        this.subtotal = subtotal;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.languageCode = languageCode;
        this.specialRequests = specialRequests;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.reminderSentAt = reminderSentAt;
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

    public TourSchedule getSchedule() {
        return schedule;
    }

    public void setSchedule(TourSchedule schedule) {
        this.schedule = schedule;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public LocalDate getTourDate() {
        return tourDate;
    }

    public void setTourDate(LocalDate tourDate) {
        this.tourDate = tourDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
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

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Instant getReminderSentAt() {
        return reminderSentAt;
    }

    public void setReminderSentAt(Instant reminderSentAt) {
        this.reminderSentAt = reminderSentAt;
    }

    /**
     * Equals based only on ID to avoid Hibernate proxy issues with lazy-loaded relations.
     * See: https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;
        Booking booking = (Booking) o;
        return id != null && id.equals(booking.getId());
    }

    /**
     * HashCode returns class hashCode for consistent behavior with proxies.
     * This ensures entities work correctly in HashSet/HashMap before and after persistence.
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Booking{" +
            "id=" + id +
            ", user=" + user +
            ", schedule=" + schedule +
            ", participants=" + participants +
            ", tourDate=" + tourDate +
            ", status='" + status + '\'' +
            ", subtotal=" + subtotal +
            ", taxAmount=" + taxAmount +
            ", totalAmount=" + totalAmount +
            ", languageCode='" + languageCode + '\'' +
            ", specialRequests='" + specialRequests + '\'' +
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            ", deletedAt=" + deletedAt +
            ", reminderSentAt=" + reminderSentAt +
            '}';
    }

    // ==================== AuditableEntity Implementation ====================

    @Override
    public String getAuditDescription() {
        String tourName = schedule != null && schedule.getTour() != null
            ? schedule.getTour().getDisplayName()
            : "Unknown Tour";
        String userName = user != null ? user.getFullName() : "Unknown User";
        return tourName + " - " + userName;
    }

    @Override
    public String getAuditEntityType() {
        return "BOOKING";
    }

    @Override
    public Map<String, Object> getAuditSnapshot() {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("status", status);
        snapshot.put("totalAmount", totalAmount);
        snapshot.put("participantCount", participants != null ? participants.size() : 0);
        return snapshot;
    }
}
