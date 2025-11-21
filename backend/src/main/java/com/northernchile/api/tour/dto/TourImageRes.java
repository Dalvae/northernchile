package com.northernchile.api.tour.dto;

import java.util.UUID;

/**
 * DTO for tour images - mapped from Media + TourMedia relationship
 */
public class TourImageRes {
    private UUID id; // Media ID
    private String imageUrl;
    private Boolean isHeroImage;
    private Boolean isFeatured;
    private Integer displayOrder;

    public TourImageRes() {
    }

    public TourImageRes(UUID id, String imageUrl, Boolean isHeroImage, Boolean isFeatured, Integer displayOrder) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.isHeroImage = isHeroImage;
        this.isFeatured = isFeatured;
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

    public Boolean getIsHeroImage() {
        return isHeroImage;
    }

    public void setIsHeroImage(Boolean isHeroImage) {
        this.isHeroImage = isHeroImage;
    }

    public Boolean getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Boolean isFeatured) {
        this.isFeatured = isFeatured;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
