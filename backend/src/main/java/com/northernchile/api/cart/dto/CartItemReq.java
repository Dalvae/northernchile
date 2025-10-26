
package com.northernchile.api.cart.dto;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class CartItemReq {
    private UUID tourId;
    private LocalDate tourDate;
    private int numAdults;
    private int numChildren;

    public CartItemReq() {
    }

    public CartItemReq(UUID tourId, LocalDate tourDate, int numAdults, int numChildren) {
        this.tourId = tourId;
        this.tourDate = tourDate;
        this.numAdults = numAdults;
        this.numChildren = numChildren;
    }

    public UUID getTourId() {
        return tourId;
    }

    public void setTourId(UUID tourId) {
        this.tourId = tourId;
    }

    public LocalDate getTourDate() {
        return tourDate;
    }

    public void setTourDate(LocalDate tourDate) {
        this.tourDate = tourDate;
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
        return numAdults == that.numAdults && numChildren == that.numChildren && Objects.equals(tourId, that.tourId) && Objects.equals(tourDate, that.tourDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tourId, tourDate, numAdults, numChildren);
    }

    @Override
    public String toString() {
        return "CartItemReq{" +
                "tourId=" + tourId +
                ", tourDate=" + tourDate +
                ", numAdults=" + numAdults +
                ", numChildren=" + numChildren +
                '}';
    }
}
