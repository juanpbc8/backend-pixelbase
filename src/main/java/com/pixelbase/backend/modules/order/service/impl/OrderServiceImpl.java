package com.pixelbase.backend.modules.order.service.impl;

import com.pixelbase.backend.common.exception.ConflictException;
import com.pixelbase.backend.common.exception.ResourceNotFoundException;
import com.pixelbase.backend.modules.catalog.exposed.CatalogExposedService;
import com.pixelbase.backend.modules.catalog.exposed.dto.ProductSharedDto;
import com.pixelbase.backend.modules.configuration.exposed.StoreExposedService;
import com.pixelbase.backend.modules.configuration.exposed.dto.StoreSharedDto;
import com.pixelbase.backend.modules.order.domain.*;
import com.pixelbase.backend.modules.order.dto.request.OrderCreateRequest;
import com.pixelbase.backend.modules.order.dto.response.OrderCreateResponse;
import com.pixelbase.backend.modules.order.infrastructure.payment.PaymentGatewayService;
import com.pixelbase.backend.modules.order.infrastructure.payment.dto.PaymentSessionResult;
import com.pixelbase.backend.modules.order.mapper.OrderAddressMapper;
import com.pixelbase.backend.modules.order.mapper.OrderMapper;
import com.pixelbase.backend.modules.order.repository.OrderRepository;
import com.pixelbase.backend.modules.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CatalogExposedService catalogExposedService;
    private final StoreExposedService storeExposedService;
    private final OrderMapper orderMapper;
    private final OrderAddressMapper orderAddressMapper;
    private final PaymentGatewayService paymentGatewayService;

    @Override
    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request, Long authenticatedUserId) {
        log.info("Iniciando flujo transaccional de checkout para el cliente: {}", request.customer().email());

        // 1. Validaciones de Negocio Cruzadas
        validateDeliveryRules(request);

        // 2. Procesamiento de Ítems e Inventario (Falla aquí si no hay stock o no existe el producto)
        List<OrderItemEntity> orderItemEntities = processAndValidateItems(request.items());

        BigDecimal totalPrice = orderItemEntities.stream()
            .map(item ->
                item.getPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity()))
            )
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Construcción de Dirección (Falla aquí si la tienda está inactiva o no existe)
        OrderAddressEntity addressEntity = buildOrderAddress(request);

        // 4. Generación de Código Correlativo mediante Secuencia
        String orderCode = generateOrderCode();

        // 5. Mapeo Principal Unificado (Inyecta hijos y ejecuta el @AfterMapping automáticamente)
        OrderEntity orderEntity = orderMapper.toOrderEntity(
            request,
            orderCode,
            totalPrice,
            authenticatedUserId,
            orderItemEntities,
            addressEntity
        );

        // 6. Persistencia Limpia en Cascada
        OrderEntity savedOrder = orderRepository.save(orderEntity);

        // 7. Simulación de Pasarela
        PaymentSessionResult gatewayResult = paymentGatewayService.createSession(
            savedOrder.getOrderCode(),
            savedOrder.getTotalPrice()
        );

        // MOCK AVANCE: Aprobación inmediata
        savedOrder.setStatus(OrderStatus.CONFIRMADO);
        savedOrder.setPaymentMethod(PaymentMethod.TARJETA);
        orderRepository.save(savedOrder);

        return new OrderCreateResponse(
            savedOrder.getOrderCode(),
            savedOrder.getStatus(),
            savedOrder.getTotalPrice(),
            gatewayResult.sessionToken(),
            gatewayResult.url()
        );
    }

    private void validateDeliveryRules(OrderCreateRequest request) {
        if (request.deliveryType() == DeliveryType.A_DOMICILIO) {
            if (request.address() == null) {
                throw new ConflictException(
                    "La dirección exacta de envío es obligatoria cuando el método de entrega es A_DOMICILIO" +
                        ".");
            }
            if (request.storeId() != null) {
                throw new ConflictException(
                    "No se puede enviar un ID de tienda si el método de entrega es A_DOMICILIO.");
            }
        }
        if (request.deliveryType() == DeliveryType.RECOJO_EN_TIENDA) {
            if (request.storeId() == null) {
                throw new ConflictException(
                    "Debe seleccionar una tienda válida para la opción RECOJO_EN_TIENDA.");
            }
            if (request.address() != null) {
                throw new ConflictException(
                    "No se debe enviar información de dirección si el método de entrega es RECOJO_EN_TIENDA" +
                        ".");
            }
        }
    }

    private List<OrderItemEntity> processAndValidateItems(List<OrderCreateRequest.OrderItemRequest> itemRequests) {
        List<OrderItemEntity> items = new ArrayList<>();
        for (OrderCreateRequest.OrderItemRequest req : itemRequests) {
            ProductSharedDto product = catalogExposedService.findBySlug(req.productSlug())
                .orElseThrow(() -> new ResourceNotFoundException(String.format(
                    "El producto con slug '%s' no existe.",
                    req.productSlug()))
                );

            if (!product.active()) {
                throw new ConflictException(String.format(
                    "El producto con slug '%s' está inactivo.",
                    req.productSlug()));
            }
            if (product.stock() < req.quantity()) {
                throw new ConflictException(String.format(
                    "Stock insuficiente para %s. Solicitado: %d, Disponible: %d",
                    product.name(),
                    req.quantity(),
                    product.stock()));
            }

            catalogExposedService.decrementStock(product.id(), req.quantity());
            OrderItemEntity itemEntity = orderMapper.toItemEntity(product, req.quantity());
            items.add(itemEntity);
        }
        return items;
    }

    private OrderAddressEntity buildOrderAddress(OrderCreateRequest request) {
        OrderAddressEntity orderAddressEntity;
        if (request.deliveryType() == DeliveryType.RECOJO_EN_TIENDA) {
            // El backend ignora lo que mande el front y clona los datos oficiales de la tienda
            StoreSharedDto store = storeExposedService.getStoreById(request.storeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "La tienda seleccionada no existe o se encuentra inactiva.")
                );
            orderAddressEntity = orderAddressMapper.toStorePickupAddress(store, request.recipient());
        } else {
            // A_DOMICILIO
            orderAddressEntity = orderAddressMapper.toHomeDeliveryAddress(request.address(),
                request.recipient());
        }
        return orderAddressEntity;
    }

    private String generateOrderCode() {
        Long sequence = orderRepository.getNextOrderCodeSequence();
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return String.format("ORD-%s-%04d", dateStr, sequence);
    }
}
