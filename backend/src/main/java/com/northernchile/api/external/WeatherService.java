package com.northernchile.api.external;

import com.northernchile.api.external.dto.WeatherForecastResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@Service
public class WeatherService {

    @Value("${weather.api.key:dummy}")
    private String apiKey;

    @Value("${weather.api.baseurl:https://api.weatherapi.com/v1/forecast.json}")
    private String baseUrl;

    @Value("${weather.api.location:San Pedro de Atacama}")
    private String location;

    @Cacheable(value = "weatherForecast", key = "#days")
    public WeatherForecastResponse getForecast(int days) {
        if ("dummy".equals(apiKey)) {
            return null;
        }

        String url = String.format("%s?key=%s&q=%s&days=%d&aqi=no&alerts=no",
                baseUrl, apiKey, location, days);

        try {
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.getForObject(url, WeatherForecastResponse.class);
        } catch (Exception e) {
            // Log the error in a real application
            System.err.println("Error fetching weather forecast: " + e.getMessage());
            return null;
        }
    }

    public boolean isWindAboveThreshold(LocalDate date, double thresholdKnots) {
        // 1 nudo = 1.852 km/h
        double thresholdKph = thresholdKnots * 1.852;

        WeatherForecastResponse forecast = getForecast(14);
        if (forecast == null || forecast.forecast == null || forecast.forecast.forecastday == null) {
            return false; // Could not get forecast, assume wind is not strong
        }

        return forecast.forecast.forecastday.stream()
                .filter(forecastDay -> LocalDate.parse(forecastDay.date).equals(date))
                .findFirst()
                .map(day -> day.hour.stream().anyMatch(hour -> hour.wind_kph > thresholdKph))
                .orElse(false); // Day not found in forecast, assume it's fine
    }
}
