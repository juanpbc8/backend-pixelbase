package com.pixelbase.backend.common.exception;

import com.pixelbase.backend.modules.security.exception.JwtAuthenticationException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    // 404 - Recursos no encontrados
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    // 404 - Recursos no encontrados lanzados por JPA
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleEntityNotFound(EntityNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    // 409 - Conflictos de negocio - Ej.: Email ya registrado
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    // 400 - Errores de solicitud - Ej.: Datos mal formados, parámetros inválidos
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    // 400 - Errores de validación - @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiError.ValidationDetail> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new ApiError.ValidationDetail(error.getField(), error.getDefaultMessage()))
            .toList();

        return buildResponse(HttpStatus.BAD_REQUEST, "Error de validación en los campos", errors);
    }

    // 400 - JSON Mal formado o tipos de datos incorrectos
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String errorDetail = "El cuerpo de la solicitud (JSON) no es válido o tiene un formato incorrecto.";

        // Opcional: Extraer un mensaje un poco más técnico para desarrollo
        if (ex.getCause() != null) {
            errorDetail = "Error de formato: " + ex.getMostSpecificCause().getMessage();
        }

        return buildResponse(HttpStatus.BAD_REQUEST, errorDetail, null);
    }

    // 400 - Error de tipo en parámetros de URL - Ej: /users/abc en lugar de /users/123
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String typeName = ex.getRequiredType() != null
            ? ex.getRequiredType().getSimpleName()
            : "desconocido";
        String message = String.format("El parámetro '%s' debe ser de tipo %s",
            ex.getName(), typeName);

        return buildResponse(HttpStatus.BAD_REQUEST, message, null);
    }

    // 401 - Intento de Login Tradicional Erróneo
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredential(BadCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED,
            "Credenciales inválidas. Por favor, verifica tu email y contraseña.",
            null);
    }

    // 401 - Tokens alterados o expirados capturados por el filtro de seguridad
    @ExceptionHandler(JwtAuthenticationException.class)
    public ResponseEntity<ApiError> handleJwtAuthenticationException(JwtAuthenticationException ex) {
        String customMsg = ex.getMessage() + " Por favor, limpie su sesión e intente nuevamente.";
        return buildResponse(HttpStatus.UNAUTHORIZED, customMsg, null);
    }

    // 401 - Intento anónimo de consumir endpoints protegidos (EntryPoint General)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleGenericAuthenticationException(AuthenticationException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED,
            "No autenticado. Debe proporcionar credenciales válidas en la cabecera para acceder a este " +
                "recurso.",
            null);
    }

    // 403 - Intento de violación de roles retransmitido por el AccessDeniedHandler
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN,
            "Acceso denegado. No tienes los privilegios necesarios para realizar esta operación.",
            null);
    }

    // 400 - Carga de imágenes excesiva
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        String message = String.format(
            "El archivo o la petición excede el límite de tamaño permitido por el sistema (Máx: %s).",
            maxFileSize
        );

        return buildResponse(HttpStatus.BAD_REQUEST, message, null);
    }

    // 500 - Red de seguridad final de la CPU para cualquier excepción no manejada
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        log.error("Error no controlado capturado: {}", ex.getMessage(), ex);

        // Al usuario (y al frontend) solo dale un mensaje genérico
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
            "Ha ocurrido un error inesperado en el servidor",
            null);
    }

    // Utilitario para no repetir código
    private ResponseEntity<ApiError> buildResponse(HttpStatus status,
                                                   String message,
                                                   List<ApiError.ValidationDetail> errors) {
        ApiError apiError = new ApiError(Instant.now(), status.value(), message, errors);
        return new ResponseEntity<>(apiError, status);
    }
}
