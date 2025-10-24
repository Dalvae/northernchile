package com.northernchile.api.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@Service
public class WeatherService {

    // In a real application, you would map the JSON response to DTOs.
    // For simplicity, we'll just return the raw JSON string for now.

    @Value("${weather.api.key:dummy}")
    private String apiKey;

    @Value("${weather.api.baseurl:https://api.weatherapi.com/v1/forecast.json}")
    private String baseUrl;

    @Value("${weather.api.location:San Pedro de Atacama}")
    private String location;

    @Cacheable(value = "weatherForecast", key = "#days")
    public String getForecast(int days) {
        if ("dummy".equals(apiKey)) {
            // Return a dummy response if no API key is configured
            return "{\"error\": \"Weather API key not configured\"}";
        }

        String url = String.format("%s?key=%s&q=%s&days=%d&aqi=no&alerts=no",
                baseUrl, apiKey, location, days);

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, String.class);
    }

    // This is a placeholder for the wind validation logic
    public boolean isWindAboveThreshold(LocalDate date, double thresholdKnots) {
        // Dummy implementation
        return false;
    }
}
