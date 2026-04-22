package com.pixelbase.backend.modules.catalog.dto.response;

import java.util.List;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        List<CategoryResponse> subCategories // Recursividad para el menú de hardware
) {
}