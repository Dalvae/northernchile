
package com.northernchile.api.tour.dto;

import java.math.BigDecimal;
import java.util.Objects;

public class TourCreateReq {
    private String name;
    private String description;
    private String category;
    private BigDecimal priceAdult;
    private BigDecimal priceChild;
    private Integer defaultMaxParticipants;
    private Integer durationHours;

    public TourCreateReq() {
    }

    public TourCreateReq(String name, String description, String category, BigDecimal priceAdult, BigDecimal priceChild, Integer defaultMaxParticipants, Integer durationHours) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.priceAdult = priceAdult;
        this.priceChild = priceChild;
        this.defaultMaxParticipants = defaultMaxParticipants;
        this.durationHours = durationHours;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TourCreateReq that = (TourCreateReq) o;
        return Objects.equals(name, that.name) && Objects.equals(description, that.description) && Objects.equals(category, that.category) && Objects.equals(priceAdult, that.priceAdult) && Objects.equals(priceChild, that.priceChild) && Objects.equals(defaultMaxParticipants, that.defaultMaxParticipants) && Objects.equals(durationHours, that.durationHours);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, category, priceAdult, priceChild, defaultMaxParticipants, durationHours);
    }

    @Override
    public String toString() {
        return "TourCreateReq{" +
                "name='" + name + ''' +
                ", description='" + description + ''' +
                ", category='" + category + ''' +
                ", priceAdult=" + priceAdult +
                ", priceChild=" + priceChild +
                ", defaultMaxParticipants=" + defaultMaxParticipants +
                ", durationHours=" + durationHours +
                '}';
    }
}
