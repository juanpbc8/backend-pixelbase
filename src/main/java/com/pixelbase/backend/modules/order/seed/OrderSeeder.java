package com.pixelbase.backend.modules.order.seed;

import com.pixelbase.backend.common.enums.DocumentType;
import com.pixelbase.backend.common.seed.DataSeeder;
import com.pixelbase.backend.modules.catalog.domain.ProductEntity;
import com.pixelbase.backend.modules.catalog.repository.ProductRepository;
import com.pixelbase.backend.modules.order.api.dto.request.OrderCreateRequest;
import com.pixelbase.backend.modules.order.api.dto.request.OrderCreateRequest.AddressRequest;
import com.pixelbase.backend.modules.order.api.dto.request.OrderCreateRequest.CustomerRequest;
import com.pixelbase.backend.modules.order.api.dto.request.OrderCreateRequest.OrderItemRequest;
import com.pixelbase.backend.modules.order.api.dto.request.OrderCreateRequest.RecipientRequest;
import com.pixelbase.backend.modules.order.domain.DeliveryType;
import com.pixelbase.backend.modules.order.domain.OrderEntity;
import com.pixelbase.backend.modules.order.domain.OrderStatus;
import com.pixelbase.backend.modules.order.domain.PaymentMethod;
import com.pixelbase.backend.modules.order.repository.OrderRepository;
import com.pixelbase.backend.modules.order.service.OrderInternalService;
import com.pixelbase.backend.modules.user.domain.UserEntity;
import com.pixelbase.backend.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@Order(7)
@RequiredArgsConstructor
public class OrderSeeder implements DataSeeder {

    private final OrderInternalService orderInternalService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public void seed() {
        if (orderRepository.count() > 0) {
            log.info(" El OrderSeeder fue omitido; ya existen órdenes registradas en la base de datos.");
            return;
        }

        // 1. Recuperar los Slugs reales de tu catálogo de hardware
        List<String> productSlugs = productRepository.findAll().stream()
            .map(ProductEntity::getSlug)
            .toList();

        if (productSlugs.isEmpty()) {
            log.error(
                "Abortando Seeder: No se encontraron productos en el catálogo para asociar a las órdenes.");
            return;
        }

        int productCounter = 0;

        // =========================================================================
        // 👤 SECCIÓN I: COMPRAS DE CLIENTES AUTENTICADOS (16 ÓRDENES)
        // =========================================================================
        for (long userId = 2; userId <= 5; userId++) {
            UserEntity user = userRepository.findById(userId).orElse(null);
            if (user == null) continue;

            var customerDto = new CustomerRequest(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone() != null ? user.getPhone() : "987654321",
                user.getDocumentType() != null ? user.getDocumentType() : DocumentType.DNI,
                user.getDocumentNumber() != null ? user.getDocumentNumber() : "7000100" + userId
            );

            if (userId == 2L || userId == 3L) {
                // --- CASO A: Clientes 2 y 3 (2 A_DOMICILIO + 2 RECOJO_EN_TIENDA) ---

                // Orden 1: A_DOMICILIO - Compra volumétrica alta para estresar stock (Carts de 3 productos)
                productCounter = executeCustomerOrder(userId, DeliveryType.A_DOMICILIO, null, customerDto,
                    createSelfRecipient(customerDto), productSlugs, productCounter, 3);

                // Orden 2: A_DOMICILIO - Envío a un Tercero (Regalo)
                var thirdPartyRecipient = new RecipientRequest("Carlos Alberto", "Gómez Ruiz", "912345678");
                productCounter = executeCustomerOrder(userId, DeliveryType.A_DOMICILIO, null, customerDto,
                    thirdPartyRecipient, productSlugs, productCounter, 1);

                // Orden 3: RECOJO_EN_TIENDA - Sede Wilson (ID: 1)
                productCounter = executeCustomerOrder(userId, DeliveryType.RECOJO_EN_TIENDA, 1L, customerDto,
                    createSelfRecipient(customerDto), productSlugs, productCounter, 2);

                // Orden 4: RECOJO_EN_TIENDA - Sede San Isidro (ID: 2) - Carrito extendido de 4 productos
                productCounter = executeCustomerOrder(userId, DeliveryType.RECOJO_EN_TIENDA, 2L, customerDto,
                    createSelfRecipient(customerDto), productSlugs, productCounter, 4);
            } else {
                // --- CASO B: Clientes 4 y 5 (3 A_DOMICILIO + 1 RECOJO_EN_TIENDA) ---

                // Orden 1, 2 y 3: A_DOMICILIO - Recibe el mismo comprador (Cantidades variables dinámicas)
                for (int i = 0; i < 3; i++) {
                    int dynamicItemCount = (i == 0) ? 4 : 2; // Estresa el pool variando entre 3 y 2 ítems
                    // por carrito
                    productCounter = executeCustomerOrder(userId, DeliveryType.A_DOMICILIO, null, customerDto,
                        createSelfRecipient(customerDto), productSlugs, productCounter, dynamicItemCount);
                }

                // Orden 4: RECOJO_EN_TIENDA - Sede física balanceada (ID 1 o 2)
                Long targetedStoreId = (userId == 4L) ? 1L : 2L;
                productCounter = executeCustomerOrder(userId,
                    DeliveryType.RECOJO_EN_TIENDA,
                    targetedStoreId,
                    customerDto,
                    createSelfRecipient(customerDto),
                    productSlugs,
                    productCounter,
                    2);
            }
        }

        // =========================================================================
        // 🌐 SECCIÓN II: COMPRAS DE INVITADOS / GUESTS (2 ÓRDENES)
        // =========================================================================
        for (int guestId = 1; guestId <= 2; guestId++) {
            var guestCustomer = new CustomerRequest(
                "Invitado N°" + guestId,
                "Anónimo",
                "guest.anonimo" + guestId + "@gmail.com",
                "99988877" + guestId,
                DocumentType.DNI,
                "4578129" + guestId
            );

            var guestAddress = new AddressRequest(
                "Calle Los Álamos " + (120 * guestId),
                "Lima", "Lima", "Santiago de Surco",
                "Frente al parque zonal"
            );

            List<OrderItemRequest> items = new ArrayList<>();
            // Carts de 2 productos para invitados incrementando volumen total
            items.add(new OrderItemRequest(productSlugs.get(productCounter % productSlugs.size()), 2));
            productCounter++;
            items.add(new OrderItemRequest(productSlugs.get(productCounter % productSlugs.size()), 1));
            productCounter++;

            var guestRequest = new OrderCreateRequest(
                DeliveryType.A_DOMICILIO,
                null,
                guestCustomer,
                guestAddress,
                createSelfRecipient(guestCustomer),
                items
            );

            orderInternalService.createOrder(guestRequest, null);
            log.info("✔ Orden GUEST procesada con éxito para el email: {}", guestCustomer.email());
        }

        // =========================================================================
        // 🛠️ SECCIÓN III: MUTACIÓN DE ESTADOS POST-CHECKOUT (BLINDAJE DE INFRAESTRUCTURA)
        // =========================================================================
        // Hook Senior: Modificamos directamente sobre la persistencia para simular un ecosistema
        // con transacciones maduras en diferentes fases logísticas (ideal para pintar KPIs variados).
        List<OrderEntity> generatedOrders = orderRepository.findAll();
        int step = 0;
        for (OrderEntity order : generatedOrders) {
            if (step % 5 == 0) {
                order.setStatus(OrderStatus.CONFIRMADO);
                order.setPaymentMethod(PaymentMethod.TARJETA);
            } else if (step % 5 == 1) {
                order.setStatus(OrderStatus.ENTREGADO);
                order.setPaymentMethod(PaymentMethod.TARJETA);
            } else if (step % 5 == 2) {
                order.setStatus(OrderStatus.PENDIENTE);
                order.setPaymentMethod(null);
            } else {
                order.setStatus(OrderStatus.CONFIRMADO);
                order.setPaymentMethod(PaymentMethod.TARJETA);
            }
            step++;
        }
        orderRepository.saveAll(generatedOrders); // Persistencia masiva batch optimizada

        // Log unificado con el formato oficial de DatabaseSeederRunner
        log.info(" ✅ -> OrderSeeder: {} órdenes comerciales registradas con éxito.",
            orderRepository.count());
    }

