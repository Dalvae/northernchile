package com.northernchile.api.alert;

import com.northernchile.api.model.WeatherAlert;
import com.northernchile.api.model.WeatherAlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WeatherAlertRepository extends JpaRepository<WeatherAlert, UUID> {

    /**
     * Encuentra alertas pendientes (sin revisar)
     */
    List<WeatherAlert> findByStatus(WeatherAlertStatus status);

    /**
     * Encuentra alertas para un schedule específico
     */
    List<WeatherAlert> findByTourSchedule_Id(UUID scheduleId);

    /**
     * Encuentra alertas pendientes para un schedule específico
     */
    List<WeatherAlert> findByTourSchedule_IdAndStatus(UUID scheduleId, WeatherAlertStatus status);

    /**
     * Cuenta alertas pendientes
     */
    long countByStatus(WeatherAlertStatus status);

    /**
     * Encuentra alertas creadas después de una fecha
     */
    List<WeatherAlert> findByCreatedAtAfter(Instant date);

    // ========== Métodos con JOIN FETCH para evitar N+1 ==========

    /**
     * Encuentra todas las alertas con schedule y tour cargados
     */
    @Query("SELECT a FROM WeatherAlert a " +
           "LEFT JOIN FETCH a.tourSchedule ts " +
           "LEFT JOIN FETCH ts.tour " +
           "ORDER BY a.createdAt DESC")
    List<WeatherAlert> findAllWithScheduleAndTour();

    /**
     * Encuentra alertas por status con schedule y tour cargados
     */
    @Query("SELECT a FROM WeatherAlert a " +
           "LEFT JOIN FETCH a.tourSchedule ts " +
           "LEFT JOIN FETCH ts.tour " +
           "WHERE a.status = :status " +
           "ORDER BY a.createdAt DESC")
    List<WeatherAlert> findByStatusWithScheduleAndTour(@Param("status") WeatherAlertStatus status);

    /**
     * Encuentra una alerta por ID con schedule y tour cargados
     */
    @Query("SELECT a FROM WeatherAlert a " +
           "LEFT JOIN FETCH a.tourSchedule ts " +
           "LEFT JOIN FETCH ts.tour " +
           "WHERE a.id = :id")
    Optional<WeatherAlert> findByIdWithScheduleAndTour(@Param("id") UUID id);

    /**
     * Encuentra alertas por scheduleId con tour cargado
     */
    @Query("SELECT a FROM WeatherAlert a " +
           "LEFT JOIN FETCH a.tourSchedule ts " +
           "LEFT JOIN FETCH ts.tour " +
           "WHERE ts.id = :scheduleId " +
           "ORDER BY a.createdAt DESC")
    List<WeatherAlert> findByScheduleIdWithTour(@Param("scheduleId") UUID scheduleId);
}
