package com.pixelbase.backend.modules.security.service;

import com.pixelbase.backend.common.exception.ConflictException;
import com.pixelbase.backend.modules.security.domain.UserDetailsImpl;
import com.pixelbase.backend.modules.security.dto.AuthResponse;
import com.pixelbase.backend.modules.security.dto.LoginRequest;
import com.pixelbase.backend.modules.security.dto.RegisterRequest;
import com.pixelbase.backend.modules.security.jwt.JwtService;
import com.pixelbase.backend.modules.user.domain.Role;
import com.pixelbase.backend.modules.user.domain.UserEntity;
import com.pixelbase.backend.modules.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final IUserService userService;
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
        UserEntity user = principal.user();

        // 3. Generar token usando el principal que ya tenemos
        String token = jwtService.generateToken(principal);

        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    public AuthResponse register(RegisterRequest request) {
        if (userService.existsByEmail(request.email())) {
            throw new ConflictException("El email ya está registrado");
        }

        UserEntity newUser = UserEntity.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.CUSTOMER) // Por defecto para ecommerce
                .enabled(true)
                .build();

        userService.register(newUser);

        // Auto-login tras registro
        return login(new LoginRequest(request.email(), request.password()));
    }
}