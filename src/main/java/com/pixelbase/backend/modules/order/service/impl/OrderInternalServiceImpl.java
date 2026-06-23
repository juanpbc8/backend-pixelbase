package com.pixelbase.backend.modules.order.service.impl;

import com.pixelbase.backend.common.dto.PageResponse;
import com.pixelbase.backend.common.exception.ConflictException;
import com.pixelbase.backend.common.exception.ResourceNotFoundException;
import com.pixelbase.backend.modules.catalog.exposed.CatalogExposedService;
import com.pixelbase.backend.modules.catalog.exposed.dto.ProductSharedDto;
import com.pixelbase.backend.modules.configuration.exposed.StoreExposedService;
import com.pixelbase.backend.modules.configuration.exposed.dto.StoreSharedDto;
import com.pixelbase.backend.modules.order.api.dto.request.OrderCreateRequest;
import com.pixelbase.backend.modules.order.api.dto.response.CustomerOrderDetailResponse;
import com.pixelbase.backend.modules.order.api.dto.response.CustomerOrderSummaryResponse;
import com.pixelbase.backend.modules.order.api.dto.response.OrderCreateResponse;
import com.pixelbase.backend.modules.order.domain.*;
import com.pixelbase.backend.modules.order.infrastructure.payment.PaymentGatewayService;
import com.pixelbase.backend.modules.order.infrastructure.payment.dto.PaymentSessionResult;
import com.pixelbase.backend.modules.order.mapper.OrderAddressMapper;
import com.pixelbase.backend.modules.order.mapper.OrderMapper;
import com.pixelbase.backend.modules.order.repository.OrderRepository;
import com.pixelbase.backend.modules.order.service.OrderInternalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderInternalServiceImpl implements OrderInternalService {
    private final OrderRepository orderRepository;
    private final CatalogExposedService catalogExposedService;
    private final StoreExposedService storeExposedService;
    private final OrderMapper orderMapper;
    private final OrderAddressMapper orderAddressMapper;
    private final PaymentGatewayService paymentGatewayService;

    @Override
    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request, Long authenticatedUserId) {
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

    // =========================================================================
    // --- CONTEXTO: CLIENTE AUTENTICADO (ZONA PROTEGIDA) ---
    // =========================================================================
    @Override
    public PageResponse<CustomerOrderSummaryResponse> getCustomerOrdersHistory(Long userId,
                                                                               Pageable pageable) {
        Page<OrderEntity> ordersPage = orderRepository.findAllByUserId(userId, pageable);

        Page<CustomerOrderSummaryResponse> summaryPage =
            ordersPage.map(orderMapper::toCustomerOrderSummaryResponse);

        return PageResponse.from(summaryPage);
    }

    @Override
    public CustomerOrderDetailResponse getCustomerOrderDetail(String orderCode, Long userId) {
        OrderEntity order = findByOrderCodeOrThrowNotFound(orderCode);

        // BLINDAJE CRÍTICO ANTI-IDOR: Evitamos que un cliente logueado vea compras de otro cliente
        if (order.getUserId() == null || !order.getUserId().equals(userId)) {
            // Ofuscación defensiva: Arrojamos 404 en lugar de 403 para no confirmar la existencia del recurso
            throw new ResourceNotFoundException(String.format("No se encontró la orden con el código '%s'.",
                orderCode));
        }

        return enrichAndBuildDetailResponse(order);
    }

    // =========================================================================
    // --- CONTEXTO: CONSULTA PÚBLICA (SOLUCIÓN INVITADOS / TRACKING / SUCCESS) ---
    // =========================================================================
    @Override
    public CustomerOrderDetailResponse getPublicOrderDetail(String orderCode, String email) {
        OrderEntity order = findByOrderCodeOrThrowNotFound(orderCode);

        // BLINDAJE DE DOBLE FACTOR LOGÍSTICO: El correo enviado por QueryParam debe hacer match exacto
        if (!order.getCustomerEmail().equalsIgnoreCase(email.trim())) {
            throw new ResourceNotFoundException(
                String.format("No se encontró la orden con el código '%s'.",
                    orderCode));
        }

        return enrichAndBuildDetailResponse(order);
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
            OrderItemEntity itemEntity = orderMapper.toOrderItemEntity(product, req.quantity());
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

    // =========================================================================
    // ENRIQUECIMIENTO ASÍNCRONO EN MEMORIA DE DATOS PARA RESPUESTA DETALLADA
    // =========================================================================
    private CustomerOrderDetailResponse enrichAndBuildDetailResponse(OrderEntity order) {
        // 1. Extraer todos los productIds únicos de la orden para la consulta masiva
        List<Long> productIds = order.getItems().stream()
            .map(OrderItemEntity::getProductId)
            .distinct()
            .toList();

        // 2. Ejecutar UNA SOLA consulta al módulo de catálogo (Cero problemas N+1)
        List<ProductSharedDto> sharedProducts = catalogExposedService.findAllByIds(productIds);

        // 3. Convertir a un mapa indexado por ID para búsquedas instantáneas O(1)
        Map<Long, ProductSharedDto> catalogMap = sharedProducts.stream()
            .collect(Collectors.toMap(ProductSharedDto::id, p -> p));

        // 4. Invocación limpia y directa delegando toda la conversión estructural a MapStruct
        return orderMapper.toCustomerOrderDetailResponse(order, catalogMap);
    }

    private OrderEntity findByOrderCodeOrThrowNotFound(String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
            .orElseThrow(() -> new ResourceNotFoundException(
                String.format("No se encontró la orden con el código '%s'.", orderCode)));
    }
}
