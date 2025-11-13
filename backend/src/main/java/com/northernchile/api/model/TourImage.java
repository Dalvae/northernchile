package com.northernchile.api.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "tour_images")
public class TourImage {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id")
    private Tour tour;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String imageUrl;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> altTextTranslations;
    private boolean heroImage;
    private int displayOrder;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Tour getTour() {
        return tour;
    }

    public void setTour(Tour tour) {
        this.tour = tour;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Map<String, String> getAltTextTranslations() {
        return altTextTranslations;
    }

    public void setAltTextTranslations(Map<String, String> altTextTranslations) {
        this.altTextTranslations = altTextTranslations;
    }

    public boolean isHeroImage() {
        return heroImage;
    }

    public void setHeroImage(boolean heroImage) {
        this.heroImage = heroImage;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }
}