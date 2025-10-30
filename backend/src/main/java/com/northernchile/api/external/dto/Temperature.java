package com.northernchile.api.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Temperature {
    public double day;
    public double min;
    public double max;
    public double night;
    public double eve;
    public double morn;
}
