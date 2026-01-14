package com.northernchile.api.alert.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response wrapper for alert history endpoint
 * Contains all alerts and grouped by schedule
 */
public record AlertHistoryRes(
        List<WeatherAlertRes> all,
        Map<UUID, List<WeatherAlertRes>> bySchedule
) {}
