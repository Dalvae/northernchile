package com.northernchile.api.weather;

import com.northernchile.api.external.WeatherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador para datos meteorológicos
 * Expone pronóstico del clima usando OpenWeatherMap Free API
 */
@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    /**
     * Obtiene el pronóstico de 5 días con datos de 3 horas
     *
     * GET /api/weather/forecast
     *
     * Retorna el pronóstico agrupado por día con información relevante:
     * - Temperatura máxima y mínima
     * - Velocidad del viento
     * - Cobertura de nubes
     * - Probabilidad de precipitación
     */
    @GetMapping("/forecast")
    public Object getForecast() {
        return weatherService.getForecast();
    }
}
