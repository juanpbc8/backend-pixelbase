package com.pixelbase.backend.modules.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pixelbase.backend.common.exception.ApiError;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        var apiError = new ApiError(
            Instant.now(),
            HttpServletResponse.SC_FORBIDDEN,
            "Acceso denegado. No tienes los privilegios necesarios para realizar esta operación.",
            null
        );

        // Aplicamos el mismo estándar de Clean Code
        response.setContentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(java.nio.charset.StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        var writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(apiError));
        writer.flush();
    }
}
