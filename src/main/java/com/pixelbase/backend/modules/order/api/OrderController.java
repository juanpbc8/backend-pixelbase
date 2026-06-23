package com.pixelbase.backend.modules.order.api;

import com.pixelbase.backend.common.security.annotation.CurrentUserId;
import com.pixelbase.backend.modules.order.api.dto.request.OrderCreateRequest;
import com.pixelbase.backend.modules.order.api.dto.response.CustomerOrderDetailResponse;
import com.pixelbase.backend.modules.order.api.dto.response.OrderCreateResponse;
import com.pixelbase.backend.modules.order.service.OrderInternalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/public/orders")
@RequiredArgsConstructor
@Tag(
    name = "Ordenes - Storefront",
    description = "Endpoints públicos para ordenes"
)
public class OrderController {
    private final OrderInternalService orderInternalService;

    // Le dice a Swagger que habilite el candado en este endpoint, pero el endpoint sigue siendo accesible
    // para invitados (sin token) gracias a la configuración de seguridad.
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @Operation(
        summary = "Procesar Checkout (Crear Pedido - Cliente/Invitado)",
        description = """
            Valida el carrito en caliente contra el catálogo, congela los precios de forma síncrona, \
            reduce inventario y genera la sesión para la pasarela de pagos.

            ### Reglas Logísticas del Contrato:
            * **Si `deliveryType` es `A_DOMICILIO`:** El objeto `address` es **obligatorio** con todos sus campos geográficos, y el campo `storeId` debe viajar como `null`.

            * **Si `deliveryType` es `RECOJO_EN_TIENDA`:** El campo `storeId` es **obligatorio** (ID de sede válida), y el objeto `address` debe viajar como `null`.

            *
            ### Control de Identidad:
            El endpoint es público. Si se envía un JWT válido, la orden se asocia automáticamente a la cuenta del cliente. \
            Si no se envía token, el backend procesa la transacción bajo la modalidad de **GUEST (Invitado)**.
            """
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
            description = "Stock insuficiente o violación de las reglas cruzadas de entrega."
        ),
    })
    public ResponseEntity<OrderCreateResponse> createOrder(
        @Valid @RequestBody OrderCreateRequest request,
        @Parameter(hidden = true) @CurrentUserId Long authenticatedUserId
    ) {
        OrderCreateResponse response = orderInternalService.createOrder(request, authenticatedUserId);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{orderCode}")
            .buildAndExpand(response.orderCode())
            .toUri();

        // Retornar el payload agnóstico para que el frontend pinte la UI o procese el botón de la pasarela
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{orderCode}")
    @Operation(
        summary = "Consulta pública de orden (Invitados / Página de Éxito)",
        description = """
            Permite al frontend pintar los detalles del pedido recién comprado o realizar un tracking sin necesidad de un token de inicio de sesión.

            ### Control de Seguridad:
            Exige como parámetro obligatorio el correo electrónico (`email`) del comprador original para evitar fugas de información por enumeración de URL."""
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Información de la orden devuelta con éxito."),
    })
    public ResponseEntity<CustomerOrderDetailResponse> getPublicOrderDetail(
        @PathVariable String orderCode,
        @RequestParam String email
    ) {
        // Reutiliza la misma lógica de negocio centralizada mapeando a DTO sin fricciones
        CustomerOrderDetailResponse response = orderInternalService.getPublicOrderDetail(orderCode, email);
        return ResponseEntity.ok(response);
    }
}
