
package com.northernchile.api.booking.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BookingRes {
    private UUID id;
    private UUID userId;
    private String userFullName;
    private String userPhoneNumber;
    private UUID scheduleId;
    private String tourName;
    private LocalDate tourDate;
    private LocalTime tourStartTime;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String languageCode;
    private String specialRequests;
    private Instant createdAt;
    private List<ParticipantRes> participants;

    public BookingRes() {
    }

    public BookingRes(UUID id, UUID userId, String userFullName, String userPhoneNumber, UUID scheduleId, String tourName, LocalDate tourDate, LocalTime tourStartTime, String status, BigDecimal subtotal, BigDecimal taxAmount, BigDecimal totalAmount, String languageCode, String specialRequests, Instant createdAt, List<ParticipantRes> participants) {
        this.id = id;
        this.userId = userId;
        this.userFullName = userFullName;
        this.userPhoneNumber = userPhoneNumber;
        this.scheduleId = scheduleId;
        this.tourName = tourName;
        this.tourDate = tourDate;
        this.tourStartTime = tourStartTime;
        this.status = status;
        this.subtotal = subtotal;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.languageCode = languageCode;
        this.specialRequests = specialRequests;
        this.createdAt = createdAt;
        this.participants = participants;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public UUID getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(UUID scheduleId) {
        this.scheduleId = scheduleId;
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

    public LocalTime getTourStartTime() {
        return tourStartTime;
    }

    public void setTourStartTime(LocalTime tourStartTime) {
        this.tourStartTime = tourStartTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<ParticipantRes> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ParticipantRes> participants) {
        this.participants = participants;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookingRes that = (BookingRes) o;
        return Objects.equals(id, that.id) && Objects.equals(userId, that.userId) && Objects.equals(userFullName, that.userFullName) && Objects.equals(userPhoneNumber, that.userPhoneNumber) && Objects.equals(scheduleId, that.scheduleId) && Objects.equals(tourName, that.tourName) && Objects.equals(tourDate, that.tourDate) && Objects.equals(tourStartTime, that.tourStartTime) && Objects.equals(status, that.status) && Objects.equals(subtotal, that.subtotal) && Objects.equals(taxAmount, that.taxAmount) && Objects.equals(totalAmount, that.totalAmount) && Objects.equals(languageCode, that.languageCode) && Objects.equals(specialRequests, that.specialRequests) && Objects.equals(createdAt, that.createdAt) && Objects.equals(participants, that.participants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, userFullName, userPhoneNumber, scheduleId, tourName, tourDate, tourStartTime, status, subtotal, taxAmount, totalAmount, languageCode, specialRequests, createdAt, participants);
    }

    @Override
    public String toString() {
        return "BookingRes{" +
                "id=" + id +
                ", userId=" + userId +
                ", userFullName='" + userFullName + '\'' +
                ", userPhoneNumber='" + userPhoneNumber + '\'' +
                ", scheduleId=" + scheduleId +
                ", tourName='" + tourName + '\'' +
                ", tourDate=" + tourDate +
                ", tourStartTime=" + tourStartTime +
                ", status='" + status + '\'' +
                ", subtotal=" + subtotal +
                ", taxAmount=" + taxAmount +
                ", totalAmount=" + totalAmount +
                ", languageCode='" + languageCode + '\'' +
                ", specialRequests='" + specialRequests + '\'' +
                ", createdAt=" + createdAt +
                ", participants=" + participants +
                '}';
    }
}
