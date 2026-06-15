package com.pixelbase.backend.modules.order.domain;

import com.pixelbase.backend.common.entity.AuditableEntity;
import com.pixelbase.backend.common.enums.DocumentType;
import com.pixelbase.backend.modules.user.domain.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_code", nullable = false, unique = true, length = 100)
    private String orderCode;

    @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", nullable = false, length = 30)
    private DeliveryType deliveryType;

    // --- Datos de Snapshot del Comprador ---
    @Column(name = "customer_first_name", nullable = false)
    private String customerFirstName;

    @Column(name = "customer_last_name", nullable = false)
    private String customerLastName;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "customer_phone", nullable = false, length = 30)
    private String customerPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_doc_type", nullable = false, length = 10)
    private DocumentType customerDocType;

    @Column(name = "customer_doc_number", nullable = false, length = 20)
    private String customerDocNumber;

    // Notas de control interno escritas exclusivamente por los Administradores
    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    // --- Relaciones de Composición (Ciclo de vida amarrado a la Orden) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Nullable por defecto para compras Guest
    private UserEntity user;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private OrderAddressEntity orderAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItemEntity> items = new ArrayList<>();
}
