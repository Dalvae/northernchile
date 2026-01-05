package com.northernchile.api.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Daily forecast data from OpenWeatherMap
 * Includes moon phase, sunrise/sunset, and weather conditions
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DailyForecast(
    long dt,
    long sunrise,
    long sunset,
    long moonrise,
    long moonset,
    @JsonProperty("moon_phase") double moonPhase,
    String summary,
    Temperature temp,
    @JsonProperty("feels_like") FeelsLike feelsLike,
    int pressure,
    int humidity,
    @JsonProperty("dew_point") double dewPoint,
    @JsonProperty("wind_speed") double windSpeed,
    @JsonProperty("wind_deg") int windDeg,
    @JsonProperty("wind_gust") Double windGust,
    List<WeatherCondition> weather,
    int clouds,
    double pop,
    Double rain,
    Double snow,
    double uvi
) {}
