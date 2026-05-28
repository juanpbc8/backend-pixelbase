package com.pixelbase.backend.modules.catalog.repository.specification;

import com.pixelbase.backend.modules.catalog.domain.CategoryEntity;
import com.pixelbase.backend.modules.catalog.domain.ProductEntity;
import com.pixelbase.backend.modules.catalog.domain.ProductStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

/**
 * Clase de utilidad para construir consultas dinámicas sobre la entidad Product.
 * Cada method devuelve una "Specification", que es una pieza de lógica de filtrado.
 */
public class ProductSpecification {

    private ProductSpecification() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Filtra productos por un texto de búsqueda que coincida con el nombre, SKU o partNumber.
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

            // Genera: (LOWER(name) LIKE %text% OR LOWER(sku) LIKE %text%...)
            return cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("sku")), pattern),
                cb.like(cb.lower(root.get("partNumber")), pattern)
            );
        };
    }

    /**
     * Filtra productos que pertenezcan a una categoría específica por su ID.
     */
    public static Specification<ProductEntity> hasCategoryHierarchical(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) return null;

            // 1. Join con la categoría asignada directamente al producto (sea el nivel que sea)
            Join<ProductEntity, CategoryEntity> immediateCategory = root.join("category", JoinType.INNER);

            // 2. LEFT JOINS para caminar hacia la raíz de forma relativa y segura
            Join<CategoryEntity, CategoryEntity> parentCategory = immediateCategory.join("parent",
                JoinType.LEFT);
            Join<CategoryEntity, CategoryEntity> grandparentCategory = parentCategory.join("parent",
                JoinType.LEFT);

            // 3. Evaluación de coincidencia en cualquiera de los peldaños ascendentes
            return cb.or(
                cb.equal(immediateCategory.get("id"), categoryId),
                cb.equal(parentCategory.get("id"), categoryId),
                cb.equal(grandparentCategory.get("id"), categoryId)
            );
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
     * Filtra solo productos con estado ACTIVO (Regla esencial para el Storefront).
     */
    public static Specification<ProductEntity> isActive() {
        return (root, query, cb) -> cb.equal(root.get("status"), ProductStatus.ACTIVO);
    }
}
