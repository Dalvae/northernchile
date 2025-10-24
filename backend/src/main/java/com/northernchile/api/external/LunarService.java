package com.northernchile.api.external;

import ch.eobermuhlner.astro.Moon;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class LunarService {

    private static final double FULL_MOON_ILLUMINATION_THRESHOLD = 0.95;

    public boolean isFullMoon(LocalDate date) {
        double illumination = getMoonIllumination(date);
        return illumination >= FULL_MOON_ILLUMINATION_THRESHOLD;
    }

    public double getMoonIllumination(LocalDate date) {
        LocalDateTime dateTime = date.atTime(12, 0); // Use midday as a reference
        return Moon.getIlluminatedFraction(dateTime);
    }
}
