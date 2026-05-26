package com.pixelbase.backend.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
    @Schema(example = "2026-05-25T14:10:00Z")
    Instant timestamp,

    @Schema(example = "400")
    int status,

    @Schema(example = "Error de validación en los campos del formulario.")
    String message,

    @Schema(description = "Lista detallada de campos inválidos. Solo aparece en contextos de error de "
        + "validación (HTTP 400).")
    List<ValidationDetail> errors
) {
    public record ValidationDetail(
        @Schema(example = "name")
        String field,

        @Schema(example = "El nombre es obligatorio")
        String message
    ) {
    }
}
