package com.northernchile.api.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Alerta climática para schedules afectados por cambios en pronóstico
 *
 * Se genera automáticamente cuando:
 * - El pronóstico empeora (viento, nubes) para un schedule existente
 * - El admin debe revisar y decidir si cancelar o mantener el tour
 */
@Entity
@Table(name = "weather_alerts")
public class WeatherAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_schedule_id", nullable = false)
    private TourSchedule tourSchedule;

    @Column(nullable = false)
    private String alertType; // "WIND", "CLOUDS", "MOON"

    @Column(nullable = false)
    private String severity; // "WARNING", "CRITICAL"

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private String status; // "PENDING", "REVIEWED", "RESOLVED"

    @Column
    private Instant resolvedAt;

    @Column
    private String resolvedBy; // UUID del admin que lo resolvió

    @Column
    private String resolution; // "CANCELLED", "KEPT", "RESCHEDULED"

    // Datos del pronóstico cuando se creó la alerta
    @Column
    private Double windSpeed; // m/s

    @Column
    private Integer cloudCoverage; // %

    @Column
    private Double moonPhase;

    // Constructor
    public WeatherAlert() {
        this.createdAt = Instant.now();
        this.status = "PENDING";
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public TourSchedule getTourSchedule() {
        return tourSchedule;
    }

    public void setTourSchedule(TourSchedule tourSchedule) {
        this.tourSchedule = tourSchedule;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public Integer getCloudCoverage() {
        return cloudCoverage;
    }

    public void setCloudCoverage(Integer cloudCoverage) {
        this.cloudCoverage = cloudCoverage;
    }

    public Double getMoonPhase() {
        return moonPhase;
    }

    public void setMoonPhase(Double moonPhase) {
        this.moonPhase = moonPhase;
    }
}
