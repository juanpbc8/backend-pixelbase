package com.pixelbase.backend.modules.order.service.impl;

import com.pixelbase.backend.common.enums.DocumentType;
import com.pixelbase.backend.common.exception.ConflictException;
import com.pixelbase.backend.common.exception.ResourceNotFoundException;
import com.pixelbase.backend.modules.catalog.exposed.CatalogExposedService;
import com.pixelbase.backend.modules.catalog.exposed.dto.ProductSharedDto;
import com.pixelbase.backend.modules.configuration.exposed.StoreExposedService;
import com.pixelbase.backend.modules.configuration.exposed.dto.StoreSharedDto;
import com.pixelbase.backend.modules.order.api.dto.request.OrderCreateRequest;
import com.pixelbase.backend.modules.order.api.dto.response.OrderCreateResponse;
import com.pixelbase.backend.modules.order.domain.*;
import com.pixelbase.backend.modules.order.infrastructure.payment.PaymentGatewayService;
import com.pixelbase.backend.modules.order.infrastructure.payment.dto.PaymentSessionResult;
import com.pixelbase.backend.modules.order.mapper.OrderAddressMapper;
import com.pixelbase.backend.modules.order.mapper.OrderMapper;
import com.pixelbase.backend.modules.order.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas Unitarias del Servicio de Órdenes (OrderInternalServiceImpl)")
class OrderInternalServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CatalogExposedService catalogExposedService;

    @Mock
    private StoreExposedService storeExposedService;

    @Mock
    private PaymentGatewayService paymentGatewayService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderAddressMapper orderAddressMapper;

    @InjectMocks
    private OrderInternalServiceImpl orderService;

    // --- ESCENARIOS EXITOSOS (HAPPY PATHS) ---
    @Test
    @DisplayName("Escenario 1: Creación exitosa de orden para CUSTOMER con entrega A_DOMICILIO")
    void createOrder_WhenCustomerAndHomeDelivery_ShouldReturnConfirmedOrder() {
        // --- ARRANGE (Preparar) ---
        Long userId = 99L;
        var request = createBaseRequest(DeliveryType.A_DOMICILIO, null);
        var productDto = createMockProductSharedDto("teclado-razer",
            "Teclado Razer",
            10,
            BigDecimal.valueOf(350.00));
        var itemEntity = OrderItemEntity.builder().priceSnapshot(productDto.price()).quantity(1).build();
        var addressEntity = OrderAddressEntity.builder().addressLine("Av. Larco 123").build();

        var orderEntity = OrderEntity.builder()
            .orderCode("ORD-20260619-0001")
            .totalPrice(BigDecimal.valueOf(350.00))
            .status(OrderStatus.PENDIENTE)
            .build();

        // Entrenamos los Mocks para simular el comportamiento del sistema
        when(catalogExposedService.findBySlug("teclado-razer")).thenReturn(Optional.of(productDto));
        doNothing().when(catalogExposedService).decrementStock(productDto.id(), 1);
        when(orderAddressMapper.toHomeDeliveryAddress(any(), any())).thenReturn(addressEntity);
        when(orderRepository.getNextOrderCodeSequence()).thenReturn(1L);
        when(orderMapper.toOrderItemEntity(productDto, 1)).thenReturn(itemEntity);
        when(orderMapper.toOrderEntity(eq(request),
            anyString(),
            any(),
            eq(userId),
            anyList(),
            eq(addressEntity)))
            .thenReturn(orderEntity);

        // Simulamos el save secuencial (Guardado inicial en PENDIENTE y luego actualización a CONFIRMADO)
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderEntity);

        when(paymentGatewayService.createSession(anyString(), any()))
            .thenReturn(new PaymentSessionResult("gateway-token", "https://payment-url.com"));

        // --- ACT (Actuar) ---
        OrderCreateResponse response = orderService.createOrder(request, userId);

        // --- ASSERT (Verificar) ---
        assertNotNull(response);
        assertEquals(orderEntity.getOrderCode(), response.orderCode());
        assertEquals(OrderStatus.CONFIRMADO,
            response.status()); // El Mock fuerza a confirmado automáticamente
        assertEquals(BigDecimal.valueOf(350.00), response.totalPrice());
        assertEquals("gateway-token", response.paymentSessionToken());
        assertEquals("https://payment-url.com", response.paymentUrl());

        // Verificaciones de comportamiento de infraestructura
        verify(catalogExposedService, times(1)).decrementStock(productDto.id(), 1);
        verify(orderRepository, times(2)).save(any(OrderEntity.class));
    }

    @Test
    @DisplayName("Escenario 2: Creación exitosa de orden para GUEST con RECOJO_EN_TIENDA")
    void createOrder_WhenGuestAndStorePickup_ShouldOverrideAddressAndReturnConfirmedOrder() {
        // --- ARRANGE (Preparar) ---
        Long guestUserId = null; // Compra invitado
        Long storeId = 2L;

        // 1. Generamos el request base
        var baseRequest = createBaseRequest(DeliveryType.RECOJO_EN_TIENDA, storeId);

        // 2. REFACTOR SENIOR: Re-instanciamos fijando el bloque address estrictamente en null
        var request = new OrderCreateRequest(
            baseRequest.deliveryType(),
            baseRequest.storeId(),
            baseRequest.customer(),
            null, // Dirección logística requerida en null para RECOJO_EN_TIENDA
            baseRequest.recipient(),
            baseRequest.items()
        );

        var productDto = createMockProductSharedDto("mouse-logitech",
            "Mouse Logitech",
            5,
            BigDecimal.valueOf(150.00));
        var storeDto = new StoreSharedDto(storeId,
            "Sede Wilson - Av. Garcilaso 1234",
            "Lima",
            "Lima",
            "Lima Cercado",
            true);

        var itemEntity = OrderItemEntity.builder().priceSnapshot(productDto.price()).quantity(1).build();
        var addressEntity =
            OrderAddressEntity.builder().addressLine(storeDto.addressLine()).district(storeDto.district()).build();
        var orderEntity = OrderEntity.builder()
            .orderCode("ORD-20260619-0002")
            .totalPrice(BigDecimal.valueOf(150.00))
            .status(OrderStatus.PENDIENTE)
            .build();

        when(catalogExposedService.findBySlug("mouse-logitech")).thenReturn(Optional.of(productDto));
        doNothing().when(catalogExposedService).decrementStock(productDto.id(), 1);
        when(storeExposedService.getStoreById(storeId)).thenReturn(Optional.of(storeDto));
        when(orderAddressMapper.toStorePickupAddress(eq(storeDto), any())).thenReturn(addressEntity);
        when(orderRepository.getNextOrderCodeSequence()).thenReturn(2L);
        when(orderMapper.toOrderItemEntity(productDto, 1)).thenReturn(itemEntity);
        when(orderMapper.toOrderEntity(eq(request),
            anyString(),
            any(),
            eq(guestUserId),
            anyList(),
            eq(addressEntity)))
            .thenReturn(orderEntity);
        when(orderRepository.save(any(OrderEntity.class))).thenReturn(orderEntity);
        when(paymentGatewayService.createSession(anyString(), any()))
            .thenReturn(new PaymentSessionResult("mock-token", "https://mock-url.com"));

        // --- ACT (Actuar) ---
        OrderCreateResponse response = orderService.createOrder(request, guestUserId);

        // --- ASSERT (Verificar) ---
        assertNotNull(response);
        assertEquals(OrderStatus.CONFIRMADO, response.status());
        assertEquals(BigDecimal.valueOf(150.00), response.totalPrice());
        verify(storeExposedService, times(1)).getStoreById(storeId);
        verify(orderAddressMapper, times(1)).toStorePickupAddress(any(), any());
        verify(orderAddressMapper, never()).toHomeDeliveryAddress(any(), any());
    }

    // --- ESCENARIOS DE ERROR Y EXCEPCIONES (SAD PATHS) ---
    @Test
    @DisplayName("Escenario 3: Lanzar ConflictException si es A_DOMICILIO pero envían storeId")
    void createOrder_WhenHomeDeliveryWithStoreId_ShouldThrowConflictException() {
        // --- ARRANGE ---
        var request = createBaseRequest(DeliveryType.A_DOMICILIO, 5L); // Conflicto de reglas

        // --- ACT & ASSERT ---
        ConflictException exception = assertThrows(ConflictException.class, () ->
            orderService.createOrder(request, 1L)
        );
        assertEquals("No se puede enviar un ID de tienda si el método de entrega es A_DOMICILIO.",
            exception.getMessage());

        // Verificamos que el servicio cortó de inmediato y nunca tocó repositorios ni mappers
        verifyNoInteractions(catalogExposedService, orderRepository, orderMapper, orderAddressMapper);
    }

    @Test
    @DisplayName("Escenario 4: Lanzar ConflictException si es RECOJO_EN_TIENDA pero el storeId es nulo")
    void createOrder_WhenStorePickupWithoutStoreId_ShouldThrowConflictException() {
        // --- ARRANGE ---
        var request = createBaseRequest(DeliveryType.RECOJO_EN_TIENDA, null); // Conflicto de reglas

        // --- ACT & ASSERT ---
        ConflictException exception = assertThrows(ConflictException.class, () ->
            orderService.createOrder(request, 1L)
        );
        assertEquals("Debe seleccionar una tienda válida para la opción RECOJO_EN_TIENDA.",
            exception.getMessage());
        verifyNoInteractions(catalogExposedService, orderRepository, orderMapper, orderAddressMapper);
    }

    @Test
    @DisplayName("Escenario 5: Lanzar ResourceNotFoundException si el producto no existe en el catálogo")
    void createOrder_WhenProductNotFound_ShouldThrowResourceNotFoundException() {
        // --- ARRANGE ---
        var request = createBaseRequest(DeliveryType.A_DOMICILIO, null);
        // Simulamos que el catálogo no encuentra el producto devolviendo una caja vacía
        when(catalogExposedService.findBySlug(anyString())).thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
            orderService.createOrder(request, 1L)
        );
        // CORRECCIÓN: Validamos el nuevo mensaje exacto y limpio
        assertTrue(exception.getMessage().contains("no existe."));

        // Integridad: Verificamos que no se toque stock ni se guarde nada
        verify(catalogExposedService, never()).decrementStock(anyLong(), anyInt());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Escenario 5b: Lanzar ConflictException si el producto existe pero está inactivo")
    void createOrder_WhenProductInactive_ShouldThrowConflictException() {
        // --- ARRANGE ---
        var request = createBaseRequest(DeliveryType.A_DOMICILIO, null);

        // Construimos un DTO de producto cuyo atributo 'active' sea estrictamente FALSE
        var inactiveProduct = new ProductSharedDto(
            12L, "teclado-razer", "Teclado Razer", "SKU-123", "PART-999",
            BigDecimal.valueOf(350.00), 10, false, null
        );

        when(catalogExposedService.findBySlug("teclado-razer")).thenReturn(Optional.of(inactiveProduct));

        // --- ACT & ASSERT ---
        ConflictException exception = assertThrows(ConflictException.class, () ->
            orderService.createOrder(request, 1L)
        );
        // VALIDACIÓN: Verificamos que salte el 409 con tu nuevo mensaje de inactividad
        assertTrue(exception.getMessage().contains("está inactivo."));

        // Integridad: No debe descontarse stock ni persistirse la orden corrupta
        verify(catalogExposedService, never()).decrementStock(anyLong(), anyInt());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Escenario 6: Lanzar ConflictException si el producto no cuenta con stock suficiente")
    void createOrder_WhenStockInsufficient_ShouldThrowConflictException() {
        // --- ARRANGE ---
        var request = createBaseRequest(DeliveryType.A_DOMICILIO, null);
        // El DTO del carrito por defecto pide cantidad 1, simulamos que el stock en catálogo es 0
        var productWithNoStock = createMockProductSharedDto("teclado-razer",
            "Teclado Razer",
            0,
            BigDecimal.valueOf(350.00));

        when(catalogExposedService.findBySlug("teclado-razer")).thenReturn(Optional.of(productWithNoStock));

        // --- ACT & ASSERT ---
        ConflictException exception = assertThrows(ConflictException.class, () ->
            orderService.createOrder(request, 1L)
        );
        assertTrue(exception.getMessage().contains("Stock insuficiente"));

        // Seguridad e integridad de datos: Verificamos que NO se decrementó stock y no se guardó la orden
        verify(catalogExposedService, never()).decrementStock(anyLong(), anyInt());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Escenario 7: Lanzar ResourceNotFoundException si la tienda seleccionada no existe o está " +
        "inactiva")
    void createOrder_WhenStoreNotFoundOrInactive_ShouldThrowResourceNotFoundException() {
        // --- ARRANGE ---
        Long storeId = 3L;

        // 1. Generamos el request base
        var baseRequest = createBaseRequest(DeliveryType.RECOJO_EN_TIENDA, storeId);

        // 2. REFACTOR SENIOR: Forzamos address a null para que pase la validación sintáctica del validador
        // de entrega
        var request = new OrderCreateRequest(
            baseRequest.deliveryType(),
            baseRequest.storeId(),
            baseRequest.customer(),
            null, // Dirección en null para evitar el ConflictException prematuro
            baseRequest.recipient(),
            baseRequest.items()
        );

        var productDto = createMockProductSharedDto("teclado-razer",
            "Teclado Razer",
            10,
            BigDecimal.valueOf(350.00));
        var itemEntity = OrderItemEntity.builder().priceSnapshot(productDto.price()).quantity(1).build();

        when(catalogExposedService.findBySlug("teclado-razer")).thenReturn(Optional.of(productDto));
        doNothing().when(catalogExposedService).decrementStock(productDto.id(), 1);
        when(orderMapper.toOrderItemEntity(productDto, 1)).thenReturn(itemEntity);

        // Simulamos que la tienda buscada no existe (devuelve vacío)
        when(storeExposedService.getStoreById(storeId)).thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
            orderService.createOrder(request, null)
        );
        assertEquals("La tienda seleccionada no existe o se encuentra inactiva.", exception.getMessage());

        // Verificamos que aunque el stock se decrementó (así está diseñado el flujo lineal antes de la
        // dirección),
        // la orden se interrumpió y jamás se guardó en la base de datos (lanzando rollback de la transacción)
        verify(orderRepository, never()).save(any());
    }

    // --- MÉTODOS HELPERS DE FÁBRICA DE DATOS FICTICIOS (FIXTURES) ---
    private OrderCreateRequest createBaseRequest(DeliveryType deliveryType, Long storeId) {
        var customer = new OrderCreateRequest.CustomerRequest(
            "Carlos", "Mendoza", "carlos@email.com", "987654321", DocumentType.DNI, "74859612"
        );
        var address = new OrderCreateRequest.AddressRequest(
            "Av. Larco 456", "Lima", "Lima", "Miraflores", "Al frente del banco"
        );
        var recipient = new OrderCreateRequest.RecipientRequest(
            "Carlos", "Mendoza", "987654321");

        // Determinamos el slug según la prueba para dar dinamismo
        String slug = (storeId != null && storeId == 2L) ? "mouse-logitech" : "teclado-razer";
        var item = new OrderCreateRequest.OrderItemRequest(slug, 1);

        return new OrderCreateRequest(deliveryType, storeId, customer, address, recipient, List.of(item));
    }

    private ProductSharedDto createMockProductSharedDto(String slug,
                                                        String name,
                                                        Integer stock,
                                                        BigDecimal price) {
        return new ProductSharedDto(12L, slug, name, "SKU-123", "PART-999", price, stock,
            true, null);
    }
}
