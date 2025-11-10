package com.northernchile.api.tour.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

/**
 * Represents a single item in a tour itinerary with time and description.
 * Used for structured content in tour details.
 */
public class ItineraryItem {

    @NotBlank(message = "Time is required for itinerary items")
    @JsonProperty("time")
    private String time;

    @NotBlank(message = "Description is required for itinerary items")
    @JsonProperty("description")
    private String description;

    public ItineraryItem() {
    }

    public ItineraryItem(String time, String description) {
        this.time = time;
        this.description = description;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItineraryItem that = (ItineraryItem) o;
        return Objects.equals(time, that.time) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, description);
    }

    @Override
    public String toString() {
        return "ItineraryItem{" +
                "time='" + time + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
