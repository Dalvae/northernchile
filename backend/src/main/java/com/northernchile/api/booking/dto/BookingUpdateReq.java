
package com.northernchile.api.booking.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

public class BookingUpdateReq {
    @NotBlank(message = "Status is required")
    private String status; // PENDING, CONFIRMED, CANCELLED

    private String specialRequests;

    public BookingUpdateReq() {
    }

    public BookingUpdateReq(String status, String specialRequests) {
        this.status = status;
        this.specialRequests = specialRequests;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookingUpdateReq that = (BookingUpdateReq) o;
        return Objects.equals(status, that.status) && Objects.equals(specialRequests, that.specialRequests);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, specialRequests);
    }

    @Override
    public String toString() {
        return "BookingUpdateReq{" +
                "status='" + status + '\'' +
                ", specialRequests='" + specialRequests + '\'' +
                '}';
    }
}
