package com.pixelbase.backend.modules.order.controller.web;

import com.pixelbase.backend.common.security.annotation.CurrentUserId;
import com.pixelbase.backend.modules.order.dto.request.OrderCreateRequest;
import com.pixelbase.backend.modules.order.dto.response.OrderCreateResponse;
import com.pixelbase.backend.modules.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api/v1/public/orders")
@RequiredArgsConstructor
@Tag(
    name = "Ordenes - Storefront",
    description = "Endpoints públicos para ordenes"
)
public class OrderController {
    private final OrderService orderService;

    // Le dice a Swagger que habilite el candado en este endpoint, pero el endpoint sigue siendo accesible
    // para invitados (sin token) gracias a la configuración de seguridad.
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @Operation(
        summary = "Procesar Checkout (Crear Pedido)",
        description = "Valida el carrito en caliente contra el catálogo, congela los snapshots comerciales " +
            "de precios/SKU, descuenta stock de forma síncrona y genera la sesión agnóstica de pagos. " +
            "Soporta compras de clientes autenticados e invitados."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Pedido registrado y confirmado en el sistema con éxito."
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Uno de los slugs de los productos enviados no existe en el catálogo."
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Stock insuficiente u otra lógica de negocio interna"
        ),
    })
    public ResponseEntity<OrderCreateResponse> createOrder(
        @Valid @RequestBody OrderCreateRequest request,
        @Parameter(hidden = true) @CurrentUserId Long authenticatedUserId
    ) {
        log.info("UserId recuperado en el controlador de forma transparente: {}", authenticatedUserId);
        OrderCreateResponse response = orderService.createOrder(request, authenticatedUserId);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{orderCode}")
            .buildAndExpand(response.orderCode())
            .toUri();

        // Retornar el payload agnóstico para que el frontend pinte la UI o procese el botón de la pasarela
        return ResponseEntity.created(location).body(response);
    }
}
