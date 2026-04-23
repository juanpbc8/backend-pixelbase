package com.pixelbase.backend.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtils {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final Pattern EDGES = Pattern.compile("(^-+)|(-+$)");

    public static String toSlug(String input) {
        if (input == null) return "";

        // 1. Quitar espacios al inicio y final
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");

        // 2. Normalización: Convierte "é" en "e" + "´" (diacrítico)
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);

        // 3. Eliminar los diacríticos (las tildes) usando Regex de bloques Unicode
        String slug = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // 4. Limpieza final: minúsculas, quitar caracteres raros y guiones sobrantes
        slug = NONLATIN.matcher(slug).replaceAll("");
        slug = slug.toLowerCase(Locale.ENGLISH);
        slug = EDGES.matcher(slug).replaceAll("");

        return slug;
    }
}