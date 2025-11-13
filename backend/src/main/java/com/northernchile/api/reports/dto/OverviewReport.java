package com.northernchile.api.reports.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class OverviewReport {
    private Instant periodStart;
    private Instant periodEnd;
    private long totalBookings;
    private long confirmedBookings;
    private long cancelledBookings;
    private BigDecimal totalRevenue;
    private long totalParticipants;
    private BigDecimal averageBookingValue;
    private double conversionRate;
    private long totalUsers;
    private long totalTours;
    private long totalSchedules;

    public OverviewReport(
            Instant periodStart,
            Instant periodEnd,
            long totalBookings,
            long confirmedBookings,
            long cancelledBookings,
            BigDecimal totalRevenue,
            long totalParticipants,
            BigDecimal averageBookingValue,
            double conversionRate,
            long totalUsers,
            long totalTours,
            long totalSchedules) {
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.totalBookings = totalBookings;
        this.confirmedBookings = confirmedBookings;
        this.cancelledBookings = cancelledBookings;
        this.totalRevenue = totalRevenue;
        this.totalParticipants = totalParticipants;
        this.averageBookingValue = averageBookingValue;
        this.conversionRate = conversionRate;
        this.totalUsers = totalUsers;
        this.totalTours = totalTours;
        this.totalSchedules = totalSchedules;
    }

    public Instant getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(Instant periodStart) {
        this.periodStart = periodStart;
    }

    public Instant getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(Instant periodEnd) {
        this.periodEnd = periodEnd;
    }

    public long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public long getConfirmedBookings() {
        return confirmedBookings;
    }

    public void setConfirmedBookings(long confirmedBookings) {
        this.confirmedBookings = confirmedBookings;
    }

    public long getCancelledBookings() {
        return cancelledBookings;
    }

    public void setCancelledBookings(long cancelledBookings) {
        this.cancelledBookings = cancelledBookings;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public long getTotalParticipants() {
        return totalParticipants;
    }

    public void setTotalParticipants(long totalParticipants) {
        this.totalParticipants = totalParticipants;
    }

    public BigDecimal getAverageBookingValue() {
        return averageBookingValue;
    }

    public void setAverageBookingValue(BigDecimal averageBookingValue) {
        this.averageBookingValue = averageBookingValue;
    }

    public double getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(double conversionRate) {
        this.conversionRate = conversionRate;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalTours() {
        return totalTours;
    }

    public void setTotalTours(long totalTours) {
        this.totalTours = totalTours;
    }

    public long getTotalSchedules() {
        return totalSchedules;
    }

    public void setTotalSchedules(long totalSchedules) {
        this.totalSchedules = totalSchedules;
    }
}