    // =========================================================================
    // --- MÉTODOS AUXILIARES / ORQUESTADORES DE FLUJO ---
    // =========================================================================

    private int executeCustomerOrder(Long userId,
                                     DeliveryType deliveryType,
                                     Long storeId,
                                     CustomerRequest customer,
                                     RecipientRequest recipient,
                                     List<String> productSlugs,
                                     int currentCounter,
                                     int itemsCount) {

        AddressRequest addressDto = null;
        if (deliveryType == DeliveryType.A_DOMICILIO) {
            addressDto = new AddressRequest(
                "Av. Pardo N° " + (currentCounter + 15),
                "Lima", "Lima", "Miraflores",
                "A media cuadra de la embajada"
            );
        }

        List<OrderItemRequest> items = new ArrayList<>();
        for (int i = 0; i < itemsCount; i++) {
            String targetSlug = productSlugs.get(currentCounter % productSlugs.size());

            // LOGICA DE VOLUMEN ALTO SENIOR:
            // Alternamos compras de hasta 2 y 3 unidades. Al dar vueltas continuas sobre la lista de 15
            // productos,
            // ciertos componentes clave sumarán alta tracción logrando bajar su stock de 25 a niveles
            // mínimos de alerta.
            int boldQuantity = (currentCounter % 3 == 0) ? 3 : 2;

            items.add(new OrderItemRequest(targetSlug, boldQuantity));
            currentCounter++;
        }

        var request = new OrderCreateRequest(
            deliveryType,
            storeId,
            customer,
            addressDto,
            recipient,
            items
        );

        orderInternalService.createOrder(request, userId);
        return currentCounter;
    }

    private RecipientRequest createSelfRecipient(CustomerRequest customer) {
        return new RecipientRequest(customer.firstName(), customer.lastName(), customer.phone());
    }
}
