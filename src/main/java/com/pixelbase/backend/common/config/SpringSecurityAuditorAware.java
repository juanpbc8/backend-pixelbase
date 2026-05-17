package com.pixelbase.backend.common.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Implementación de AuditorAware que extrae el email del usuario autenticado desde el SecurityContext.
 * Devuelve 'SYSTEM' cuando no hay un principal autenticado disponible.
 */
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    private static final String SYSTEM = "SYSTEM";
    private static final String ANONYMOUS = "anonymousUser";

    @Override
    @NonNull
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Si no hay autenticación o el usuario es el objeto anónimo de Spring Security
        if (authentication == null || !authentication.isAuthenticated() || ANONYMOUS.equalsIgnoreCase(authentication.getName())) {
            return Optional.of(SYSTEM);
        }

        // authentication.getName() ya resuelve internamente si el principal es UserDetails o String
        return Optional.ofNullable(authentication.getName()).or(() -> Optional.of(SYSTEM));
    }
}
