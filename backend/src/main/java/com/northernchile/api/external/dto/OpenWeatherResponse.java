package com.northernchile.api.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * OpenWeatherMap One Call API 3.0 Response
 * https://openweathermap.org/api/one-call-3
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenWeatherResponse {
    public double lat;
    public double lon;
    public String timezone;

    @JsonProperty("timezone_offset")
    public int timezoneOffset;

    public CurrentWeather current;
    public List<DailyForecast> daily;
    public List<HourlyForecast> hourly;
    public List<Alert> alerts;
}
