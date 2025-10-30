package com.northernchile.api.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherCondition {
    public int id; // Weather condition id
    public String main; // Group of weather parameters (Rain, Snow, Clouds, etc.)
    public String description; // Weather condition within the group
    public String icon; // Weather icon id
}
