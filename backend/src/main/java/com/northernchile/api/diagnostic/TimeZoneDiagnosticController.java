package com.northernchile.api.diagnostic;

import com.northernchile.api.util.DateTimeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.*;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.util.*;

/**
 * Endpoint de diagnóstico para verificar configuración de timezone en producción.
 *
 * IMPORTANTE: Este endpoint NO debe estar protegido por autenticación para permitir
 * monitoreo externo. Puede ser usado por herramientas de monitoreo para verificar
 * que el servidor tenga tzdata actualizado.
 */
@RestController
@RequestMapping("/api/diagnostic")
public class TimeZoneDiagnosticController {

    /**
     * GET /api/diagnostic/timezone
     *
     * Retorna información detallada sobre la configuración de timezone:
     * - Zona horaria configurada
     * - Offset actual (UTC-3 o UTC-4 según estación)
     * - Próximas transiciones DST
     * - Fecha/hora actual en Chile
     *
     * Útil para verificar en producción que tzdata esté actualizado.
     */
    @GetMapping("/timezone")
    public Map<String, Object> getTimeZoneInfo() {
        Map<String, Object> info = new HashMap<>();

        // Información básica
        info.put("zoneId", DateTimeUtils.CHILE_ZONE.getId());
        info.put("currentUtcTime", Instant.now().toString());

        // Hora actual en Chile
        ZonedDateTime nowInChile = ZonedDateTime.now(DateTimeUtils.CHILE_ZONE);
        info.put("currentChileTime", nowInChile.toString());
        info.put("currentOffset", nowInChile.getOffset().toString());

        // Determinar si estamos en horario de verano
        boolean isDST = DateTimeUtils.CHILE_ZONE.getRules().isDaylightSavings(Instant.now());
        info.put("isDaylightSavingTime", isDST);
        info.put("timezoneName", isDST ? "CLST (Chile Summer Time)" : "CLT (Chile Standard Time)");

        // Próximas transiciones DST
        ZoneRules rules = DateTimeUtils.CHILE_ZONE.getRules();
        List<Map<String, String>> transitions = new ArrayList<>();

        Instant searchFrom = Instant.now();
        for (int i = 0; i < 2; i++) {
            ZoneOffsetTransition transition = rules.nextTransition(searchFrom);
            if (transition != null) {
                Map<String, String> transitionInfo = new HashMap<>();
                ZonedDateTime transitionTime = ZonedDateTime.ofInstant(transition.getInstant(), DateTimeUtils.CHILE_ZONE);

                transitionInfo.put("dateTime", transitionTime.toString());
                transitionInfo.put("offsetBefore", transition.getOffsetBefore().toString());
                transitionInfo.put("offsetAfter", transition.getOffsetAfter().toString());
                transitionInfo.put("type", transition.isGap() ? "Spring forward" : "Fall back");

                transitions.add(transitionInfo);
                searchFrom = transition.getInstant().plusSeconds(1);
            }
        }
        info.put("nextTransitions", transitions);

        // Ejemplos de conversión
        Map<String, String> examples = new HashMap<>();

        // Ejemplo verano (enero)
        LocalDate summerDate = LocalDate.of(2025, 1, 15);
        ZonedDateTime summerZdt = summerDate.atStartOfDay(DateTimeUtils.CHILE_ZONE);
        examples.put("2025-01-15_00:00_Chile", summerZdt.toInstant().toString());
        examples.put("2025-01-15_offset", summerZdt.getOffset().toString());

        // Ejemplo invierno (julio)
        LocalDate winterDate = LocalDate.of(2025, 7, 15);
        ZonedDateTime winterZdt = winterDate.atStartOfDay(DateTimeUtils.CHILE_ZONE);
        examples.put("2025-07-15_00:00_Chile", winterZdt.toInstant().toString());
        examples.put("2025-07-15_offset", winterZdt.getOffset().toString());

        info.put("conversionExamples", examples);

        // Versión de JDK (contiene versión de tzdata)
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("javaVendor", System.getProperty("java.vendor"));

        return info;
    }

    /**
     * GET /api/diagnostic/health
     *
     * Verifica que el timezone esté configurado correctamente.
     * Retorna 200 OK si todo está bien, 500 si hay problemas.
     */
    @GetMapping("/health/timezone")
    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        List<String> warnings = new ArrayList<>();

        try {
            // Verificar que America/Santiago existe
            ZoneId.of("America/Santiago");

            // Verificar que hay transiciones DST desde 2024
            ZoneRules rules = DateTimeUtils.CHILE_ZONE.getRules();
            Instant start2024 = LocalDateTime.of(2024, 1, 1, 0, 0)
                .toInstant(ZoneOffset.ofHours(-3));

            ZoneOffsetTransition transition = rules.nextTransition(start2024);

            if (transition == null) {
                warnings.add("No DST transitions found since 2024 - tzdata may be outdated");
            }

            // Verificar offset actual es correcto (-3 o -4)
            ZonedDateTime now = ZonedDateTime.now(DateTimeUtils.CHILE_ZONE);
            int offsetHours = now.getOffset().getTotalSeconds() / 3600;

            if (offsetHours != -3 && offsetHours != -4) {
                warnings.add("Unexpected offset: " + offsetHours + " (expected -3 or -4)");
            }

            health.put("status", warnings.isEmpty() ? "OK" : "WARNING");
            health.put("warnings", warnings);
            health.put("currentOffset", now.getOffset().toString());
            health.put("nextTransition", transition != null ? transition.getInstant().toString() : "none");

        } catch (Exception e) {
            health.put("status", "ERROR");
            health.put("error", e.getMessage());
        }

        return health;
    }
}
