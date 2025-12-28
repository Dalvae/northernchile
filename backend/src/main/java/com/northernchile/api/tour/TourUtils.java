package com.northernchile.api.tour;

import com.northernchile.api.model.Tour;

import java.util.Map;

/**
 * Utility class for common Tour-related operations.
 */
public final class TourUtils {

    private TourUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Get the localized tour name with fallbacks.
     * Fallback order: preferredLang -> "es" -> any available -> displayName
     *
     * @param tour The tour entity
     * @param preferredLang The preferred language code (e.g., "es", "en", "pt")
     * @return The tour name in the best available language
     */
    public static String getTourName(Tour tour, String preferredLang) {
        if (tour == null) {
            return "Unknown Tour";
        }

        Map<String, String> names = tour.getNameTranslations();

        if (names == null || names.isEmpty()) {
            return tour.getDisplayName() != null ? tour.getDisplayName() : "Unknown Tour";
        }

        // Try preferred language
        if (preferredLang != null) {
            String name = names.get(preferredLang);
            if (name != null && !name.isBlank()) {
                return name;
            }
        }

        // Fallback to Spanish
        String spanishName = names.get("es");
        if (spanishName != null && !spanishName.isBlank()) {
            return spanishName;
        }

        // Fallback to any available translation
        return names.values().stream()
                .filter(n -> n != null && !n.isBlank())
                .findFirst()
                .orElse(tour.getDisplayName() != null ? tour.getDisplayName() : "Unknown Tour");
    }

    /**
     * Get the tour name with Spanish as default language.
     *
     * @param tour The tour entity
     * @return The tour name, preferring Spanish
     */
    public static String getTourName(Tour tour) {
        return getTourName(tour, "es");
    }
}
