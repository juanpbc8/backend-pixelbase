package com.pixelbase.backend.modules.order.api;

import com.pixelbase.backend.common.dto.PageResponse;
import com.pixelbase.backend.common.security.annotation.CurrentUserId;
import com.pixelbase.backend.modules.order.api.dto.response.CustomerOrderDetailResponse;
import com.pixelbase.backend.modules.order.api.dto.response.CustomerOrderSummaryResponse;
import com.pixelbase.backend.modules.order.service.OrderInternalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customer/orders")
@RequiredArgsConstructor
@Tag(name = "Customer Orders", description = "Panel privado para que los clientes consulten su historial.")
public class CustomerOrderController {

    private final OrderInternalService orderInternalService;

    @GetMapping
    @Operation(summary = "Listar mis compras de forma paginada", description = "Retorna el historial de " +
        "compras del cliente ordenado de forma cronológica descendente.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Historial de compras procesado exitosamente.")
    })
    public ResponseEntity<PageResponse<CustomerOrderSummaryResponse>> getMyOrders(
        @Parameter(hidden = true) @CurrentUserId Long authenticatedUserId,
        @ParameterObject // Desglosa page, size y sort limpiamente en Swagger
        @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        var response = orderInternalService.getCustomerOrdersHistory(authenticatedUserId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderCode}")
    @Operation(summary = "Ver detalle de una compra privada", description = "Muestra el detalle completo de" +
        " un pedido asegurando la propiedad del recurso.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detalle de orden procesado.")
    })
    public ResponseEntity<CustomerOrderDetailResponse> getMyOrderDetail(
        @Parameter(hidden = true) @CurrentUserId Long authenticatedUserId,
        @PathVariable String orderCode
    ) {
        var response = orderInternalService.getCustomerOrderDetail(orderCode, authenticatedUserId);
        return ResponseEntity.ok(response);
    }
}
