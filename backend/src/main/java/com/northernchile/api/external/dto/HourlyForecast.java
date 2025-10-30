package com.northernchile.api.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HourlyForecast {
    public long dt;
    public double temp;

    @JsonProperty("feels_like")
    public double feelsLike;

    public int pressure;
    public int humidity;

    @JsonProperty("dew_point")
    public double dewPoint;

    public double uvi;
    public int clouds;
    public int visibility;

    @JsonProperty("wind_speed")
    public double windSpeed;

    @JsonProperty("wind_deg")
    public int windDeg;

    @JsonProperty("wind_gust")
    public Double windGust;

    public List<WeatherCondition> weather;
    public double pop; // Probability of precipitation
}
