package com.northernchile.api.alert;

import com.northernchile.api.model.WeatherAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface WeatherAlertRepository extends JpaRepository<WeatherAlert, UUID> {

    /**
     * Encuentra alertas pendientes (sin revisar)
     */
    List<WeatherAlert> findByStatus(String status);

    /**
     * Encuentra alertas para un schedule específico
     */
    List<WeatherAlert> findByTourSchedule_Id(UUID scheduleId);

    /**
     * Encuentra alertas pendientes para un schedule específico
     */
    List<WeatherAlert> findByTourSchedule_IdAndStatus(UUID scheduleId, String status);

    /**
     * Cuenta alertas pendientes
     */
    long countByStatus(String status);

    /**
     * Encuentra alertas creadas después de una fecha
     */
    List<WeatherAlert> findByCreatedAtAfter(Instant date);
}
