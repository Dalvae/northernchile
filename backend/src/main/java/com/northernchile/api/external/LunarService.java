package com.northernchile.api.external;

import org.shredzone.commons.suncalc.MoonIllumination;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class LunarService {

    private static final double FULL_MOON_ILLUMINATION_THRESHOLD = 0.95; // 95% de iluminación

    public boolean isFullMoon(LocalDate date) {
        double illumination = getMoonIllumination(date);
        return illumination >= FULL_MOON_ILLUMINATION_THRESHOLD;
    }

    public double getMoonIllumination(LocalDate date) {
        ZonedDateTime zonedDateTime = date.atStartOfDay(ZoneId.systemDefault());
        MoonIllumination moonIllumination = MoonIllumination.compute()
                .on(zonedDateTime)
                .execute();
        return moonIllumination.getFraction();
    }
}
