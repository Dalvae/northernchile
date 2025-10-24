package com.northernchile.api.external;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class LunarService {

    // This is a placeholder. A real implementation would use a library like astro-algo.
    public boolean isFullMoon(LocalDate date) {
        // Dummy implementation: returns true if the day is 15th of the month
        return date.getDayOfMonth() == 15;
    }

    public double getMoonIllumination(LocalDate date) {
        // Dummy implementation
        if (date.getDayOfMonth() == 15) return 1.0;
        if (date.getDayOfMonth() == 1) return 0.0;
        return 0.5;
    }
}
