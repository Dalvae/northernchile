package com.northernchile.api.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CurrentWeather(
    long dt,
    long sunrise,
    long sunset,
    double temp,
    @JsonProperty("feels_like") double feelsLike,
    int pressure,
    int humidity,
    @JsonProperty("dew_point") double dewPoint,
    double uvi,
    int clouds,
    int visibility,
    @JsonProperty("wind_speed") double windSpeed,
    @JsonProperty("wind_deg") int windDeg,
    @JsonProperty("wind_gust") Double windGust,
    List<WeatherCondition> weather
) {}
