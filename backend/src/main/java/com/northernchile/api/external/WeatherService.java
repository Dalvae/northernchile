package com.northernchile.api.external;

import com.northernchile.api.external.dto.DailyForecast;
import com.northernchile.api.external.dto.OpenWeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Servicio para obtener datos del clima de OpenWeatherMap
 * https://openweathermap.org/api/one-call-3
 */
@Service
public class WeatherService {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Santiago");

    @Value("${weather.api.key:dummy}")
    private String apiKey;

    @Value("${weather.api.baseurl:https://api.openweathermap.org/data/3.0/onecall}")
    private String baseUrl;

    // San Pedro de Atacama coordinates
    @Value("${weather.api.lat:-22.9083}")
    private String latitude;

    @Value("${weather.api.lon:-68.1999}")
    private String longitude;

    /**
     * Obtiene pronóstico de OpenWeatherMap One Call API 3.0
     * Caché de 24 horas para evitar llamadas excesivas
     * @return Respuesta del API con 8 días de pronóstico + datos astronómicos
     */
    @Cacheable(value = "weatherForecast", key = "'onecall'")
    public OpenWeatherResponse getForecast() {
        if ("dummy".equals(apiKey)) {
            return null;
        }

        // One Call API 3.0: daily forecast hasta 8 días
        // Excluimos minutely, hourly para ahorrar datos
        String url = String.format("%s?lat=%s&lon=%s&exclude=minutely,hourly&units=metric&appid=%s",
                baseUrl, latitude, longitude, apiKey);

        try {
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.getForObject(url, OpenWeatherResponse.class);
        } catch (Exception e) {
            System.err.println("Error fetching OpenWeather forecast: " + e.getMessage());
            return null;
        }
    }

    /**
     * Verifica si el viento supera un umbral en una fecha específica
     * @param date Fecha a verificar
     * @param thresholdKnots Umbral en nudos
     * @return true si el viento supera el umbral
     */
    public boolean isWindAboveThreshold(LocalDate date, double thresholdKnots) {
        // Convertir nudos a m/s (OpenWeather usa m/s)
        // 1 nudo = 0.514444 m/s
        double thresholdMs = thresholdKnots * 0.514444;

        OpenWeatherResponse forecast = getForecast();
        if (forecast == null || forecast.daily == null) {
            return false; // Sin datos, asumir OK
        }

        return forecast.daily.stream()
                .filter(day -> {
                    LocalDate forecastDate = Instant.ofEpochSecond(day.dt)
                            .atZone(ZONE_ID)
                            .toLocalDate();
                    return forecastDate.equals(date);
                })
                .findFirst()
                .map(day -> {
                    // Verificar wind_speed o wind_gust
                    double maxWind = day.windSpeed;
                    if (day.windGust != null && day.windGust > maxWind) {
                        maxWind = day.windGust;
                    }
                    return maxWind > thresholdMs;
                })
                .orElse(false);
    }

    /**
     * Verifica si un día está muy nublado (>80% de nubes)
     * @param date Fecha a verificar
     * @return true si está muy nublado
     */
    public boolean isCloudyDay(LocalDate date) {
        OpenWeatherResponse forecast = getForecast();
        if (forecast == null || forecast.daily == null) {
            return false;
        }

        return forecast.daily.stream()
                .filter(day -> {
                    LocalDate forecastDate = Instant.ofEpochSecond(day.dt)
                            .atZone(ZONE_ID)
                            .toLocalDate();
                    return forecastDate.equals(date);
                })
                .findFirst()
                .map(day -> day.clouds > 80) // Más de 80% nublado
                .orElse(false);
    }

    /**
     * Obtiene el pronóstico diario para una fecha específica
     */
    public DailyForecast getDailyForecast(LocalDate date) {
        OpenWeatherResponse forecast = getForecast();
        if (forecast == null || forecast.daily == null) {
            return null;
        }

        return forecast.daily.stream()
                .filter(day -> {
                    LocalDate forecastDate = Instant.ofEpochSecond(day.dt)
                            .atZone(ZONE_ID)
                            .toLocalDate();
                    return forecastDate.equals(date);
                })
                .findFirst()
                .orElse(null);
    }
}
