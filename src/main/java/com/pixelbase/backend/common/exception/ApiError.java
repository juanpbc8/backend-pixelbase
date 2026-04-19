package com.pixelbase.backend.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        LocalDateTime timestamp,
        int status,
        String message,
        List<ValidationDetail> errors // Solo aparece si hay errores de validación
) {
    public record ValidationDetail(String field, String message) {
    }
}