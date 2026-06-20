package com.pixelbase.backend.modules.security.controller.web;

import com.pixelbase.backend.modules.security.dto.AuthResponse;
import com.pixelbase.backend.modules.security.dto.LoginRequest;
import com.pixelbase.backend.modules.security.dto.RegisterRequest;
import com.pixelbase.backend.modules.security.service.AuthService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints para login y registro")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "401",
            description = "Credenciales inválidas"
        )
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Usuario registrado exitosamente"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "El email ya existe"
        )
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerClient(request));
    }
}
