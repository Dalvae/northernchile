package com.northernchile.api.pricing.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class PricingCalculationReq {
    
    private List<PricingItem> items;

    public List<PricingItem> getItems() {
        return items;
    }

    public void setItems(List<PricingItem> items) {
        this.items = items;
    }

    public static class PricingItem {
        private UUID scheduleId;
        private int numParticipants;

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
    }
}
