package com.pixelbase.backend.modules.user.api;

import com.pixelbase.backend.common.security.annotation.CurrentUserId;
import com.pixelbase.backend.modules.user.api.dto.request.CustomerProfileUpdateRequest;
import com.pixelbase.backend.modules.user.api.dto.response.CustomerProfileResponse;
import com.pixelbase.backend.modules.user.service.UserInternalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer/profile")
@RequiredArgsConstructor
@Tag(name = "Customer Profile", description = "Endpoints para la gestión de datos personales del cliente " +
    "autenticado.")
public class CustomerProfileController {

    private final UserInternalService userInternalService;

    @GetMapping
    @Operation(
        summary = "Obtener datos del perfil",
        description = "Recupera de forma segura los datos personales del cliente actual utilizando la " +
            "identidad del JWT."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil recuperado con éxito."),
    })
    public ResponseEntity<CustomerProfileResponse> getProfile(
        @Parameter(hidden = true) @CurrentUserId Long authenticatedUserId
    ) {
        CustomerProfileResponse response = userInternalService.getProfile(authenticatedUserId);
        return ResponseEntity.ok(response);
    }

    @Valid
    @PutMapping
    @Operation(
        summary = "Actualizar datos del perfil",
        description = "Modifica los campos editables del perfil actual. Valida de forma síncrona la " +
            "unicidad del número de documento."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil actualizado exitosamente."),
        @ApiResponse(responseCode = "409", description = "El número de documento ya está siendo usado por " +
            "otra cuenta.")
    })
    public ResponseEntity<CustomerProfileResponse> updateProfile(
        @Parameter(hidden = true) @CurrentUserId Long authenticatedUserId,
        @Valid @RequestBody CustomerProfileUpdateRequest request
    ) {
        CustomerProfileResponse response = userInternalService.updateProfile(authenticatedUserId, request);
        return ResponseEntity.ok(response);
    }
}
