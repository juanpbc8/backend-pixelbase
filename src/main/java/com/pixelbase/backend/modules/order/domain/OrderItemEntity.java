package com.pixelbase.backend.modules.order.domain;

import com.pixelbase.backend.modules.catalog.domain.ProductEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantity;

    // Los 4 Snapshots Históricos Contra Modificaciones del Catálogo
    @Column(name = "price_snapshot", nullable = false, precision = 15, scale = 2)
    private BigDecimal priceSnapshot;

    @Column(name = "product_name_snapshot", nullable = false)
    private String productNameSnapshot;

    @Column(name = "part_number_snapshot", nullable = false)
    private String partNumberSnapshot;

    @Column(name = "sku_snapshot", nullable = false, length = 10)
    private String skuSnapshot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    // Relación viva con el producto para estadísticas, reportes de categorías/marcas e imágenes en caliente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;
}
