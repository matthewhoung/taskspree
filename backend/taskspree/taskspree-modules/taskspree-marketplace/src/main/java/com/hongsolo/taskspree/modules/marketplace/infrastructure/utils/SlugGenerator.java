package com.hongsolo.taskspree.modules.marketplace.infrastructure.utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility for generating URL-friendly slugs from strings.
 */
public final class SlugGenerator {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern MULTIPLE_DASHES = Pattern.compile("-+");

    private SlugGenerator() {
        // Prevent instantiation
    }

    /**
     * Generate a URL-friendly slug from a string.
     *
     * @param input The input string (e.g., marketplace name)
     * @return A lowercase, hyphenated slug
     */
    public static String generate(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Input cannot be null or blank");
        }

        String noWhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(noWhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        slug = MULTIPLE_DASHES.matcher(slug).replaceAll("-");
        slug = slug.toLowerCase(Locale.ENGLISH);

        // Remove leading and trailing dashes
        slug = slug.replaceAll("^-+", "").replaceAll("-+$", "");

        if (slug.isEmpty()) {
            throw new IllegalArgumentException("Input produced empty slug");
        }

        return slug;
    }

    /**
     * Generate a unique slug by appending a suffix if needed.
     *
     * @param baseSlug The base slug
     * @param suffix   A unique suffix (e.g., UUID portion)
     * @return A unique slug
     */
    public static String generateUnique(String baseSlug, String suffix) {
        return baseSlug + "-" + suffix;
    }
}