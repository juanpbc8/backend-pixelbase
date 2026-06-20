package com.pixelbase.backend.modules.order.service;

import com.pixelbase.backend.modules.order.dto.request.OrderCreateRequest;
import com.pixelbase.backend.modules.order.dto.response.OrderCreateResponse;

public interface OrderService {
    /**
     * Orquesta la creación transaccional de un pedido en el sistema
     * y genera la sesión de pago con la pasarela correspondiente.
     *
     * @param request             Datos del formulario de checkout enviado por el cliente.
     * @param authenticatedUserId Id del usuario si está logueado, null si es invitado.
     * @return Resumen de la orden creada con los datos para la pasarela de pago.
     */
    OrderCreateResponse createOrder(OrderCreateRequest request, Long authenticatedUserId);
}
