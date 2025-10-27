
package com.northernchile.api.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "tours")
public class Tour {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> nameTranslations;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> descriptionTranslations;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TourImage> images;

    private boolean isWindSensitive;
    private boolean isMoonSensitive;
    private boolean isCloudSensitive; // NUEVO CAMPO

    @Column(unique = true, length = 100)
    private String contentKey;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAdult;

    @Column(precision = 10, scale = 2)
    private BigDecimal priceChild;

    @Column(nullable = false)
    private Integer defaultMaxParticipants;

    @Column(nullable = false)
    private Integer durationHours;

    private Boolean isRecurring = false;

    @Column(length = 100)
    private String recurrenceRule;

    @Column(length = 20)
    private String status = "DRAFT";

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public Tour() {
    }

    public Tour(UUID id, User owner, Map<String, String> nameTranslations, Map<String, String> descriptionTranslations, List<TourImage> images, boolean isWindSensitive, boolean isMoonSensitive, boolean isCloudSensitive, String contentKey, String category, BigDecimal priceAdult, BigDecimal priceChild, Integer defaultMaxParticipants, Integer durationHours, Boolean isRecurring, String recurrenceRule, String status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.owner = owner;
        this.nameTranslations = nameTranslations;
        this.descriptionTranslations = descriptionTranslations;
        this.images = images;
        this.isWindSensitive = isWindSensitive;
        this.isMoonSensitive = isMoonSensitive;
        this.isCloudSensitive = isCloudSensitive;
        this.contentKey = contentKey;
        this.category = category;
        this.priceAdult = priceAdult;
        this.priceChild = priceChild;
        this.defaultMaxParticipants = defaultMaxParticipants;
        this.durationHours = durationHours;
        this.isRecurring = isRecurring;
        this.recurrenceRule = recurrenceRule;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Map<String, String> getNameTranslations() {
        return nameTranslations;
    }

    public void setNameTranslations(Map<String, String> nameTranslations) {
        this.nameTranslations = nameTranslations;
    }

    public Map<String, String> getDescriptionTranslations() {
        return descriptionTranslations;
    }

    public void setDescriptionTranslations(Map<String, String> descriptionTranslations) {
        this.descriptionTranslations = descriptionTranslations;
    }

    public List<TourImage> getImages() {
        return images;
    }

    public void setImages(List<TourImage> images) {
        this.images = images;
    }

    public boolean isWindSensitive() {
        return isWindSensitive;
    }

    public void setWindSensitive(boolean windSensitive) {
        isWindSensitive = windSensitive;
    }

    public boolean isMoonSensitive() {
        return isMoonSensitive;
    }

    public void setMoonSensitive(boolean moonSensitive) {
        isMoonSensitive = moonSensitive;
    }

    public boolean isCloudSensitive() {
        return isCloudSensitive;
    }

    public void setCloudSensitive(boolean cloudSensitive) {
        isCloudSensitive = cloudSensitive;
    }

    public String getContentKey() {
        return contentKey;
    }

    public void setContentKey(String contentKey) {
        this.contentKey = contentKey;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPriceAdult() {
        return priceAdult;
    }

    public void setPriceAdult(BigDecimal priceAdult) {
        this.priceAdult = priceAdult;
    }

    public BigDecimal getPriceChild() {
        return priceChild;
    }

    public void setPriceChild(BigDecimal priceChild) {
        this.priceChild = priceChild;
    }

    public Integer getDefaultMaxParticipants() {
        return defaultMaxParticipants;
    }

    public void setDefaultMaxParticipants(Integer defaultMaxParticipants) {
        this.defaultMaxParticipants = defaultMaxParticipants;
    }

    public Integer getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(Integer durationHours) {
        this.durationHours = durationHours;
    }

    public Boolean getRecurring() {
        return isRecurring;
    }

    public void setRecurring(Boolean recurring) {
        isRecurring = recurring;
    }

    public String getRecurrenceRule() {
        return recurrenceRule;
    }

    public void setRecurrenceRule(String recurrenceRule) {
        this.recurrenceRule = recurrenceRule;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
        Tour tour = (Tour) o;
        return isWindSensitive == tour.isWindSensitive && isMoonSensitive == tour.isMoonSensitive && isCloudSensitive == tour.isCloudSensitive && Objects.equals(id, tour.id) && Objects.equals(owner, tour.owner) && Objects.equals(nameTranslations, tour.nameTranslations) && Objects.equals(descriptionTranslations, tour.descriptionTranslations) && Objects.equals(images, tour.images) && Objects.equals(contentKey, tour.contentKey) && Objects.equals(category, tour.category) && Objects.equals(priceAdult, tour.priceAdult) && Objects.equals(priceChild, tour.priceChild) && Objects.equals(defaultMaxParticipants, tour.defaultMaxParticipants) && Objects.equals(durationHours, tour.durationHours) && Objects.equals(isRecurring, tour.isRecurring) && Objects.equals(recurrenceRule, tour.recurrenceRule) && Objects.equals(status, tour.status) && Objects.equals(createdAt, tour.createdAt) && Objects.equals(updatedAt, tour.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, owner, nameTranslations, descriptionTranslations, images, isWindSensitive, isMoonSensitive, isCloudSensitive, contentKey, category, priceAdult, priceChild, defaultMaxParticipants, durationHours, isRecurring, recurrenceRule, status, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "Tour{" +
                "id=" + id +
                ", owner=" + owner +
                ", nameTranslations=" + nameTranslations +
                ", descriptionTranslations=" + descriptionTranslations +
                ", images=" + images +
                ", isWindSensitive=" + isWindSensitive +
                ", isMoonSensitive=" + isMoonSensitive +
                ", isCloudSensitive=" + isCloudSensitive +
                ", contentKey='" + contentKey + '\'' +
                ", category='" + category +
                ", priceAdult=" + priceAdult +
                ", priceChild=" + priceChild +
                ", defaultMaxParticipants=" + defaultMaxParticipants +
                ", durationHours=" + durationHours +
                ", isRecurring=" + isRecurring +
                ", recurrenceRule='" + recurrenceRule + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
