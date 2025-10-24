package com.northernchile.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "private_tour_requests")
public class PrivateTourRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String requestedTourType;
    private Instant requestedDatetime;
    private int numAdults;
    private int numChildren;

    @Column(columnDefinition = "TEXT")
    private String specialRequests;

    private String status = "PENDING";
    private BigDecimal quotedPrice;
    private String paymentLinkId;

    @CreationTimestamp
    private Instant createdAt;
}
