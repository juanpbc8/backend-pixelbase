package com.pixelbase.backend.modules.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pixelbase.backend.common.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        var apiError = new ApiError(
            Instant.now(),
            HttpServletResponse.SC_UNAUTHORIZED,
            "No autenticado. Debe proporcionar credenciales válidas en la cabecera Authorization para " +
                "acceder a este recurso.",
            null
        );

        response.setContentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        var writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(apiError));
        writer.flush(); // El contenedor de Spring se encarga de cerrar el stream automáticamente
    }
}
