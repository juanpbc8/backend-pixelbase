package com.pixelbase.backend.modules.security.api.admin;

import com.pixelbase.backend.modules.security.api.web.dto.request.RegisterRequest;
import com.pixelbase.backend.modules.security.service.AuthService;
import com.pixelbase.backend.modules.user.exposed.dto.UserAuthDto;
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
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin Users Management", description = "Endpoints de administración para gestión de usuarios")
public class AdminUserController {

    private final AuthService authService;

    @PostMapping("/register-admin")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Usuario administrador registrado exitosamente"
        ),
        @ApiResponse(
            responseCode = "409",
            description = "El email ya existe"
        )
    })
    public ResponseEntity<UserAuthDto> registerAdmin(@Valid @RequestBody RegisterRequest request) {
        UserAuthDto newAdmin = authService.registerAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newAdmin);
    }
}
