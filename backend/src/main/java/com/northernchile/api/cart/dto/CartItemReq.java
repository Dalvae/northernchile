
package com.northernchile.api.cart.dto;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class CartItemReq {
    private UUID scheduleId;
    private int numAdults;
    private int numChildren;

    public CartItemReq() {
    }

    public CartItemReq(UUID scheduleId, int numAdults, int numChildren) {
        this.scheduleId = scheduleId;
        this.numAdults = numAdults;
        this.numChildren = numChildren;
    }

    public UUID getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(UUID scheduleId) {
        this.scheduleId = scheduleId;
    }

    public int getNumAdults() {
        return numAdults;
    }

    public void setNumAdults(int numAdults) {
        this.numAdults = numAdults;
    }

    public int getNumChildren() {
        return numChildren;
    }

    public void setNumChildren(int numChildren) {
        this.numChildren = numChildren;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItemReq that = (CartItemReq) o;
        return numAdults == that.numAdults && numChildren == that.numChildren && Objects.equals(scheduleId, that.scheduleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheduleId, numAdults, numChildren);
    }

    @Override
    public String toString() {
        return "CartItemReq{" +
                "scheduleId=" + scheduleId +
                ", numAdults=" + numAdults +
                ", numChildren=" + numChildren +
                '}';
    }
}
