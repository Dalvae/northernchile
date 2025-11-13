package com.northernchile.api.reports.dto;

import java.math.BigDecimal;

public class TopTourReport {
    private String tourName;
    private int bookingsCount;
    private BigDecimal revenue;
    private long participants;

    public TopTourReport(String tourName, int bookingsCount, BigDecimal revenue, long participants) {
        this.tourName = tourName;
        this.bookingsCount = bookingsCount;
        this.revenue = revenue;
        this.participants = participants;
    }

    public String getTourName() {
        return tourName;
    }

    public void setTourName(String tourName) {
        this.tourName = tourName;
    }

    public int getBookingsCount() {
        return bookingsCount;
    }

    public void setBookingsCount(int bookingsCount) {
        this.bookingsCount = bookingsCount;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public long getParticipants() {
        return participants;
    }

    public void setParticipants(long participants) {
        this.participants = participants;
    }
}
