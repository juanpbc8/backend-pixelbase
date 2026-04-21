package com.pixelbase.backend.modules.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

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

        // Extraer el token JWT (después de "Bearer ")
        String jwt = authHeader.substring(7);

        String userEmail = jwtService.extractSubject(jwt);

        // Si el email no es nulo y el usuario no está autenticado aún
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Cargar los detalles del usuario desde la base de datos
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // Validar el token
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // Crear el objeto de autenticación
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
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
    }
}