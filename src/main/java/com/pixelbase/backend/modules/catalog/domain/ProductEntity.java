package com.pixelbase.backend.modules.catalog.domain;

import com.pixelbase.backend.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, unique = true)
    private String slug;

    private Integer stock;

    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    // Manejo de JSONB nativo para especificaciones de hardware
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> specifications;

    @Column(name = "part_number", unique = true)
    private String partNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private BrandEntity brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<ProductImageEntity> images;

    public void addImage(ProductImageEntity image) {
        if (images == null) {
            images = new ArrayList<>();
        }
        this.images.add(image);
        image.setProduct(this); // Sincroniza el lado "many"
    }

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = ProductStatus.ACTIVO;
        }
        if (this.stock == null) {
            this.stock = 0;
        }
    }
}
