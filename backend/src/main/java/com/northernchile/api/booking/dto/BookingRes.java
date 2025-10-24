package com.northernchile.api.booking.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class BookingRes {
    private UUID id;
    private UUID userId;
    private String userFullName;
    private UUID scheduleId;
    private String tourName;
    private LocalDate tourDate;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String languageCode;
    private String specialRequests;
    private Instant createdAt;
    private List<ParticipantRes> participants;
}
