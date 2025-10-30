package com.northernchile.api.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Daily forecast data from OpenWeatherMap
 * Includes moon phase, sunrise/sunset, and weather conditions
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DailyForecast {
    public long dt; // Unix timestamp
    public long sunrise;
    public long sunset;
    public long moonrise;
    public long moonset;

    /**
     * Moon phase: 0 and 1 = new moon, 0.25 = first quarter,
     * 0.5 = full moon, 0.75 = last quarter
     */
    @JsonProperty("moon_phase")
    public double moonPhase; // 0.0 - 1.0

    public String summary; // Human-readable summary

    public Temperature temp; // Temperatures

    @JsonProperty("feels_like")
    public FeelsLike feelsLike;

    public int pressure;
    public int humidity;

    @JsonProperty("dew_point")
    public double dewPoint;

    @JsonProperty("wind_speed")
    public double windSpeed; // m/s by default

    @JsonProperty("wind_deg")
    public int windDeg;

    @JsonProperty("wind_gust")
    public Double windGust;

    public List<WeatherCondition> weather;
    public int clouds; // Cloudiness, %
    public double pop; // Probability of precipitation
    public Double rain; // Rain volume, mm
    public Double snow; // Snow volume, mm
    public double uvi; // UV index
}
