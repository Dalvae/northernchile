package com.northernchile.api.util;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Utility for generating SEO-friendly URL slugs from text.
 * Converts strings like "Tour Astronómico" to "tour-astronomico"
 */
@Component
public class SlugGenerator {

    /**
     * Generates a URL-friendly slug from the given text.
     *
     * @param text The text to convert to a slug
     * @return A lowercase, hyphenated slug with no special characters
     */
    public String generateSlug(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        // Normalize to NFD (decompose accented characters)
        String normalized = Normalizer.normalize(text.trim(), Normalizer.Form.NFD);

        // Remove diacritical marks (accents)
        String withoutAccents = normalized.replaceAll("\\p{M}", "");

        // Convert to lowercase
        String lowercase = withoutAccents.toLowerCase(Locale.ROOT);

        // Replace spaces and non-alphanumeric characters with hyphens
        String slug = lowercase.replaceAll("[^a-z0-9]+", "-");

        // Remove leading/trailing hyphens
        slug = slug.replaceAll("^-+|-+$", "");

        return slug;
    }

    /**
     * Generates a unique slug by appending a suffix if needed.
     *
     * @param baseText The base text to convert to a slug
     * @param suffix Optional suffix to append (e.g., a number for uniqueness)
     * @return A slug with optional suffix
     */
    public String generateSlug(String baseText, String suffix) {
        String baseSlug = generateSlug(baseText);

        if (suffix != null && !suffix.isBlank()) {
            return baseSlug + "-" + suffix;
        }

        return baseSlug;
    }
}
