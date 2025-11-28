package com.northernchile.api.pricing.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class PricingCalculationRes {
    
    private List<PricingLineItem> items;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal taxRate;
    private BigDecimal totalAmount;

    public List<PricingLineItem> getItems() {
        return items;
    }

    public void setItems(List<PricingLineItem> items) {
        this.items = items;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public static class PricingLineItem {
        private UUID scheduleId;
        private UUID tourId;
        private String tourName;
        private int numParticipants;
        private BigDecimal pricePerParticipant;
        private BigDecimal lineTotal;

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

        public BigDecimal getLineTotal() {
            return lineTotal;
        }

        public void setLineTotal(BigDecimal lineTotal) {
            this.lineTotal = lineTotal;
        }
    }
}
