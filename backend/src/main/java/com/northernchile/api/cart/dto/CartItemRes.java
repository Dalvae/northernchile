
package com.northernchile.api.cart.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class CartItemRes {
    private UUID itemId;
    private UUID scheduleId;
    private UUID tourId;
    private String tourName;
    private int numParticipants;
    private BigDecimal pricePerParticipant;
    private BigDecimal itemTotal;

    public CartItemRes() {
    }

    public CartItemRes(UUID itemId, UUID scheduleId, UUID tourId, String tourName, int numParticipants, BigDecimal pricePerParticipant, BigDecimal itemTotal) {
        this.itemId = itemId;
        this.scheduleId = scheduleId;
        this.tourId = tourId;
        this.tourName = tourName;
        this.numParticipants = numParticipants;
        this.pricePerParticipant = pricePerParticipant;
        this.itemTotal = itemTotal;
    }

    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }

    public UUID getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(UUID scheduleId) {
        this.scheduleId = scheduleId;
    }

    public UUID getTourId() {
        return tourId;
    }

    public void setTourId(UUID tourId) {
        this.tourId = tourId;
    }

    public String getTourName() {
        return tourName;
    }

    public void setTourName(String tourName) {
        this.tourName = tourName;
    }

    public int getNumParticipants() {
        return numParticipants;
    }

    public void setNumParticipants(int numParticipants) {
        this.numParticipants = numParticipants;
    }

    public BigDecimal getPricePerParticipant() {
        return pricePerParticipant;
    }

    public void setPricePerParticipant(BigDecimal pricePerParticipant) {
        this.pricePerParticipant = pricePerParticipant;
    }

    public BigDecimal getItemTotal() {
        return itemTotal;
    }

    public void setItemTotal(BigDecimal itemTotal) {
        this.itemTotal = itemTotal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItemRes that = (CartItemRes) o;
        return numParticipants == that.numParticipants && Objects.equals(itemId, that.itemId) && Objects.equals(scheduleId, that.scheduleId) && Objects.equals(tourId, that.tourId) && Objects.equals(tourName, that.tourName) && Objects.equals(pricePerParticipant, that.pricePerParticipant) && Objects.equals(itemTotal, that.itemTotal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, scheduleId, tourId, tourName, numParticipants, pricePerParticipant, itemTotal);
    }

    @Override
    public String toString() {
        return "CartItemRes{" +
                "itemId=" + itemId +
                ", scheduleId=" + scheduleId +
                ", tourId=" + tourId +
                ", tourName='" + tourName + '\'' +
                ", numParticipants=" + numParticipants +
                ", pricePerParticipant=" + pricePerParticipant +
                ", itemTotal=" + itemTotal +
                '}';
    }
}
