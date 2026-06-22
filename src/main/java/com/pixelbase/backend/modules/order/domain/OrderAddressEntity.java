package com.pixelbase.backend.modules.order.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "address_line", nullable = false)
    private String addressLine;

    @Column(length = 100, nullable = false)
    private String department;

    @Column(length = 100, nullable = false)
    private String province;

    @Column(length = 100, nullable = false)
    private String district;

    private String reference;

    // --- Datos de Persona que Recibe / Recoge en Tienda ---
    @Column(name = "contact_first_name", nullable = false)
    private String contactFirstName;

    @Column(name = "contact_last_name", nullable = false)
    private String contactLastName;

    @Column(name = "contact_phone", nullable = false, length = 30)
    private String contactPhone;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;
}
