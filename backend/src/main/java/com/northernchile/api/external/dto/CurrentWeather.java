package com.northernchile.api.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrentWeather {
    public long dt; // Unix timestamp
    public long sunrise;
    public long sunset;
    public double temp;

    @JsonProperty("feels_like")
    public double feelsLike;

    public int pressure;
    public int humidity;

    @JsonProperty("dew_point")
    public double dewPoint;

    public double uvi; // UV index
    public int clouds; // Cloudiness, %
    public int visibility; // Visibility, meters

    @JsonProperty("wind_speed")
    public double windSpeed; // Wind speed (m/s by default)

    @JsonProperty("wind_deg")
    public int windDeg; // Wind direction, degrees

    @JsonProperty("wind_gust")
    public Double windGust; // Wind gust (m/s)

    public List<WeatherCondition> weather;
}
