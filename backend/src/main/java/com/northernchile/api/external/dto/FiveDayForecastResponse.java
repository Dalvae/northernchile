package com.northernchile.api.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * OpenWeatherMap 5 Day / 3 Hour Forecast API Response (FREE)
 * https://openweathermap.org/forecast5
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FiveDayForecastResponse(
    String cod,
    int message,
    int cnt,
    List<ForecastItem> list,
    City city
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ForecastItem(
        long dt,
        Main main,
        List<Weather> weather,
        Clouds clouds,
        Wind wind,
        int visibility,
        double pop,
        Rain rain,
        Snow snow,
        Sys sys,
        @JsonProperty("dt_txt") String dtTxt
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Main(
        double temp,
        @JsonProperty("feels_like") double feelsLike,
        @JsonProperty("temp_min") double tempMin,
        @JsonProperty("temp_max") double tempMax,
        int pressure,
        @JsonProperty("sea_level") int seaLevel,
        @JsonProperty("grnd_level") int grndLevel,
        int humidity,
        @JsonProperty("temp_kf") double tempKf
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Weather(
        int id,
        String main,
        String description,
        String icon
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Clouds(int all) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Wind(double speed, int deg, double gust) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Rain(@JsonProperty("3h") double threeHours) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Snow(@JsonProperty("3h") double threeHours) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Sys(String pod) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record City(
        int id,
        String name,
        Coord coord,
        String country,
        int population,
        int timezone,
        long sunrise,
        long sunset
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Coord(double lat, double lon) {}
}
