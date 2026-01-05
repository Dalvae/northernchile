package com.northernchile.api.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * OpenWeatherMap One Call API 3.0 Response
 * https://openweathermap.org/api/one-call-3
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenWeatherResponse(
    double lat,
    double lon,
    String timezone,
    @JsonProperty("timezone_offset") int timezoneOffset,
    CurrentWeather current,
    List<DailyForecast> daily,
    List<HourlyForecast> hourly,
    List<Alert> alerts
) {}
