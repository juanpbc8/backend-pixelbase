package com.pixelbase.backend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Recursos no encontrados (404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    // Conflictos de negocio (409) - Ej: Email ya registrado
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    // Errores de solicitud (400) - Ej: Datos mal formados, parámetros inválidos
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    // Errores de validación (400) - @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiError.ValidationDetail> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ApiError.ValidationDetail(error.getField(), error.getDefaultMessage()))
                .toList();

        return buildResponse(HttpStatus.BAD_REQUEST, "Error de validación en los campos", errors);
    }

    // JSON Mal formado o tipos de datos incorrectos (400)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String errorDetail = "El cuerpo de la solicitud (JSON) no es válido o tiene un formato incorrecto.";

        // Opcional: Extraer un mensaje un poco más técnico para desarrollo
        if (ex.getCause() != null) {
            errorDetail = "Error de formato: " + ex.getMostSpecificCause().getMessage();
        }

        return buildResponse(HttpStatus.BAD_REQUEST, errorDetail, null);
    }

    // Error de tipo en parámetros de URL (400) - Ej: /users/abc en lugar de /users/123
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("El parámetro '%s' debe ser de tipo %s",
                ex.getName(), ex.getRequiredType().getSimpleName());

        return buildResponse(HttpStatus.BAD_REQUEST, message, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        // Loguea el error real en la consola/archivo
        ex.printStackTrace();

        // Al usuario (y al frontend) solo dale un mensaje genérico
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ha ocurrido un error inesperado en el servidor", null);
    }

    // Utilitario para no repetir código
    private ResponseEntity<ApiError> buildResponse(HttpStatus status, String message, List<ApiError.ValidationDetail> errors) {
        ApiError apiError = new ApiError(LocalDateTime.now(), status.value(), message, errors);
        return new ResponseEntity<>(apiError, status);
    }
}