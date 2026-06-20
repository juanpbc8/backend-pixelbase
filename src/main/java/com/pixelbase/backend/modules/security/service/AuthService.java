package com.pixelbase.backend.modules.security.service;

import com.pixelbase.backend.common.exception.ConflictException;
import com.pixelbase.backend.modules.security.domain.UserDetailsImpl;
import com.pixelbase.backend.modules.security.dto.AuthResponse;
import com.pixelbase.backend.modules.security.dto.LoginRequest;
import com.pixelbase.backend.modules.security.dto.RegisterRequest;
import com.pixelbase.backend.modules.security.jwt.JwtService;
import com.pixelbase.backend.modules.user.exposed.UserExposedService;
import com.pixelbase.backend.modules.user.exposed.dto.UserAuthDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserExposedService userExposedService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        // 1. Autenticar: Spring Security llama internamente a UserDetailsServiceImpl
        // El AuthenticationManager lanzará DisabledException si enabled=false
        Authentication authenticated = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // 2. Recuperar el Principal (nuestro record UserDetailsImpl) del resultado de la autenticación
        UserDetailsImpl principal = (UserDetailsImpl) authenticated.getPrincipal();
        // Accedemos directamente a la entidad dentro del record
        UserAuthDto user = principal.user();

        // 3. Generar token usando el principal que ya tenemos
        String token = jwtService.generateToken(principal);

        return new AuthResponse(token, user.email(), user.role());
    }

    public AuthResponse registerClient(RegisterRequest request) {
        if (userExposedService.existsByEmail(request.email())) {
            throw new ConflictException("El email ya está registrado");
        }

        // Delegamos la creación al módulo "user". Security no sabe cómo se construye un usuario.
        userExposedService.saveClient(
            request.email(),
            passwordEncoder.encode(request.password())
        );

        // Auto-login tras registro
        return login(new LoginRequest(request.email(), request.password()));
    }

    public UserAuthDto registerAdmin(RegisterRequest request) {
        if (userExposedService.existsByEmail(request.email())) {
            throw new ConflictException("El email ya está registrado");
        }

        // Registramos y retornamos el DTO con la info del nuevo admin creado
        return userExposedService.saveAdmin(
            request.email(),
            passwordEncoder.encode(request.password())
        );
    }
}
