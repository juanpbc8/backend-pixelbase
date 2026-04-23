package com.pixelbase.backend.modules.catalog.repository.specification;

import com.pixelbase.backend.modules.catalog.domain.ProductEntity;
import com.pixelbase.backend.modules.catalog.domain.ProductStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * Clase de utilidad para construir consultas dinámicas sobre la entidad Product.
 * Cada método devuelve una "Specification", que es una pieza de lógica de filtrado.
 */
public class ProductSpecification {
    /**
     * Filtra productos por un texto de búsqueda que coincida con el nombre o el SKU.
     *
     * @param text Texto a buscar (ej: "logitech" o "LAP-ASUS-001")
     */
    public static Specification<ProductEntity> hasSearch(String text) {
        // root: Es la entidad raíz (ProductEntity)
        // query: Permite modificar la consulta (ej.: agregar joins, ordenar, etc.)
        // cb (CriteriaBuilder): Fabrica de condiciones (ej.: like, between, and, etc.)
        // Devolver null en este caso significa no aplicar filtro
        return (root, query, cb) -> {
            if (text == null || text.isBlank()) return null;

            // Convertimos a minúsculas para una búsqueda insensible a mayúsculas
            String pattern = "%" + text.toLowerCase() + "%";

            // Genera: (LOWER(name) LIKE %text% OR LOWER(sku) LIKE %text%)
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("sku")), pattern)
            );
        };
    }

    /**
     * Filtra productos que pertenezcan a una categoría específica por su ID.
     */
    public static Specification<ProductEntity> hasCategory(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) return null;
            // Genera: category_id = categoryId
            return cb.equal(root.get("category").get("id"), categoryId);
        };
    }

    /**
     * Filtra productos por marca.
     */
    public static Specification<ProductEntity> hasBrand(Long brandId) {
        return (root, query, cb) -> {
            if (brandId == null) return null;
            // Genera: brand_id = brandId
            return cb.equal(root.get("brand").get("id"), brandId);
        };
    }

    /**
     * Filtra productos dentro de un rango de precio.
     */
    public static Specification<ProductEntity> priceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min != null && max != null) return cb.between(root.get("price"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("price"), min);
            return cb.lessThanOrEqualTo(root.get("price"), max);
        };
    }

    /**
     * Filtra solo productos con estado ACTIVE (Regla esencial para el Storefront).
     */
    public static Specification<ProductEntity> isActive() {
        return (root, query, cb) -> cb.equal(root.get("status"), ProductStatus.ACTIVE);
    }
}