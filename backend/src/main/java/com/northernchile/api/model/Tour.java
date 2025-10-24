// backend/src/main/java/com/northernchile/api/model/Tour.java
package com.northernchile.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tours")
public class Tour {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(unique = true, length = 100)
    private String contentKey;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAdult;

    @Column(precision = 10, scale = 2)
    private BigDecimal priceChild;

    @Column(nullable = false)
    private Integer defaultMaxParticipants;

    @Column(nullable = false)
    private Integer durationHours;

    private Boolean isWindSensitive = false;

    private Boolean isRecurring = false;

    @Column(length = 100)
    private String recurrenceRule;

    @Column(length = 20)
    private String status = "DRAFT";

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
