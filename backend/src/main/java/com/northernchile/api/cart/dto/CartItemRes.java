
package com.northernchile.api.cart.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class CartItemRes {
    private UUID itemId;
    private UUID tourId;
    private String tourName;
    private LocalDate tourDate;
    private int numAdults;
    private int numChildren;
    private BigDecimal priceAdult;
    private BigDecimal priceChild;
    private BigDecimal itemTotal;

    public CartItemRes() {
    }

    public CartItemRes(UUID itemId, UUID tourId, String tourName, LocalDate tourDate, int numAdults, int numChildren, BigDecimal priceAdult, BigDecimal priceChild, BigDecimal itemTotal) {
        this.itemId = itemId;
        this.tourId = tourId;
        this.tourName = tourName;
        this.tourDate = tourDate;
        this.numAdults = numAdults;
        this.numChildren = numChildren;
        this.priceAdult = priceAdult;
        this.priceChild = priceChild;
        this.itemTotal = itemTotal;
    }

    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
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
        return numAdults == that.numAdults && numChildren == that.numChildren && Objects.equals(itemId, that.itemId) && Objects.equals(tourId, that.tourId) && Objects.equals(tourName, that.tourName) && Objects.equals(tourDate, that.tourDate) && Objects.equals(priceAdult, that.priceAdult) && Objects.equals(priceChild, that.priceChild) && Objects.equals(itemTotal, that.itemTotal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, tourId, tourName, tourDate, numAdults, numChildren, priceAdult, priceChild, itemTotal);
    }

    @Override
    public String toString() {
        return "CartItemRes{" +
                "itemId=" + itemId +
                ", tourId=" + tourId +
                ", tourName='" + tourName + ''' +
                ", tourDate=" + tourDate +
                ", numAdults=" + numAdults +
                ", numChildren=" + numChildren +
                ", priceAdult=" + priceAdult +
                ", priceChild=" + priceChild +
                ", itemTotal=" + itemTotal +
                '}';
    }
}
