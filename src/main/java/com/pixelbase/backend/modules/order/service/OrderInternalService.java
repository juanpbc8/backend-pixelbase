package com.pixelbase.backend.modules.order.service;

import com.pixelbase.backend.common.dto.PageResponse;
import com.pixelbase.backend.modules.order.api.customer.dto.response.CustomerOrderDetailResponse;
import com.pixelbase.backend.modules.order.api.customer.dto.response.CustomerOrderSummaryResponse;
import com.pixelbase.backend.modules.order.api.web.dto.request.OrderCreateRequest;
import com.pixelbase.backend.modules.order.api.web.dto.response.OrderCreateResponse;
import org.springframework.data.domain.Pageable;

public interface OrderInternalService {
    /**
     * Orquesta la creación transaccional de un pedido en el sistema
     * y genera la sesión de pago con la pasarela correspondiente.
     *
     * @param request             Datos del formulario de checkout enviado por el cliente.
     * @param authenticatedUserId Id del usuario si está logueado, null si es invitado.
     * @return Resumen de la orden creada con los datos para la pasarela de pago.
     */
    OrderCreateResponse createOrder(OrderCreateRequest request, Long authenticatedUserId);

    PageResponse<CustomerOrderSummaryResponse> getCustomerOrdersHistory(Long userId, Pageable pageable);

    CustomerOrderDetailResponse getCustomerOrderDetail(String orderCode, Long userId);

    // --- SOLUCIÓN GUEST: CONSULTA PÚBLICA / PÁGINA DE ÉXITO ---
    CustomerOrderDetailResponse getPublicOrderDetail(String orderCode, String email);
}
