package com.northernchile.api.cart.dto;

import java.util.Objects;
import java.util.UUID;

public class CartItemReq {
    private UUID scheduleId;
    private int numParticipants = 1;

    public CartItemReq() {
    }

    public CartItemReq(UUID scheduleId, int numParticipants) {
        this.scheduleId = scheduleId;
        this.numParticipants = numParticipants;
    }

    public UUID getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(UUID scheduleId) {
        this.scheduleId = scheduleId;
    }

    public int getNumParticipants() {
        return numParticipants;
    }

    public void setNumParticipants(int numParticipants) {
        this.numParticipants = numParticipants;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItemReq that = (CartItemReq) o;
        return numParticipants == that.numParticipants && Objects.equals(scheduleId, that.scheduleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheduleId, numParticipants);
    }

    @Override
    public String toString() {
        return "CartItemReq{" +
                "scheduleId=" + scheduleId +
                ", numParticipants=" + numParticipants +
                '}';
    }
}