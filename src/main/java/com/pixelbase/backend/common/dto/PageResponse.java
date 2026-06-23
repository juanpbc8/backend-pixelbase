package com.pixelbase.backend.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
    List<T> content,
    int pageNumber,
    int pageSize,
    long totalElements,
    int totalPages,
    boolean last
) {
    /**
     * Fábrica estática agnóstica para mapear cualquier Page de Spring Data.
     * Desacopla los servicios y centraliza la transformación estructural del monolito.
     */
    public static <T> PageResponse<T> from(Page<T> springPage) {
        return new PageResponse<>(
            springPage.getContent(),
            springPage.getNumber(),
            springPage.getSize(),
            springPage.getTotalElements(),
            springPage.getTotalPages(),
            springPage.isLast()
        );
    }
}
