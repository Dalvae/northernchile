package com.northernchile.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Weather API configuration properties.
 * Settings for OpenWeatherMap integration.
 */
@Component
@ConfigurationProperties(prefix = "weather")
public class WeatherProperties {

    private Api api = new Api();

    public Api getApi() {
        return api;
    }

    public void setApi(Api api) {
        this.api = api;
    }

    public static class Api {
        private String key = "dummy";
        private double lat = -22.9083; // San Pedro de Atacama
        private double lon = -68.1999;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }
    }
}
