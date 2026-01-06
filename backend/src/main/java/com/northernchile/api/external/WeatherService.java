package com.northernchile.api.external;

import com.northernchile.api.external.dto.DailyForecast;
import com.northernchile.api.external.dto.FiveDayForecastResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para obtener datos del clima de OpenWeatherMap
 * Usa la API gratuita de pronóstico de 5 días
 * https://openweathermap.org/forecast5
 */
@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);
    private static final ZoneId ZONE_ID = ZoneId.of("America/Santiago");

    // Self-injection to allow internal calls to pass through the Spring proxy (for @Cacheable)
    @org.springframework.context.annotation.Lazy
    @org.springframework.beans.factory.annotation.Autowired
    private WeatherService self;

    @Value("${weather.api.key:dummy}")
    private String apiKey;

    // San Pedro de Atacama coordinates
    @Value("${weather.api.lat:-22.9083}")
    private String latitude;

    @Value("${weather.api.lon:-68.1999}")
    private String longitude;

    /**
     * Obtiene pronóstico de 5 días con datos cada 3 horas (API gratuita)
     * Caché de 3 horas para evitar llamadas excesivas
     * @return Datos agrupados por día con temperaturas, viento, nubes, etc.
     */
    @Cacheable(value = "weatherForecast", key = "'fiveday'")
    public Map<String, Object> getForecast() {
        if ("dummy".equals(apiKey)) {
            log.warn("API key is 'dummy', skipping weather fetch");
            return createEmptyForecast();
        }

        // Free 5 Day / 3 Hour Forecast API
        String url = String.format("https://api.openweathermap.org/data/2.5/forecast?lat=%s&lon=%s&units=metric&appid=%s",
                latitude, longitude, apiKey);

        log.debug("Fetching weather from: {}", url.replace(apiKey, "***"));

        try {
            RestTemplate restTemplate = new RestTemplate();
            FiveDayForecastResponse response = restTemplate.getForObject(url, FiveDayForecastResponse.class);

            if (response == null) {
                log.error("Weather API response is null");
                return createEmptyForecast();
            }

            if (response.list() == null) {
                log.error("Weather API response.list is null");
                return createEmptyForecast();
            }

            log.info("Received {} forecast items from Weather API", response.list().size());

            // Agrupar por día y procesar
            Map<String, Object> result = processForecastData(response);
            log.info("Processed into {} daily forecasts", ((List<?>) result.get("daily")).size());
            return result;
        } catch (Exception e) {
            log.error("Error fetching OpenWeather forecast: {}", e.getMessage(), e);
            return createEmptyForecast();
        }
    }

    /**
     * Procesa los datos de 3 horas y los agrupa por día
     */
    private Map<String, Object> processForecastData(FiveDayForecastResponse response) {
        // Agrupar items por fecha
        Map<LocalDate, List<FiveDayForecastResponse.ForecastItem>> byDay = response.list().stream()
                .collect(Collectors.groupingBy(item ->
                    Instant.ofEpochSecond(item.dt()).atZone(ZONE_ID).toLocalDate()
                ));

        // Convertir a formato que espera el frontend
        List<Map<String, Object>> dailyForecasts = byDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<FiveDayForecastResponse.ForecastItem> items = entry.getValue();

                    // Calcular máximos y mínimos del día
                    double tempMax = items.stream().mapToDouble(i -> i.main().tempMax()).max().orElse(0);
                    double tempMin = items.stream().mapToDouble(i -> i.main().tempMin()).min().orElse(0);
                    double tempDay = items.stream().mapToDouble(i -> i.main().temp()).average().orElse(0);

                    // Viento máximo del día
                    double windSpeed = items.stream().mapToDouble(i -> i.wind().speed()).max().orElse(0);
                    double windGust = items.stream().mapToDouble(i -> i.wind().gust()).max().orElse(0);

                    // Promedio de nubes
                    double clouds = items.stream().mapToDouble(i -> i.clouds().all()).average().orElse(0);

                    // Probabilidad máxima de precipitación
                    double pop = items.stream().mapToDouble(i -> i.pop()).max().orElse(0);

                    // Condición climática más frecuente
                    Map.Entry<String, Long> mostFrequentWeather = items.stream()
                            .flatMap(i -> i.weather().stream())
                            .collect(Collectors.groupingBy(w -> w.main(), Collectors.counting()))
                            .entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .orElse(null);

                    FiveDayForecastResponse.Weather representativeWeather = items.get(0).weather().get(0);
                    if (mostFrequentWeather != null) {
                        representativeWeather = items.stream()
                                .flatMap(i -> i.weather().stream())
                                .filter(w -> w.main().equals(mostFrequentWeather.getKey()))
                                .findFirst()
                                .orElse(representativeWeather);
                    }

                    // Timestamp del mediodía (para dt)
                    long dt = items.stream()
                            .filter(i -> {
                                int hour = Instant.ofEpochSecond(i.dt()).atZone(ZONE_ID).getHour();
                                return hour >= 12 && hour <= 15;
                            })
                            .findFirst()
                            .map(i -> i.dt())
                            .orElse(items.get(0).dt());

                    Map<String, Object> dayForecast = new HashMap<>();
                    dayForecast.put("dt", dt);
                    dayForecast.put("temp", Map.of(
                            "max", tempMax,
                            "min", tempMin,
                            "day", tempDay
                    ));
                    dayForecast.put("windSpeed", windSpeed);
                    dayForecast.put("windGust", windGust);
                    dayForecast.put("clouds", (int) clouds);
                    dayForecast.put("pop", pop);
                    dayForecast.put("weather", List.of(Map.of(
                            "id", representativeWeather.id(),
                            "main", representativeWeather.main(),
                            "description", representativeWeather.description(),
                            "icon", representativeWeather.icon()
                    )));

                    return dayForecast;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("daily", dailyForecasts);
        return result;
    }

    private Map<String, Object> createEmptyForecast() {
        Map<String, Object> result = new HashMap<>();
        result.put("daily", new ArrayList<>());
        return result;
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

        Map<String, Object> forecast = self.getForecast();
        if (forecast == null || !forecast.containsKey("daily")) {
            return false; // Sin datos, asumir OK
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> daily = (List<Map<String, Object>>) forecast.get("daily");

        return daily.stream()
                .filter(day -> {
                    long dt = ((Number) day.get("dt")).longValue();
                    LocalDate forecastDate = Instant.ofEpochSecond(dt)
                            .atZone(ZONE_ID)
                            .toLocalDate();
                    return forecastDate.equals(date);
                })
                .findFirst()
                .map(day -> {
                    double windSpeed = ((Number) day.get("windSpeed")).doubleValue();
                    double windGust = ((Number) day.get("windGust")).doubleValue();
                    double maxWind = Math.max(windSpeed, windGust);
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
        Map<String, Object> forecast = self.getForecast();
        if (forecast == null || !forecast.containsKey("daily")) {
            return false;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> daily = (List<Map<String, Object>>) forecast.get("daily");

        return daily.stream()
                .filter(day -> {
                    long dt = ((Number) day.get("dt")).longValue();
                    LocalDate forecastDate = Instant.ofEpochSecond(dt)
                            .atZone(ZONE_ID)
                            .toLocalDate();
                    return forecastDate.equals(date);
                })
                .findFirst()
                .map(day -> {
                    int clouds = ((Number) day.get("clouds")).intValue();
                    return clouds > 80;
                })
                .orElse(false);
    }

    /**
     * Obtiene el pronóstico diario para una fecha específica
     */
    public DailyForecast getDailyForecast(LocalDate date) {
        Map<String, Object> forecast = self.getForecast();
        if (forecast == null || !forecast.containsKey("daily")) {
            return null;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> daily = (List<Map<String, Object>>) forecast.get("daily");

        Map<String, Object> dayData = daily.stream()
                .filter(day -> {
                    long dt = ((Number) day.get("dt")).longValue();
                    LocalDate forecastDate = Instant.ofEpochSecond(dt)
                            .atZone(ZONE_ID)
                            .toLocalDate();
                    return forecastDate.equals(date);
                })
                .findFirst()
                .orElse(null);

        if (dayData == null) {
            return null;
        }

        // Convertir Map a DailyForecast
        long dt = ((Number) dayData.get("dt")).longValue();
        double windSpeed = ((Number) dayData.get("windSpeed")).doubleValue();
        Double windGust = ((Number) dayData.get("windGust")).doubleValue();
        int clouds = ((Number) dayData.get("clouds")).intValue();

        return new DailyForecast(
                dt,
                0L, // sunrise - not available from 5-day API
                0L, // sunset - not available from 5-day API
                0L, // moonrise - not available from 5-day API
                0L, // moonset - not available from 5-day API
                0.0, // moonPhase - not available from 5-day API
                null, // summary - not available from 5-day API
                null, // temp - not used in this context
                null, // feelsLike - not used in this context
                0, // pressure - not used in this context
                0, // humidity - not used in this context
                0.0, // dewPoint - not used in this context
                windSpeed,
                0, // windDeg - not used in this context
                windGust,
                null, // weather - not used in this context
                clouds,
                0.0, // pop - not used in this context
                null, // rain - not used in this context
                null, // snow - not used in this context
                0.0 // uvi - not used in this context
        );
    }
}
