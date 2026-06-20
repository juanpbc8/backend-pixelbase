package com.pixelbase.backend.modules.security.jwt;

import com.pixelbase.backend.modules.security.exception.JwtAuthenticationException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    // Inyectamos el resolver de excepciones global de Spring para conectar los filtros con tu EntryPoint
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Si no hay header o no comienza con "Bearer ", continuar con el siguiente filtro
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Limpiar espacios en blanco accidentales o maliciosos (Evita el bypass de "Bearer ")
        final String jwt = authHeader.substring(7).trim();
        if (jwt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            // Intento de extracción del sujeto (Aquí es donde un token corrupto explotará)
            String userEmail = jwtService.extractSubject(jwt);

            // Si el email no es nulo y el usuario no está autenticado aún
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Cargar los detalles del usuario desde la base de datos
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Validar el token
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // Crear el objeto de autenticación
                    var authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );

                    // Establecer detalles adicionales de la petición
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Actualizar el SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            // Si algo falla aquí (como un token expirado), Spring Security
            // lo detectará automáticamente y activará tu RestAuthenticationEntryPoint.
            // Continuar con la cadena de filtros
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            log.error("Error crítico de autenticación JWT interceptado en el filtro: {}", e.getMessage());

            // Limpiar el contexto por seguridad y delegar al HandlerExceptionResolver
            // Esto forzará a Spring a invocar tu RestAuthenticationEntryPoint de manera determinista,
            // garantizando que el Frontend reciba tu JSON ApiError (401) en lugar de un HTML 500 del
            // servidor.
            SecurityContextHolder.clearContext();
            handlerExceptionResolver.resolveException(request,
                response, null,
                new JwtAuthenticationException("Token inválido o expirado.", e));
        }
    }
}
