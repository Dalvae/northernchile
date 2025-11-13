
package com.northernchile.api.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.northernchile.api.tour.dto.ContentBlock;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
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
    @Column(name = "name_translations", columnDefinition = "jsonb", nullable = false)
    private Map<String, String> nameTranslations;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "description_blocks_translations", columnDefinition = "jsonb")
    private Map<String, java.util.List<ContentBlock>> descriptionBlocksTranslations;

    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TourImage> images = new ArrayList<>();

    private boolean windSensitive;
    private boolean moonSensitive;
    private boolean cloudSensitive;

    @Column(unique = true, length = 100)
    private String contentKey;

    @Column(unique = true, length = 255, nullable = false)
    private String slug;

    // Structured content fields
    @Column(name = "guide_name", length = 255)
    private String guideName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "itinerary_translations", columnDefinition = "jsonb")
    private Map<String, Object> itineraryTranslations;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "equipment_translations", columnDefinition = "jsonb")
    private Map<String, Object> equipmentTranslations;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "additional_info_translations", columnDefinition = "jsonb")
    private Map<String, Object> additionalInfoTranslations;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer defaultMaxParticipants;

    @Column(nullable = false)
    private Integer durationHours;

    @Column(name = "default_start_time")
    private LocalTime defaultStartTime;

    private Boolean recurring = false;

    @Column(length = 100)
    private String recurrenceRule;

    @Column(length = 20, nullable = false)
    private String status = "DRAFT";

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public Tour() {
    }

    public Tour(UUID id, User owner, Map<String, String> nameTranslations, List<TourImage> images, boolean windSensitive, boolean moonSensitive, boolean cloudSensitive, String contentKey, String category, BigDecimal price, Integer defaultMaxParticipants, Integer durationHours, LocalTime defaultStartTime, Boolean recurring, String recurrenceRule, String status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.owner = owner;
        this.nameTranslations = nameTranslations;
        this.images = images;
        this.windSensitive = windSensitive;
        this.moonSensitive = moonSensitive;
        this.cloudSensitive = cloudSensitive;
        this.contentKey = contentKey;
        this.category = category;
        this.price = price;
        this.defaultMaxParticipants = defaultMaxParticipants;
        this.durationHours = durationHours;
        this.defaultStartTime = defaultStartTime;
        this.recurring = recurring;
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

    /**
     * Helper method to get the tour name in Spanish (default language).
     * If Spanish translation is not available, returns a default fallback.
     *
     * @return Tour name in Spanish or "Tour sin nombre" as fallback
     */
    public String getDisplayName() {
        return nameTranslations != null ? nameTranslations.getOrDefault("es", "Tour sin nombre") : "Tour sin nombre";
    }


    public List<TourImage> getImages() {
        return images;
    }

    public void setImages(List<TourImage> images) {
        this.images = images;
    }

    public boolean isWindSensitive() {
        return windSensitive;
    }

    public void setWindSensitive(boolean windSensitive) {
        this.windSensitive = windSensitive;
    }

    public boolean isMoonSensitive() {
        return moonSensitive;
    }

    public void setMoonSensitive(boolean moonSensitive) {
        this.moonSensitive = moonSensitive;
    }

    public boolean isCloudSensitive() {
        return cloudSensitive;
    }

    public void setCloudSensitive(boolean cloudSensitive) {
        this.cloudSensitive = cloudSensitive;
    }

    public String getContentKey() {
        return contentKey;
    }

    public void setContentKey(String contentKey) {
        this.contentKey = contentKey;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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

    public LocalTime getDefaultStartTime() {
        return defaultStartTime;
    }

    public void setDefaultStartTime(LocalTime defaultStartTime) {
        this.defaultStartTime = defaultStartTime;
    }

    public Boolean getRecurring() {
        return recurring;
    }

    public void setRecurring(Boolean recurring) {
        this.recurring = recurring;
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

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public String getGuideName() {
        return guideName;
    }

    public void setGuideName(String guideName) {
        this.guideName = guideName;
    }

    public Map<String, Object> getItineraryTranslations() {
        return itineraryTranslations;
    }

    public void setItineraryTranslations(Map<String, Object> itineraryTranslations) {
        this.itineraryTranslations = itineraryTranslations;
    }

    public Map<String, Object> getEquipmentTranslations() {
        return equipmentTranslations;
    }

    public void setEquipmentTranslations(Map<String, Object> equipmentTranslations) {
        this.equipmentTranslations = equipmentTranslations;
    }

    public Map<String, Object> getAdditionalInfoTranslations() {
        return additionalInfoTranslations;
    }
 
    public void setAdditionalInfoTranslations(Map<String, Object> additionalInfoTranslations) {
        this.additionalInfoTranslations = additionalInfoTranslations;
    }

    public Map<String, java.util.List<ContentBlock>> getDescriptionBlocksTranslations() {
        return descriptionBlocksTranslations;
    }

    public void setDescriptionBlocksTranslations(Map<String, java.util.List<ContentBlock>> descriptionBlocksTranslations) {
        this.descriptionBlocksTranslations = descriptionBlocksTranslations;
    }
 
    @Override

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tour tour = (Tour) o;
        return windSensitive == tour.windSensitive && moonSensitive == tour.moonSensitive && cloudSensitive == tour.cloudSensitive && Objects.equals(id, tour.id) && Objects.equals(owner, tour.owner) && Objects.equals(nameTranslations, tour.nameTranslations) && Objects.equals(images, tour.images) && Objects.equals(contentKey, tour.contentKey) && Objects.equals(category, tour.category) && Objects.equals(price, tour.price) && Objects.equals(defaultMaxParticipants, tour.defaultMaxParticipants) && Objects.equals(durationHours, tour.durationHours) && Objects.equals(defaultStartTime, tour.defaultStartTime) && Objects.equals(recurring, tour.recurring) && Objects.equals(recurrenceRule, tour.recurrenceRule) && Objects.equals(status, tour.status) && Objects.equals(createdAt, tour.createdAt) && Objects.equals(updatedAt, tour.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, owner, nameTranslations, images, windSensitive, moonSensitive, cloudSensitive, contentKey, category, price, defaultMaxParticipants, durationHours, defaultStartTime, recurring, recurrenceRule, status, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "Tour{" +
                "id=" + id +
                ", owner=" + owner +
                ", nameTranslations=" + nameTranslations +
                 ", images=" + images +

                ", windSensitive=" + windSensitive +
                ", moonSensitive=" + moonSensitive +
                ", cloudSensitive=" + cloudSensitive +
                ", contentKey='" + contentKey + '\'' +
                ", category='" + category +
                ", price=" + price +
                ", defaultMaxParticipants=" + defaultMaxParticipants +
                ", durationHours=" + durationHours +
                ", defaultStartTime=" + defaultStartTime +
                ", recurring=" + recurring +
                ", recurrenceRule='" + recurrenceRule + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
