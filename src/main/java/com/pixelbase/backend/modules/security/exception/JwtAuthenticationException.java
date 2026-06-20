package com.pixelbase.backend.modules.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Excepción puente para envolver los fallos matemáticos de JJWT
 * dentro del ecosistema de autenticación de Spring Security.
 */
public class JwtAuthenticationException extends AuthenticationException {

    public JwtAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
