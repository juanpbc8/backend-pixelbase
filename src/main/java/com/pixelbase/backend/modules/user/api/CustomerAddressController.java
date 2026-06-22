package com.pixelbase.backend.modules.user.api;

import com.pixelbase.backend.common.security.annotation.CurrentUserId;
import com.pixelbase.backend.modules.user.api.dto.request.CustomerAddressSaveRequest;
import com.pixelbase.backend.modules.user.api.dto.response.CustomerAddressResponse;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/customer/addresses")
@RequiredArgsConstructor
@Tag(name = "Customer Addresses", description = "Endpoints para el CRUD de la libreta de direcciones " +
    "logísticas del cliente.")
public class CustomerAddressController {

    private final UserInternalService userInternalService;

    @GetMapping
    @Operation(
        summary = "Listar direcciones de envío",
        description = "Recupera la colección completa de ubicaciones geográficas de entrega asociadas a la " +
            "cuenta."
    )
    @ApiResponse(responseCode = "200", description = "Colección de direcciones devuelta con éxito (puede " +
        "ser una lista vacía).")
    public ResponseEntity<List<CustomerAddressResponse>> getAddresses(
        @Parameter(hidden = true) @CurrentUserId Long authenticatedUserId
    ) {
        List<CustomerAddressResponse> responses = userInternalService.getAddresses(authenticatedUserId);
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @Operation(
        summary = "Registrar nueva dirección",
        description = "Guarda una ubicación logística en la cuenta. Si es la primera, el sistema fuerza " +
            "automáticamente el flag por defecto."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Dirección guardada exitosamente.")
    })
    public ResponseEntity<CustomerAddressResponse> createAddress(
        @Parameter(hidden = true) @CurrentUserId Long authenticatedUserId,
        @Valid @RequestBody CustomerAddressSaveRequest request
    ) {
        CustomerAddressResponse response = userInternalService.createAddress(authenticatedUserId, request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.id())
            .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Modificar dirección existente",
        description = "Actualiza los datos de una dirección. Cuenta con blindaje Anti-IDOR: Si el ID no " +
            "pertenece al cliente, arroja 404."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dirección modificada con éxito.")
    })
    public ResponseEntity<CustomerAddressResponse> updateAddress(
        @PathVariable Long id,
        @Valid @RequestBody CustomerAddressSaveRequest request,
        @Parameter(hidden = true) @CurrentUserId Long authenticatedUserId
    ) {
        CustomerAddressResponse response = userInternalService.updateAddress(id,
            authenticatedUserId,
            request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/default")
    @Operation(
        summary = "Establecer dirección predeterminada rápida",
        description = "Cambia el flag principal de envío. Automáticamente apaga el flag por defecto de " +
            "todas las demás ubicaciones del cliente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Flag predeterminado rotado exitosamente.")
    })
    public ResponseEntity<CustomerAddressResponse> changeDefaultAddress(
        @PathVariable Long id,
        @Parameter(hidden = true) @CurrentUserId Long authenticatedUserId
    ) {
        CustomerAddressResponse response = userInternalService.changeDefaultAddress(id, authenticatedUserId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar dirección",
        description = "Remueve físicamente la ubicación. Si se borra la dirección por defecto actual, " +
            "hereda automáticamente el flag a la dirección restante más antigua."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Dirección eliminada exitosamente")
    })
    public ResponseEntity<Void> deleteAddress(
        @PathVariable Long id,
        @Parameter(hidden = true) @CurrentUserId Long authenticatedUserId
    ) {
        userInternalService.deleteAddress(id, authenticatedUserId);
        return ResponseEntity.noContent().build();
    }
}
