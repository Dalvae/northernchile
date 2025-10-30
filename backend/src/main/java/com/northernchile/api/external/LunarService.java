package com.northernchile.api.external;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Servicio para calcular fase lunar usando método Julian Date
 *
 * Este servicio NO depende de APIs externas - calcula la fase lunar matemáticamente
 * basándose en el ciclo sinódico lunar de 29.53058867 días.
 *
 * Ventajas:
 * - ✅ Funciona offline (sin llamadas HTTP)
 * - ✅ Sin límite de días (pasado, presente, futuro)
 * - ✅ Instantáneo (~0.001ms por cálculo)
 * - ✅ Precisión: ±0.5 días (suficiente para tours astronómicos)
 *
 * Moon phase values:
 * - 0.0 (y 1.0) = New moon (luna nueva)
 * - 0.25 = First quarter (cuarto creciente)
 * - 0.5 = Full moon (luna llena)
 * - 0.75 = Last quarter (cuarto menguante)
 */
@Service
public class LunarService {

    // Luna llena cuando moon_phase está entre 0.45 y 0.55 (~3 días de iluminación alta)
    private static final double FULL_MOON_MIN = 0.45;
    private static final double FULL_MOON_MAX = 0.55;

    // Constantes para cálculo Julian Date
    private static final double JULIAN_EPOCH = 2440587.5; // 1 Jan 1970 00:00 UTC en Julian Date
    private static final double KNOWN_NEW_MOON = 2451550.1; // 6 Jan 2000 18:14 UTC (luna nueva conocida)
    private static final double SYNODIC_MONTH = 29.53058867; // Ciclo lunar en días
    private static final double SECONDS_PER_DAY = 86400.0;

    /**
     * Verifica si una fecha tiene luna llena
     * @param date Fecha a verificar
     * @return true si es luna llena (moon_phase entre 0.45 y 0.55)
     */
    public boolean isFullMoon(LocalDate date) {
        double phase = getMoonPhase(date);
        return phase >= FULL_MOON_MIN && phase <= FULL_MOON_MAX;
    }

    /**
     * Calcula la fase lunar para cualquier fecha usando método Julian Date
     *
     * Este método calcula la fase lunar basándose en:
     * 1. Convertir la fecha a Julian Date
     * 2. Calcular días desde una luna nueva conocida (6 Jan 2000)
     * 3. Dividir por el ciclo sinódico lunar (29.53 días)
     * 4. Obtener la parte fraccionaria (fase de 0.0 a 1.0)
     *
     * @param date Fecha a verificar (puede ser pasado, presente o futuro)
     * @return Fase lunar: 0.0=new moon, 0.25=first quarter, 0.5=full moon, 0.75=last quarter
     */
    public double getMoonPhase(LocalDate date) {
        // Convertir LocalDate a Julian Date
        ZonedDateTime zdt = date.atStartOfDay(ZoneId.of("UTC"));
        double julianDate = JULIAN_EPOCH + (zdt.toEpochSecond() / SECONDS_PER_DAY);

        // Calcular días desde luna nueva conocida
        double daysSinceNewMoon = julianDate - KNOWN_NEW_MOON;

        // Calcular número de ciclos lunares completos
        double lunarCycles = daysSinceNewMoon / SYNODIC_MONTH;

        // Obtener solo la parte fraccionaria (fase actual)
        double phase = lunarCycles - Math.floor(lunarCycles);

        return phase;
    }

    /**
     * Obtiene el porcentaje de iluminación lunar (0-100%)
     * Calculado aproximadamente desde moon_phase
     */
    public int getMoonIllumination(LocalDate date) {
        double phase = getMoonPhase(date);

        // Convertir fase (0-1) a iluminación (0-100%)
        // 0 = 0%, 0.25 = 50%, 0.5 = 100%, 0.75 = 50%, 1 = 0%
        double illumination;
        if (phase <= 0.5) {
            // Waxing: de 0% a 100%
            illumination = phase * 200;
        } else {
            // Waning: de 100% a 0%
            illumination = (1 - phase) * 200;
        }

        return (int) Math.round(illumination);
    }

    /**
     * Obtiene la fase lunar como texto descriptivo
     */
    public String getMoonPhaseName(LocalDate date) {
        double phase = getMoonPhase(date);

        if (phase < 0.03 || phase > 0.97) return "New Moon";
        if (phase >= 0.03 && phase < 0.22) return "Waxing Crescent";
        if (phase >= 0.22 && phase < 0.28) return "First Quarter";
        if (phase >= 0.28 && phase < 0.47) return "Waxing Gibbous";
        if (phase >= 0.47 && phase < 0.53) return "Full Moon";
        if (phase >= 0.53 && phase < 0.72) return "Waning Gibbous";
        if (phase >= 0.72 && phase < 0.78) return "Last Quarter";
        if (phase >= 0.78 && phase <= 0.97) return "Waning Crescent";

        return "Unknown";
    }
}
