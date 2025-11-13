package com.northernchile.api.reports.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BookingsByDayReport {
    private LocalDate date;
    private int count;
    private BigDecimal revenue;

    public BookingsByDayReport(LocalDate date, int count, BigDecimal revenue) {
        this.date = date;
        this.count = count;
        this.revenue = revenue;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }
}
