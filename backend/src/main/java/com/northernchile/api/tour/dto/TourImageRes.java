package com.northernchile.api.tour.dto;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class TourImageRes {
    private UUID id;
    private String imageUrl;
    private Map<String, String> altTextTranslations;
    private boolean isHeroImage;
    private int displayOrder;

    public TourImageRes() {
    }

    public TourImageRes(UUID id, String imageUrl, Map<String, String> altTextTranslations, boolean isHeroImage, int displayOrder) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.altTextTranslations = altTextTranslations;
        this.isHeroImage = isHeroImage;
        this.displayOrder = displayOrder;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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
        return isHeroImage;
    }

    public void setHeroImage(boolean heroImage) {
        isHeroImage = heroImage;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TourImageRes that = (TourImageRes) o;
        return isHeroImage == that.isHeroImage && displayOrder == that.displayOrder && Objects.equals(id, that.id) && Objects.equals(imageUrl, that.imageUrl) && Objects.equals(altTextTranslations, that.altTextTranslations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, imageUrl, altTextTranslations, isHeroImage, displayOrder);
    }

    @Override
    public String toString() {
        return "TourImageRes{"
                + "id=" + id + 
                ", imageUrl='" + imageUrl + "'"
                + ", altTextTranslations=" + altTextTranslations + 
                ", isHeroImage=" + isHeroImage + 
                ", displayOrder=" + displayOrder + 
                '}';
    }
}