package com.northernchile.api.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeelsLike {
    public double day;
    public double night;
    public double eve;
    public double morn;
}
