package com.northernchile.api.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * OpenWeatherMap 5 Day / 3 Hour Forecast API Response (FREE)
 * https://openweathermap.org/forecast5
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FiveDayForecastResponse {
    public String cod;
    public int message;
    public int cnt;
    public List<ForecastItem> list;
    public City city;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ForecastItem {
        public long dt; // Unix timestamp
        public Main main;
        public List<Weather> weather;
        public Clouds clouds;
        public Wind wind;
        public int visibility;
        public double pop; // Probability of precipitation (0-1)
        public Rain rain;
        public Snow snow;
        public Sys sys;

        @JsonProperty("dt_txt")
        public String dtTxt; // "2025-01-15 12:00:00"
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Main {
        public double temp;

        @JsonProperty("feels_like")
        public double feelsLike;

        @JsonProperty("temp_min")
        public double tempMin;

        @JsonProperty("temp_max")
        public double tempMax;

        public int pressure;

        @JsonProperty("sea_level")
        public int seaLevel;

        @JsonProperty("grnd_level")
        public int grndLevel;

        public int humidity;

        @JsonProperty("temp_kf")
        public double tempKf;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Weather {
        public int id;
        public String main; // "Rain", "Clouds", "Clear"
        public String description;
        public String icon;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Clouds {
        public int all; // Cloudiness percentage
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Wind {
        public double speed; // m/s
        public int deg; // degrees
        public double gust; // m/s
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Rain {
        @JsonProperty("3h")
        public double threeHours; // Rain volume for last 3h, mm
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Snow {
        @JsonProperty("3h")
        public double threeHours; // Snow volume for last 3h, mm
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sys {
        public String pod; // "d" = day, "n" = night
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class City {
        public int id;
        public String name;
        public Coord coord;
        public String country;
        public int population;
        public int timezone;
        public long sunrise;
        public long sunset;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Coord {
        public double lat;
        public double lon;
    }
}
